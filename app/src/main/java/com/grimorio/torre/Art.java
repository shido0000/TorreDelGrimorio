package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Toda la ilustración del juego, dibujada por código con estilo chibi/cartoon. */
public final class Art {

    private static final int OUT = Ui.OUTLINE;

    private static Paint fill(int c) { return Ui.paint(c); }

    private static Paint stroke(float w, int c) {
        Paint p = Ui.paint(c);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        return p;
    }

    private static void circle(Canvas c, float x, float y, float r, int col) {
        c.drawCircle(x, y, r, fill(col));
    }

    private static void circleO(Canvas c, float x, float y, float r, int col, float ow) {
        circle(c, x, y, r, col);
        c.drawCircle(x, y, r, stroke(ow, OUT));
    }

    private static void rrect(Canvas c, float l, float t, float r, float b, float rx, int col) {
        c.drawRoundRect(new RectF(l, t, r, b), rx, rx, fill(col));
    }

    private static void rrectO(Canvas c, float l, float t, float r, float b, float rx, int col, float ow) {
        rrect(c, l, t, r, b, rx, col);
        c.drawRoundRect(new RectF(l, t, r, b), rx, rx, stroke(ow, OUT));
    }

    private static void eyes(Canvas c, float x, float y, float r, float sep, int pupil) {
        circleO(c, x - sep, y, r, 0xFFFFFFFF, r * 0.28f);
        circleO(c, x + sep, y, r, 0xFFFFFFFF, r * 0.28f);
        float look = r * 0.35f;
        circle(c, x - sep + look * 0.4f, y + look * 0.2f, r * 0.5f, pupil);
        circle(c, x + sep + look * 0.4f, y + look * 0.2f, r * 0.5f, pupil);
    }

    private static void shadow(Canvas c, float x, float y, float s) {
        Paint p = Ui.paint(0x44000000);
        c.drawOval(new RectF(x - s * 0.4f, y - s * 0.08f, x + s * 0.4f, y + s * 0.12f), p);
    }

    // ================== MONSTRUOS ==================
    // art ids: 0 baba, 1 colmillo, 2 ponzona, 3 mocha, 4 chispa, 5 rocoso, 6 ojo, 7 draco, 8 fenix

    public static void drawMon(Canvas c, int art, float x, float y, float s, float t, boolean dead, float flash) {
        if (dead) { drawTomb(c, x, y, s); return; }
        float bob = (float) Math.sin(t * 3 + x * 0.05) * s * 0.04f;
        y += bob;
        shadow(c, x, y + s * 0.42f, s);
        float ow = Math.max(3, s * 0.055f);
        drawMonBody(c, art, x, y, s, t, ow);
        if (flash > 0) {
            Paint fl = Ui.paint((int) (flash * 150) << 24 | 0xFFFFFF);
            c.drawCircle(x, y - s * 0.05f, s * 0.45f, fl);
        }
    }

