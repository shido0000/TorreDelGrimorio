package com.grimorio.torre;

import android.graphics.Canvas;

import java.util.Random;

/** Pase del Invocador: suscripción semanal o mensual, desbloqueada 7 días tras instalar. */
public class SubScene implements GameView.Scene {

    GameView g;
    Ui.Btn bBack, bWeekly, bMonthly, bRestore;

    public SubScene(final GameView g) {
        this.g = g;
        bBack = new Ui.Btn(40, 40, 140, 90, "◀", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new MenuScene(g)); }
        });
        bWeekly = new Ui.Btn(90, 1420, 420, 150, "Pase Semanal", 0xFF42A5F5, 0xFF1565C0, new Runnable() {
            public void run() { launch("pase_semanal"); }
        });
        bMonthly = new Ui.Btn(570, 1420, 420, 150, "Pase Mensual", 0xFFAB47BC, 0xFF6A1B9A, new Runnable() {
            public void run() { launch("pase_mensual"); }
        });
        bRestore = new Ui.Btn(290, 1610, 500, 100, "Restaurar compras", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() {
                g.billing.start();
                g.toast("Buscando compras...");
            }
        });
        g.billing.setListener(new Billing.Listener() {
            public void onProductsReady() { }
            public void onPurchaseOk(String sku) {
                g.toast("¡Pase del Invocador activo! ¡Gracias!");
                g.setScene(new MenuScene(g));
            }
        });
    }

    private void launch(String sku) {
        boolean ok = g.billing.launch(g.act, sku, "subs");
        if (!ok) g.toast("Suscripciones no disponibles (¿sin Google Play?)");
    }

    public void update(float dt) { }

    public void touch(int action, float x, float y) {
        if (action != android.view.MotionEvent.ACTION_UP) return;
        hit(bBack, x, y); hit(bWeekly, x, y); hit(bMonthly, x, y); hit(bRestore, x, y);
    }

    private void hit(Ui.Btn b, float x, float y) {
        if (b.hit(x, y)) Ui.fire(b);
    }

    public boolean back() {
        g.setScene(new MenuScene(g));
        return true;
    }

    public void draw(Canvas c, float t) {
        c.drawRect(0, 0, 1080, 1920, Ui.paint(0xFF0D2B45));
        Random rnd = new Random(31);
        for (int i = 0; i < 40; i++) {
            float x = rnd.nextFloat() * 1080, y = rnd.nextFloat() * 1920;
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 2 + i);
            c.drawCircle(x, y, 2 + tw * 3, Ui.paint(0xAAFFC93C));
        }

        Ui.panel(c, 90, 80, 900, 150, 0xFF1565C0, 0xFF0D47A1);
        Ui.textC(c, "★ PASE DEL INVOCADOR ★", 540, 150, 50, true, 0xFFFFF59D);
        Ui.textC(c, "Apoya la Torre y recibe ventajas eternas", 540, 205, 28, false, 0xFFB3E5FC);

        // beneficios
        Ui.panel(c, 90, 280, 900, 560, 0xFFF5E6C8, 0xFF8D6E63);
        String[] perks = {
                "✦ Oro x1,5 en todo el juego",
                "✦ +50% de gemas en cada compra",
                "✦ Recompensa diaria: 100 gemas",
                "✦ Aura dorada para tus monstruos",
                "✦ Insignia exclusiva de Invocador",
        };
        for (int i = 0; i < perks.length; i++) {
            Ui.textC(c, perks[i], 540, 360 + i * 90, 36, true, 0xFF3A2E20);
        }

        // estado
        if (g.save.pasePagado()) {
            Ui.panel(c, 90, 900, 900, 200, 0xFF66BB6A, 0xFF2E7D32);
            Ui.textC(c, "¡PASE ACTIVO!", 540, 980, 52, true, 0xFFFFFFFF);
            Ui.textC(c, "Plan: " + (g.save.subSku.equals("pase_mensual") ? "Mensual" : "Semanal")
                    + "  ·  Se renueva automáticamente", 540, 1050, 30, false, 0xFFE8F5E9);
            bWeekly.enabled = false;
            bMonthly.enabled = false;
            bWeekly.label = "Ya activo";
            bMonthly.label = "Ya activo";
        } else if (!g.save.paseDisponible()) {
            long dias = g.save.diasPruebaRestantes();
            Ui.panel(c, 90, 900, 900, 200, 0xFFFFB300, 0xFFE65100);
            Ui.textC(c, "PRUEBA DE FUNDADOR ACTIVA", 540, 975, 42, true, 0xFF3E2723);
            Ui.textC(c, "Disfrutas los beneficios gratis. La suscripción se desbloquea en "
                    + dias + (dias == 1 ? " día." : " días."), 540, 1045, 28, false, 0xFF4E342E);
            bWeekly.enabled = false;
            bMonthly.enabled = false;
            bWeekly.label = "Disponible en " + dias + "d";
            bMonthly.label = "Disponible en " + dias + "d";
        } else {
            Ui.panel(c, 90, 900, 900, 200, 0xFFEF5350, 0xFFB71C1C);
            Ui.textC(c, "Tu prueba gratuita terminó", 540, 975, 42, true, 0xFFFFFFFF);
            Ui.textC(c, "Suscríbete para conservar los beneficios", 540, 1045, 30, false, 0xFFFFEBEE);
            bWeekly.enabled = true;
            bMonthly.enabled = true;
            bWeekly.label = "Pase Semanal";
            bMonthly.label = "Pase Mensual";
        }

        String pw = g.billing.price("pase_semanal");
        String pm = g.billing.price("pase_mensual");
        bWeekly.sub = pw == null ? (g.billing.isReady() ? "Consultando..." : "Requiere Google Play") : pw + " / semana";
        bMonthly.sub = pm == null ? (g.billing.isReady() ? "Consultando..." : "Requiere Google Play") : pm + " / mes";
        bWeekly.draw(c, t);
        bMonthly.draw(c, t);
        bRestore.draw(c, t);

        Ui.textC(c, "Cancela cuando quieras desde Google Play. Los beneficios de la prueba\nfinalizan a los 7 días de instalar si no te suscribes.",
                540, 1800, 24, false, 0xFF90CAF9);
        bBack.draw(c, t);
    }
}
