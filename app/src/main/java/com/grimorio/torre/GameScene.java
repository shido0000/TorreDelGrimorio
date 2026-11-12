package com.grimorio.torre;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

/** Escena de juego: defensa idle de la Torre del Grimorio. */
public class GameScene implements GameView.Scene {

    public static final int COLS = 9, ROWS = 13, TILE = 108, X0 = 54, Y0 = 250;
    private static final int[] PATH_CELLS = {
            0,2, 1,2, 2,2, 3,2, 4,2, 4,3, 4,4, 4,5, 3,5, 2,5, 1,5,
            1,6, 1,7, 1,8, 2,8, 3,8, 4,8, 5,8, 6,8, 6,9, 6,10, 6,11, 7,11, 8,11
    };

    class Hero {
        Defs.HDef d;
        float dist, hp, maxHp, cd, flash;
        float scale = 1;
        boolean escaped;
        float x, y;
    }

    class RMon {
        Save.Mon src;
        Defs.MDef d;
        float hp, maxHp, cd, deadT, flash;
        float x, y;
    }

    class Proj {
        float x, y, dmg;
        Hero target;
        RMon owner;
    }

    class FT {
        float x, y, life;
        String txt;
        int col;
    }

    class Slash {
        float x, y, ang, life;
    }

    GameView g;
    float[] segX, segY, segLen, cum;
    float pathLen;
    boolean[] valid = new boolean[ROWS * COLS];
    boolean[] isPath = new boolean[ROWS * COLS];
    int chestCell;

    ArrayList<RMon> placed = new ArrayList<RMon>();
    ArrayList<Hero> heroes = new ArrayList<Hero>();
    ArrayList<Proj> projs = new ArrayList<Proj>();
    ArrayList<FT> fts = new ArrayList<FT>();
    ArrayList<Slash> slashes = new ArrayList<Slash>();
    ArrayList<String> queue = new ArrayList<String>();
    float spawnT;

    int chestHits;
    int state = 0; // 0 combate, 1 cuenta atrás siguiente, 2 fallo, cuenta atrás
    float countdown;
    String banner;
    float bannerT;

    float speed = 1;
    boolean paused;
    boolean editMode;
    int placingBench = -1;
    int pickedUp = -1;
    int selected = -1;
    float saveT;

    Ui.Btn btnSpeed, btnPause, btnEdit, btnSummon, btnShop;
    Ui.Btn bResume, bMenu, bUp, bSell, bMove, bClose;
    boolean showPausePanel;

    public GameScene(GameView g) {
        this.g = g;
        buildPath();
        buildValid();
        rebuildPlaced();
        btnSpeed = new Ui.Btn(640, 150, 130, 78, "x1", 0xFF42A5F5, 0xFF1565C0, new Runnable() {
            public void run() {
                speed = speed == 1 ? 2 : (speed == 2 ? 4 : 1);
                btnSpeed.label = "x" + (int) speed;
            }
        });
        btnPause = new Ui.Btn(790, 150, 130, 78, "II", 0xFF78909C, 0xFF455A64, new Runnable() {
            public void run() { showPausePanel = true; }
        });
        btnEdit = new Ui.Btn(60, 1712, 200, 92, "Editar", 0xFF8D6E63, 0xFF5D4037, new Runnable() {
            public void run() {
                editMode = !editMode;
                btnEdit.label = editMode ? "Listo" : "Editar";
                btnEdit.fill = editMode ? 0xFF66BB6A : 0xFF8D6E63;
                selected = -1;
            }
        });
        btnSummon = new Ui.Btn(300, 1712, 230, 92, "Invocar", 0xFFAB47BC, 0xFF6A1B9A, new Runnable() {
            public void run() { g.persist(); g.setScene(new SummonScene(g)); }
        });
        btnShop = new Ui.Btn(570, 1712, 230, 92, "Tienda", 0xFFFFB300, 0xFFE65100, new Runnable() {
            public void run() { g.persist(); g.setScene(new ShopScene(g)); }
        });
        bResume = new Ui.Btn(340, 950, 400, 110, "Reanudar", 0xFF66BB6A, 0xFF2E7D32, new Runnable() {
            public void run() { showPausePanel = false; }
        });
        bMenu = new Ui.Btn(340, 1100, 400, 110, "Menú principal", 0xFFEF5350, 0xFFB71C1C, new Runnable() {
            public void run() { g.persist(); g.setScene(new MenuScene(g)); }
        });
        bUp = new Ui.Btn(620, 1420, 200, 90, "Mejorar", 0xFF66BB6A, 0xFF2E7D32, new Runnable() {
            public void run() { upgradeSelected(); }
        });
        bSell = new Ui.Btn(840, 1420, 180, 90, "Vender", 0xFFEF5350, 0xFFB71C1C, new Runnable() {
            public void run() { sellSelected(); }
        });
        bMove = new Ui.Btn(620, 1530, 200, 90, "Mover", 0xFF42A5F5, 0xFF1565C0, new Runnable() {
            public void run() {
                pickedUp = selected;
                selected = -1;
            }
        });
        bClose = new Ui.Btn(840, 1530, 180, 90, "Cerrar", 0xFF90A4AE, 0xFF455A64, new Runnable() {
            public void run() { selected = -1; }
        });

        if (g.save.bestWave == 0) {
            int ch = Story.chapterForWave(1, g.save.story);
            if (ch >= 0) {
                g.save.story = ch + 1;
                g.setScene(new StoryScene(g, ch, this));
            }
        }
        startWave(g.save.wave);
    }