    private static void drawMonBody(Canvas c, int art, float x, float y, float s, float t, float ow) {
        switch (art) {
            case 0: { // Baba, limo
                float r = s * 0.38f;
                Path p = new Path();
                p.moveTo(x - r, y + s * 0.3f);
                p.quadTo(x - r, y - r * 0.6f, x, y - r * 0.6f);
                p.quadTo(x + r, y - r * 0.6f, x + r, y + s * 0.3f);
                p.close();
                Paint fp = fill(0xFF7ED957);
                c.drawPath(p, fp);
                c.drawPath(p, stroke(ow, OUT));
                circle(c, x - r * 0.4f, y - r * 0.1f, r * 0.16f, 0xCCFFFFFF);
                eyes(c, x, y - r * 0.05f, r * 0.2f, r * 0.42f, 0xFF33323A);
                c.drawArc(new RectF(x - r * 0.25f, y + r * 0.1f, x + r * 0.25f, y + r * 0.5f), 0, 180, false, stroke(ow * 0.7f, OUT));
                break;
            }
            case 1: { // Colmillo, lobo
                float r = s * 0.3f;
                circleO(c, x, y + s * 0.12f, r * 1.1f, 0xFF9E9E9E, ow);
                // orejas
                Path e1 = new Path(); e1.moveTo(x - r * 0.9f, y - r * 0.7f); e1.lineTo(x - r * 0.45f, y - r * 1.7f); e1.lineTo(x - r * 0.1f, y - r * 0.8f); e1.close();
                Path e2 = new Path(); e2.moveTo(x + r * 0.9f, y - r * 0.7f); e2.lineTo(x + r * 0.45f, y - r * 1.7f); e2.lineTo(x + r * 0.1f, y - r * 0.8f); e2.close();
                Paint fp = fill(0xFF757575); c.drawPath(e1, fp); c.drawPath(e2, fp);
                c.drawPath(e1, stroke(ow, OUT)); c.drawPath(e2, stroke(ow, OUT));
                // cabeza
                circleO(c, x, y - r * 0.35f, r, 0xFFBDBDBD, ow);
                circleO(c, x, y - r * 0.05f, r * 0.42f, 0xFFEEEEEE, ow * 0.6f);
                circle(c, x, y - r * 0.15f, r * 0.14f, 0xFF33323A);
                eyes(c, x, y - r * 0.55f, r * 0.16f, r * 0.45f, 0xFFD32F2F);
                // cola
                c.drawLine(x + r * 1.0f, y + s * 0.15f, x + r * 1.6f, y - r * 0.4f, stroke(ow * 1.4f, 0xFF757575));
                break;
            }
            case 2: { // Ponzoña, avispa
                float r = s * 0.3f;
                float wing = (float) Math.sin(t * 22) * s * 0.06f;
                Paint wf = fill(0xCCFFFFFF);
                c.drawOval(new RectF(x - r * 1.5f, y - r * 1.3f - wing, x - r * 0.2f, y - r * 0.2f - wing), wf);
                c.drawOval(new RectF(x + r * 0.2f, y - r * 1.3f - wing, x + r * 1.5f, y - r * 0.2f - wing), wf);
                c.drawOval(new RectF(x - r * 1.5f, y - r * 1.3f - wing, x - r * 0.2f, y - r * 0.2f - wing), stroke(ow * 0.6f, OUT));
                c.drawOval(new RectF(x + r * 0.2f, y - r * 1.3f - wing, x + r * 1.5f, y - r * 0.2f - wing), stroke(ow * 0.6f, OUT));
                // cuerpo con rayas
                rrectO(c, x - r * 0.75f, y - r * 0.4f, x + r * 0.75f, y + r * 0.9f, r * 0.7f, 0xFFFFD54F, ow);
                Paint bl = fill(0xFF33323A);
                RectF b1 = new RectF(x - r * 0.75f, y - r * 0.15f, x + r * 0.75f, y + r * 0.15f);
                c.save(); c.clipRect(x - r * 0.75f, y - r * 0.4f, x + r * 0.75f, y + r * 0.9f);
                c.drawRoundRect(b1, r * 0.2f, r * 0.2f, bl);
                RectF b2 = new RectF(x - r * 0.75f, y + r * 0.35f, x + r * 0.75f, y + r * 0.62f);
                c.drawRoundRect(b2, r * 0.2f, r * 0.2f, bl);
                c.restore();
                // aguijón
                Path st = new Path(); st.moveTo(x, y + r * 0.85f); st.lineTo(x - r * 0.15f, y + r * 1.25f); st.lineTo(x + r * 0.15f, y + r * 1.25f); st.close();
                c.drawPath(st, fill(0xFF8D6E63)); c.drawPath(st, stroke(ow * 0.6f, OUT));
                circleO(c, x, y - r * 0.55f, r * 0.55f, 0xFF33323A, ow);
                eyes(c, x, y - r * 0.6f, r * 0.13f, r * 0.24f, 0xFFFFEB3B);
                break;
            }
            case 3: { // Mocha, gata
                float r = s * 0.32f;
                circleO(c, x, y + s * 0.16f, r * 0.95f, 0xFF9E9EA7, ow);
                Path tail = new Path();
                tail.moveTo(x + r * 0.8f, y + s * 0.2f);
                tail.quadTo(x + r * 1.8f, y + s * 0.1f, x + r * 1.5f, y - r * 0.6f);
                c.drawPath(tail, stroke(ow * 1.6f, 0xFF9E9EA7));
                // orejas
                Path e1 = new Path(); e1.moveTo(x - r * 0.85f, y - r * 0.5f); e1.lineTo(x - r * 0.7f, y - r * 1.5f); e1.lineTo(x - r * 0.15f, y - r * 0.85f); e1.close();
                Path e2 = new Path(); e2.moveTo(x + r * 0.85f, y - r * 0.5f); e2.lineTo(x + r * 0.7f, y - r * 1.5f); e2.lineTo(x + r * 0.15f, y - r * 0.85f); e2.close();
                c.drawPath(e1, fill(0xFF9E9EA7)); c.drawPath(e2, fill(0xFF9E9EA7));
                c.drawPath(e1, stroke(ow, OUT)); c.drawPath(e2, stroke(ow, OUT));
                circleO(c, x, y - r * 0.35f, r, 0xFFB9BAC2, ow);
                // hocico blanco
                circle(c, x, y - r * 0.12f, r * 0.42f, 0xFFF5F5F5);
                eyes(c, x, y - r * 0.45f, r * 0.15f, r * 0.45f, 0xFF43A047);
                Path nose = new Path(); nose.moveTo(x - r * 0.1f, y - r * 0.2f); nose.lineTo(x + r * 0.1f, y - r * 0.2f); nose.lineTo(x, y - r * 0.05f); nose.close();
                c.drawPath(nose, fill(0xFFE57373));
                // bufanda roja
                rrectO(c, x - r * 0.75f, y + r * 0.25f, x + r * 0.75f, y + r * 0.55f, r * 0.2f, 0xFFE53935, ow * 0.7f);
                // bigotes
                c.drawLine(x - r * 0.5f, y - r * 0.12f, x - r * 1.1f, y - r * 0.2f, stroke(ow * 0.5f, OUT));
                c.drawLine(x - r * 0.5f, y - r * 0.02f, x - r * 1.1f, y + r * 0.02f, stroke(ow * 0.5f, OUT));
                c.drawLine(x + r * 0.5f, y - r * 0.12f, x + r * 1.1f, y - r * 0.2f, stroke(ow * 0.5f, OUT));
                c.drawLine(x + r * 0.5f, y - r * 0.02f, x + r * 1.1f, y + r * 0.02f, stroke(ow * 0.5f, OUT));
                break;
            }
            case 4: { // Chispa, murciélago
                float r = s * 0.3f;
                float flap = (float) Math.sin(t * 14) * s * 0.08f;
                Path w1 = new Path();
                w1.moveTo(x - r * 0.6f, y);
                w1.quadTo(x - r * 1.9f, y - r * 1.1f - flap, x - r * 2.1f, y + r * 0.4f);
                w1.quadTo(x - r * 1.4f, y + r * 0.15f, x - r * 0.6f, y + r * 0.5f);
                w1.close();
                Path w2 = new Path();
                w2.moveTo(x + r * 0.6f, y);
                w2.quadTo(x + r * 1.9f, y - r * 1.1f - flap, x + r * 2.1f, y + r * 0.4f);
                w2.quadTo(x + r * 1.4f, y + r * 0.15f, x + r * 0.6f, y + r * 0.5f);
                w2.close();
                c.drawPath(w1, fill(0xFF5E35B1)); c.drawPath(w2, fill(0xFF5E35B1));
                c.drawPath(w1, stroke(ow * 0.8f, OUT)); c.drawPath(w2, stroke(ow * 0.8f, OUT));
                circleO(c, x, y, r, 0xFF7E57C2, ow);
                // orejas puntiagudas
                Path e1 = new Path(); e1.moveTo(x - r * 0.7f, y - r * 0.55f); e1.lineTo(x - r * 0.85f, y - r * 1.5f); e1.lineTo(x - r * 0.15f, y - r * 0.85f); e1.close();
                Path e2 = new Path(); e2.moveTo(x + r * 0.7f, y - r * 0.55f); e2.lineTo(x + r * 0.85f, y - r * 1.5f); e2.lineTo(x + r * 0.15f, y - r * 0.85f); e2.close();
                c.drawPath(e1, fill(0xFF7E57C2)); c.drawPath(e2, fill(0xFF7E57C2));
                c.drawPath(e1, stroke(ow * 0.8f, OUT)); c.drawPath(e2, stroke(ow * 0.8f, OUT));
                eyes(c, x, y - r * 0.1f, r * 0.18f, r * 0.42f, 0xFFFFEB3B);
                // colmillos
                Path f1 = new Path(); f1.moveTo(x - r * 0.22f, y + r * 0.3f); f1.lineTo(x - r * 0.1f, y + r * 0.6f); f1.lineTo(x - r * 0.02f, y + r * 0.3f); f1.close();
                Path f2 = new Path(); f2.moveTo(x + r * 0.22f, y + r * 0.3f); f2.lineTo(x + r * 0.1f, y + r * 0.6f); f2.lineTo(x + r * 0.02f, y + r * 0.3f); f2.close();
                c.drawPath(f1, fill(0xFFFFFFFF)); c.drawPath(f2, fill(0xFFFFFFFF));
                break;
            }
            case 5: { // Rocoso, gólem
                float r = s * 0.34f;
                rrectO(c, x - r, y - r * 0.9f, x + r, y + s * 0.38f, r * 0.5f, 0xFF8D8D8D, ow);
                rrectO(c, x - r * 1.35f, y - r * 0.2f, x - r * 0.75f, y + s * 0.25f, r * 0.3f, 0xFF7A7A7A, ow * 0.8f);
                rrectO(c, x + r * 0.75f, y - r * 0.2f, x + r * 1.35f, y + s * 0.25f, r * 0.3f, 0xFF7A7A7A, ow * 0.8f);
                // musgo
                circle(c, x - r * 0.5f, y - r * 0.75f, r * 0.28f, 0xFF7ED957);
                circle(c, x + r * 0.4f, y - r * 0.85f, r * 0.2f, 0xFF7ED957);
                // ojos brillantes
                circle(c, x - r * 0.35f, y - r * 0.3f, r * 0.14f, 0xFFFFC93C);
                circle(c, x + r * 0.35f, y - r * 0.3f, r * 0.14f, 0xFFFFC93C);
                c.drawLine(x - r * 0.45f, y + r * 0.25f, x + r * 0.45f, y + r * 0.25f, stroke(ow * 0.7f, OUT));
                // grietas
                c.drawLine(x - r * 0.1f, y - r * 0.6f, x + r * 0.15f, y - r * 0.35f, stroke(ow * 0.5f, 0xFF616161));
                break;
            }
            case 6: { // Ojo Místico
                float r = s * 0.34f;
                float fl = (float) Math.sin(t * 2.2) * s * 0.05f;
                // tentáculos flotantes
                for (int i = -1; i <= 1; i++) {
                    Path tp = new Path();
                    tp.moveTo(x + i * r * 0.55f, y + r * 0.7f);
                    tp.quadTo(x + i * r * 0.75f + fl, y + r * 1.25f, x + i * r * 0.45f - fl, y + r * 1.5f);
                    c.drawPath(tp, stroke(ow * 1.3f, 0xFF7B1FA2));
                }
                circleO(c, x, y + fl * 0.5f, r, 0xFFEDE7F6, ow);
                circle(c, x, y + fl * 0.5f, r * 0.55f, 0xFF7B1FA2);
                circle(c, x, y + fl * 0.5f, r * 0.3f, 0xFF33323A);
                circle(c, x - r * 0.18f, y - r * 0.2f + fl * 0.5f, r * 0.1f, 0xFFFFFFFF);
                break;
            }
            case 7: { // Draco, dragón
                float r = s * 0.3f;
                float wing = (float) Math.sin(t * 8) * s * 0.07f;
                // alas
                Path w1 = new Path();
                w1.moveTo(x - r * 0.5f, y);
                w1.quadTo(x - r * 2.0f, y - r * 1.4f - wing, x - r * 1.9f, y + r * 0.5f);
                w1.quadTo(x - r * 1.1f, y + r * 0.2f, x - r * 0.5f, y + r * 0.4f);
                w1.close();
                Path w2 = new Path();
                w2.moveTo(x + r * 0.5f, y);
                w2.quadTo(x + r * 2.0f, y - r * 1.4f - wing, x + r * 1.9f, y + r * 0.5f);
                w2.quadTo(x + r * 1.1f, y + r * 0.2f, x + r * 0.5f, y + r * 0.4f);
                w2.close();
                c.drawPath(w1, fill(0xFF43A047)); c.drawPath(w2, fill(0xFF43A047));
                c.drawPath(w1, stroke(ow * 0.8f, OUT)); c.drawPath(w2, stroke(ow * 0.8f, OUT));
                // cuerpo
                circleO(c, x, y + s * 0.14f, r * 0.9f, 0xFF66BB6A, ow);
                rrectO(c, x - r * 0.4f, y + r * 0.1f, x + r * 0.4f, y + s * 0.35f, r * 0.3f, 0xFFFFF59D, ow * 0.6f);
                // cabeza
                circleO(c, x, y - r * 0.4f, r, 0xFF66BB6A, ow);
                // cuernos
                Path h1 = new Path(); h1.moveTo(x - r * 0.5f, y - r * 0.95f); h1.lineTo(x - r * 0.75f, y - r * 1.6f); h1.lineTo(x - r * 0.25f, y - r * 1.05f); h1.close();
                Path h2 = new Path(); h2.moveTo(x + r * 0.5f, y - r * 0.95f); h2.lineTo(x + r * 0.75f, y - r * 1.6f); h2.lineTo(x + r * 0.25f, y - r * 1.05f); h2.close();
                c.drawPath(h1, fill(0xFFFFC93C)); c.drawPath(h2, fill(0xFFFFC93C));
                c.drawPath(h1, stroke(ow * 0.6f, OUT)); c.drawPath(h2, stroke(ow * 0.6f, OUT));
                eyes(c, x, y - r * 0.45f, r * 0.15f, r * 0.45f, 0xFF33323A);
                // fire breath chibi
                circle(c, x, y - r * 0.05f, r * 0.1f, 0xFF33323A);
                break;
            }
            default: { // Fénix
                float r = s * 0.3f;
                float wing = (float) Math.sin(t * 10) * s * 0.09f;
                Path w1 = new Path();
                w1.moveTo(x - r * 0.4f, y);
                w1.quadTo(x - r * 2.1f, y - r * 1.2f - wing, x - r * 2.3f, y + r * 0.6f);
                w1.quadTo(x - r * 1.2f, y + r * 0.2f, x - r * 0.4f, y + r * 0.5f);
                w1.close();
                Path w2 = new Path();
                w2.moveTo(x + r * 0.4f, y);
                w2.quadTo(x + r * 2.1f, y - r * 1.2f - wing, x + r * 2.3f, y + r * 0.6f);
                w2.quadTo(x + r * 1.2f, y + r * 0.2f, x + r * 0.4f, y + r * 0.5f);
                w2.close();
                c.drawPath(w1, fill(0xFFFF7043)); c.drawPath(w2, fill(0xFFFF7043));
                c.drawPath(w1, stroke(ow * 0.8f, OUT)); c.drawPath(w2, stroke(ow * 0.8f, OUT));
                circleO(c, x, y + s * 0.1f, r * 0.95f, 0xFFFF5722, ow);
                circleO(c, x, y - r * 0.45f, r * 0.8f, 0xFFFF8A65, ow);
                // cresta de fuego
                Path f1 = new Path(); f1.moveTo(x, y - r * 1.15f); f1.quadTo(x - r * 0.35f, y - r * 1.7f, x, y - r * 2.0f); f1.quadTo(x + r * 0.35f, y - r * 1.7f, x, y - r * 1.15f); f1.close();
                c.drawPath(f1, fill(0xFFFFC93C)); c.drawPath(f1, stroke(ow * 0.6f, 0xFFE65100));
                eyes(c, x, y - r * 0.55f, r * 0.14f, r * 0.4f, 0xFF33323A);
                // cola de fuego
                Path tl = new Path();
                tl.moveTo(x - r * 0.3f, y + r * 0.8f);
                tl.quadTo(x, y + r * 2.0f, x + r * 0.3f, y + r * 0.8f);
                tl.close();
                c.drawPath(tl, fill(0xFFFFC93C));
                break;
            }
        }
    }

