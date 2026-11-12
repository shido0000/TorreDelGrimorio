using UnityEngine;
using UnityEngine.UI;

/// <summary>Logros permanentes: 13 logros con progreso y recompensa de gemas.</summary>
public class AchieveScreen : MonoBehaviour, IBackHandler {
    class Bar {
        public Text Progress, State;
        public Button Claim;
        public Image Fill;
    }

    readonly System.Collections.Generic.List<Bar> _bars = new System.Collections.Generic.List<Bar>();

    void Start() {
        var bg = UiKit.Panel(gameObject, 0, 0, 1080, 1920, UiKit.C(0x4A148C), UiKit.C(0x1A0560));
        bg.raycastTarget = true;

        var back = UiKit.Btn(gameObject, 30, 30, 190, 80, "< Men\u00fa",
            UiKit.C(0x90A4AE), UiKit.C(0x455A64), () => App.Show<MenuScreen>());
        back.GetComponentInChildren<Text>().fontSize = 32;

        UiKit.TextC(gameObject, "LOGROS", 540, 50, 54, UiKit.C(0xFFC93C), true, true);
        UiKit.TextC(gameObject, "Permanentes \u00b7 se cumplen una sola vez",
            540, 130, 30, new Color(1, 1, 1, 0.85f));

        var viewportGo = new GameObject("viewport", typeof(RectTransform), typeof(Image), typeof(RectMask2D));
        viewportGo.transform.SetParent(gameObject.transform, false);
        var vp = UiKit.RT(viewportGo, 40, 220, 1000, 1580);
        vp.anchorMin = new Vector2(0, 1); vp.anchorMax = new Vector2(0, 1);
        vp.pivot = new Vector2(0, 1);
        vp.anchoredPosition = new Vector2(40, -220);
        vp.sizeDelta = new Vector2(1000, 1580);
        viewportGo.GetComponent<Image>().color = new Color(0, 0, 0, 0.01f);

        float contentH = Achievements.List.Length * 230f + 20f;
        var contentGo = new GameObject("content", typeof(RectTransform));
        contentGo.transform.SetParent(viewportGo.transform, false);
        var content = (RectTransform)contentGo.transform;
        content.anchorMin = new Vector2(0, 1); content.anchorMax = new Vector2(1, 1);
        content.pivot = new Vector2(0.5f, 1f);
        content.anchoredPosition = new Vector2(0, 0);
        content.sizeDelta = new Vector2(0, contentH);

        var scroll = viewportGo.AddComponent<ScrollRect>();
        scroll.content = content;
        scroll.movementType = ScrollRect.MovementType.Clamped;
        scroll.scrollSensitivity = 30;
        scroll.horizontal = false;
        scroll.vertical = true;

        for (int i = 0; i < Achievements.List.Length; i++) {
            int idx = i;
            var row = UiKit.Panel(contentGo, 20, 20 + i * 230, 920, 210, UiKit.C(0x311B92), UiKit.C(0x1A0560));
            UiKit.Text(row.gameObject, (string)Achievements.List[i][0], 35, 22, 34, UiKit.C(0xFFE082), true);
            UiKit.Text(row.gameObject, (string)Achievements.List[i][1], 35, 72, 26, new Color(1, 1, 1, 0.85f));

            var track = UiKit.Panel(row.gameObject, 35, 125, 520, 36, UiKit.C(0x1A0A4A), new Color(0, 0, 0, 0));
            track.raycastTarget = false;
            var fillGo = new GameObject("fill", typeof(RectTransform), typeof(Image));
            fillGo.transform.SetParent(track.gameObject.transform, false);
            var frt = (RectTransform)fillGo.transform;
            frt.anchorMin = new Vector2(0, 0); frt.anchorMax = new Vector2(0, 1);
            frt.pivot = new Vector2(0, 0.5f);
            frt.anchoredPosition = new Vector2(5, 0);
            frt.sizeDelta = new Vector2(0, 25);
            var fill = fillGo.GetComponent<Image>();
            fill.sprite = UiKit.Circle();
            fill.type = Image.Type.Sliced;
            fill.color = UiKit.C(0xFFB300);
            fill.raycastTarget = false;

            var claim = UiKit.Btn(row.gameObject, 600, 105, 290, 85, "", UiKit.C(0xFFB300), UiKit.C(0xE65100),
                () => Claim(idx));
            Text claimTxt = claim.GetComponentInChildren<Text>();
            claimTxt.fontSize = 30;

            _bars.Add(new Bar {
                Progress = UiKit.Text(row.gameObject, "", 35, 168, 24, new Color(1, 1, 1, 0.7f)),
                State = claimTxt,
                Claim = claim,
                Fill = fill,
            });
        }

        Refresh();
    }

    void Claim(int i) {
        if (Achievements.Claim(App.Save, i)) {
            App.Save.Persist();
            App.Audio.Sfx("coin");
            App.Toast("\u00a1+" + Achievements.List[i][4] + " gemas!");
            Refresh();
        }
    }

    void Refresh() {
        for (int i = 0; i < _bars.Count; i++) {
            int prog = Mathf.Min(Achievements.Progress(App.Save, i), Achievements.Goal(i));
            int goal = Achievements.Goal(i);
            bool done = Achievements.Done(App.Save, i);
            bool claimed = Achievements.Claimed(App.Save, i);
            var b = _bars[i];
            b.Progress.text = prog + " / " + goal + "  \u00b7  Recompensa: " + Achievements.List[i][4] + " gemas";
            var frt = (RectTransform)b.Fill.transform;
            frt.sizeDelta = new Vector2(508f * prog / goal, 25);
            if (claimed) {
                b.State.text = "Hecho";
                b.Claim.interactable = false;
                b.Claim.image.color = UiKit.C(0x616161);
            } else if (done) {
                b.State.text = "Reclamar";
                b.Claim.interactable = true;
                b.Claim.image.color = UiKit.C(0xFFB300);
            } else {
                b.State.text = "Bloqueado";
                b.Claim.interactable = false;
                b.Claim.image.color = UiKit.C(0x616161);
            }
        }
    }

    public bool OnBack() {
        App.Show<MenuScreen>();
        return true;
    }
}
