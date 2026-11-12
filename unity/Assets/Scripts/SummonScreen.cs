using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

/// <summary>Invocaci\u00f3n por gemas: portal animado, tirada x1 o x10 y revelaci\u00f3n.</summary>
public class SummonScreen : MonoBehaviour, IBackHandler {
    GameObject _portal, _burst, _reveal;
    Image _portalRing, _portalMon, _revealMon;
    Text _gemsTxt, _revealTitle, _revealName, _revealDesc;
    System.Random _rnd = new System.Random();
    float _t;
    readonly List<Image> _sparks = new List<Image>();
    bool _revealing;

    void Start() {
        var bg = UiKit.Panel(gameObject, 0, 0, 1080, 1920, UiKit.C(0x1A0560), UiKit.C(0x0D0221));
        bg.raycastTarget = true;

        for (int i = 0; i < 26; i++) {
            var s = new GameObject("spark", typeof(RectTransform), typeof(Image));
            s.transform.SetParent(gameObject.transform, false);
            UiKit.RT(s, Random.Range(40f, 1040f), Random.Range(200f, 1500f), 14, 14);
            var im = s.GetComponent<Image>();
            im.sprite = UiKit.Circle();
            im.color = new Color(1f, 1f, 0.7f, Random.Range(0.25f, 0.7f));
            im.raycastTarget = false;
            _sparks.Add(im);
        }

        UiKit.TextC(gameObject, "PORTAL DE INVOCACI\u00d3N", 540, 90, 52, UiKit.C(0xFFC93C), true, true);

        _portal = new GameObject("portal", typeof(RectTransform));
        _portal.transform.SetParent(gameObject.transform, false);
        UiKit.RT(_portal, 240, 380, 600, 600);
        var ringGo = new GameObject("ring", typeof(RectTransform), typeof(Image));
        ringGo.transform.SetParent(_portal.transform, false);
        var ringRt = (RectTransform)ringGo.transform;
        ringRt.anchorMin = Vector2.zero; ringRt.anchorMax = Vector2.one;
        ringRt.offsetMin = Vector2.zero; ringRt.offsetMax = Vector2.zero;
        _portalRing = ringGo.GetComponent<Image>();
        _portalRing.sprite = UiKit.Circle();
        _portalRing.color = UiKit.C(0x7C4DFF);
        _portalRing.raycastTarget = false;
        var monGo = new GameObject("mon", typeof(RectTransform), typeof(Image));
        monGo.transform.SetParent(_portal.transform, false);
        var monRt = (RectTransform)monGo.transform;
        monRt.anchorMin = new Vector2(0.5f, 0.5f); monRt.anchorMax = new Vector2(0.5f, 0.5f);
        monRt.sizeDelta = new Vector2(340, 340);
        monRt.anchoredPosition = Vector2.zero;
        _portalMon = monGo.GetComponent<Image>();
        _portalMon.sprite = UiKit.Art("mon7");
        _portalMon.raycastTarget = false;

        _gemsTxt = UiKit.TextC(gameObject, "", 540, 1030, 38, UiKit.C(0xFFE082), true, true);

        UiKit.Btn(gameObject, 140, 1130, 370, 130, "Invocar x1", UiKit.C(0xAB47BC), UiKit.C(0x6A1B9A),
            () => TrySummon(1), "100 gemas");
        UiKit.Btn(gameObject, 570, 1130, 370, 130, "Invocar x10", UiKit.C(0xFFB300), UiKit.C(0xE65100),
            () => TrySummon(10), "900 gemas");

        UiKit.TextC(gameObject,
            "Probabilidades: Com\u00fan 60% \u00b7 Raro 27% \u00b7 \u00c9pico 10% \u00b7 Legendario 3%",
            540, 1330, 28, new Color(1, 1, 1, 0.8f), false, true);
        UiKit.TextC(gameObject,
            "Si ya tienes el monstruo, \u00a1sube un nivel en su lugar!",
            540, 1380, 28, new Color(1, 1, 1, 0.65f), false, true);

        var back = UiKit.Btn(gameObject, 30, 30, 190, 90, "< Men\u00fa",
            UiKit.C(0x90A4AE), UiKit.C(0x455A64), () => App.Show<MenuScreen>());
        back.GetComponentInChildren<Text>().fontSize = 34;

        BuildReveal();
        Refresh();
    }