    private static void drawTomb(Canvas c, float x, float y, float s) {
        float w = s * 0.5f, h = s * 0.55f;
        rrectO(c, x - w / 2, y + s * 0.42f - h, x + w / 2, y + s * 0.42f, w * 0.4f, 0xFFB0BEC5, 4);
        c.drawLine(x, y + s * 0.42f - h * 0.75f, x, y + s * 0.42f - h * 0.35f, stroke(4, OUT));
        c.drawLine(x - w * 0.2f, y + s * 0.42f - h * 0.6f, x + w * 0.2f, y + s * 0.42f - h * 0.6f, stroke(4, OUT));
    }

    // ================== HÉROES ==================
    // art ids: 0 escudero, 1 caballero, 2 arquera, 3 mago, 4 guardián, 5 rey

    public static void drawHero(Canvas c, int art, float x, float y, float s, float t, float hpFrac, boolean boss) {
        float bob = (float) Math.abs(Math.sin(t * 6 + x * 0.07)) * s * 0.05f;
        shadow(c, x, y + s * 0.42f, s);
        float ow = Math.max(3, s * 0.05f);
        y -= bob;
        switch (art) {
            case 0: { // Escudero
                rrectO(c, x - s * 0.22f, y - s * 0.05f, x + s * 0.22f, y + s * 0.38f, s * 0.12f, 0xFF8D6E63, ow);
                circleO(c, x, y - s * 0.22f, s * 0.24f, 0xFFFFCC80, ow);
                rrectO(c, x - s * 0.27f, y - s * 0.42f, x + s * 0.27f, y - s * 0.18f, s * 0.1f, 0xFF9E9E9E, ow);
                circle(c, x - s * 0.12f, y - s * 0.2f, s * 0.035f, 0xFF33323A);
                circle(c, x + s * 0.12f, y - s * 0.2f, s * 0.035f, 0xFF33323A);
                // escudo madera
                Path sh = new Path();
                sh.moveTo(x + s * 0.22f, y - s * 0.1f);
                sh.lineTo(x + s * 0.42f, y - s * 0.05f);
                sh.lineTo(x + s * 0.42f, y + s * 0.2f);
                sh.lineTo(x + s * 0.3f, y + s * 0.34f);
                sh.lineTo(x + s * 0.22f, y + s * 0.25f);
                sh.close();
                c.drawPath(sh, fill(0xFFA1887F)); c.drawPath(sh, stroke(ow * 0.7f, OUT));
                break;
            }
            case 1: { // Caballero
                rrectO(c, x - s * 0.24f, y - s * 0.05f, x + s * 0.24f, y + s * 0.38f, s * 0.12f, 0xFFB0BEC5, ow);
                circleO(c, x, y - s * 0.22f, s * 0.24f, 0xFFCFD8DC, ow);
                rrectO(c, x - s * 0.28f, y - s * 0.44f, x + s * 0.28f, y - s * 0.2f, s * 0.1f, 0xFF90A4AE, ow);
                // pluma
                Path pl = new Path(); pl.moveTo(x, y - s * 0.44f); pl.quadTo(x + s * 0.18f, y - s * 0.7f, x + s * 0.3f, y - s * 0.6f); pl.quadTo(x + s * 0.15f, y - s * 0.52f, x, y - s * 0.4f); pl.close();
                c.drawPath(pl, fill(0xFFE53935)); c.drawPath(pl, stroke(ow * 0.5f, OUT));
                // visera
                c.drawLine(x - s * 0.16f, y - s * 0.24f, x + s * 0.16f, y - s * 0.24f, stroke(ow * 0.7f, OUT));
                // espada
                c.drawLine(x + s * 0.24f, y + s * 0.1f, x + s * 0.5f, y - s * 0.35f, stroke(ow * 1.1f, 0xFFECEFF1));
                // escudo azul
                Path sh = new Path();
                sh.moveTo(x - s * 0.24f, y - s * 0.12f);
                sh.lineTo(x - s * 0.45f, y - s * 0.06f);
                sh.lineTo(x - s * 0.45f, y + s * 0.2f);
                sh.lineTo(x - s * 0.32f, y + s * 0.34f);
                sh.lineTo(x - s * 0.24f, y + s * 0.25f);
                sh.close();
                c.drawPath(sh, fill(0xFF1E88E5)); c.drawPath(sh, stroke(ow * 0.7f, OUT));
                circle(c, x - s * 0.34f, y + s * 0.1f, s * 0.06f, 0xFFFFC93C);
                break;
            }
            case 2: { // Arquera
                rrectO(c, x - s * 0.18f, y - s * 0.02f, x + s * 0.18f, y + s * 0.38f, s * 0.1f, 0xFF43A047, ow);
                circleO(c, x, y - s * 0.2f, s * 0.22f, 0xFFFFCC80, ow);
                // capucha
                Path hood = new Path();
                hood.moveTo(x - s * 0.26f, y - s * 0.1f);
                hood.quadTo(x - s * 0.3f, y - s * 0.55f, x, y - s * 0.52f);
                hood.quadTo(x + s * 0.3f, y - s * 0.55f, x + s * 0.26f, y - s * 0.1f);
                hood.quadTo(x, y - s * 0.3f, x - s * 0.26f, y - s * 0.1f);
                hood.close();
                c.drawPath(hood, fill(0xFF2E7D32)); c.drawPath(hood, stroke(ow * 0.7f, OUT));
                eyes(c, x, y - s * 0.22f, s * 0.04f, s * 0.1f, 0xFF33323A);
                // arco
                Path bow = new Path();
                bow.moveTo(x + s * 0.3f, y - s * 0.35f);
                bow.quadTo(x + s * 0.5f, y, x + s * 0.3f, y + s * 0.35f);
                c.drawPath(bow, stroke(ow * 0.8f, 0xFF795548));
                c.drawLine(x + s * 0.3f, y - s * 0.35f, x + s * 0.3f, y + s * 0.35f, stroke(2, 0xFFEEEEEE));
                break;
            }
            case 3: { // Mago Renegado
                rrectO(c, x - s * 0.2f, y - s * 0.02f, x + s * 0.2f, y + s * 0.38f, s * 0.1f, 0xFF1E88E5, ow);
                circleO(c, x, y - s * 0.2f, s * 0.2f, 0xFFFFCC80, ow);
                // sombrero puntiagudo
                Path hat = new Path();
                hat.moveTo(x - s * 0.32f, y - s * 0.3f);
                hat.lineTo(x + s * 0.32f, y - s * 0.3f);
                hat.lineTo(x + s * 0.05f, y - s * 0.75f);
                hat.close();
                c.drawPath(hat, fill(0xFF1565C0)); c.drawPath(hat, stroke(ow * 0.7f, OUT));
                circle(c, x + s * 0.05f, y - s * 0.78f, s * 0.05f, 0xFFAB47BC);
                eyes(c, x, y - s * 0.2f, s * 0.04f, s * 0.1f, 0xFF33323A);
                // bastón con orbe
                c.drawLine(x - s * 0.3f, y + s * 0.35f, x - s * 0.3f, y - s * 0.4f, stroke(ow * 0.8f, 0xFF795548));
                float glow = 1 + (float) Math.sin(t * 5) * 0.15f;
                circleO(c, x - s * 0.3f, y - s * 0.48f, s * 0.09f * glow, 0xFFAB47BC, ow * 0.5f);
                break;
            }
            case 4: { // Guardián de Hierro
                rrectO(c, x - s * 0.32f, y - s * 0.08f, x + s * 0.32f, y + s * 0.4f, s * 0.14f, 0xFF78909C, ow);
                circleO(c, x, y - s * 0.26f, s * 0.26f, 0xFF90A4AE, ow);
                rrectO(c, x - s * 0.3f, y - s * 0.5f, x + s * 0.3f, y - s * 0.24f, s * 0.1f, 0xFF607D8B, ow);
                eyes(c, x, y - s * 0.28f, s * 0.04f, s * 0.12f, 0xFFE53935);
                // escudo torre
                rrectO(c, x - s * 0.55f, y - s * 0.25f, x - s * 0.3f, y + s * 0.35f, s * 0.08f, 0xFF546E7A, ow);
                c.drawLine(x - s * 0.425f, y - s * 0.15f, x - s * 0.425f, y + s * 0.25f, stroke(ow * 0.5f, OUT));
                // maza
                c.drawLine(x + s * 0.32f, y + s * 0.2f, x + s * 0.52f, y - s * 0.2f, stroke(ow, 0xFF795548));
                circleO(c, x + s * 0.55f, y - s * 0.26f, s * 0.1f, 0xFF78909C, ow * 0.6f);
                break;
            }
            default: { // Rey Héroe Aldric (jefe)
                float cape = (float) Math.sin(t * 4) * s * 0.03f;
                // capa
                Path cp = new Path();
                cp.moveTo(x - s * 0.3f, y - s * 0.25f);
                cp.quadTo(x - s * 0.55f - cape, y + s * 0.1f, x - s * 0.42f, y + s * 0.42f);
                cp.lineTo(x + s * 0.42f, y + s * 0.42f);
                cp.quadTo(x + s * 0.55f + cape, y + s * 0.1f, x + s * 0.3f, y - s * 0.25f);
                cp.close();
                c.drawPath(cp, fill(0xFFC62828)); c.drawPath(cp, stroke(ow * 0.7f, OUT));
                rrectO(c, x - s * 0.26f, y - s * 0.05f, x + s * 0.26f, y + s * 0.38f, s * 0.12f, 0xFFFDD835, ow);
                circleO(c, x, y - s * 0.24f, s * 0.24f, 0xFFE8B98A, ow);
                // barba
                Path bd = new Path();
                bd.moveTo(x - s * 0.14f, y - s * 0.18f);
                bd.quadTo(x, y + s * 0.05f, x + s * 0.14f, y - s * 0.18f);
                bd.quadTo(x, y - s * 0.05f, x - s * 0.14f, y - s * 0.18f);
                bd.close();
                c.drawPath(bd, fill(0xFFEEEEEE));
                eyes(c, x, y - s * 0.28f, s * 0.035f, s * 0.1f, 0xFF33323A);
                // corona
                Path cr = new Path();
                cr.moveTo(x - s * 0.24f, y - s * 0.42f);
                cr.lineTo(x - s * 0.24f, y - s * 0.58f);
                cr.lineTo(x - s * 0.12f, y - s * 0.48f);
                cr.lineTo(x, y - s * 0.62f);
                cr.lineTo(x + s * 0.12f, y - s * 0.48f);
                cr.lineTo(x + s * 0.24f, y - s * 0.58f);
                cr.lineTo(x + s * 0.24f, y - s * 0.42f);
                cr.close();
                c.drawPath(cr, fill(0xFFFFC93C)); c.drawPath(cr, stroke(ow * 0.6f, OUT));
                // espada grande
                c.drawLine(x + s * 0.26f, y + s * 0.15f, x + s * 0.58f, y - s * 0.45f, stroke(ow * 1.3f, 0xFFECEFF1));
                c.drawLine(x + s * 0.2f, y + s * 0.05f, x + s * 0.36f, y + s * 0.0f, stroke(ow * 0.9f, 0xFF795548));
                break;
            }
        }
        // barra de vida
        if (hpFrac < 1) {
            float bw = s * 0.7f, bh = s * 0.07f;
            float bx = x - bw / 2, by = y - s * (boss ? 0.85f : 0.62f);
            rrect(c, bx - 2, by - 2, bx + bw + 2, by + bh + 2, bh, 0xFF33323A);
            rrect(c, bx, by, bx + bw, by + bh, bh * 0.5f, 0xFF616161);
            if (hpFrac > 0) rrect(c, bx, by, bx + bw * hpFrac, by + bh, bh * 0.5f, boss ? 0xFFE53935 : 0xFF66BB6A);
        }
    }

