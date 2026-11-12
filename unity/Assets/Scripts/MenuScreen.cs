using UnityEngine;
using UnityEngine.UI;

/// <summary>Menú principal con acceso a todas las pantallas y selector de dificultad.</summary>
public class MenuScreen : MonoBehaviour, IBackHandler {
    Text _status;
    GameObject _offline;
    bool _checked;

    void Start() {
        UiKit.Image(gameObject, "menubg", 0, 0, 1080, 1920);

        var title = UiKit.Panel(gameObject, 120, 80, 840, 210, UiKit.C(0x311B92), UiKit.C(0x1A0560));
        title.raycastTarget = false;
        UiKit.TextC(title.gameObject, "LA TORRE DEL", 420, 30, 60, UiKit.C(0xFFC93C), true, true);
        UiKit.TextC(title.gameObject, "GRIMORIO", 420, 100, 74, UiKit.C(0xFFE082), true, true);
        UiKit.TextC(title.gameObject, "Idle · AFK · Tower Defense", 420, 185, 30, UiKit.C(0xCE93D8), false, true);

        var res = UiKit.Panel(gameObject, 30, 320, 1020, 90, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        res.raycastTarget = false;
        UiKit.Image(res.gameObject, "coin", 30, 15, 60, 60);
        _goldTxt = UiKit.Text(res.gameObject, "", 105, 15, 40, UiKit.C(0x9C6A00), true);
        UiKit.Image(res.gameObject, "gem", 370, 20, 50, 50);
        _gemTxt = UiKit.Text(res.gameObject, "", 435, 15, 40, UiKit.C(0x00838F), true);
        _waveTxt = UiKit.Text(res.gameObject, "", 640, 22, 30, UiKit.C(0x3A2E20), true);

        Btn(290, 920, 500, 115, "JUGAR", 0x66BB6A, 0x2E7D32, () => App.Show<GameScreen>());
        Btn(290, 1050, 500, 95, "Invocar monstruos", 0xAB47BC, 0x6A1B9A, () => App.Show<SummonScreen>());
        Btn(290, 1160, 500, 95, "Tienda y mejoras", 0xFFB300, 0xE65100, () => App.Show<ShopScreen>());
        Btn(290, 1270, 500, 95, "Misiones diarias", 0x26C6DA, 0x00838F, () => App.Show<DailyScreen>());
        Btn(290, 1380, 500, 95, "Pase del Invocador", 0x42A5F5, 0x1565C0, () => App.Show<SubScreen>());
        Btn(290, 1490, 240, 88, "Historia", 0x90A4AE, 0x455A64,
            () => App.ShowStory(Mathf.Max(0, App.Save.story - 1)));
        Btn(550, 1490, 240, 88, "Logros", 0xFFB300, 0xE65100, () => App.Show<AchieveScreen>());
        _difBtn = Btn(290, 1595, 500, 92, "", 0x66BB6A, 0x2E7D32, () => {
            App.Save.difficulty = (App.Save.difficulty + 1) % 3;
            App.Save.Persist();
            string[] msg = {
                "Dificultad Normal: enemigos y recompensas estándar",
                "Dificultad Difícil: enemigos +50%, oro +40%",
                "Dificultad Pesadilla: enemigos +130%, oro +100%" };
            App.Toast(msg[App.Save.difficulty]);
            RefreshDif();
        });

        _status = UiKit.TextC(gameObject, "", 540, 1720, 30, UiKit.C(0x33691E), true, true);

        if (App.Save.PaseActivo() && SaveData.Now() - App.Save.lastDailyClaim > 86400000L) {
            var daily = UiKit.Panel(gameObject, 190, 870, 700, 110, UiKit.C(0x1B5E20), UiKit.C(0xFFE082));
            UiKit.TextC(daily.gameObject, "Recompensa diaria del Pase: +100 gemas",
                350, 32, 30, UiKit.C(0xFFF176), true, true);
            daily.gameObject.AddComponent<DailyBonusTap>();
        }
    }

    Text _goldTxt, _gemTxt, _waveTxt;
    Button _difBtn;

    void Btn(float x, float y, float w, float h, string label, int top, int bottom,
             System.Action onClick) {
        UiKit.Btn(gameObject, x, y, w, h, label, UiKit.C(top), UiKit.C(bottom), () => onClick());
    }

    void RefreshDif() {
        int[] dc = { 0x66BB6A, 0xFFB300, 0xEF5350 };
        _difBtn.image.color = UiKit.C(dc[App.Save.difficulty]);
        _difBtn.GetComponentInChildren<Text>().text = "Dificultad: " + Defs.Dif[App.Save.difficulty];
    }

    void Update() {
        if (!_checked) {
            _checked = true;
            long elapsed = SaveData.Now() - App.Save.lastSeen;
            if (elapsed > 120000 && App.Save.IncomePerSec() > 0) {
                long earned = (long)(App.Save.IncomePerSec() * Mathf.Min(elapsed / 1000f, 8 * 3600));
                App.Save.gold += earned;
                _offline = new GameObject("Offline", typeof(RectTransform));
                _offline.transform.SetParent(gameObject.transform, false);
                UiKit.Stretch((RectTransform)_offline.transform);
                var dim = UiKit.Panel(_offline, 0, 0, 1080, 1920, new Color(0, 0, 0, 0.6f), new Color(0, 0, 0, 0));
                var panel = UiKit.Panel(_offline, 140, 700, 800, 420, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
                UiKit.TextC(panel.gameObject, "¡Bienvenido de nuevo!", 400, 50, 46, UiKit.C(0x3A2E20), true);
                UiKit.TextC(panel.gameObject, "¡Tus monstruos trabajaron mientras estabas fuera!\n+" +
                    UiKit.Fmt(earned) + " de oro\n\n(Toca para continuar)", 400, 130, 32, UiKit.C(0x2E7D32));
                dim.gameObject.AddComponent<DismissOnTap>();
            }
            App.Save.Persist();
            RefreshDif();
        }
        _goldTxt.text = UiKit.Fmt(App.Save.gold);
        _gemTxt.text = App.Save.gems.ToString();
        _waveTxt.text = "Oleada " + Mathf.Max(1, App.Save.wave) + " · Récord " + App.Save.bestWave;
        if (App.Save.PasePagado()) {
            _status.text = "Pase del Invocador ACTIVO";
            _status.color = UiKit.C(0x1565C0);
        } else if (App.Save.PaseDisponible()) {
            _status.text = "Tu prueba terminó: ¡suscríbete al Pase!";
            _status.color = UiKit.C(0xB71C1C);
        } else {
            _status.text = "Prueba de fundador: " + App.Save.DiasPruebaRestantes() + " día(s) restantes";
            _status.color = UiKit.C(0x33691E);
        }
    }

    public bool OnBack() => false;

    class DailyBonusTap : MonoBehaviour, UnityEngine.EventSystems.IPointerClickHandler {
        public void OnPointerClick(UnityEngine.EventSystems.PointerEventData e) {
            App.Save.lastDailyClaim = SaveData.Now();
            App.Save.AddGems(100);
            App.Save.Persist();
            App.Audio.Sfx("coin");
            App.Toast("¡+100 gemas del Pase del Invocador!");
            Destroy(gameObject);
        }
    }

    class DismissOnTap : MonoBehaviour, UnityEngine.EventSystems.IPointerClickHandler {
        public void OnPointerClick(UnityEngine.EventSystems.PointerEventData e) {
            Destroy(transform.parent.gameObject);
        }
    }
}
