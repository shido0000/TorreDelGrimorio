package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/** Menú principal con historia, acceso a invocación, tienda y Pase. */
public class MenuScene implements GameView.Scene {

    GameView g;
    Ui.Btn bPlay, bSummon, bShop, bQuests, bPass, bStory;
    boolean showOffline;
    String offlineMsg;
    boolean checked = false;

    public MenuScene(GameView g) {
        this.g = g;
        bPlay = new Ui.Btn(290, 950, 500, 120, "JUGAR", 0xFF66BB6A, 0xFF2E7D32, new Runnable() {
            public void run() { g.setScene(new GameScene(g)); }
        });
        bSummon = new Ui.Btn(290, 1090, 500, 100, "Invocar monstruos", 0xFFAB47BC, 0xFF6A1B9A, new Runnable() {
            public void run() { g.setScene(new SummonScene(g)); }
        });
        bShop = new Ui.Btn(290, 1210, 500, 100, "Tienda y mejoras", 0xFFFFB300, 0xFFE65100, new Runnable() {
            public void run() { g.setScene(new ShopScene(g)); }
        });
        bQuests = new Ui.Btn(290, 1330, 500, 100, "Misiones diarias", 0xFF26C6DA, 0xFF00838F, new Runnable() {
            public void run() { g.setScene(new DailyScene(g)); }
        });
        bPass = new Ui.Btn(290, 1450, 500, 100, "Pase del Invocador", 0xFF42A5F5, 0xFF1565C0, new Runnable() {
            public void run() { g.setScene(new SubScene(g)); }
        });
        bStory = new Ui.Btn(290, 1570, 500, 90, "Historia", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new StoryScene(g, Math.max(0, g.save.story - 1), new MenuScene(g))); }
        });
    }

    public void update(float dt) {
        if (checked) return;
        checked = true;
        Quests.checkDay(g.save);
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
        hit(bPlay, x, y); hit(bSummon, x, y); hit(bShop, x, y); hit(bQuests, x, y); hit(bPass, x, y); hit(bStory, x, y);
        if (dailyAvailable() && Math.abs(x - 540) < 280 && Math.abs(y - 880) < 60) {
            g.save.lastDailyClaim = System.currentTimeMillis();
            g.save.addGems(100);
            g.persist();
            g.toast("¡+100 gemas del Pase del Invocador!");
        }
    }

    private void hit(Ui.Btn b, float x, float y) {
        if (b.hit(x, y)) Ui.fire(b);
    }

    public boolean back() {
        return false;
    }

    public void draw(Canvas c, float t) {
        // cielo con degradado y sol
        Paint sky = Ui.paint(0);
        sky.setShader(new LinearGradient(0, 0, 0, 1920,
                0xFF7EC8F0, 0xFFB8E6A0, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 1080, 1920, sky);
        Paint sun = Ui.paint(0);
        sun.setShader(new RadialGradient(880, 240, 260,
                0xFFFFF59D, 0x00FFF59D, Shader.TileMode.CLAMP));
        c.drawCircle(880, 240, 260, sun);
        Art.drawCoin(c, 880, 240, 70);

        // nubes
        Paint cloud = Ui.paint(0xE6FFFFFF);
        for (int i = 0; i < 5; i++) {
            float cx2 = ((t * (10 + i * 6) + i * 260) % 1300) - 110;
            float cy2 = 130 + (i % 3) * 90;
            c.drawOval(new RectF(cx2 - 100, cy2 - 30, cx2 + 100, cy2 + 30), cloud);
            c.drawOval(new RectF(cx2 - 45, cy2 - 58, cx2 + 55, cy2 + 8), cloud);
        }

        // colinas
        Paint hill1 = Ui.paint(0xFF66BB6A);
        c.drawOval(new RectF(-300, 1150, 700, 1750), hill1);
        Paint hill2 = Ui.paint(0xFF57A85C);
        c.drawOval(new RectF(400, 1200, 1400, 1800), hill2);
        // pradera
        Paint grass = Ui.paint(0);
        grass.setShader(new LinearGradient(0, 1350, 0, 1920,
                0xFF7EC850, 0xFF5FAE4A, Shader.TileMode.CLAMP));
        c.drawRect(0, 1380, 1080, 1920, grass);

        // decoración
        Art.drawTree(c, 120, 1420, 200, 0);
        Art.drawTree(c, 960, 1450, 230, 1);
        Art.drawBush(c, 250, 1560, 120);
        Art.drawFlower(c, 60, 1700, 60, 0);
        Art.drawFlower(c, 1010, 1660, 60, 1);
        Art.drawRock(c, 880, 1760, 70);

        // torre al fondo
        c.drawRoundRect(new RectF(440, 560, 640, 1100), 24, 24, Ui.paint(0xFF8D6E63));
        c.drawRoundRect(new RectF(410, 500, 670, 600), 28, 28, Ui.paint(0xFF6D4C41));
        c.drawRect(500, 700, 580, 830, Ui.paint(0xFF4E342E));
        c.drawRect(500, 930, 580, 1060, Ui.paint(0xFF4E342E));
        c.drawLine(540, 500, 540, 380, Ui.paint(0xFF5D4037));
        android.graphics.Path flag = new android.graphics.Path();
        flag.moveTo(540, 385); flag.lineTo(650, 425); flag.lineTo(540, 465); flag.close();
        c.drawPath(flag, Ui.paint(0xFFE53935));
        Paint fs = Ui.paint(Ui.OUTLINE);
        fs.setStyle(Paint.Style.STROKE);
        fs.setStrokeWidth(6);
        c.drawPath(flag, fs);

        // personajes flotando
        float bob1 = (float) Math.sin(t * 2) * 14;
        float bob2 = (float) Math.sin(t * 2 + 1.5f) * 12;
        Art.drawMon(c, 7, 220, 1210 + bob1, 190, t, false, 0);
        Art.drawMon(c, 3, 860, 1230 + bob2, 160, t, false, 0);
        Art.drawPortrait(c, 0, 540, 1150, 170, t);

        // título
        Ui.panel(c, 120, 80, 840, 210, 0xFF311B92, 0xFF1A0560);
        Paint tp = Ui.text(64, true, 0xFFFFC93C);
        tp.setTextAlign(Paint.Align.CENTER);
        Ui.textO(c, "LA TORRE DEL", 540, 165, tp);
        Paint tp2 = Ui.text(76, true, 0xFFFFE082);
        tp2.setTextAlign(Paint.Align.CENTER);
        Ui.textO(c, "GRIMORIO", 540, 255, tp2);
        Ui.textCO(c, "Idle · AFK · Tower Defense", 540, 300, 32, false, 0xFFCE93D8);

        // recursos
        Ui.panel(c, 30, 320, 1020, 90, 0xFFF7EBC9, 0xFF8D6E63);
        Art.drawCoin(c, 90, 365, 30);
        Ui.textCO(c, Ui.fmt(g.save.gold), 148, 380, 40, true, 0xFF9C6A00);
        Art.drawGem(c, 430, 365, 26);
        Ui.textCO(c, String.valueOf(g.save.gems), 474, 380, 40, true, 0xFF00838F);
        Ui.textCO(c, "Oleada " + Math.max(1, g.save.wave) + " · Récord " + g.save.bestWave, 1000, 380, 32, true, 0xFF3A2E20);

        if (dailyAvailable()) {
            float pulse = 1 + (float) Math.sin(t * 5) * 0.04f;
            Ui.textCO(c, "★ Recompensa diaria del Pase: +100 gemas ★", 540, 890, 36 * pulse, true, 0xFFFFF176);
        }

        bPlay.draw(c, t);
        bSummon.draw(c, t);
        bShop.draw(c, t);
        bQuests.draw(c, t);
        boolean hayPremio = false;
        for (int i = 0; i < 3; i++) if (Quests.done(g.save, i) && !Quests.claimed(g.save, i)) hayPremio = true;
        if (hayPremio) {
            float pulse = 1 + (float) Math.sin(t * 6) * 0.15f;
            c.drawCircle(770, 1330, 34 * pulse, Ui.paint(0xFFE53935));
            Ui.textCO(c, "!", 770, 1348, 44 * pulse, true, 0xFFFFFFFF);
        }
        bPass.draw(c, t);
        bStory.draw(c, t);

        if (!g.save.paseDisponible()) {
            Ui.textCO(c, "Prueba de fundador: " + g.save.diasPruebaRestantes() + " día(s) restantes",
                    540, 1700, 30, true, 0xFF33691E);
        } else if (g.save.pasePagado()) {
            Ui.textCO(c, "Pase del Invocador ACTIVO", 540, 1700, 30, true, 0xFF1565C0);
        } else {
            Ui.textCO(c, "Tu prueba terminó: ¡suscríbete al Pase!", 540, 1700, 30, true, 0xFFB71C1C);
        }
        Ui.textCO(c, "v1.3 · Hecho con magia chibi", 540, 1885, 24, false, 0xFF33691E);

        if (showOffline) {
            Paint dim = Ui.paint(0x99000000);
            c.drawRect(0, 0, 1080, 1920, dim);
            Ui.panel(c, 140, 700, 800, 420, 0xFFF7EBC9, 0xFF8D6E63);
            Ui.textCO(c, "¡Bienvenido de nuevo!", 540, 790, 48, true, 0xFF3A2E20);
            Ui.textCO(c, offlineMsg, 540, 900, 34, false, 0xFF2E7D32);
            Ui.textCO(c, "(Toca para continuar)", 540, 1060, 26, false, 0xFF6D4C41);
        }
    }
}