    // ================== OBJETOS Y ESCENARIO ==================

    public static void drawCoin(Canvas c, float x, float y, float r) {
        circleO(c, x, y, r, 0xFFFFC93C, r * 0.18f);
        circle(c, x, y, r * 0.55f, 0xFFFFE082);
        Paint p = Ui.text(r * 0.9f, true, 0xFFB8860B);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText("$", x, y + r * 0.32f, p);
    }

    public static void drawGem(Canvas c, float x, float y, float r) {
        Path p = new Path();
        p.moveTo(x, y - r);
        p.lineTo(x + r * 0.85f, y - r * 0.2f);
        p.lineTo(x, y + r);
        p.lineTo(x - r * 0.85f, y - r * 0.2f);
        p.close();
        c.drawPath(p, fill(0xFF4DD0E1));
        c.drawPath(p, stroke(r * 0.16f, OUT));
        Path gl = new Path();
        gl.moveTo(x - r * 0.3f, y - r * 0.25f);
        gl.lineTo(x - r * 0.05f, y - r * 0.05f);
        gl.lineTo(x - r * 0.35f, y + r * 0.1f);
        gl.close();
        c.drawPath(gl, fill(0xB3FFFFFF));
    }

    public static void drawChest(Canvas c, float x, float y, float s, float t, int hits, float maxHp) {
        float glow = 0.5f + 0.5f * (float) Math.sin(t * 2);
        Paint g = Ui.paint(hits >= 3 ? 0x33E53935 : 0x33FFC93C);
        c.drawCircle(x, y, s * (1.1f + glow * 0.15f), g);
        rrectO(c, x - s * 0.55f, y - s * 0.1f, x + s * 0.55f, y + s * 0.45f, s * 0.12f, 0xFF8D6E63, s * 0.05f);
        rrectO(c, x - s * 0.55f, y - s * 0.42f, x + s * 0.55f, y - s * 0.05f, s * 0.25f, 0xFFA1887F, s * 0.05f);
        rrectO(c, x - s * 0.1f, y - s * 0.18f, x + s * 0.1f, y + s * 0.2f, s * 0.05f, 0xFFFFC93C, s * 0.04f);
        drawGem(c, x, y - s * 0.55f, s * 0.16f);
        // integridad
        float bw = s * 1.1f;
        float bx = x - bw / 2, by = y + s * 0.6f;
        rrect(c, bx - 2, by - 2, bx + bw + 2, by + 8, 5, 0xFF33323A);
        int left = Math.max(0, 3 - hits);
        float frac = left / 3f;
        if (frac > 0) rrect(c, bx, by, bx + bw * frac, by + 6, 3, 0xFF66BB6A);
    }

