using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

/// <summary>Herramientas para construir la UI por código (coordenadas 1080x1920, origen arriba-izquierda).</summary>
public static class UiKit {
    public static Font _font;
    static readonly Dictionary<string, Sprite> Sprites = new Dictionary<string, Sprite>();
    static Sprite _round, _roundGrad;

    public static Font Font() {
        if (_font == null) {
            _font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            if (_font == null) _font = Resources.GetBuiltinResource<Font>("Arial.ttf");
        }
        return _font;
    }

    public static Sprite Art(string name) {
        if (!Sprites.TryGetValue(name, out var s)) {
            s = Resources.Load<Sprite>("Art/" + name);
            Sprites[name] = s;
        }
        return s;
    }

    static Sprite Round(bool gradient) {
        if (gradient && _roundGrad != null) return _roundGrad;
        if (!gradient && _round != null) return _round;
        const int S = 128, R = 34;
        var tex = new Texture2D(S, S, TextureFormat.ARGB32, false);
        var half = new Vector2(S / 2f, S / 2f);
        for (int y = 0; y < S; y++) {
            for (int x = 0; x < S; x++) {
                float px = Mathf.Abs(x - half.x), py = Mathf.Abs(y - half.y);
                float dx = Mathf.Max(px - (half.x - R), 0), dy = Mathf.Max(py - (half.y - R), 0);
                float dist = Mathf.Sqrt(dx * dx + dy * dy);
                float a = dist > R - 1.5f ? 0f : (dist > R - 3f ? 0.55f : 1f);
                float shade = gradient ? Mathf.Lerp(1f, 0.62f, y / (float)S) : 1f;
                tex.SetPixel(x, y, new Color(shade, shade, shade, a));
            }
        }
        tex.Apply();
        var sp = Sprite.Create(tex, new Rect(0, 0, S, S), new Vector2(0.5f, 0.5f), S,
            0, SpriteMeshType.FullRect, new Vector4(R, R, R, R));
        if (gradient) _roundGrad = sp; else _round = sp;
        return sp;
    }

    public static RectTransform RT(GameObject go, float x, float y, float w, float h) {
        var rt = (RectTransform)go.transform;
        rt.anchorMin = new Vector2(0, 1);
        rt.anchorMax = new Vector2(0, 1);
        rt.pivot = new Vector2(0, 1);
        rt.anchoredPosition = new Vector2(x, -y);
        rt.sizeDelta = new Vector2(w, h);
        return rt;
    }

    public static void Stretch(RectTransform rt) {
        rt.anchorMin = Vector2.zero;
        rt.anchorMax = Vector2.one;
        rt.pivot = new Vector2(0.5f, 0.5f);
        rt.offsetMin = Vector2.zero;
        rt.offsetMax = Vector2.zero;
    }

    public static Image Image(GameObject parent, string art, float x, float y, float w, float h) {
        var go = new GameObject("img_" + art, typeof(RectTransform), typeof(Image));
        go.transform.SetParent(parent.transform, false);
        RT(go, x, y, w, h);
        var im = go.GetComponent<Image>();
        im.sprite = Art(art);
        im.preserveAspect = true;
        return im;
    }

    public static Image Panel(GameObject parent, float x, float y, float w, float h, Color fill, Color edge) {
        var go = new GameObject("panel", typeof(RectTransform), typeof(Image));
        go.transform.SetParent(parent.transform, false);
        RT(go, x, y, w, h);
        var im = go.GetComponent<Image>();
        im.type = Image.Type.Sliced;
        im.sprite = Round(false);
        im.color = fill;
        if (edge.a > 0.01f) {
            var border = new GameObject("edge", typeof(RectTransform), typeof(Image));
            border.transform.SetParent(go.transform, false);
            var brt = RT(border, -4, -4, w + 8, h + 8);
            brt.anchorMin = new Vector2(0, 0);
            brt.anchorMax = new Vector2(0, 0);
            brt.pivot = new Vector2(0, 0);
            brt.anchoredPosition = new Vector2(-4, -4);
            var bim = border.GetComponent<Image>();
            bim.type = Image.Type.Sliced;
            bim.sprite = Round(false);
            bim.color = edge;
            bim.raycastTarget = false;
            im.transform.SetSiblingIndex(0);
        }
        return im;
    }

