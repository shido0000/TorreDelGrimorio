package com.grimorio.torre;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Random;

/** Portal de invocación (gacha) y revelación de monstruos nuevos. */
public class SummonScene implements GameView.Scene {

    static class Result {
        String id;
        boolean isNew;
        int lvl;
    }

    GameView g;
    Ui.Btn bBack, bX1, bX10;
    ArrayList<Result> results = new ArrayList<Result>();
    boolean revealing;
    float revealT;

    public SummonScene(GameView g) {
        this.g = g;
        bBack = new Ui.Btn(40, 40, 140, 90, "◀", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { g.setScene(new MenuScene(g)); }
        });
        bX1 = new Ui.Btn(90, 1620, 420, 130, "Invocar x1", 0xFFAB47BC, 0xFF6A1B9A, new Runnable() {
            public void run() { trySummon(1); }
        });
        bX10 = new Ui.Btn(570, 1620, 420, 130, "Invocar x10", 0xFF7B1FA2, 0xFF4A148C, new Runnable() {
            public void run() { trySummon(10); }
        });
    }

    private void trySummon(int n) {
        int cost = n == 1 ? 100 : 900;
        if (g.save.gems < cost) {
            g.toast("Te faltan gemas. ¡Visita la Tienda!");
            return;
        }
        g.save.gems -= cost;
        Sound.play(Sound.SUMMON);
        results.clear();
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            String id = Defs.rollMonster(rnd);
            Result r = new Result();
            r.id = id;
            boolean existed = false;
            for (Save.Mon m : g.save.mons) if (m.id.equals(id)) { existed = true; break; }
            r.isNew = !existed;
            r.lvl = g.save.addOrLevel(id) ? (findLvl(id)) : 1;
            g.save.qSummons++;
            g.save.totalSummons++;
            results.add(r);
        }
        g.persist();
        revealing = true;
        revealT = 0;
    }

    private int findLvl(String id) {
        for (Save.Mon m : g.save.mons) if (m.id.equals(id)) return m.lvl;
        return 1;
    }

    public void update(float dt) {
        revealT += dt;
    }

    public void touch(int action, float x, float y) {
        if (action != android.view.MotionEvent.ACTION_UP) return;
        if (revealing) {
            results.remove(0);
            if (results.isEmpty()) revealing = false;
            return;
        }
        hit(bBack, x, y); hit(bX1, x, y); hit(bX10, x, y);
    }

    private void hit(Ui.Btn b, float x, float y) {
        if (b.hit(x, y)) Ui.fire(b);
    }

    public boolean back() {
        if (revealing) { results.clear(); revealing = false; return true; }
        g.setScene(new MenuScene(g));
        return true;
    }

    public void draw(Canvas c, float t) {
        c.drawRect(0, 0, 1080, 1920, Ui.paint(0xFF2A1A4A));
        Random rnd = new Random(7);
        for (int i = 0; i < 60; i++) {
            float x = rnd.nextFloat() * 1080, y = rnd.nextFloat() * 1920;
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 2 + i);
            c.drawCircle(x, y, 3 + tw * 3, Ui.paint(0xAAFFFFFF));
        }

        if (revealing && !results.isEmpty()) {
            drawReveal(c, t);
            return;
        }

        Ui.panel(c, 140, 120, 800, 150, 0xFF311B92, 0xFF1A0560);
        Ui.textC(c, "PORTAL DE INVOCACIÓN", 540, 215, 52, true, 0xFFCE93D8);
        Art.drawGem(c, 470, 330, 26);
        Ui.textC(c, String.valueOf(g.save.gems), 510, 345, 40, true, 0xFF4DD0E1);

        Art.drawPortal(c, 540, 800, 260, t);
        Art.drawMon(c, 7, 540, 810, 190, t, false, 0);

        Ui.textC(c, "Común 60% · Raro 27% · Épico 10% · Legendario 3%", 540, 1180, 30, true, 0xFFCE93D8);
        Ui.textC(c, "Los repetidos suben de nivel al monstruo", 540, 1240, 28, false, 0xFFB39DDB);

        bX1.label = "Invocar x1";
        bX1.sub = "100 gemas";
        bX1.enabled = g.save.gems >= 100;
        bX1.draw(c, t);
        bX10.label = "Invocar x10";
        bX10.sub = "900 gemas";
        bX10.enabled = g.save.gems >= 900;
        bX10.draw(c, t);
        bBack.draw(c, t);
    }

    private void drawReveal(Canvas c, float t) {
        Result r = results.get(0);
        Defs.MDef d = Defs.mon(r.id);
        int col = Defs.RARITY_COL[d.rarity];
        int edge = Defs.RARITY_EDGE[d.rarity];
        float pop = Math.min(1, revealT * 3);
        Art.drawBurst(c, 540, 760, 900 * pop, t, col, edge);
        c.drawCircle(540, 760, 330 * pop, Ui.paint(0xEEFFFFFF));
        android.graphics.Paint st = Ui.paint(edge);
        st.setStyle(android.graphics.Paint.Style.STROKE);
        st.setStrokeWidth(12);
        c.drawCircle(540, 760, 330 * pop, st);

        c.save();
        c.scale(pop, pop, 540, 760);
        Art.drawMon(c, d.art, 540, 780, 380, t, false, 0);
        c.restore();

        Ui.panel(c, 190, 1150, 700, 260, 0xFFFFFFFF, edge);
        if (r.isNew) Ui.textC(c, "¡NUEVO!", 540, 1215, 44, true, edge);
        else Ui.textC(c, "¡Sube a Nivel " + r.lvl + "!", 540, 1215, 44, true, edge);
        Ui.textC(c, d.name, 540, 1290, 60, true, Ui.DARKTXT);
        Ui.textC(c, Defs.RARITY[d.rarity], 540, 1355, 36, true, col);

        Ui.textCO(c, d.desc, 540, 1490, 30, false, 0xFFE0E0E0);
        Ui.textCO(c, "(Toca para continuar" + (results.size() > 1 ? " · quedan " + results.size() : "") + ")",
                540, 1800, 30, true, 0xFFB39DDB);
    }
}