    public static void drawPortal(Canvas c, float x, float y, float s, float t) {
        float pulse = 1 + (float) Math.sin(t * 3) * 0.06f;
        circle(c, x, y, s * 1.05f * pulse, 0xFF311B92);
        circle(c, x, y, s * 0.92f * pulse, 0xFF7B1FA2);
        // espiral
        Paint p = stroke(s * 0.06f, 0xFFCE93D8);
        Path sp = new Path();
        for (int i = 0; i < 60; i++) {
            double ang = i * 0.22 + t * 1.5;
            double rad = s * 0.85 * pulse * i / 60.0;
            float px = x + (float) (Math.cos(ang) * rad);
            float py = y + (float) (Math.sin(ang) * rad);
            if (i == 0) sp.moveTo(px, py); else sp.lineTo(px, py);
        }
        c.drawPath(sp, p);
        c.drawCircle(x, y, s * 1.02f * pulse, stroke(s * 0.06f, OUT));
        // estrellas
        for (int i = 0; i < 5; i++) {
            double ang = t * 1.2 + i * 1.256;
            float px = x + (float) (Math.cos(ang) * s * 1.25f);
            float py = y + (float) (Math.sin(ang) * s * 1.25f);
            circle(c, px, py, s * 0.06f, 0xFFFFC93C);
        }
    }

