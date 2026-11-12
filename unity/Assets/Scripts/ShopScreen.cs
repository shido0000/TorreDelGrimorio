using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

/// <summary>Tienda: gemas y mejoras eternas (compras reales) + mejoras por gemas.</summary>
public class ShopScreen : MonoBehaviour, IBackHandler {
    Text _goldTxt, _gemTxt;
    readonly List<Text> _upBtnTexts = new List<Text>();
    System.Action<string> _onPurchased;

    static readonly string[][] IapItems = {
        new[] { "gemas_100", "Pu\u00f1ado de gemas", "+100 gemas para el portal", "coin" },
        new[] { "gemas_550", "Bolsa de gemas", "+550 gemas (mejor precio)", "gem" },
        new[] { "gemas_1200", "Cofre de gemas", "+1200 gemas (oferta del Grimorio)", "gem" },
        new[] { "mejora_oro_eterna", "Bendici\u00f3n del Oro", "+25% de oro PARA SIEMPRE", "coin" },
        new[] { "mejora_dano_eterna", "Furia Eterna", "+25% de da\u00f1o PARA SIEMPRE", "coin" },
    };

    void Start() {
        var bg = UiKit.Panel(gameObject, 0, 0, 1080, 1920, UiKit.C(0x3E2723), UiKit.C(0x1B1110));
        bg.raycastTarget = true;

        UiKit.TextC(gameObject, "TIENDA DEL GRIMORIO", 540, 40, 50, UiKit.C(0xFFC93C), true, true);
        var res = UiKit.Panel(gameObject, 30, 120, 1020, 80, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        res.raycastTarget = false;
        UiKit.Image(res.gameObject, "coin", 30, 10, 60, 60);
        _goldTxt = UiKit.Text(res.gameObject, "", 105, 10, 36, UiKit.C(0x9C6A00), true);
        UiKit.Image(res.gameObject, "gem", 400, 15, 50, 50);
        _gemTxt = UiKit.Text(res.gameObject, "", 465, 10, 36, UiKit.C(0x00838F), true);

        var back = UiKit.Btn(gameObject, 30, 30, 190, 80, "< Men\u00fa",
            UiKit.C(0x90A4AE), UiKit.C(0x455A64), () => App.Show<MenuScreen>());
        back.GetComponentInChildren<Text>().fontSize = 32;

        float y = 230;
        foreach (var item in IapItems) {
            var row = UiKit.Panel(gameObject, 60, y, 960, 170, UiKit.C(0x4E342E), UiKit.C(0x2B1B16));
            UiKit.Image(row.gameObject, item[3], 25, 40, 90, 90);
            UiKit.Text(row.gameObject, item[1], 140, 25, 38, Color.white, true);
            UiKit.Text(row.gameObject, item[2], 140, 78, 28, new Color(1, 1, 1, 0.75f));
            string sku = item[0];
            UiKit.Btn(row.gameObject, 620, 40, 310, 90, Purchaser.Price(sku),
                UiKit.C(0xFFB300), UiKit.C(0xE65100), () => App.Purchaser.Buy(sku));
            y += 190;
        }

        UiKit.TextC(gameObject, "\u2014 Mejoras con gemas \u2014", 540, y + 10, 36, UiKit.C(0xFFE082), true, true);
        y += 70;

        string[][] ups = {
            new[] { "Entrenamiento de Oro", "+10% de oro por nivel" },
            new[] { "Filo Oscuro", "+10% de da\u00f1o por nivel" },
            new[] { "Piel de Piedra", "+10% de vida de la torre por nivel" },
        };
        for (int i = 0; i < ups.Length; i++) {
            int idx = i;
            var row = UiKit.Panel(gameObject, 60, y, 960, 170, UiKit.C(0x4E342E), UiKit.C(0x2B1B16));
            UiKit.Text(row.gameObject, ups[i][0], 30, 25, 36, Color.white, true);
            UiKit.Text(row.gameObject, ups[i][1], 30, 80, 28, new Color(1, 1, 1, 0.75f));
            var btn = UiKit.Btn(row.gameObject, 560, 40, 370, 90, "", UiKit.C(0x26C6DA), UiKit.C(0x00838F),
                null);
            Text btnTxt = btn.GetComponentInChildren<Text>();
            btn.onClick.AddListener(() => BuyUpgrade(idx, btnTxt));
            _upBtnTexts.Add(btnTxt);
            y += 190;
        }

        _onPurchased = _ => Refresh();
        App.Purchaser.OnPurchased += _onPurchased;
        Refresh();
    }

    void OnDestroy() {
        if (_onPurchased != null) App.Purchaser.OnPurchased -= _onPurchased;
    }

    long Cost(int lvl) => 200 + (long)lvl * 150;

    void BuyUpgrade(int i, Text btnTxt) {
        int lvl = i == 0 ? App.Save.upOro : i == 1 ? App.Save.upDan : App.Save.upVid;
        long cost = Cost(lvl);
        if (App.Save.gems < cost) {
            App.Toast("\u00a1Te faltan gemas!");
            return;
        }
        App.Save.gems -= cost;
        if (i == 0) App.Save.upOro++; else if (i == 1) App.Save.upDan++; else App.Save.upVid++;
        App.Save.Persist();
        App.Audio.Sfx("levelup");
        App.Toast("\u00a1Mejora comprada! Nivel " + (lvl + 1));
        Refresh();
    }

    void Refresh() {
        _goldTxt.text = UiKit.Fmt(App.Save.gold);
        _gemTxt.text = App.Save.gems.ToString();
        for (int i = 0; i < _upBtnTexts.Count; i++) {
            int lvl = i == 0 ? App.Save.upOro : i == 1 ? App.Save.upDan : App.Save.upVid;
            bool owned = i == 0 ? App.Save.iapOro : i == 1 && App.Save.iapDan;
            string extra = i < 2 && owned ? " (+25% eterno)" : "";
            _upBtnTexts[i].text = "Nivel " + lvl + extra + " \u2014 " + UiKit.Fmt(Cost(lvl)) + " gemas";
        }
    }

    public bool OnBack() {
        App.Show<MenuScreen>();
        return true;
    }
}