    void BuildReveal() {
        _reveal = new GameObject("reveal", typeof(RectTransform));
        _reveal.transform.SetParent(gameObject.transform, false);
        UiKit.Stretch((RectTransform)_reveal.transform);
        _reveal.SetActive(false);

        var dim = UiKit.Panel(_reveal, 0, 0, 1080, 1920, new Color(0, 0, 0, 0.78f), new Color(0, 0, 0, 0));
        dim.raycastTarget = true;

        var burstGo = new GameObject("burst", typeof(RectTransform), typeof(Image));
        burstGo.transform.SetParent(_reveal.transform, false);
        var brt = (RectTransform)burstGo.transform;
        brt.anchorMin = new Vector2(0.5f, 1f); brt.anchorMax = new Vector2(0.5f, 1f);
        brt.pivot = new Vector2(0.5f, 0.5f);
        brt.anchoredPosition = new Vector2(0, -700);
        brt.sizeDelta = new Vector2(520, 520);
        _burst = burstGo;
        _burst.GetComponent<Image>().sprite = UiKit.Circle();
        _burst.GetComponent<Image>().raycastTarget = false;

        _revealMon = UiKit.Image(_reveal, "", 340, 530, 400, 400);
        _revealMon.raycastTarget = false;
        _revealTitle = UiKit.TextC(_reveal, "", 540, 990, 54, UiKit.C(0xFFC93C), true, true);
        _revealName = UiKit.TextC(_reveal, "", 540, 1080, 46, Color.white, true, true);
        _revealDesc = UiKit.TextC(_reveal, "", 540, 1160, 32, new Color(1, 1, 1, 0.85f));
        ((RectTransform)_revealDesc.transform).sizeDelta = new Vector2(900, 160);

        UiKit.Btn(_reveal, 290, 1420, 500, 120, "\u00a1Genial!", UiKit.C(0x66BB6A), UiKit.C(0x2E7D32),
            () => CloseReveal());
    }

    void Refresh() {
        _gemsTxt.text = "Tienes " + App.Save.gems + " gemas";
    }

    void TrySummon(int n) {
        int cost = n == 1 ? 100 : 900;
        if (_revealing) return;
        if (App.Save.gems < cost) {
            App.Toast("\u00a1Te faltan gemas! Visita la Tienda");
            return;
        }
        App.Save.gems -= cost;
        App.Audio.Sfx("summon");
        string[] ids = new string[n];
        for (int i = 0; i < n; i++) {
            ids[i] = Defs.RollMonster(_rnd);
            bool nuevo = !App.Save.AddOrLevel(ids[i]);
            App.Save.qSummons++;
            App.Save.totalSummons++;
            ids[i] = (nuevo ? "\u00a1NUEVO!|" : "lvl|") + ids[i];
        }
        App.Save.Persist();
        ShowReveal(ids[n - 1]);
        Refresh();
    }

    void ShowReveal(string tagged) {
        bool nuevo = tagged.StartsWith("\u00a1NUEVO!|");
        string id = tagged.Substring(tagged.IndexOf('|') + 1);
        var d = Defs.Mon(id);
        _revealing = true;
        _reveal.SetActive(true);
        _revealMon.sprite = UiKit.Art("mon" + d.art);
        _burst.GetComponent<Image>().color = UiKit.C(Defs.RarityCol[d.rarity]);
        _revealTitle.text = nuevo ? "\u00a1NUEVO MONSTRUO!" : "\u00a1Sube a Nivel " + App.Save.MonLevel(id) + "!";
        _revealTitle.color = UiKit.C(Defs.RarityCol[d.rarity]);
        _revealName.text = d.name + " \u00b7 " + Defs.Rarity[d.rarity];
        _revealDesc.text = d.desc;
        _t = 0f;
    }

    void CloseReveal() {
        _revealing = false;
        _reveal.SetActive(false);
    }

    void Update() {
        _t += Time.unscaledDeltaTime;
        float pulse = 1f + Mathf.Sin(_t * 3f) * 0.06f;
        var prt = (RectTransform)_portal.transform;
        prt.localScale = new Vector3(pulse, pulse, 1f);
        _portalRing.color = Color.Lerp(UiKit.C(0x7C4DFF), UiKit.C(0xB388FF),
            (Mathf.Sin(_t * 3f) + 1f) * 0.5f);

        for (int i = 0; i < _sparks.Count; i++) {
            var rt = (RectTransform)_sparks[i].transform;
            rt.anchoredPosition += new Vector2(0, 60f * Time.unscaledDeltaTime);
            if (rt.anchoredPosition.y > 1550) rt.anchoredPosition = new Vector2(
                Random.Range(40f, 1040f), 200f);
        }

        if (_revealing && _burst != null) {
            float s = 1f + Mathf.Sin(_t * 6f) * 0.05f;
            _burst.transform.localScale = new Vector3(s, s, 1f);
        }
    }

    public bool OnBack() {
        if (_revealing) { CloseReveal(); return true; }
        App.Show<MenuScreen>();
        return true;
    }
}