    public static void drawTree(Canvas c, float x, float y, float s, int variant) {
        int[] greens = {0xFF66BB6A, 0xFF4CAF50, 0xFF81C784};
        int g = greens[variant % greens.length];
        rrectO(c, x - s * 0.08f, y - s * 0.2f, x + s * 0.08f, y + s * 0.25f, s * 0.05f, 0xFF795548, s * 0.04f);
        circleO(c, x, y - s * 0.45f, s * 0.42f, g, s * 0.05f);
        circleO(c, x - s * 0.3f, y - s * 0.25f, s * 0.28f, g, s * 0.05f);
        circleO(c, x + s * 0.3f, y - s * 0.25f, s * 0.28f, g, s * 0.05f);
    }

    public static void drawRock(Canvas c, float x, float y, float s) {
        Path p = new Path();
        p.moveTo(x - s * 0.4f, y + s * 0.2f);
        p.lineTo(x - s * 0.25f, y - s * 0.15f);
        p.lineTo(x + s * 0.1f, y - s * 0.25f);
        p.lineTo(x + s * 0.4f, y + s * 0.05f);
        p.lineTo(x + s * 0.3f, y + s * 0.2f);
        p.close();
        c.drawPath(p, fill(0xFF9E9E9E));
        c.drawPath(p, stroke(s * 0.05f, OUT));
    }