    // ---------- construcción del tablero ----------

    private void buildPath() {
        int n = PATH_CELLS.length / 2;
        segX = new float[n + 1];
        segY = new float[n + 1];
        segLen = new float[n];
        cum = new float[n + 1];
        segX[0] = X0 - TILE * 1.5f;
        segY[0] = Y0 + PATH_CELLS[1] * TILE + TILE / 2f;
        for (int i = 0; i < n; i++) {
            int col = PATH_CELLS[i * 2], row = PATH_CELLS[i * 2 + 1];
            isPath[row * COLS + col] = true;
            segX[i + 1] = X0 + col * TILE + TILE / 2f;
            segY[i + 1] = Y0 + row * TILE + TILE / 2f;
        }
        chestCell = PATH_CELLS[(n - 1) * 2 + 1] * COLS + PATH_CELLS[(n - 1) * 2];
        pathLen = 0;
        for (int i = 0; i < n; i++) {
            float dx = segX[i + 1] - segX[i], dy = segY[i + 1] - segY[i];
            segLen[i] = (float) Math.sqrt(dx * dx + dy * dy);
            cum[i + 1] = cum[i] + segLen[i];
        }
        pathLen = cum[n];
    }

    private void buildValid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int idx = r * COLS + c;
                if (isPath[idx]) continue;
                boolean adj =
                        (c > 0 && isPath[idx - 1]) || (c < COLS - 1 && isPath[idx + 1]) ||
                        (r > 0 && isPath[idx - COLS]) || (r < ROWS - 1 && isPath[idx + COLS]);
                valid[idx] = adj;
            }
        }
    }

    public static int cellCenterX(int cell) { return X0 + (cell % COLS) * TILE + TILE / 2; }
    public static int cellCenterY(int cell) { return Y0 + (cell / COLS) * TILE + TILE / 2; }

    private void rebuildPlaced() {
        placed.clear();
        for (Save.Mon m : g.save.mons) {
            if (m.cell < 0) continue;
            RMon r = new RMon();
            r.src = m;
            r.d = Defs.mon(m.id);
            r.maxHp = Defs.monHp(r.d, m.lvl) * (float) g.save.hpMult();
            r.hp = r.maxHp;
            r.x = cellCenterX(m.cell);
            r.y = cellCenterY(m.cell);
            placed.add(r);
        }
    }

    // ---------- oleadas ----------

    private void startWave(int n) {
        g.save.wave = n;
        if (n > g.save.bestWave) g.save.bestWave = n;
        queue.clear();
        queue.addAll(Defs.buildWave(n));
        spawnT = 0.6f;
        chestHits = 0;
        state = 0;
        heroes.clear();
        projs.clear();
        int ch = Story.chapterForWave(n, g.save.story);
        if (ch >= 0) {
            g.save.story = ch + 1;
            g.persist();
            g.setScene(new StoryScene(g, ch, this));
        }
    }

    private void waveCleared() {
        int n = g.save.wave;
        long bonus = (long) ((60 + 25 * n) * g.save.goldMult());
        g.save.gold += bonus;
        int gems = 0;
        if (n % 10 == 0) gems += 5;
        else if (n % 5 == 0) gems += 2;
        if (gems > 0) g.save.addGems(gems);
        banner = "¡Oleada " + n + " superada!  +" + Ui.fmt(bonus) + " oro" + (gems > 0 ? "  +" + gems + " gemas" : "");
        bannerT = 3;
        state = 1;
        countdown = 4;
        g.persist();
    }

    private void waveFailed() {
        long lost = (long) (g.save.gold * 0.15);
        g.save.gold -= lost;
        banner = "¡El cofre cayó! Pierdes " + Ui.fmt(lost) + " de oro";
        bannerT = 3;
        state = 2;
        countdown = 3.5f;
        heroes.clear();
        projs.clear();
        g.persist();
    }

    // ---------- update ----------

    public void update(float dt) {
        float dtg = paused ? 0 : dt * speed;
        if (bannerT > 0) bannerT -= dt;

        // ingresos idle
        if (dtg > 0) g.save.gold += (long) (g.save.incomePerSec() * dtg);

        // oleada
        if (dtg > 0) {
            if (state == 0) {
                spawnT -= dtg;
                if (spawnT <= 0 && !queue.isEmpty()) {
                    spawnHero(queue.remove(0));
                    spawnT = 0.9f;
                }
                if (queue.isEmpty() && heroes.isEmpty()) waveCleared();
            } else if (state == 1 || state == 2) {
                countdown -= dtg;
                if (countdown <= 0) {
                    int next = state == 1 ? g.save.wave + 1 : g.save.wave;
                    startWave(next);
                }
            }
        }

        if (dtg > 0) updateHeroes(dtg);
        if (dtg > 0) updateMonsters(dtg);
        if (dtg > 0) updateProjs(dtg);

        for (int i = fts.size() - 1; i >= 0; i--) {
            FT f = fts.get(i);
            f.life -= dt;
            f.y -= 60 * dt;
            if (f.life <= 0) fts.remove(i);
        }
        for (int i = slashes.size() - 1; i >= 0; i--) {
            Slash s = slashes.get(i);
            s.life -= dt;
            if (s.life <= 0) slashes.remove(i);
        }

        saveT += dt;
        if (saveT > 10) {
            saveT = 0;
            g.persist();
        }
    }

    private void spawnHero(String id) {
        Hero h = new Hero();
        h.d = Defs.hero(id);
        float s = Defs.waveScale(g.save.wave);
        h.maxHp = h.d.hp * s;
        h.hp = h.maxHp;
        h.dist = 0;
        if (h.d.boss) h.scale = 1.5f;
        heroes.add(h);
        if (h.d.boss) {
            banner = "¡" + h.d.name + " ataca!";
            bannerT = 2.5f;
        }
    }

    private void posAt(float d, float[] out) {
        if (d <= 0) { out[0] = segX[0]; out[1] = segY[0]; return; }
        if (d >= pathLen) { out[0] = segX[segX.length - 1]; out[1] = segY[segY.length - 1]; return; }
        int i = 1;
        while (i < cum.length && cum[i] < d) i++;
        float t2 = (d - cum[i - 1]) / segLen[i - 1];
        out[0] = segX[i - 1] + (segX[i] - segX[i - 1]) * t2;
        out[1] = segY[i - 1] + (segY[i] - segY[i - 1]) * t2;
    }

    private float[] tmpPos = new float[2];

    private void updateHeroes(float dt) {
        for (Hero h : heroes) {
            if (h.flash > 0) h.flash -= dt * 4;
            // objetivo: monstruo vivo más cercano
            RMon tgt = null;
            float best = Float.MAX_VALUE;
            float range = h.d.range * TILE;
            for (RMon m : placed) {
                if (m.deadT > 0) continue;
                float dx = m.x - h.x, dy = m.y - h.y;
                float dd = dx * dx + dy * dy;
                if (dd < best && dd <= range * range) { best = dd; tgt = m; }
            }
            if (tgt != null) {
                h.cd -= dt;
                if (h.cd <= 0) {
                    h.cd = h.d.atkSpd;
                    float dmg = h.d.dmg * Defs.waveScale(g.save.wave);
                    tgt.hp -= dmg;
                    tgt.flash = 1;
                    slashes.add(new Slash());
                    Slash sl = slashes.get(slashes.size() - 1);
                    sl.x = tgt.x; sl.y = tgt.y; sl.ang = (float) Math.toDegrees(Math.atan2(tgt.y - h.y, tgt.x - h.x)); sl.life = 0.25f;
                    if (tgt.hp <= 0) {
                        tgt.deadT = 12;
                        tgt.hp = 0;
                        fts.add(ft(tgt.x, tgt.y, "¡Caído!", 0xFFEF5350));
                    }
                }
            } else {
                h.dist += h.d.speed * TILE * dt;
                posAt(h.dist, tmpPos);
                h.x = tmpPos[0];
                h.y = tmpPos[1];
                if (h.dist >= pathLen) {
                    // llega al cofre: roba oro y escapa
                    long steal = Math.max(10, (long) (g.save.gold * 0.08));
                    g.save.gold = Math.max(0, g.save.gold - steal);
                    fts.add(ft(segX[segX.length - 1], segY[segY.length - 1] - 80, "-" + Ui.fmt(steal) + " oro", 0xFFEF5350));
                    chestHits++;
                    h.escaped = true;
                    if (chestHits >= 3) { waveFailed(); return; }
                }
            }
        }
        for (int i = heroes.size() - 1; i >= 0; i--) if (heroes.get(i).escaped) heroes.remove(i);
    }

    private FT ft(float x, float y, String txt, int col) {
        FT f = new FT();
        f.x = x; f.y = y; f.txt = txt; f.col = col; f.life = 1.1f;
        return f;
    }

    private void updateMonsters(float dt) {
        for (RMon m : placed) {
            if (m.flash > 0) m.flash -= dt * 4;
            if (m.deadT > 0) {
                m.deadT -= dt;
                if (m.deadT <= 0) m.hp = m.maxHp;
                continue;
            }
            m.cd -= dt;
            if (m.cd > 0) continue;
            Hero tgt = null;
            float best = Float.MAX_VALUE;
            float range = m.d.range * TILE;
            for (Hero h : heroes) {
                float dx = h.x - m.x, dy = h.y - m.y;
                float dd = dx * dx + dy * dy;
                if (dd < best && dd <= range * range) { best = dd; tgt = h; }
            }
            if (tgt != null) {
                m.cd = m.d.atkSpd;
                Proj p = new Proj();
                p.x = m.x; p.y = m.y - 20;
                p.dmg = Defs.monDmg(m.d, m.src.lvl) * (float) g.save.dmgMult();
                p.target = tgt;
                p.owner = m;
                projs.add(p);
            }
        }
    }

    private void updateProjs(float dt) {
        for (int i = projs.size() - 1; i >= 0; i--) {
            Proj p = projs.get(i);
            Hero h = p.target;
            if (h.escaped || h.hp <= 0 || !heroes.contains(h)) { projs.remove(i); continue; }
            float dx = h.x - p.x, dy = h.y - p.y;
            float dd = (float) Math.sqrt(dx * dx + dy * dy);
            float step = 950 * dt;
            if (dd <= step + 12) {
                h.hp -= p.dmg;
                h.flash = 1;
                fts.add(ft(h.x, h.y - 40, String.valueOf((long) p.dmg), 0xFFFFEB3B));
                if (h.hp <= 0) {
                    long rw = (long) (Defs.killReward(h.d, g.save.wave) * g.save.goldMult());
                    g.save.gold += rw;
                    fts.add(ft(h.x, h.y - 70, "+" + Ui.fmt(rw), 0xFFFFC93C));
                    if (p.owner != null) {
                        p.owner.src.xp += h.d.boss ? 25 : 5;
                        long need = Defs.xpNeed(p.owner.src.lvl);
                        if (p.owner.src.xp >= need) {
                            p.owner.src.xp -= need;
                            p.owner.src.lvl++;
                            p.owner.maxHp = Defs.monHp(p.owner.d, p.owner.src.lvl) * (float) g.save.hpMult();
                            p.owner.hp = p.owner.maxHp;
                            fts.add(ft(p.owner.x, p.owner.y - 90, "¡Nivel " + p.owner.src.lvl + "!", 0xFF66BB6A));
                        }
                    }
                    heroes.remove(h);
                }
                projs.remove(i);
                continue;
            }
            p.x += dx / dd * step;
            p.y += dy / dd * step;
        }
    }

    // ---------- acciones ----------

    private void upgradeSelected() {
        if (selected < 0 || selected >= placed.size()) return;
        RMon m = placed.get(selected);
        long cost = Defs.monUpCost(m.d, m.src.lvl);
        if (g.save.gold < cost) {
            fts.add(ft(m.x, m.y - 80, "¡Oro insuficiente!", 0xFFEF5350));
            return;
        }
        g.save.gold -= cost;
        m.src.lvl++;
        m.maxHp = Defs.monHp(m.d, m.src.lvl) * (float) g.save.hpMult();
        m.hp = m.maxHp;
        fts.add(ft(m.x, m.y - 80, "¡Nivel " + m.src.lvl + "!", 0xFF66BB6A));
        g.persist();
    }

    private void sellSelected() {
        if (selected < 0 || selected >= placed.size()) return;
        RMon m = placed.get(selected);
        long refund = (long) (Defs.monUpCost(m.d, m.src.lvl) * 0.4);
        g.save.gold += refund;
        m.src.cell = -1;
        fts.add(ft(m.x, m.y - 80, "+" + Ui.fmt(refund), 0xFFFFC93C));
        selected = -1;
        rebuildPlaced();
        g.persist();
    }

    // ---------- entrada ----------

    public void touch(int action, float x, float y) {
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) return;
        boolean down = action == MotionEvent.ACTION_DOWN;

        if (showPausePanel) {
            if (down) { press(bResume, x, y); press(bMenu, x, y); }
            else { click(bResume, x, y); click(bMenu, x, y); }
            return;
        }
        if (selected >= 0) {
            if (down) { press(bUp, x, y); press(bSell, x, y); press(bMove, x, y); press(bClose, x, y); }
            else {
                click(bUp, x, y); click(bSell, x, y); click(bMove, x, y); click(bClose, x, y);
                // tocar fuera cierra
                if (!inRect(x, y, 540, 1150, 520, 500)) selected = -1;
            }
            return;
        }
        if (down) {
            if (press(btnSpeed, x, y)) return;
            if (press(btnPause, x, y)) return;
            if (press(btnEdit, x, y)) return;
            if (press(btnSummon, x, y)) return;
            if (press(btnShop, x, y)) return;
            // banquillo
            ArrayList<Save.Mon> bench = g.save.bench();
            if (handleBenchTap(bench, x, y)) return;
            // celdas del tablero
            int col = (int) ((x - X0) / TILE), row = (int) ((y - Y0) / TILE);
            if (col >= 0 && col < COLS && row >= 0 && row < ROWS) {
                int cell = row * COLS + col;
                if (placingBench >= 0) {
                    if (valid[cell] && !occupied(cell) && g.save.placedCount() < g.save.maxSlots(g.save.wave)) {
                        Save.Mon m = bench.get(placingBench);
                        m.cell = cell;
                        placingBench = -1;
                        rebuildPlaced();
                        g.persist();
                    }
                    return;
                }
                if (pickedUp >= 0) {
                    if (valid[cell] && !occupied(cell)) {
                        placed.get(pickedUp).src.cell = cell;
                        pickedUp = -1;
                        rebuildPlaced();
                        g.persist();
                    }
                    return;
                }
                if (editMode) {
                    for (int i = 0; i < placed.size(); i++) {
                        RMon m = placed.get(i);
                        if (Math.abs(m.x - x) < TILE / 2 && Math.abs(m.y - y) < TILE / 2) {
                            selected = i;
                            return;
                        }
                    }
                }
            }
        } else {
            click(btnSpeed, x, y);
            click(btnPause, x, y);
            click(btnEdit, x, y);
            click(btnSummon, x, y);
            click(btnShop, x, y);
            handleBenchRelease(x, y);
        }
    }

    private float benchDownX, benchDownY;
    private int benchDownIdx = -1;

    private boolean handleBenchTap(ArrayList<Save.Mon> bench, float x, float y) {
        // ranuras del banquillo en el panel inferior
        if (x < 40 || x > 790 || y < 1700 || y > 1870) return false;
        int idx = (int) ((x - 60) / 170);
        if (idx < 0 || idx >= Math.min(bench.size(), 4)) return false;
        benchDownIdx = idx;
        benchDownX = x; benchDownY = y;
        return true;
    }

    private void handleBenchRelease(float x, float y) {
        if (benchDownIdx < 0) return;
        boolean hit = Math.abs(x - benchDownX) < 30 && Math.abs(y - benchDownY) < 30;
        int idx = benchDownIdx;
        benchDownIdx = -1;
        if (!hit) return;
        ArrayList<Save.Mon> bench = g.save.bench();
        if (idx >= bench.size()) return;
        if (g.save.placedCount() >= g.save.maxSlots(g.save.wave)) {
            banner = "¡No hay huecos! Sube de oleada o vende un monstruo";
            bannerT = 2.5f;
            return;
        }
        placingBench = idx;
    }

    private boolean occupied(int cell) {
        for (Save.Mon m : g.save.mons) if (m.cell == cell) return true;
        return false;
    }

    private boolean inRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private boolean press(Ui.Btn b, float x, float y) {
        if (b.enabled && b.hit(x, y)) { b.press = true; return true; }
        return false;
    }

    private void click(Ui.Btn b, float x, float y) {
        boolean was = b.press;
        b.press = false;
        if (was && b.enabled && b.hit(x, y) && b.on != null) b.on.run();
    }

    public boolean back() {
        if (showPausePanel) { showPausePanel = false; return true; }
        if (selected >= 0) { selected = -1; return true; }
        if (placingBench >= 0) { placingBench = -1; return true; }
        if (editMode) { editMode = false; btnEdit.label = "Editar"; btnEdit.fill = 0xFF8D6E63; return true; }
        showPausePanel = true;
        return true;
    }

    // ---------- dibujo ----------

    public void draw(Canvas c, float t) {
        drawBoard(c, t);
        drawChest(c, t);
        for (RMon m : placed) {
            float size = TILE * 0.85f;
            if (pickedUp >= 0 && placed.get(pickedUp) == m) continue;
            Art.drawMon(c, m.d.art, m.x, m.y, size, t, m.deadT > 0, Math.max(0, m.flash));
            if (m.deadT <= 0 && m.hp < m.maxHp) {
                float bw = TILE * 0.6f;
                float bx = m.x - bw / 2, by = m.y - TILE * 0.55f;
                c.drawRoundRect(new RectF(bx - 2, by - 2, bx + bw + 2, by + 10), 6, 6, Ui.paint(0xFF33323A));
                float frac = m.hp / m.maxHp;
                if (frac > 0) c.drawRoundRect(new RectF(bx, by, bx + bw * frac, by + 8), 4, 4, Ui.paint(0xFF66BB6A));
            }
            if (editMode && m.deadT <= 0) {
                Paint pr = Ui.paint(0);
                pr.setStyle(Paint.Style.STROKE);
                pr.setStrokeWidth(4);
                pr.setColor(0xAAFFEB3B);
                c.drawCircle(m.x, m.y, TILE * 0.48f, pr);
            }
        }
        for (Hero h : heroes) {
            Art.drawHero(c, h.d.art, h.x, h.y, TILE * 0.72f * h.scale, t, h.hp / h.maxHp, h.d.boss);
        }
        for (Proj p : projs) {
            c.drawCircle(p.x, p.y, 12, Ui.paint(0xFFFFEB3B));
            Paint pe = Ui.paint(0xFFFF5722);
            pe.setStyle(Paint.Style.STROKE);
            pe.setStrokeWidth(4);
            c.drawCircle(p.x, p.y, 12, pe);
        }
        for (Slash s : slashes) {
            Paint sp = Ui.paint(0xFFFFFFFF);
            sp.setStyle(Paint.Style.STROKE);
            sp.setStrokeWidth(10);
            c.drawArc(new RectF(s.x - 40, s.y - 40, s.x + 40, s.y + 40), s.ang - 50, 100, false, sp);
        }
        for (FT f : fts) {
            Ui.textCO(c, f.txt, f.x, f.y, 40, true, f.col);
        }

        drawHud(c, t);
        if (placingBench >= 0 || pickedUp >= 0) drawValidCells(c);
        if (selected >= 0) drawInfoPanel(c);
        if (showPausePanel) drawPausePanel(c);
        if (bannerT > 0 && banner != null) {
            Ui.panel(c, 90, 420, 900, 130, 0xFF37474F, 0xFF263238);
            Ui.textCO(c, banner, 540, 500, 44, true, 0xFFFFC93C);
        }
    }

    private void drawValidCells(Canvas c) {
        for (int r = 0; r < ROWS; r++) {
            for (int col = 0; col < COLS; col++) {
                int cell = r * COLS + col;
                if (!valid[cell] || occupied(cell)) continue;
                Paint p = Ui.paint(0x5566BB6A);
                c.drawRoundRect(new RectF(X0 + col * TILE + 6, Y0 + r * TILE + 6,
                        X0 + col * TILE + TILE - 6, Y0 + r * TILE + TILE - 6), 16, 16, p);
            }
        }
    }

    private void drawBoard(Canvas c, float t) {
        int chapter = ((g.save.wave - 1) / 10) % 4;
        int[] grass = {0xFF7EC850, 0xFF5FAE4A, 0xFF8CCB5E, 0xFF6DBE55};
        int[] grassD = {0xFF74BD49, 0xFF55A543, 0xFF81C258, 0xFF63B44E};
        int[] pathCol = {0xFFC8996A, 0xFFB08A5E, 0xFFD0A475, 0xFFA9835C};
        int pc = pathCol[chapter];

        // franja de cielo con nubes detrás del HUD
        Paint sky = Ui.paint(0);
        sky.setShader(new android.graphics.LinearGradient(0, 0, 0, Y0,
                0xFF8ED6FF, 0xFFCDEFB8, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 1080, Y0, sky);
        Paint cloud = Ui.paint(0xD9FFFFFF);
        for (int i = 0; i < 4; i++) {
            float cx2 = ((t * (12 + i * 5) + i * 340) % 1300) - 110;
            float cy2 = 60 + (i % 2) * 70;
            c.drawOval(new RectF(cx2 - 90, cy2 - 28, cx2 + 90, cy2 + 28), cloud);
            c.drawOval(new RectF(cx2 - 40, cy2 - 52, cx2 + 50, cy2 + 10), cloud);
        }

        // césped con degradado y cuadros
        Paint pg = Ui.paint(0);
        pg.setShader(new android.graphics.LinearGradient(0, Y0, 0, 1920,
                Ui.lighten(grass[chapter], 0.10f), Ui.darken(grass[chapter], 0.10f), android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, Y0, 1080, 1920, pg);
        Paint pd = Ui.paint(grassD[chapter]);
        for (int r = 0; r < ROWS; r++)
            for (int col = 0; col < COLS; col++)
                if ((r + col) % 2 == 0)
                    c.drawRect(X0 + col * TILE, Y0 + r * TILE, X0 + col * TILE + TILE, Y0 + r * TILE + TILE, pd);

        // briznas de hierba deterministas
        Random gr = new Random(55 + chapter);
        Paint blade = Ui.paint(Ui.darken(grass[chapter], 0.28f));
        blade.setStrokeWidth(5);
        for (int i = 0; i < 90; i++) {
            float x = gr.nextFloat() * 1080;
            float y = Y0 + gr.nextFloat() * (1920 - Y0);
            int cell = (int) (((y - Y0) / TILE)) * COLS + (int) ((x - X0) / TILE);
            if (cell < 0 || cell >= isPath.length || isPath[cell]) continue;
            c.drawLine(x, y, x - 8, y - 22, blade);
            c.drawLine(x, y, x, y - 28, blade);
            c.drawLine(x, y, x + 8, y - 20, blade);
        }

        // camino: borde oscuro + relleno + línea central discontinua
        Path pth = new Path();
        pth.moveTo(segX[0], segY[0]);
        for (int i = 1; i < segX.length; i++) pth.lineTo(segX[i], segY[i]);
        Paint pb = Ui.paint(0xFF6D4C41);
        pb.setStyle(Paint.Style.STROKE);
        pb.setStrokeWidth(TILE * 1.0f);
        pb.setStrokeCap(Paint.Cap.BUTT);
        pb.setStrokeJoin(Paint.Join.ROUND);
        c.drawPath(pth, pb);
        Paint pcx = Ui.paint(pc);
        pcx.setStyle(Paint.Style.STROKE);
        pcx.setStrokeWidth(TILE * 0.84f);
        pcx.setStrokeCap(Paint.Cap.BUTT);
        pcx.setStrokeJoin(Paint.Join.ROUND);
        c.drawPath(pth, pcx);
        Paint pIn = Ui.paint(Ui.lighten(pc, 0.18f));
        pIn.setStyle(Paint.Style.STROKE);
        pIn.setStrokeWidth(10);
        pIn.setPathEffect(new android.graphics.DashPathEffect(new float[]{34, 30}, 0));
        c.drawPath(pth, pIn);
        // piedritas
        Paint peb = Ui.paint(0x59FFFFFF);
        Random rnd = new Random(1234);
        for (int i = 0; i < 46; i++) {
            float d = rnd.nextFloat() * pathLen;
            posAt(d, tmpPos);
            float ox2 = (rnd.nextFloat() - 0.5f) * TILE * 0.5f;
            c.drawCircle(tmpPos[0] + ox2, tmpPos[1] + (rnd.nextFloat() - 0.5f) * TILE * 0.4f, 5 + rnd.nextFloat() * 6, peb);
        }

        // decoración con sprites
        Random dr = new Random(77 + chapter);
        for (int i = 0; i < 30; i++) {
            int col = dr.nextInt(COLS), row = dr.nextInt(ROWS);
            int cell = row * COLS + col;
            if (isPath[cell] || valid[cell]) continue;
            float x = X0 + col * TILE + TILE / 2f + (dr.nextFloat() - 0.5f) * 30;
            float y = Y0 + row * TILE + TILE / 2f + (dr.nextFloat() - 0.5f) * 30;
            int kind = dr.nextInt(5);
            if (kind == 0) Art.drawTree(c, x, y, TILE * 1.0f, dr.nextInt(3));
            else if (kind == 1) Art.drawRock(c, x, y, TILE * 0.55f);
            else if (kind == 2) Art.drawFlower(c, x, y, TILE * 0.42f, dr.nextInt(3));
            else if (kind == 3) Art.drawBush(c, x, y, TILE * 0.6f);
        }

        // viñeta sutil
        Paint vg = Ui.paint(0);
        vg.setShader(new android.graphics.RadialGradient(540, 960, 1150,
                0x00000000, 0x26000000, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 1080, 1920, vg);

        // entrada: portal por donde entran los héroes
        float ex = segX[0] + 20, ey = segY[0];
        Art.drawPortal(c, ex, ey, TILE * 0.42f, t * 0.7f);
    }

    private void drawChest(Canvas c, float t) {
        float cx = cellCenterX(chestCell), cy = cellCenterY(chestCell);
        Art.drawChest(c, cx, cy, TILE * 0.85f, t, chestHits, 3);
        Ui.textC(c, "GRIMORIO", cx, cy + TILE * 0.85f, 26, true, 0xFF5D4037);
    }

    private void drawHud(Canvas c, float t) {
        // barra superior
        Ui.panel(c, 30, 30, 1020, 100, 0xFFF7EBC9, 0xFF8D6E63);
        Art.drawCoin(c, 92, 80, 32);
        Ui.textCO(c, Ui.fmt(g.save.gold), 148, 94, 42, true, 0xFF9C6A00);
        Art.drawGem(c, 430, 80, 28);
        Ui.textCO(c, String.valueOf(g.save.gems), 474, 94, 42, true, 0xFF00838F);
        Ui.textCO(c, "+" + Ui.fmt(g.save.incomePerSec()) + "/s", 705, 92, 34, true, 0xFF2E7D32);
        Ui.textCO(c, "Oleada " + g.save.wave, 948, 70, 42, true, 0xFF3A2E20);
        int alive = heroes.size() + queue.size();
        Ui.textCO(c, alive > 0 ? "Héroes: " + alive : "Camino despejado", 948, 116, 26, false, 0xFF6D4C41);
        btnSpeed.draw(c, t);
        btnPause.draw(c, t);

        // barra inferior
        Ui.panel(c, 30, 1660, 760, 230, 0xFFF7EBC9, 0xFF8D6E63);
        Ui.textCO(c, editMode ? "Modo edición: toca un monstruo" : "Toca un monstruo del banquillo", 410, 1704, 28, true, 0xFF6D4C41);
        ArrayList<Save.Mon> bench = g.save.bench();
        int n = Math.min(bench.size(), 4);
        for (int i = 0; i < n; i++) {
            Save.Mon m = bench.get(i);
            float bx = 80 + i * 170, by = 1720;
            boolean sel = placingBench == i;
            Paint bp = Ui.paint(sel ? 0xFFFFF59D : 0xFFEAD9AC);
            c.drawRoundRect(new RectF(bx, by, bx + 140, by + 140), 22, 22, bp);
            Paint st = Ui.paint(0xFF8D6E63);
            st.setStyle(Paint.Style.STROKE);
            st.setStrokeWidth(6);
            c.drawRoundRect(new RectF(bx, by, bx + 140, by + 140), 22, 22, st);
            Art.drawMon(c, Defs.mon(m.id).art, bx + 70, by + 74, 78, t, false, 0);
            Ui.textCO(c, "Nv " + m.lvl, bx + 70, by + 134, 24, true, 0xFF3A2E20);
        }
        if (bench.size() > 4) Ui.textCO(c, "+" + (bench.size() - 4) + " más", 690, 1800, 26, true, 0xFF6D4C41);
        Ui.textCO(c, "Huecos: " + g.save.placedCount() + "/" + g.save.maxSlots(g.save.wave), 410, 1868, 26, true, 0xFF2E7D32);

        btnEdit.draw(c, t);
        btnSummon.draw(c, t);
        btnShop.draw(c, t);
    }

    private void drawInfoPanel(Canvas c) {
        RMon m = placed.get(selected);
        Ui.panel(c, 540, 1150, 520, 500, 0xFFF5E6C8, 0xFF8D6E63);
        Ui.textC(c, m.d.name, 800, 1210, 44, true, Defs.RARITY_COL[m.d.rarity]);
        Ui.textC(c, "Nivel " + m.src.lvl + "  ·  " + Defs.RARITY[m.d.rarity], 800, 1260, 30, false, 0xFF3A2E20);
        long dmg = (long) (Defs.monDmg(m.d, m.src.lvl) * g.save.dmgMult());
        Ui.textC(c, "Daño: " + dmg + "   Vida: " + (long) m.maxHp, 800, 1310, 30, false, 0xFF3A2E20);
        Ui.textC(c, "Alcance: " + m.d.range + "   Oro: +" + Ui.fmt((long) (m.d.oro * (1 + 0.12 * (m.src.lvl - 1)) * g.save.goldMult())) + "/s", 800, 1355, 26, false, 0xFF2E7D32);
        long cost = Defs.monUpCost(m.d, m.src.lvl);
        bUp.label = cost <= g.save.gold ? "Mejorar" : "Mejorar";
        bUp.sub = Ui.fmt(cost) + " oro";
        bUp.enabled = g.save.gold >= cost;
        bUp.draw(c, 0);
        bSell.draw(c, 0);
        bMove.draw(c, 0);
        bClose.draw(c, 0);
        // barra de experiencia
        float w = 440, x = 580, y = 1395;
        c.drawRoundRect(new RectF(x, y, x + w, y + 24), 12, 12, Ui.paint(0xFFD7CCC8));
        float frac = Math.min(1, m.src.xp / (float) Defs.xpNeed(m.src.lvl));
        c.drawRoundRect(new RectF(x, y, x + w * frac, y + 24), 12, 12, Ui.paint(0xFFAB47BC));
        Ui.textC(c, "XP " + m.src.xp + "/" + Defs.xpNeed(m.src.lvl), 800, 1452, 22, true, 0xFF6A1B9A);
    }

    private void drawPausePanel(Canvas c) {
        Paint dim = Ui.paint(0x99000000);
        c.drawRect(0, 0, 1080, 1920, dim);
        Ui.panel(c, 240, 700, 600, 640, 0xFFF5E6C8, 0xFF8D6E63);
        Ui.textC(c, "PAUSA", 540, 790, 60, true, 0xFF3A2E20);
        Ui.textC(c, "Oleada " + g.save.wave + "  ·  Récord: " + g.save.bestWave, 540, 850, 30, false, 0xFF6D4C41);
        bResume.draw(c, 0);
        bMenu.draw(c, 0);
        Ui.textC(c, "El oro se sigue ganando\nmientras estés fuera", 540, 1290, 26, false, 0xFF2E7D32);
    }
}
