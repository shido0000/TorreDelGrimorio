package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/** Menú principal con historia, acceso a invocación, tienda y Pase. */
public class MenuScene implements GameView.Scene {

    GameView g;
    Ui.Btn bPlay, bSummon, bShop, bPass, bStory;
    boolean showOffline;
    String offlineMsg;
    boolean showDaily;
    boolean checked = false;

    public MenuScene(GameView g) {
        this.g = g;
        bPlay = new Ui.Btn(290, 980, 500, 130, "JUGAR", 0xFF66BB6A, 0xFF2E7D32, new Runnable() {
            public void run() { g.setScene(new GameScene(g)); }
        });
        bSummon = new Ui.Btn(290, 1140, 500, 110, "Invocar monstruos", 0xFFAB47BC, 0xFF6A1B9A, new Runnable() {
            public void run() { g.setScene(new SummonScene(g)); }
        });
        bShop = new Ui.Btn(290, 1270, 500, 110, "Tienda y mejoras", 0xFFFFB300, 0xFFE65100, new Runnable() {
            public void run() { g.setScene(new ShopScene(g)); }
        });
        bPass = new Ui.Btn(290, 1400, 500, 110, "Pase del Invocador", 0xFF42A5F5, 0xFF1565C0, new Runnable() {
            public void run() { g.setScene(new SubScene(g)); }
        });
        bStory = new Ui.Btn(290, 1530, 500, 100, "Historia", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new StoryScene(g, Math.max(0, g.save.story - 1), new MenuScene(g))); }
        });
    }

    public void update(float dt) {
        if (checked) return;
        checked = true;
        long now = System.currentTimeMillis();
        long elapsed = now - g.save.lastSeen;
        if (elapsed > 120000 && g.save.incomePerSec() > 0) {
            long earned = (long) (g.save.incomePerSec() * Math.min(elapsed / 1000.0, 8 * 3600));
            g.save.gold += earned;
            offlineMsg = "¡Tus monstruos trabajaron mientras estabas fuera!\n\n+" + Ui.fmt(earned) + " de oro";
            showOffline = true;
        }
        g.persist();
    }

    public boolean dailyAvailable() {
        return g.save.paseActivo() && System.currentTimeMillis() - g.save.lastDailyClaim > 86400000L;
    }

    public void touch(int action, float x, float y) {
        if (action != android.view.MotionEvent.ACTION_UP) return;
        if (showOffline) { showOffline = false; return; }
        if (showDaily) { showDaily = false; g.persist(); return; }
        hit(bPlay, x, y); hit(bSummon, x, y); hit(bShop, x, y); hit(bPass, x, y); hit(bStory, x, y);
        if (dailyAvailable() && Math.abs(x - 540) < 260 && Math.abs(y - 870) < 60) {
            g.save.lastDailyClaim = System.currentTimeMillis();
            g.save.addGems(100);
            g.persist();
            g.toast("¡+100 gemas del Pase del Invocador!");
        }
    }

    private void hit(Ui.Btn b, float x, float y) {
        if (b.hit(x, y) && b.on != null) b.on.run();
    }

    public boolean back() {
        return false;
    }

    public void draw(Canvas c, float t) {
        // fondo
        c.drawRect(0, 0, 1080, 1920, Ui.paint(0xFF7EC850));
        Random rnd = new Random(99);
        for (int i = 0; i < 14; i++) {
            float x = rnd.nextFloat() * 1080, y = 500 + rnd.nextFloat() * 1300;
            int k = rnd.nextInt(3);
            if (k == 0) Art.drawTree(c, x, y, 120 + rnd.nextFloat() * 80, rnd.nextInt(3));
            else if (k == 1) Art.drawRock(c, x, y, 50);
            else Art.drawFlower(c, x, y, 40, rnd.nextInt(3));
        }
        // torre al fondo
        Ui.paint(0xFF8D6E63);
        c.drawRoundRect(new RectF(430, 380, 650, 900), 20, 20, Ui.paint(0xFF8D6E63));
        c.drawRoundRect(new RectF(400, 330, 680, 420), 24, 24, Ui.paint(0xFF6D4C41));
        c.drawRect(500, 500, 580, 620, Ui.paint(0xFF4E342E));
        c.drawRect(500, 700, 580, 820, Ui.paint(0xFF4E342E));
        // banderín
        c.drawLine(540, 330, 540, 220, Ui.paint(0xFF5D4037));
        Path2(c, 540, 225, 640, 260, 540, 295, 0xFFE53935);
        // personajes
        Art.drawMon(c, 7, 250, 870, 200, t, false, 0);
        Art.drawMon(c, 3, 830, 880, 170, t, false, 0);
        Art.drawPortrait(c, 0, 540, 830, 150, t);

        // título
        Ui.panel(c, 140, 90, 800, 190, 0xFF311B92, 0xFF1A0560);
        Ui.textC(c, "LA TORRE DEL GRIMORIO", 540, 165, 56, true, 0xFFFFC93C);
        Ui.textC(c, "Idle · AFK · Tower Defense", 540, 230, 34, false, 0xFFCE93D8);

        // recursos
        Ui.panel(c, 30, 300, 1020, 90, 0xFFF5E6C8, 0xFF8D6E63);
        Art.drawCoin(c, 90, 345, 28);
        Ui.textC(c, Ui.fmt(g.save.gold), 135, 360, 38, true, 0xFFB8860B);
        Art.drawGem(c, 420, 345, 24);
        Ui.textC(c, String.valueOf(g.save.gems), 460, 360, 38, true, 0xFF00838F);
        Ui.textC(c, "Oleada " + Math.max(1, g.save.wave) + " · Récord " + g.save.bestWave, 1000, 360, 32, true, 0xFF3A2E20);

        if (dailyAvailable()) {
            float pulse = 1 + (float) Math.sin(t * 5) * 0.05f;
            Ui.textC(c, "★ Recompensa diaria del Pase: +100 gemas ★", 540, 880, 36 * pulse, true, 0xFFFFC93C);
        }

        bPlay.draw(c, t);
        bSummon.draw(c, t);
        bShop.draw(c, t);
        bPass.draw(c, t);
        bStory.draw(c, t);

        if (!g.save.paseDisponible()) {
            Ui.textC(c, "Prueba de fundador: " + g.save.diasPruebaRestantes() + " día(s) restantes",
                    540, 1690, 30, true, 0xFF33691E);
        } else if (g.save.pasePagado()) {
            Ui.textC(c, "Pase del Invocador ACTIVO", 540, 1690, 30, true, 0xFF1565C0);
        } else {
            Ui.textC(c, "Tu prueba terminó: ¡suscríbete al Pase!", 540, 1690, 30, true, 0xFFB71C1C);
        }
        Ui.textC(c, "v1.0 · Hecho con magia chibi", 540, 1880, 24, false, 0xFF33691E);

        if (showOffline) {
            Paint dim = Ui.paint(0x99000000);
            c.drawRect(0, 0, 1080, 1920, dim);
            Ui.panel(c, 140, 700, 800, 420, 0xFFF5E6C8, 0xFF8D6E63);
            Ui.textC(c, "¡Bienvenido de nuevo!", 540, 790, 48, true, 0xFF3A2E20);
            Ui.textC(c, offlineMsg, 540, 900, 34, false, 0xFF2E7D32);
            Ui.textC(c, "(Toca para continuar)", 540, 1060, 26, false, 0xFF6D4C41);
        }
        if (showDaily) {
            Paint dim = Ui.paint(0x99000000);
            c.drawRect(0, 0, 1080, 1920, dim);
        }
    }

    private void Path2(Canvas c, float x1, float y1, float x2, float y2, float x3, float y3, int col) {
        android.graphics.Path p = new android.graphics.Path();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        p.close();
        c.drawPath(p, Ui.paint(col));
        Paint st = Ui.paint(Ui.OUTLINE);
        st.setStyle(Paint.Style.STROKE);
        st.setStrokeWidth(5);
        c.drawPath(p, st);
    }
}