    public static void drawFlower(Canvas c, float x, float y, float s, int variant) {
        int[] cols = {0xFFF06292, 0xFFBA68C8, 0xFFFFD54F};
        int col = cols[variant % cols.length];
        for (int i = 0; i < 5; i++) {
            double a = i * 1.256;
            circle(c, x + (float) Math.cos(a) * s * 0.16f, y + (float) Math.sin(a) * s * 0.16f, s * 0.11f, col);
        }
        circle(c, x, y, s * 0.09f, 0xFFFFC93C);
    }

    /** Retrato grande para los diálogos de la historia. */
    public static void drawPortrait(Canvas c, int who, float x, float y, float s, float t) {
        switch (who) {
            case 0: // El Invocador (tú): túnica roja con estrella, como el cómic
                rrectO(c, x - s * 0.3f, y, x + s * 0.3f, y + s * 0.75f, s * 0.15f, 0xFFE53935, s * 0.05f);
                circleO(c, x, y - s * 0.2f, s * 0.28f, 0xFFE8B98A, s * 0.05f);
                // máscara verde
                rrectO(c, x - s * 0.24f, y - s * 0.3f, x + s * 0.24f, y - s * 0.12f, s * 0.08f, 0xFF43A047, s * 0.04f);
                eyes(c, x, y - s * 0.21f, s * 0.045f, s * 0.11f, 0xFF33323A);
                // estrella en el pecho
                Path star = starPath(x, y + s * 0.35f, s * 0.16f, s * 0.07f);
                c.drawPath(star, fill(0xFFFFC93C));
                c.drawPath(star, stroke(s * 0.03f, OUT));
                // gorro
                Path h = new Path();
                h.moveTo(x - s * 0.3f, y - s * 0.35f);
                h.quadTo(x, y - s * 0.75f, x + s * 0.3f, y - s * 0.35f);
                h.close();
                c.drawPath(h, fill(0xFFC62828)); c.drawPath(h, stroke(s * 0.04f, OUT));
                break;
            case 1: // Mocha
                drawMon(c, 3, x, y + s * 0.35f, s * 1.4f, t, false, 0);
                break;
            case 2: // Sir Aldric, el Rey Héroe traidor
                drawHero(c, 5, x, y + s * 0.35f, s * 1.1f, t, 1, true);
                break;
            case 3: // Maestro Orin (espíritu)
                Paint ghost = Ui.paint(0x88B3E5FC);
                circle(c, x, y + s * 0.2f, s * 0.35f, 0x99B3E5FC);
                circleO(c, x, y - s * 0.15f, s * 0.26f, 0xFFB3E5FC, s * 0.04f);
                eyes(c, x, y - s * 0.18f, s * 0.04f, s * 0.1f, 0xFF37474F);
                Path beard = new Path();
                beard.moveTo(x - s * 0.16f, y - s * 0.02f);
                beard.quadTo(x, y + s * 0.5f, x + s * 0.16f, y - s * 0.02f);
                beard.close();
                c.drawPath(beard, fill(0xFFECEFF1));
                break;
            case 4: // Elara, la Arquera Capitana
                drawHero(c, 2, x, y + s * 0.35f, s * 1.1f, t, 1, false);
                break;
            default: // heraldo
                drawHero(c, 1, x, y + s * 0.35f, s * 1.1f, t, 1, false);
                break;
        }
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

    /** Fondo de ráfaga radial para las revelaciones de invocación. */
    public static void drawBurst(Canvas c, float cx, float cy, float R, float t, int col1, int col2) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        int n = 14;
        for (int i = 0; i < n; i++) {
            float a0 = (float) (i * 2 * Math.PI / n + t * 0.4);
            Path w = new Path();
            w.moveTo(cx, cy);
            float span = (float) (Math.PI / n);
            w.arcTo(new RectF(cx - R, cy - R, cx + R, cy + R), (float) Math.toDegrees(a0), (float) Math.toDegrees(span));
            w.close();
            p.setColor(i % 2 == 0 ? col1 : col2);
            c.drawPath(w, p);
        }
    }
}
