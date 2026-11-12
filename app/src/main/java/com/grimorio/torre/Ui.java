package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/** Utilidades de interfaz: botones con degradado y brillo, paneles y texto con contorno. */
public final class Ui {

    public static final int OUTLINE = 0xFF33323A;
    public static final int GOLD = 0xFFFFC93C;
    public static final int GOLD_DARK = 0xFFB8860B;
    public static final int GEM = 0xFF4DD0E1;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int DARKTXT = 0xFF3A2E20;

    public static int lighten(int col, float f) {
        int r = Math.min(255, (int) (((col >> 16) & 0xFF) * (1 + f)));
        int g = Math.min(255, (int) (((col >> 8) & 0xFF) * (1 + f)));
        int b = Math.min(255, (int) ((col & 0xFF) * (1 + f)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int darken(int col, float f) {
        int r = Math.max(0, (int) (((col >> 16) & 0xFF) * (1 - f)));
        int g = Math.max(0, (int) (((col >> 8) & 0xFF) * (1 - f)));
        int b = Math.max(0, (int) ((col & 0xFF) * (1 - f)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static class Btn {
        public float x, y, w, h;
        public String label;
        public int fill, edge;
        public boolean enabled = true;
        public boolean press;
        public Runnable on;
        public String sub;

        public Btn(float x, float y, float w, float h, String label, int fill, int edge, Runnable on) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.label = label; this.fill = fill; this.edge = edge; this.on = on;
        }

        public boolean hit(float tx, float ty) {
            return tx >= x && tx <= x + w && ty >= y && ty <= y + h;
        }

        public void draw(Canvas c, float t) {
            int base = enabled ? fill : 0xFF9E9E9E;
            int e = enabled ? edge : 0xFF616161;
            float dy = press ? 5 : 0;
            float off = press ? 4 : 10;
            float r = h * 0.34f;

            Paint p = paint(0x5A000000);
            c.drawRoundRect(new RectF(x + 4, y + off, x + w, y + h + off), r, r, p);

            p = paint(e);
            c.drawRoundRect(new RectF(x, y + dy, x + w, y + h + dy), r, r, p);

            p = paint(0);
            p.setShader(new LinearGradient(x, y + dy, x, y + h + dy,
                    lighten(base, 0.28f), darken(base, 0.12f), Shader.TileMode.CLAMP));
            c.drawRoundRect(new RectF(x + 7, y + 7 + dy, x + w - 7, y + h - 7 + dy), r * 0.8f, r * 0.8f, p);

            p = paint(0x50FFFFFF);
            c.drawRoundRect(new RectF(x + 16, y + 12 + dy, x + w - 16, y + h * 0.42f + dy), r * 0.7f, r * 0.7f, p);

            p = paint(OUTLINE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6);
            c.drawRoundRect(new RectF(x, y + dy, x + w, y + h + dy), r, r, p);

            float ts = h * 0.4f;
            Paint tp = text(ts, true, enabled ? WHITE : 0xFFEEEEEE);
            while (tp.measureText(label) > w - 34 && ts > 14) {
                ts -= 2;
                tp = text(ts, true, enabled ? WHITE : 0xFFEEEEEE);
            }
            float cy = y + h / 2f + dy + (sub == null ? 0 : -16);
            textO(c, label, x + w / 2f, cy + ts * 0.35f, tp);
            if (sub != null) {
                Paint sp = text(28, false, 0xF2FFFFFF);
                textO(c, sub, x + w / 2f, y + h * 0.8f + dy, sp);
            }
        }
    }

    private static final RectF tmp = new RectF();

    public static Paint paint(int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);
        return p;
    }

    public static Paint text(float size, boolean bold, int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(Typeface.create(Typeface.SANS_SERIF, bold ? Typeface.BOLD : Typeface.NORMAL));
        return p;
    }

    /** Texto con contorno oscuro (legible sobre cualquier fondo). */
    public static void textO(Canvas c, String s, float cx, float cy, Paint p) {
        Paint st = new Paint(p);
        st.setStyle(Paint.Style.STROKE);
        st.setStrokeWidth(Math.max(4, p.getTextSize() * 0.14f));
        st.setColor(0xB3000000);
        st.setStrokeJoin(Paint.Join.ROUND);
        c.drawText(s, cx, cy, st);
        c.drawText(s, cx, cy, p);
    }

    public static void textC(Canvas c, String s, float cx, float cy, float size, boolean bold, int color) {
        Paint p = text(size, bold, color);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(s, cx, cy, p);
    }

    public static void textCO(Canvas c, String s, float cx, float cy, float size, boolean bold, int color) {
        Paint p = text(size, bold, color);
        p.setTextAlign(Paint.Align.CENTER);
        textO(c, s, cx, cy, p);
    }

    public static void textR(Canvas c, String s, float rx, float cy, float size, boolean bold, int color) {
        Paint p = text(size, bold, color);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(s, rx, cy, p);
    }

    /** Panel redondeado con degradado sutil y borde oscuro. */
    public static void panel(Canvas c, float x, float y, float w, float h, int fill, int edge) {
        Paint p = paint(0x66000000);
        c.drawRoundRect(new RectF(x + 6, y + 10, x + w + 6, y + h + 10), 34, 34, p);
        p = paint(edge);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), 34, 34, p);
        p = paint(0);
        p.setShader(new LinearGradient(x, y, x, y + h, lighten(fill, 0.12f), darken(fill, 0.06f), Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(x + 8, y + 8, x + w - 8, y + h - 8), 26, 26, p);
        p = paint(0x38FFFFFF);
        c.drawRoundRect(new RectF(x + 16, y + 12, x + w - 16, y + h * 0.3f), 20, 20, p);
    }

    public static void outlineRound(Canvas c, float x, float y, float w, float h, float r, int color, float sw) {
        Paint p = paint(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(sw);
        tmp.set(x, y, x + w, y + h);
        c.drawRoundRect(tmp, r, r, p);
    }

    public static String fmt(long v) {
        if (v >= 1000000000L) return String.format("%.2fB", v / 1000000000.0);
        if (v >= 1000000L) return String.format("%.2fM", v / 1000000.0);
        if (v >= 10000L) return String.format("%.1fK", v / 1000.0);
        return String.valueOf(v);
    }
}
