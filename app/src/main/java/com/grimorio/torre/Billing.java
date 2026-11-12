package com.grimorio.torre;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Envoltorio de Google Play Billing (pagos y suscripciones).
 *
 * Productos esperados en Play Console:
 *  - Suscripciones: pase_semanal, pase_mensual
 *  - Consumibles:   gemas_100, gemas_550, gemas_1200
 *  - No consumibles: mejora_oro_eterna, mejora_dano_eterna
 */
public class Billing {

    public interface Listener {
        void onProductsReady();
        void onPurchaseOk(String sku);
    }

    private final Context ctx;
    private final GameView game;
    private BillingClient client;
    private boolean ready;
    private final List<ProductDetails> details = new ArrayList<ProductDetails>();
    private Listener listener;

    public static final String[] SUB_SKUS = {"pase_semanal", "pase_mensual"};
    public static final String[] CONSUMABLES = {"gemas_100", "gemas_550", "gemas_1200"};
    public static final String[] NON_CONSUMABLES = {"mejora_oro_eterna", "mejora_dano_eterna"};

    public Billing(Context ctx, GameView game) {
        this.ctx = ctx;
        this.game = game;
    }

    public void setListener(Listener l) { this.listener = l; }

    public boolean isReady() { return ready; }

    public void start() {
        client = BillingClient.newBuilder(ctx)
                .setListener(purchasesListener)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
        client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult r) {
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    ready = true;
                    queryProducts();
                    restorePurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                ready = false;
            }
        });
    }

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> ps = new ArrayList<QueryProductDetailsParams.Product>();
        for (String s : SUB_SKUS)
            ps.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(s).setProductType(BillingClient.ProductType.SUBS).build());
        for (String s : CONSUMABLES)
            ps.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(s).setProductType(BillingClient.ProductType.INAPP).build());
        for (String s : NON_CONSUMABLES)
            ps.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(s).setProductType(BillingClient.ProductType.INAPP).build());
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(ps).build();
        client.queryProductDetailsAsync(params, new com.android.billingclient.api.ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(BillingResult r, List<ProductDetails> list) {
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    synchronized (details) {
                        details.clear();
                        details.addAll(list);
                    }
                    if (listener != null) listener.onProductsReady();
                }
            }
        });
    }

    private void restorePurchases() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS).build(),
                new com.android.billingclient.api.PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(BillingResult r, List<Purchase> list) {
                        if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) applySubs(list);
                    }
                });
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP).build(),
                new com.android.billingclient.api.PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(BillingResult r, List<Purchase> list) {
                        if (r.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                        for (Purchase p : list) {
                            if (p.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
                            for (String sku : p.getProducts()) {
                                if (sku.equals("mejora_oro_eterna")) game.save.iapOro = true;
                                if (sku.equals("mejora_dano_eterna")) game.save.iapDan = true;
                                if (isConsumable(sku) && !p.isAcknowledged()) {
                                    // consumible reembolsado/restaurado: entregar de nuevo y consumir
                                    deliverConsumable(sku);
                                    consume(p);
                                }
                            }
                        }
                    }
                });
    }

    private final PurchasesUpdatedListener purchasesListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult r, List<Purchase> list) {
            if (r.getResponseCode() != BillingClient.BillingResponseCode.OK || list == null) return;
            for (Purchase p : list) {
                if (p.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
                for (String sku : p.getProducts()) {
                    if (sku.equals("mejora_oro_eterna")) game.save.iapOro = true;
                    else if (sku.equals("mejora_dano_eterna")) game.save.iapDan = true;
                    else if (isConsumable(sku)) deliverConsumable(sku);
                    else if (sku.equals("pase_semanal") || sku.equals("pase_mensual")) {
                        game.save.subActive = true;
                        game.save.subSku = sku;
                    }
                }
                if (isConsumable(p.getProducts().get(0))) consume(p);
                else acknowledge(p);
            }
            game.persist();
            if (listener != null) listener.onPurchaseOk(list.get(0).getProducts().get(0));
        }
    };

    private boolean isConsumable(String sku) {
        for (String s : CONSUMABLES) if (s.equals(sku)) return true;
        return false;
    }

    private void deliverConsumable(String sku) {
        if (sku.equals("gemas_100")) game.save.addGems(100);
        else if (sku.equals("gemas_550")) game.save.addGems(550);
        else if (sku.equals("gemas_1200")) game.save.addGems(1200);
    }

    private void consume(Purchase p) {
        ConsumeParams cp = ConsumeParams.newBuilder().setPurchaseToken(p.getPurchaseToken()).build();
        client.consumeAsync(cp, new com.android.billingclient.api.ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult r, String token) { }
        });
    }

    private void acknowledge(Purchase p) {
        if (p.isAcknowledged()) return;
        AcknowledgePurchaseParams ap = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(p.getPurchaseToken()).build();
        client.acknowledgePurchase(ap, new com.android.billingclient.api.AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult r) { }
        });
    }

    public void applySubs(List<Purchase> list) {
        boolean active = false;
        String sku = "";
        for (Purchase p : list) {
            if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                active = true;
                sku = p.getProducts().isEmpty() ? sku : p.getProducts().get(0);
                acknowledge(p);
            }
        }
        game.save.subActive = active;
        if (active) game.save.subSku = sku;
        game.persist();
    }

    /** Lanza el flujo de compra de un producto por su id. */
    public boolean launch(Activity act, String sku, String type) {
        if (!ready) return false;
        ProductDetails pd = null;
        synchronized (details) {
            for (ProductDetails d : details) {
                if (d.getProductId().equals(sku)) { pd = d; break; }
            }
        }
        if (pd == null) return false;
        BillingFlowParams.ProductDetailsParams.Builder pb = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(pd);
        if (type.equals(BillingClient.ProductType.SUBS)) {
            List<ProductDetails.SubscriptionOfferDetails> offers = pd.getSubscriptionOfferDetails();
            if (offers == null || offers.isEmpty()) return false;
            pb.setOfferToken(offers.get(0).getOfferToken());
        }
        BillingFlowParams flow = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                        java.util.Collections.singletonList(pb.build()))
                .build();
        client.launchBillingFlow(act, flow);
        return true;
    }

    /** Precio formateado de un producto, o null si no está cargado. */
    public String price(String sku) {
        synchronized (details) {
            for (ProductDetails d : details) {
                if (d.getProductId().equals(sku)) {
                    if (d.getProductType().equals(BillingClient.ProductType.SUBS)) {
                        List<ProductDetails.SubscriptionOfferDetails> offers = d.getSubscriptionOfferDetails();
                        if (offers != null && !offers.isEmpty()) {
                            for (ProductDetails.PricingPhase ph : offers.get(0).getPricingPhases().getPricingPhaseList()) {
                                if (ph.getFormattedPrice() != null && !ph.getFormattedPrice().isEmpty()
                                        && !ph.getFormattedPrice().equals("Gratis")) return ph.getFormattedPrice();
                            }
                        }
                        return null;
                    }
                    ProductDetails.OneTimePurchaseOfferDetails ot = d.getOneTimePurchaseOfferDetails();
                    return ot == null ? null : ot.getFormattedPrice();
                }
            }
        }
        return null;
    }
}
