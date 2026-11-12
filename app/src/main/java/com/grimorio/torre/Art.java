package com.grimorio.torre;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.HashMap;

/** Sprites PNG generados (estilo chibi con cel-shading) + efectos dibujados por código. */
public final class Art {

    private static final HashMap<String, Bitmap> CACHE = new HashMap<String, Bitmap>();
    private static BitmapPaint BP = new BitmapPaint();

    private static class BitmapPaint extends Paint {
        BitmapPaint() {
            setAntiAlias(true);
            setFilterBitmap(true);
        }
    }

    public static void init(Context ctx) {
        if (!CACHE.isEmpty()) return;
        String[] names = {
                "mon0", "mon1", "mon2", "mon3", "mon4", "mon5", "mon6", "mon7", "mon8",
                "hero0", "hero1", "hero2", "hero3", "hero4", "hero5",
                "aprendiz", "orin", "chest", "coin", "gem",
                "tree0", "tree1", "tree2", "rock", "flower0", "flower1", "flower2", "bush",
                "bg0", "bg1", "bg2", "bg3", "menubg"
        };
        for (String n : names) {
            int id = ctx.getResources().getIdentifier(n, "drawable", ctx.getPackageName());
            if (id != 0) CACHE.put(n, BitmapFactory.decodeResource(ctx.getResources(), id));
        }
    }

    private static Bitmap bmp(String key) {
        return CACHE.get(key);
    }

    /** Fondo de batalla pre-renderizado (césped, camino y decoración) según el capítulo. */
    public static void drawBgBattle(Canvas c, int chapter) {
        Bitmap b = bmp("bg" + (chapter % 4));
        if (b != null) c.drawBitmap(b, 0, 0, BP);
    }

    /** Fondo del menú pre-renderizado (colinas, torre y pradera). */
    public static void drawBgMenu(Canvas c) {
        Bitmap b = bmp("menubg");
        if (b != null) c.drawBitmap(b, 0, 0, BP);
    }

    /** Dibuja un sprite PNG centrado en (x, y) dentro de un cuadrado de lado s*1.5, anclado al suelo. */
    private static void sprite(Canvas c, String key, float x, float y, float s, float bob, float squash) {
        Bitmap b = bmp(key);
        if (b == null) return;
        float half = s * 0.75f;
        float h = half * 2 * squash;
        RectF dst = new RectF(x - half, y - s * 1.05f + bob + (h - half * 2), x + half, y + s * 0.45f + bob);
        c.drawBitmap(b, null, dst, BP);
    }

    private static void shadow(Canvas c, float x, float y, float s) {
        Paint p = Ui.paint(0x55000000);
        c.drawOval(new RectF(x - s * 0.42f, y - s * 0.06f, x + s * 0.42f, y + s * 0.14f), p);
    }

    // ================== MONSTRUOS ==================

    public static void drawMon(Canvas c, int art, float x, float y, float s, float t, boolean dead, float flash) {
        if (dead) { drawTomb(c, x, y, s); return; }
        float ph = t * 3 + x * 0.05f;
        float bob = (float) Math.sin(ph) * s * 0.05f;
        float squash = 1 + (float) Math.sin(ph * 2) * 0.025f;
        shadow(c, x, y + s * 0.42f, s);
        sprite(c, "mon" + art, x, y, s, bob, squash);
        if (flash > 0) {
            Paint fl = Ui.paint(((int) (flash * 140) << 24) | 0xFFFFFF);
            c.drawCircle(x, y - s * 0.1f, s * 0.5f, fl);
        }
    }

