using UnityEngine;
using UnityEngine.UI;

/// <summary>Pantalla de historia: di\u00e1logo con retratos, avanza tocando la pantalla.</summary>
public class StoryScreen : MonoBehaviour, IBackHandler {
    static readonly string[] Portrait = { "aprendiz", "mon3", "hero5", "orin", "hero2" };
    static readonly int[] PortraitCol = { 0x42A5F5, 0xFFB300, 0xEF5350, 0x66BB6A, 0xAB47BC };

    int _chapter, _line;
    StoryData.Chapter Ch => StoryData.Chapters[_chapter];

    Image _portrait;
    Text _nameTxt, _talkTxt, _hintTxt, _titleTxt;
    float _cool = 0.35f;

    void Start() {
        _chapter = Mathf.Clamp(App.PendingStoryChapter, 0, StoryData.Chapters.Length - 1);
        _line = 0;

        UiKit.Image(gameObject, "menubg", 0, 0, 1080, 1920).color = new Color(0.45f, 0.4f, 0.55f);
        var dim = UiKit.Panel(gameObject, 0, 0, 1080, 1920, new Color(0.06f, 0.04f, 0.12f, 0.82f),
            new Color(0, 0, 0, 0));
        dim.raycastTarget = true;

        _titleTxt = UiKit.TextC(gameObject, Ch.title, 540, 130, 46, UiKit.C(0xFFC93C), true, true);

        var frame = UiKit.Panel(gameObject, 290, 280, 500, 500, UiKit.C(0x3E2763), UiKit.C(0x9575CD));
        _portrait = UiKit.Image(frame.gameObject, "", 25, 25, 450, 450);

        var box = UiKit.Panel(gameObject, 90, 1330, 900, 330, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        box.raycastTarget = true;
        _nameTxt = UiKit.TextC(box.gameObject, "", 450, 26, 40, UiKit.C(0x6A1B9A), true);
        _talkTxt = UiKit.TextC(box.gameObject, "", 450, 84, 34, UiKit.C(0x3A2E20));
        ((RectTransform)_talkTxt.transform).sizeDelta = new Vector2(820, 220);

        _hintTxt = UiKit.TextC(gameObject, "Toca la pantalla para continuar...", 540, 1700, 30,
            new Color(1, 1, 1, 0.75f), false, true);

        Show();
    }

    void Show() {
        var l = Ch.lines[_line];
        _titleTxt.text = Ch.title;
        _portrait.sprite = UiKit.Art(Portrait[l.who]);
        _portrait.gameObject.GetComponent<RectTransform>().sizeDelta = new Vector2(450, 450);
        _nameTxt.text = l.name;
        _nameTxt.color = UiKit.C(PortraitCol[l.who]);
        _talkTxt.text = l.text;
        _hintTxt.text = (_line < Ch.lines.Length - 1)
            ? "Toca la pantalla para continuar..."
            : "Toca para finalizar";
    }

    void Update() {
        _cool -= Time.unscaledDeltaTime;
        if (_cool > 0) return;
        bool tap = Input.GetMouseButtonDown(0) || Input.GetKeyDown(KeyCode.Space) ||
            Input.GetKeyDown(KeyCode.Return);
        if (!tap) return;
        _cool = 0.35f;
        App.Audio.Sfx("click");
        _line++;
        if (_line < Ch.lines.Length) { Show(); return; }
        if (App.Save.story < _chapter + 1) App.Save.story = _chapter + 1;
        App.Save.Persist();
        App.Audio.ApplyMusic();
        App.Show<GameScreen>();
    }

    public bool OnBack() {
        App.Audio.ApplyMusic();
        App.Show<MenuScreen>();
        return true;
    }
}
