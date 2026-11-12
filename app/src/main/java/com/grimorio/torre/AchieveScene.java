package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/** Logros permanentes: lista desplazable con progreso y recompensas de gemas. */
public class AchieveScene implements GameView.Scene {

    GameView g;
    Ui.Btn bBack;
    Ui.Btn[] claims = new Ui.Btn[Achievements.LIST.length];
    float scroll = 0, maxScroll = 0;
    float downY, lastY;
    boolean dragging;
    boolean claimedToast;

    public AchieveScene(final GameView g) {
        this.g = g;
        bBack = new Ui.Btn(40, 40, 140, 90, "\u25c0", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new MenuScene(g)); }
        });
        for (int i = 0; i < Achievements.LIST.length; i++) {
            final int idx = i;
            claims[i] = new Ui.Btn(700, 0, 280, 96, "Reclamar", 0xFF66BB6A, 0xFF2E7D32, new Runnable() {
                public void run() {
                    if (Achievements.claim(g.save, idx)) {
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
            g.toast("\u00a1Logro reclamado!");
        }
        maxScroll = 260 + Achievements.LIST.length * 250 + 120 - 1920;
        if (maxScroll < 0) maxScroll = 0;
    }

    public void touch(int action, float x, float y) {
        if (action == android.view.MotionEvent.ACTION_DOWN) {
            downY = y; lastY = y; dragging = false;
            return;
        }
        if (action == android.view.MotionEvent.ACTION_MOVE) {
            if (Math.abs(y - downY) > 30) dragging = true;
            if (dragging) {
                scroll -= (y - lastY);
                if (scroll < 0) scroll = 0;
                if (scroll > maxScroll) scroll = maxScroll;
            }
            lastY = y;
            return;
        }
        if (action != android.view.MotionEvent.ACTION_UP) return;
        if (dragging) { dragging = false; return; }
        hit(bBack, x, y);
        for (int i = 0; i < claims.length; i++) {
            if (claims[i].enabled && claims[i].hit(x, y + scroll)) {
                Ui.fire(claims[i]);
                return;
            }
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
        Paint bg = Ui.paint(0);
        bg.setShader(new android.graphics.LinearGradient(0, 0, 0, 1920,
                0xFF4A148C, 0xFF880E4F, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 1080, 1920, bg);
        Random rnd = new Random(23);
        for (int i = 0; i < 30; i++) {
            float x = rnd.nextFloat() * 1080, y = rnd.nextFloat() * 1920;
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 2 + i);
            c.drawCircle(x, y, 2 + tw * 3, Ui.paint(0x55FFC93C));
        }

        Ui.panel(c, 90, 60, 900, 160, 0xFF311B92, 0xFF1A0560);
        Ui.textCO(c, "LOGROS PERMANENTES", 540, 140, 52, true, 0xFFFFC93C);
        Ui.textCO(c, "Se cumplen una sola vez \u00b7 \u00a1recompensas de gemas!", 540, 195, 28, false, 0xFFCE93D8);

        c.save();
        c.translate(0, -scroll);
        for (int i = 0; i < Achievements.LIST.length; i++) {
            float iy = 260 + i * 250;
            Ui.panel(c, 60, iy, 960, 220, 0xFFF7EBC9, 0xFF8D6E63);
            String title = (String) Achievements.LIST[i][0];
            String desc = (String) Achievements.LIST[i][1];
            int reward = (Integer) Achievements.LIST[i][4];
            boolean done = Achievements.done(g.save, i);
            boolean claimed = Achievements.claimed(g.save, i);

            Ui.textCO(c, (claimed ? "\u2713 " : "") + title, 110, iy + 62, 40, true,
                    claimed ? 0xFF2E7D32 : 0xFF3A2E20);
            Ui.textCO(c, desc, 110, iy + 112, 28, false, 0xFF6D4C41);

            float w = 540, bx = 110, by = iy + 145;
            int prog = Math.min(Achievements.progress(g.save, i), Achievements.goal(i));
            float frac = prog / (float) Achievements.goal(i);
            c.drawRoundRect(new RectF(bx, by, bx + w, by + 30), 15, 15, Ui.paint(0xFFD7CCC8));
            if (frac > 0) c.drawRoundRect(new RectF(bx, by, bx + w * frac, by + 30), 15, 15,
                    Ui.paint(done ? 0xFF66BB6A : 0xFFAB47BC));
            Ui.textCO(c, Ui.fmt(prog) + "/" + Ui.fmt(Achievements.goal(i)), bx + w / 2, by + 25, 22, true, 0xFFFFFFFF);

            Ui.Btn cb = claims[i];
            cb.y = iy + 62;
            if (claimed) {
                cb.label = "RECLAMADO";
                cb.enabled = false;
            } else if (done) {
                cb.label = "+" + reward + " \u2726";
                cb.enabled = true;
            } else {
                cb.label = "+" + reward + " \u2726";
                cb.enabled = false;
            }
            cb.draw(c, t);
            Art.drawGem(c, 660, iy + 110, 22);
        }
        c.restore();

        bBack.draw(c, t);
        Art.drawGem(c, 900, 105, 24);
        Ui.textCO(c, String.valueOf(g.save.gems), 940, 120, 36, true, 0xFF4DD0E1);
    }
}