    private static void drawTomb(Canvas c, float x, float y, float s) {
        float w = s * 0.52f, h = s * 0.58f;
        RectF r = new RectF(x - w / 2, y + s * 0.42f - h, x + w / 2, y + s * 0.42f);
        Paint p = Ui.paint(0xFFB0BEC5);
        c.drawRoundRect(r, w * 0.45f, w * 0.45f, p);
        p = Ui.paint(Ui.OUTLINE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(6);
        c.drawRoundRect(r, w * 0.45f, w * 0.45f, p);
        c.drawLine(x, y + s * 0.42f - h * 0.75f, x, y + s * 0.42f - h * 0.3f, p);
        c.drawLine(x - w * 0.22f, y + s * 0.42f - h * 0.58f, x + w * 0.22f, y + s * 0.42f - h * 0.58f, p);
        Ui.textC(c, "zzz", x + w * 0.4f, y + s * 0.42f - h - 8, s * 0.18f, true, 0xAAFFFFFF);
    }

    // ================== HÉROES ==================

    public static void drawHero(Canvas c, int art, float x, float y, float s, float t, float hpFrac, boolean boss) {
        float ph = t * 7 + x * 0.07f;
        float bob = -(float) Math.abs(Math.sin(ph)) * s * 0.06f;
        float lean = (float) Math.sin(t * 2 + x * 0.03f) * 2.5f;
        shadow(c, x, y + s * 0.42f, s);
        c.save();
        c.rotate(lean, x, y);
        sprite(c, "hero" + art, x, y, s, bob, 1);
        c.restore();
        if (hpFrac < 1) {
            float bw = s * 0.75f, bh = s * 0.09f;
            float bx = x - bw / 2, by = y - s * (boss ? 1.15f : 0.85f);
            RectF bg = new RectF(bx - 3, by - 3, bx + bw + 3, by + bh + 3);
            Paint p = Ui.paint(0xFF33323A);
            c.drawRoundRect(bg, bh, bh, p);
            p = Ui.paint(0xFF616161);
            c.drawRoundRect(new RectF(bx, by, bx + bw, by + bh), bh * 0.5f, bh * 0.5f, p);
            if (hpFrac > 0) {
                p = Ui.paint(boss ? 0xFFE53935 : 0xFF66BB6A);
                c.drawRoundRect(new RectF(bx, by, bx + bw * hpFrac, by + bh), bh * 0.5f, bh * 0.5f, p);
            }
        }
    }

    // ================== OBJETOS ==================

    public static void drawCoin(Canvas c, float x, float y, float r) {
        Bitmap b = bmp("coin");
        if (b == null) return;
        RectF dst = new RectF(x - r, y - r, x + r, y + r);
        c.drawBitmap(b, null, dst, BP);
    }

    public static void drawGem(Canvas c, float x, float y, float r) {
        Bitmap b = bmp("gem");
        if (b == null) return;
        RectF dst = new RectF(x - r, y - r, x + r, y + r);
        c.drawBitmap(b, null, dst, BP);
    }

    public static void drawChest(Canvas c, float x, float y, float s, float t, int hits, float maxHp) {
        float glow = 0.5f + 0.5f * (float) Math.sin(t * 2);
        Paint g = Ui.paint(hits >= 2 ? 0x30E53935 : 0x30FFC93C);
        c.drawCircle(x, y, s * (1.15f + glow * 0.18f), g);
        sprite(c, "chest", x, y, s, 0, 1);
        int left = Math.max(0, 3 - hits);
        float bw = s * 1.15f;
        float bx = x - bw / 2, by = y + s * 0.62f;
        Paint p = Ui.paint(0xFF33323A);
        c.drawRoundRect(new RectF(bx - 3, by - 3, bx + bw + 3, by + s * 0.16f + 3), 8, 8, p);
        p = Ui.paint(0xFF616161);
        float frac = left / 3f;
        c.drawRoundRect(new RectF(bx, by, bx + bw, by + s * 0.16f), 6, 6, p);
        if (frac > 0) {
            p = Ui.paint(left >= 2 ? 0xFF66BB6A : 0xFFFFB300);
            c.drawRoundRect(new RectF(bx, by, bx + bw * frac, by + s * 0.16f), 6, 6, p);
        }
    }

    public static void drawTree(Canvas c, float x, float y, float s, int variant) {
        sprite(c, "tree" + (variant % 3), x, y, s, 0, 1);
    }

    public static void drawRock(Canvas c, float x, float y, float s) {
        sprite(c, "rock", x, y, s, 0, 1);
    }

    public static void drawFlower(Canvas c, float x, float y, float s, int variant) {
        sprite(c, "flower" + (variant % 3), x, y, s, 0, 1);
    }

    public static void drawBush(Canvas c, float x, float y, float s) {
        sprite(c, "bush", x, y, s, 0, 1);
    }

    /** Retrato para los diálogos: 0 tú, 1 Mocha, 2 Aldric, 3 Orin, 4 Elara. */
    public static void drawPortrait(Canvas c, int who, float x, float y, float s, float t) {
        float bob = (float) Math.sin(t * 2.2) * s * 0.03f;
        switch (who) {
            case 0: sprite(c, "aprendiz", x, y, s, bob, 1); break;
            case 1: drawMon(c, 3, x, y + s * 0.35f, s * 1.35f, t, false, 0); break;
            case 2: drawHero(c, 5, x, y + s * 0.35f, s * 1.05f, t, 1, true); break;
            case 3: sprite(c, "orin", x, y, s, bob, 1); break;
            case 4: drawHero(c, 2, x, y + s * 0.35f, s * 1.05f, t, 1, false); break;
            default: drawHero(c, 1, x, y + s * 0.35f, s * 1.05f, t, 1, false); break;
        }
    }

    // ================== EFECTOS ==================

    public static void drawPortal(Canvas c, float x, float y, float s, float t) {
        float pulse = 1 + (float) Math.sin(t * 3) * 0.06f;
        Paint glow = Ui.paint(0x557B1FA2);
        c.drawCircle(x, y, s * 1.5f * pulse, glow);
        c.drawCircle(x, y, s * 1.05f * pulse, Ui.paint(0xFF311B92));
        Paint grad = Ui.paint(0xFF7B1FA2);
        grad.setShader(new RadialGradient(x, y, s * 0.95f * pulse,
                0xFFB39DDB, 0xFF4A148C, Shader.TileMode.CLAMP));
        c.drawCircle(x, y, s * 0.92f * pulse, grad);
        Paint p = Ui.paint(0xFFCE93D8);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * 0.06f);
        Path sp = new Path();
        for (int i = 0; i < 60; i++) {
            double ang = i * 0.22 + t * 1.5;
            double rad = s * 0.85 * pulse * i / 60.0;
            float px = x + (float) (Math.cos(ang) * rad);
            float py = y + (float) (Math.sin(ang) * rad);
            if (i == 0) sp.moveTo(px, py); else sp.lineTo(px, py);
        }
        c.drawPath(sp, p);
        c.drawCircle(x, y, s * 1.02f * pulse, strokeWrap(s * 0.06f, Ui.OUTLINE));
        for (int i = 0; i < 5; i++) {
            double ang = t * 1.2 + i * 1.256;
            float px = x + (float) (Math.cos(ang) * s * 1.3f);
            float py = y + (float) (Math.sin(ang) * s * 1.3f);
            c.drawCircle(px, py, s * 0.07f, Ui.paint(0xFFFFC93C));
        }
    }

