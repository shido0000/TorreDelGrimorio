using UnityEngine;
using UnityEngine.UI;

/// <summary>Pase del Invocador: prueba de fundador de 7 d\u00edas y suscripci\u00f3n semanal/mensual.</summary>
public class SubScreen : MonoBehaviour, IBackHandler {
    void Start() {
        var bg = UiKit.Panel(gameObject, 0, 0, 1080, 1920, UiKit.C(0x0D47A1), UiKit.C(0x061642));
        bg.raycastTarget = true;

        var back = UiKit.Btn(gameObject, 30, 30, 190, 80, "< Men\u00fa",
            UiKit.C(0x90A4AE), UiKit.C(0x455A64), () => App.Show<MenuScreen>());
        back.GetComponentInChildren<Text>().fontSize = 32;

        UiKit.TextC(gameObject, "PASE DEL INVOCADOR", 540, 60, 54, UiKit.C(0xFFC93C), true, true);
        UiKit.TextC(gameObject, "Apoya el juego y recibe ventajas exclusivas",
            540, 140, 30, new Color(1, 1, 1, 0.85f), false, true);

        var perks = UiKit.Panel(gameObject, 90, 240, 900, 470, UiKit.C(0x1565C0), UiKit.C(0x0B2E6B));
        UiKit.TextC(perks.gameObject, "VENTAJAS DEL PASE", 450, 30, 38, UiKit.C(0xFFE082), true);
        string[] lines = {
            "- Recompensa diaria: +100 gemas cada d\u00eda",
            "- +50% de oro en todas las batallas",
            "- Sin anuncios (nunca los habr\u00e1, pero igual)",
            "- Marco dorado de Guardi\u00e1n en tu torre",
            "- Acceso anticipado a nuevos monstruos",
        };
        for (int i = 0; i < lines.Length; i++)
            UiKit.Text(perks.gameObject, lines[i], 60, 110 + i * 68, 32, Color.white);

        var s = App.Save;
        if (s.PasePagado()) {
            var panel = UiKit.Panel(gameObject, 140, 820, 800, 380, UiKit.C(0x1B5E20), UiKit.C(0x0C3A10));
            UiKit.TextC(panel.gameObject, "SUSCRIPCI\u00d3N ACTIVA", 400, 50, 46, UiKit.C(0xC8FAD0), true);
            string sku = s.subSku == "pase_mensual" ? "Plan mensual" : "Plan semanal";
            UiKit.TextC(panel.gameObject, "Plan: " + sku + "\n\u00a1Gracias por apoyar La Torre del Grimorio!\n\n(Administra o cancela en Google Play)",
                400, 130, 32, Color.white);
        } else if (!s.PaseDisponible()) {
            var panel = UiKit.Panel(gameObject, 140, 820, 800, 380, UiKit.C(0xB71C1C), UiKit.C(0x5F0000));
            UiKit.TextC(panel.gameObject, "Tu prueba termin\u00f3", 400, 40, 44, UiKit.C(0xFFCDD2), true);
            UiKit.TextC(panel.gameObject, "La torre sigue defendi\u00e9ndose, pero sin el Pase\nno podr\u00e1s reclamar las ventajas exclusivas.",
                400, 110, 30, Color.white);
            UiKit.TextC(panel.gameObject, "\u00a1Suscr\u00edbete abajo para seguir al frente!",
                400, 210, 32, UiKit.C(0xFFE082), true);
        } else {
            var panel = UiKit.Panel(gameObject, 140, 820, 800, 380, UiKit.C(0x1B5E20), UiKit.C(0x0C3A10));
            UiKit.TextC(panel.gameObject, "PRUEBA DE FUNDADOR", 400, 40, 42, UiKit.C(0xC8FAD0), true);
            UiKit.TextC(panel.gameObject, "Te quedan " + s.DiasPruebaRestantes() + " d\u00eda(s) de todas las ventajas.\n\nCuando termine, podr\u00e1s continuar con el Pase\nmensual o semanal. \u00a1Sin compromiso!",
                400, 110, 30, Color.white);
        }

        if (!s.PasePagado()) {
            UiKit.Btn(gameObject, 140, 1330, 800, 130, "Suscribirse \u00b7 Semanal",
                UiKit.C(0x42A5F5), UiKit.C(0x1565C0), () => App.Purchaser.Buy("pase_semanal"),
                Purchaser.Price("pase_semanal"));
            UiKit.Btn(gameObject, 140, 1490, 800, 130, "Suscribirse \u00b7 Mensual",
                UiKit.C(0x7E57C2), UiKit.C(0x4527A0), () => App.Purchaser.Buy("pase_mensual"),
                Purchaser.Price("pase_mensual") + " \u00b7 \u00a1Ahorra 25%!");
            UiKit.TextC(gameObject, "Se cobra a trav\u00e9s de tu cuenta de Google Play. Cancela cuando quieras.",
                540, 1660, 26, new Color(1, 1, 1, 0.7f));
        } else {
            UiKit.Btn(gameObject, 290, 1400, 500, 110, "Volver al men\u00fa",
                UiKit.C(0x66BB6A), UiKit.C(0x2E7D32), () => App.Show<MenuScreen>());
        }
    }

    public bool OnBack() {
        App.Show<MenuScreen>();
        return true;
    }
}