    public static Text Text(GameObject parent, string msg, float x, float y, float size, Color col,
                            bool bold = false, bool outline = false, TextAnchor align = TextAnchor.Left) {
        var go = new GameObject("txt", typeof(RectTransform), typeof(Text));
        go.transform.SetParent(parent.transform, false);
        var rt = RT(go, x, y - size, 1600, size * 1.6f);
        var t = go.GetComponent<Text>();
        t.font = Font();
        t.fontSize = Mathf.RoundToInt(size);
        t.color = col;
        t.text = msg;
        t.alignment = align;
        t.horizontalOverflow = HorizontalWrapMode.Overflow;
        t.verticalOverflow = VerticalWrapMode.Overflow;
        t.raycastTarget = false;
        if (bold) t.fontStyle = FontStyle.Bold;
        if (outline) {
            var o = go.AddComponent<Outline>();
            o.effectColor = new Color32(38, 36, 46, 255);
            o.effectDistance = new Vector2(size * 0.075f, -size * 0.075f);
        }
        return t;
    }

    public static Text TextC(GameObject parent, string msg, float cx, float y, float size, Color col,
                             bool bold = false, bool outline = false) {
        var t = Text(parent, msg, 0, y, size, col, bold, outline, TextAnchor.UpperCenter);
        var rt = (RectTransform)t.transform;
        rt.anchoredPosition = new Vector2(cx - 800, -y + size);
        return t;
    }

    public static Button Btn(GameObject parent, float x, float y, float w, float h, string label,
                             Color top, Color bottom, UnityEngine.Events.UnityAction onClick, string sub = null) {
        var go = new GameObject("btn", typeof(RectTransform), typeof(Image), typeof(Button));
        go.transform.SetParent(parent.transform, false);
        RT(go, x, y, w, h);
        var im = go.GetComponent<Image>();
        im.type = Image.Type.Sliced;
        im.sprite = Round(true);
        im.color = top;
        var b = go.GetComponent<Button>();
        var colors = b.colors;
        colors.pressedColor = new Color(0.75f, 0.75f, 0.75f);
        colors.fadeDuration = 0.06f;
        b.colors = colors;
        if (onClick != null) b.onClick.AddListener(() => { App.Audio.Sfx("click"); onClick(); });
        var t = Text(go, label, w * 0.1f, h * (sub != null ? 0.16f : 0.5f), h * (sub != null ? 0.36f : 0.42f),
            Color.white, true, true, TextAnchor.UpperCenter);
        ((RectTransform)t.transform).sizeDelta = new Vector2(w * 0.9f, h * 0.7f);
        if (sub != null) {
            var st = Text(go, sub, w * 0.1f, h * 0.58f, h * 0.26f,
                new Color(1f, 1f, 1f, 0.92f), false, true, TextAnchor.UpperCenter);
            ((RectTransform)st.transform).sizeDelta = new Vector2(w * 0.9f, h * 0.4f);
        }
        return b;
    }

    static readonly string[] SEP = { "." };

    public static string Fmt(long n) {
        string s = Mathf.Abs(n) >= 1e9f
            ? (n / 1e9f).ToString("0.#") + "B"
            : Mathf.Abs(n) >= 1e6f ? (n / 1e6f).ToString("0.#") + "M"
            : Mathf.Abs(n) >= 1e4f ? (n / 1e3f).ToString("0.#") + "K"
            : n.ToString("#,0").Replace(",", ".");
        return s;
    }

    public static Color C(int hex) {
        return new Color32((byte)((hex >> 16) & 255), (byte)((hex >> 8) & 255), (byte)(hex & 255), 255);
    }

    public static Sprite Circle() {
        if (!Sprites.TryGetValue("__circle", out var s)) {
            var tex = new Texture2D(64, 64, TextureFormat.ARGB32, false);
            for (int y = 0; y < 64; y++)
                for (int x = 0; x < 64; x++) {
                    float d = Vector2.Distance(new Vector2(x, y), new Vector2(32, 32));
                    tex.SetPixel(x, y, d > 31 ? new Color(1, 1, 1, 0) : Color.white);
                }
            tex.Apply();
            s = Sprite.Create(tex, new Rect(0, 0, 64, 64), new Vector2(32, 32), 64);
            Sprites["__circle"] = s;
        }
        return s;
    }
}

public class ToastView : MonoBehaviour {
    Text _t;
    float _life;

    public void Show(string msg) {
        UiKit.Panel(gameObject, 190, 1600, 700, 150, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        _t = UiKit.TextC(gameObject, msg, 540, 1645, 34, UiKit.C(0x3A2E20), true, false);
        _life = 2.4f;
    }

    void Update() {
        _life -= Time.unscaledDeltaTime;
        if (_life <= 0) Destroy(gameObject);
    }
}