    private static Paint strokeWrap(float w, int col) {
        Paint p = Ui.paint(col);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w);
        return p;
    }

    /** Ráfaga radial con degradado para las revelaciones. */
    public static void drawBurst(Canvas c, float cx, float cy, float R, float t, int col1, int col2) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        int n = 16;
        for (int i = 0; i < n; i++) {
            float a0 = (float) (i * 2 * Math.PI / n + t * 0.35);
            float span = (float) (Math.PI / n * 0.9);
            Path w = new Path();
            w.moveTo(cx, cy);
            w.arcTo(new RectF(cx - R, cy - R, cx + R, cy + R),
                    (float) Math.toDegrees(a0), (float) Math.toDegrees(span));
            w.close();
            p.setColor(i % 2 == 0 ? col1 : col2);
            c.drawPath(w, p);
        }
        Paint halo = Ui.paint(0xFFFFFFFF);
        halo.setShader(new RadialGradient(cx, cy, R * 0.75f,
                0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, R * 0.75f, halo);
    }

    public static Path starPath(float cx, float cy, float R, float r) {
        Path p = new Path();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float rad = (i % 2 == 0) ? R : r;
            float px = cx + (float) Math.cos(a) * rad;
            float py = cy + (float) Math.sin(a) * rad;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.close();
        return p;
    }
}
