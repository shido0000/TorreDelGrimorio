package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/** Misiones diarias con recompensas de gemas (se renuevan cada día). */
public class DailyScene implements GameView.Scene {

    GameView g;
    Ui.Btn bBack;
    Ui.Btn[] claims = new Ui.Btn[3];
    boolean claimedToast;

    public DailyScene(final GameView g) {
        this.g = g;
        Quests.checkDay(g.save);
        bBack = new Ui.Btn(40, 40, 140, 90, "◀", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new MenuScene(g)); }
        });
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            claims[i] = new Ui.Btn(700, 0, 280, 100, "Reclamar", 0xFF66BB6A, 0xFF2E7D32, new Runnable() {
                public void run() {
                    if (Quests.claim(g.save, idx)) {
                        Sound.play(Sound.COIN);
                        g.persist();
                        claimedToast = true;
                    }
                }
            });
        }
    }

    public void update(float dt) {
        if (claimedToast) {
            claimedToast = false;
            g.toast("¡Recompensa reclamada!");
        }
    }

    public void touch(int action, float x, float y) {
        if (action != android.view.MotionEvent.ACTION_UP) return;
        hit(bBack, x, y);
        for (int i = 0; i < 3; i++) {
            float iy = 340 + i * 340;
            claims[i].y = iy + 150;
            if (claims[i].enabled && claims[i].hit(x, y)) Ui.fire(claims[i]);
        }
    }

    private void hit(Ui.Btn b, float x, float y) {
        if (b.hit(x, y)) Ui.fire(b);
    }

    public boolean back() {
        g.setScene(new MenuScene(g));
        return true;
    }

    public void draw(Canvas c, float t) {
        Paint sky = Ui.paint(0);
        sky.setShader(new android.graphics.LinearGradient(0, 0, 0, 1920,
                0xFF3E2723, 0xFF5D4037, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 1080, 1920, sky);
        Random rnd = new Random(11);
        for (int i = 0; i < 30; i++) {
            float x = rnd.nextFloat() * 1080, y = rnd.nextFloat() * 1920;
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 2 + i);
            c.drawCircle(x, y, 2 + tw * 3, Ui.paint(0x55FFC93C));
        }

        Ui.panel(c, 90, 60, 900, 160, 0xFF311B92, 0xFF1A0560);
        Ui.textCO(c, "MISIONES DIARIAS", 540, 140, 52, true, 0xFFFFC93C);
        Ui.textCO(c, "Se renuevan cada día a medianoche", 540, 195, 28, false, 0xFFCE93D8);

        for (int i = 0; i < 3; i++) {
            float iy = 340 + i * 340;
            Ui.panel(c, 60, iy, 960, 290, 0xFFF7EBC9, 0xFF8D6E63);
            String title = (String) Quests.QUESTS[i][0];
            String desc = (String) Quests.QUESTS[i][1];
            int reward = (Integer) Quests.QUESTS[i][3];
            Ui.textCO(c, title, 110, iy + 70, 42, true, 0xFF3A2E20);
            Ui.textCO(c, desc, 110, iy + 125, 30, false, 0xFF6D4C41);

            // barra de progreso
            float w = 540, bx = 110, by = iy + 165;
            int prog = Math.min(Quests.progress(g.save, i), Quests.goal(i));
            float frac = prog / (float) Quests.goal(i);
            c.drawRoundRect(new RectF(bx, by, bx + w, by + 34), 17, 17, Ui.paint(0xFFD7CCC8));
            if (frac > 0) c.drawRoundRect(new RectF(bx, by, bx + w * frac, by + 34), 17, 17,
                    Ui.paint(Quests.done(g.save, i) ? 0xFF66BB6A : 0xFF42A5F5));
            Ui.textCO(c, prog + "/" + Quests.goal(i), bx + w / 2, by + 28, 24, true, 0xFFFFFFFF);

            Ui.Btn cb = claims[i];
            cb.y = iy + 150;
            if (Quests.claimed(g.save, i)) {
                cb.label = "RECLAMADO";
                cb.enabled = false;
            } else if (Quests.done(g.save, i)) {
                cb.label = "+" + reward + " gemas";
                cb.enabled = true;
            } else {
                cb.label = "+" + reward + " gemas";
                cb.enabled = false;
            }
            cb.draw(c, t);
            Art.drawGem(c, 640, iy + 200, 24);
        }

        bBack.draw(c, t);
        Art.drawGem(c, 900, 105, 24);
        Ui.textCO(c, String.valueOf(g.save.gems), 940, 120, 36, true, 0xFF4DD0E1);
    }
}
