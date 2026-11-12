package com.grimorio.torre;

import android.graphics.Canvas;

import java.util.ArrayList;

/** Tienda: gemas y mejoras eternas (pagos reales) y mejoras con gemas. */
public class ShopScene implements GameView.Scene {

    static class Item {
        String title, sub;
        int col, edge;
        String sku;      // null = compra con gemas
        int gemWhich;    // 0 oro, 1 daño, 2 vida (si sku == null)
        Ui.Btn btn;

        Item(String title, String sub, int col, int edge, String sku) {
            this.title = title; this.sub = sub; this.col = col; this.edge = edge; this.sku = sku;
        }

        Item(String title, String sub, int col, int edge, int gemWhich) {
            this.title = title; this.sub = sub; this.col = col; this.edge = edge; this.gemWhich = gemWhich;
        }
    }

    GameView g;
    Ui.Btn bBack, bPass;
    ArrayList<Item> items = new ArrayList<Item>();
    float scroll = 0, maxScroll = 0;
    float downY, lastY;
    boolean dragging;
    boolean purchasedToast;

    public ShopScene(final GameView g) {
        this.g = g;

        items.add(new Item("Puñado de gemas", "+100 gemas al instante", 0xFF4DD0E1, 0xFF00838F, "gemas_100"));
        items.add(new Item("Bolsa de gemas", "+550 gemas (mejor valor)", 0xFF4DD0E1, 0xFF00838F, "gemas_550"));
        items.add(new Item("Cofre de gemas", "+1200 gemas", 0xFF4DD0E1, 0xFF00838F, "gemas_1200"));
        items.add(new Item("Bendición del Grimorio", "+25% de oro para siempre", 0xFFFFB300, 0xFFE65100, "mejora_oro_eterna"));
        items.add(new Item("Sello de Fuerza", "+25% de daño para siempre", 0xFFEF5350, 0xFFB71C1C, "mejora_dano_eterna"));
        items.add(new Item("Fortuna", "Oro +10% por nivel (con gemas)", 0xFFFFC93C, 0xFFB8860B, 0));
        items.add(new Item("Furia", "Daño +10% por nivel (con gemas)", 0xFFEF9A9A, 0xFFB71C1C, 1));
        items.add(new Item("Coraza", "Vida +10% por nivel (con gemas)", 0xFF90CAF9, 0xFF1565C0, 2));

        float y = 260;
        for (Item it : items) {
            final Item fit = it;
            it.btn = new Ui.Btn(700, y + 55, 280, 100, "", it.col, it.edge, new Runnable() {
                public void run() { buy(fit); }
            });
            y += 190;
        }

        bBack = new Ui.Btn(40, 40, 140, 90, "◀", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new MenuScene(g)); }
        });
        bPass = new Ui.Btn(240, 40, 600, 90, "★ Pase del Invocador ★", 0xFF42A5F5, 0xFF1565C0, new Runnable() {
            public void run() { g.setScene(new SubScene(g)); }
        });

        g.billing.setListener(new Billing.Listener() {
            public void onProductsReady() { }
            public void onPurchaseOk(String sku) { purchasedToast = true; }
        });
    }

    private int gemCost(int which) {
        int lvl = which == 0 ? g.save.upOro : which == 1 ? g.save.upDan : g.save.upVid;
        return 200 + lvl * 150;
    }

    private void buy(Item it) {
        if (it.sku == null) {
            int cost = gemCost(it.gemWhich);
            if (g.save.gems < cost) { g.toast("Gemas insuficientes"); return; }
            g.save.gems -= cost;
            if (it.gemWhich == 0) g.save.upOro++;
            else if (it.gemWhich == 1) g.save.upDan++;
            else g.save.upVid++;
            g.persist();
            g.toast("¡Mejora comprada!");
        } else {
            if (!g.billing.launch(g.act, it.sku, BillingClient_INAPP(it.sku))) {
                g.toast("Tienda no disponible (¿sin Google Play?)");
            }
        }
    }

    private String BillingClient_INAPP(String sku) {
        return (sku.equals("pase_semanal") || sku.equals("pase_mensual"))
                ? "subs" : "inapp";
    }

    public void update(float dt) {
        if (purchasedToast) {
            purchasedToast = false;
            g.toast("¡Compra completada! ¡Gracias!");
        }
        maxScroll = 260 + items.size() * 190 + 220 - 1920;
        if (maxScroll < 0) maxScroll = 0;
    }

    public void touch(int action, float x, float y) {
        if (action == android.view.MotionEvent.ACTION_DOWN) {
            downY = y; lastY = y; dragging = false;
            return;
        }
        if (action == android.view.MotionEvent.ACTION_MOVE) {
            if (Math.abs(y - downY) > 30) dragging = true;
            if (dragging) {
                scroll -= (y - lastY);
                if (scroll < 0) scroll = 0;
                if (scroll > maxScroll) scroll = maxScroll;
            }
            lastY = y;
            return;
        }
        if (action != android.view.MotionEvent.ACTION_UP) return;
        if (dragging) { dragging = false; return; }
        float vy = y + scroll;
        hit(bBack, x, y);
        hit(bPass, x, y);
        for (Item it : items) {
            if (it.btn.enabled && it.btn.hit(x, vy) && it.btn.on != null) { it.btn.on.run(); return; }
        }
    }

    public boolean back() {
        g.setScene(new MenuScene(g));
        return true;
    }

    private void hit(Ui.Btn b, float x, float y) {
        if (b.hit(x, y) && b.on != null) b.on.run();
    }

    public void draw(Canvas c, float t) {
        c.drawRect(0, 0, 1080, 1920, Ui.paint(0xFF263238));
        c.save();
        c.translate(0, -scroll);

        Ui.panel(c, 140, 60, 800, 110, 0xFF311B92, 0xFF1A0560);
        Ui.textC(c, "TIENDA DEL GRIMORIO", 540, 135, 48, true, 0xFFFFC93C);

        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            float iy = 200 + i * 190;
            if (i == 0) Ui.textC(c, "PAGA CON DINERO REAL", 540, iy + 20, 32, true, 0xFFB0BEC5);
            if (i == 5) Ui.textC(c, "MEJORAS CON GEMAS", 540, iy + 20, 32, true, 0xFFB0BEC5);
            Ui.panel(c, 60, iy + 40, 960, 150, 0xFF37474F, 0xFF263238);
            Ui.textC(c, it.title, 110, iy + 100, 40, true, it.col);
            Ui.textC(c, it.sub, 110, iy + 155, 28, false, 0xFFB0BEC5);

            String label;
            boolean enabled = true;
            if (it.sku == null) {
                int lvl = it.gemWhich == 0 ? g.save.upOro : it.gemWhich == 1 ? g.save.upDan : g.save.upVid;
                int cost = gemCost(it.gemWhich);
                label = cost + " gemas";
                enabled = g.save.gems >= cost;
                if (lvl > 0) Ui.textC(c, "Nivel " + lvl + " (+" + lvl * 10 + "%)", 660, iy + 75, 24, false, 0xFF80CBC4);
            } else {
                String price = g.billing.price(it.sku);
                label = price == null ? "Cargando..." : price;
                if ((it.sku.equals("mejora_oro_eterna") && g.save.iapOro) ||
                        (it.sku.equals("mejora_dano_eterna") && g.save.iapDan)) {
                    label = "COMPRADO";
                    enabled = false;
                }
            }
            it.btn.label = label;
            it.btn.enabled = enabled;
            it.btn.sub = null;
            it.btn.draw(c, t);
        }

        c.restore();

        bBack.draw(c, t);
        bPass.draw(c, t);
        Art.drawGem(c, 900, 85, 24);
        Ui.textC(c, String.valueOf(g.save.gems), 940, 100, 36, true, 0xFF4DD0E1);
    }
}
