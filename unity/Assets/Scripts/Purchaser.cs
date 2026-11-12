using System;
using System.Collections.Generic;
using UnityEngine;

#if UNITY_PURCHASING
using UnityEngine.Purchasing;
#endif

/// <summary>
/// Compras: gemas (consumibles), mejoras eternas (no consumibles)
/// y Pase del Invocador (suscripción semanal/mensual).
/// Si el paquete Unity IAP no está instalado, funciona con avisos simulados.
/// </summary>
public class Purchaser : MonoBehaviour {
    public Action<string> OnPurchased;
    readonly Dictionary<string, string> _prices = new Dictionary<string, string>();

    public static readonly string[] Skus = {
        "gemas_100", "gemas_550", "gemas_1200", "mejora_oro_eterna", "mejora_dano_eterna",
        "pase_semanal", "pase_mensual"
    };

#if UNITY_PURCHASING
    IStoreController _ctrl;
    IExtensionProvider _ext;

    void Awake() {
        try {
            var builder = ConfigurationBuilder.Instance(StandardPurchasingModule.Instance());
            builder.AddProduct("gemas_100", ProductType.Consumable);
            builder.AddProduct("gemas_550", ProductType.Consumable);
            builder.AddProduct("gemas_1200", ProductType.Consumable);
            builder.AddProduct("mejora_oro_eterna", ProductType.NonConsumable);
            builder.AddProduct("mejora_dano_eterna", ProductType.NonConsumable);
            builder.AddProduct("pase_semanal", ProductType.Subscription);
            builder.AddProduct("pase_mensual", ProductType.Subscription);
            UnityPurchasing.Initialize(this, builder);
        } catch (Exception e) {
            Debug.LogWarning("IAP no disponible: " + e.Message);
        }
    }

    public void OnInitialized(IStoreController ctrl, IExtensionProvider ext) {
        _ctrl = ctrl;
        _ext = ext;
        foreach (var p in ctrl.products.all)
            if (p.metadata != null && !string.IsNullOrEmpty(p.metadata.localizedPriceString))
                _prices[p.definition.id] = p.metadata.localizedPriceString;
    }

    public void OnInitializeFailed(InitializationFailureReason reason) {
        Debug.LogWarning("IAP falló: " + reason);
    }

    public PurchaseProcessingResult ProcessPurchase(PurchaseEventArgs e) {
        Apply(e.purchasedProduct.definition.id);
        return PurchaseProcessingResult.Complete;
    }

    public void OnPurchaseFailed(Product p, PurchaseFailureReason r) {
        App.Toast("Compra cancelada");
    }
#endif

    public string Price(string sku) {
#if UNITY_PURCHASING
        if (_prices.TryGetValue(sku, out var p)) return p;
#endif
        switch (sku) {
            case "gemas_100": return "1,99 €";
            case "gemas_550": return "7,99 €";
            case "gemas_1200": return "14,99 €";
            case "mejora_oro_eterna": return "4,99 €";
            case "mejora_dano_eterna": return "4,99 €";
            case "pase_semanal": return "1,99 € / semana";
            case "pase_mensual": return "5,99 € / mes";
        }
        return "?";
    }

    public void Buy(string sku) {
#if UNITY_PURCHASING
        if (_ctrl != null) {
            var p = _ctrl.products.WithID(sku);
            if (p != null && p.availableToPurchase) { _ctrl.InitiatePurchase(p); return; }
        }
#endif
        App.Toast("Tienda no disponible en este dispositivo");
    }

    public void Apply(string sku) {
        var s = App.Save;
        switch (sku) {
            case "gemas_100": s.AddGems(100); break;
            case "gemas_550": s.AddGems(550); break;
            case "gemas_1200": s.AddGems(1200); break;
            case "mejora_oro_eterna": s.iapOro = true; break;
            case "mejora_dano_eterna": s.iapDan = true; break;
            case "pase_semanal":
            case "pase_mensual":
                s.subActive = true;
                s.subSku = sku;
                break;
        }
        s.Persist();
        OnPurchased?.Invoke(sku);
        App.Toast("¡Compra completada! ¡Gracias!");
    }
}
