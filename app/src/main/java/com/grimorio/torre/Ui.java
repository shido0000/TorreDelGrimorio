package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Utilidades de interfaz: botones redondeados, paneles y texto estilo cartoon. */
public final class Ui {

    public static final int OUTLINE = 0xFF33323A;
    public static final int GOLD = 0xFFFFC93C;
    public static final int GOLD_DARK = 0xFFB8860B;
    public static final int GEM = 0xFF4DD0E1;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int DARKTXT = 0xFF3A2E20;

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
            int f = enabled ? fill : 0xFF8A8A8A;
            int e = enabled ? edge : 0xFF555555;
            float off = press ? 6 : 10;
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(e);
            c.drawRoundRect(new RectF(x, y + off, x + w, y + h + off), h * 0.32f, h * 0.32f, p);
            p.setColor(f);
            c.drawRoundRect(new RectF(x, y + (press ? 4 : 0), x + w, y + h + (press ? 4 : 0)), h * 0.32f, h * 0.32f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(7);
            p.setColor(OUTLINE);
            c.drawRoundRect(new RectF(x, y + (press ? 4 : 0), x + w, y + h + (press ? 4 : 0)), h * 0.32f, h * 0.32f, p);
            float ts = h * 0.42f;
            Paint tp = text(ts, true, WHITE);
            while (tp.measureText(label) > w - 30 && ts > 14) {
                ts -= 2;
                tp = text(ts, true, WHITE);
            }
            float cy = y + h / 2f + (press ? 4 : 0) + (sub == null ? 0 : -14);
            c.drawText(label, x + w / 2f, cy + ts * 0.35f, tp);
            if (sub != null) {
                Paint sp = text(30, false, 0xE6FFFFFF);
                c.drawText(sub, x + w / 2f, y + h * 0.78f + (press ? 4 : 0), sp);
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

    public static void textC(Canvas c, String s, float cx, float cy, float size, boolean bold, int color) {
        Paint p = text(size, bold, color);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(s, cx, cy, p);
    }

    public static void textR(Canvas c, String s, float rx, float cy, float size, boolean bold, int color) {
        Paint p = text(size, bold, color);
        p.setTextAlign(Paint.Align.RIGHT);
        c.drawText(s, rx, cy, p);
    }

    /** Panel tipo pergamino/cartel redondeado con borde oscuro. */
    public static void panel(Canvas c, float x, float y, float w, float h, int fill, int edge) {
        Paint p = paint(0x66000000);
        c.drawRoundRect(new RectF(x + 6, y + 10, x + w + 6, y + h + 10), 34, 34, p);
        p.setColor(edge);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), 34, 34, p);
        p.setColor(fill);
        c.drawRoundRect(new RectF(x + 8, y + 8, x + w - 8, y + h - 8), 26, 26, p);
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
