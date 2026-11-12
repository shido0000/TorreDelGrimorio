using UnityEngine;
using UnityEngine.UI;

/// <summary>Misiones diarias: 3 misiones que se reinician cada d\u00eda y dan gemas.</summary>
public class DailyScreen : MonoBehaviour, IBackHandler {
    readonly System.Collections.Generic.List<Bar> _bars = new System.Collections.Generic.List<Bar>();

    class Bar {
        public Text Progress, State;
        public Button Claim;
        public Image Fill;
        public GameObject Row;
    }

    void Start() {
        var bg = UiKit.Panel(gameObject, 0, 0, 1080, 1920, UiKit.C(0x00695C), UiKit.C(0x00332E));
        bg.raycastTarget = true;

        var back = UiKit.Btn(gameObject, 30, 30, 190, 80, "< Men\u00fa",
            UiKit.C(0x90A4AE), UiKit.C(0x455A64), () => App.Show<MenuScreen>());
        back.GetComponentInChildren<Text>().fontSize = 32;

        UiKit.TextC(gameObject, "MISIONES DIARIAS", 540, 50, 52, UiKit.C(0xFFC93C), true, true);
        UiKit.TextC(gameObject, "Se reinician cada d\u00eda a medianoche \u00b7 Gana gemas",
            540, 130, 30, new Color(1, 1, 1, 0.85f));

        for (int i = 0; i < Quests.List.Length; i++) {
            int idx = i;
            var row = UiKit.Panel(gameObject, 60, 230 + i * 260, 960, 230, UiKit.C(0x004D40), UiKit.C(0x00251E));
            UiKit.Text(row.gameObject, (string)Quests.List[i][0], 40, 25, 38, UiKit.C(0xFFE082), true);
            UiKit.Text(row.gameObject, (string)Quests.List[i][1], 40, 80, 28, new Color(1, 1, 1, 0.85f));

            var track = UiKit.Panel(row.gameObject, 40, 140, 560, 40, UiKit.C(0x1B3B36), new Color(0, 0, 0, 0));
            track.raycastTarget = false;
            var fillGo = new GameObject("fill", typeof(RectTransform), typeof(Image));
            fillGo.transform.SetParent(track.gameObject.transform, false);
            var frt = (RectTransform)fillGo.transform;
            frt.anchorMin = new Vector2(0, 0); frt.anchorMax = new Vector2(0, 1);
            frt.pivot = new Vector2(0, 0.5f);
            frt.anchoredPosition = new Vector2(6, 0);
            frt.sizeDelta = new Vector2(0, 28);
            var fill = fillGo.GetComponent<Image>();
            fill.sprite = UiKit.Circle();
            fill.type = Image.Type.Sliced;
            fill.color = UiKit.C(0x66BB6A);
            fill.raycastTarget = false;

            var claim = UiKit.Btn(row.gameObject, 640, 120, 290, 90, "", UiKit.C(0xFFB300), UiKit.C(0xE65100),
                () => Claim(idx));
            Text claimTxt = claim.GetComponentInChildren<Text>();
            claimTxt.fontSize = 32;

            _bars.Add(new Bar {
                Progress = UiKit.Text(row.gameObject, "", 40, 185, 26, new Color(1, 1, 1, 0.7f)),
                State = claimTxt,
                Claim = claim,
                Fill = fill,
                Row = row.gameObject,
            });
        }

        UiKit.TextC(gameObject, "Las gemas sirven para invocar monstruos y comprar mejoras.",
            540, 1100, 28, new Color(1, 1, 1, 0.7f));
        Refresh();
    }

    void Claim(int i) {
        if (Quests.Claim(App.Save, i)) {
            App.Save.Persist();
            App.Audio.Sfx("coin");
            App.Toast("\u00a1+" + Quests.List[i][3] + " gemas!");
            Refresh();
        }
    }

    void Refresh() {
        Quests.CheckDay(App.Save);
        for (int i = 0; i < _bars.Count; i++) {
            int prog = Mathf.Min(Quests.Progress(App.Save, i), Quests.Goal(i));
            int goal = Quests.Goal(i);
            bool done = Quests.Done(App.Save, i);
            bool claimed = Quests.Claimed(App.Save, i);
            var b = _bars[i];
            b.Progress.text = prog + " / " + goal;
            var frt = (RectTransform)b.Fill.transform;
            frt.sizeDelta = new Vector2(548f * prog / goal, 28);
            if (claimed) {
                b.State.text = "Reclamado";
                b.Claim.interactable = false;
                b.Claim.image.color = UiKit.C(0x616161);
            } else if (done) {
                b.State.text = "Reclamar";
                b.Claim.interactable = true;
                b.Claim.image.color = UiKit.C(0xFFB300);
            } else {
                b.State.text = "En curso...";
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
