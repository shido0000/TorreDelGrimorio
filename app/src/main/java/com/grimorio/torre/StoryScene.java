package com.grimorio.torre;

import android.graphics.Canvas;

/** Escena de historia: diálogos con retratos estilo cómic. */
public class StoryScene implements GameView.Scene {

    GameView g;
    int chapter;
    int line;
    GameView.Scene next;

    public StoryScene(GameView g, int chapter, GameView.Scene next) {
        this.g = g;
        this.chapter = Math.max(0, Math.min(chapter, Story.CHAPTERS.length - 1));
        this.next = next;
        this.line = 0;
    }

    public void update(float dt) { }

    public void touch(int action, float x, float y) {
        if (action != android.view.MotionEvent.ACTION_UP) return;
        Story.Chapter ch = Story.CHAPTERS[chapter];
        line++;
        if (line >= ch.lines.length) {
            g.persist();
            g.setScene(next);
        }
    }

    public boolean back() {
        g.persist();
        g.setScene(next);
        return true;
    }

    public void draw(Canvas c, float t) {
        Story.Chapter ch = Story.CHAPTERS[chapter];
        Story.Line l = ch.lines[line];

        // fondo según capítulo
        int[] bgs = {0xFF2E5D34, 0xFF37474F, 0xFF1B5E20, 0xFF4E342E, 0xFF311B92, 0xFF0D47A1};
        c.drawRect(0, 0, 1080, 1920, Ui.paint(bgs[chapter % bgs.length]));
        // viñeta decorativa
        java.util.Random rnd = new java.util.Random(chapter * 17 + 3);
        for (int i = 0; i < 30; i++) {
            float x = rnd.nextFloat() * 1080, y = rnd.nextFloat() * 1300;
            float tw = 0.5f + 0.5f * (float) Math.sin(t * 2 + i);
            c.drawCircle(x, y, 2 + tw * 3, Ui.paint(0x66FFFFFF));
        }

        // título del capítulo
        Ui.panel(c, 90, 80, 900, 130, 0xFF311B92, 0xFF1A0560);
        Ui.textC(c, ch.title, 540, 140, 42, true, 0xFFFFC93C);
        Ui.textC(c, "Toca para continuar", 540, 190, 24, false, 0xFFB39DDB);

        // escena: retrato del hablante (o del otro personaje)
        float px = l.who == 2 || l.who == 4 ? 780 : 300;
        float py = 620;
        Art.drawPortrait(c, l.who, px, py, 260, t);
        if (l.who != 1) Art.drawPortrait(c, 1, l.who == 2 || l.who == 4 ? 300 : 780, py + 40, 170, t);

        // caja de diálogo
        Ui.panel(c, 60, 1350, 960, 420, 0xFFF5E6C8, 0xFF8D6E63);
        // nombre
        Ui.panel(c, 100, 1300, 480, 90, Defs.RARITY_COL[1], 0xFF1565C0);
        Ui.textC(c, l.name, 340, 1360, 38, true, 0xFFFFFFFF);
        // texto envuelto
        wrapText(c, l.text, 110, 1480, 860, 36, Ui.DARKTXT);

        Ui.textC(c, (line + 1) + " / " + ch.lines.length, 540, 1810, 26, false, 0xFF6D4C41);
    }

    private void wrapText(Canvas c, String text, float x, float y, float maxW, float size, int col) {
        android.graphics.Paint p = Ui.text(size, false, col);
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        float yy = y;
        for (String w : words) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (p.measureText(test) > maxW) {
                c.drawText(cur.toString(), x, yy, p);
                yy += size * 1.35f;
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (cur.length() > 0) c.drawText(cur.toString(), x, yy, p);
    }
}
