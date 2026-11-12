package com.grimorio.torre;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/** Estado global del juego con persistencia en SharedPreferences (JSON). */
public final class Save {

    public static class Mon {
        public String id;
        public int lvl = 1, xp = 0;
        public int cell = -1; // índice de celda del tablero; -1 = banquillo

        public JSONObject toJson() {
            try {
                JSONObject o = new JSONObject();
                o.put("id", id);
                o.put("lvl", lvl);
                o.put("xp", xp);
                o.put("cell", cell);
                return o;
            } catch (Exception e) { return new JSONObject(); }
        }

        public static Mon fromJson(JSONObject o) {
            Mon m = new Mon();
            m.id = o.optString("id", "baba");
            m.lvl = o.optInt("lvl", 1);
            m.xp = o.optInt("xp", 0);
            m.cell = o.optInt("cell", -1);
            return m;
        }
    }

    public long gold = 250;
    public int gems = 300;
    public int wave = 1;
    public int bestWave = 0;
    public ArrayList<Mon> mons = new ArrayList<Mon>();

    // Mejoras compradas con gemas (cada nivel +10 %)
    public int upOro, upDan, upVid;
    // Compras eternas con dinero real (+25 % permanente)
    public boolean iapOro, iapDan;

    // Suscripción / prueba de fundador
    public long install;          // fecha de instalación (ms)
    public long lastSeen;         // último momento jugando
    public boolean subActive;
    public String subSku;
    public long lastDailyClaim;   // reclamación diaria de gemas del Pase

    // Historia: capítulos vistos
    public int story = 0;

    // Audio
    public boolean soundOn = true;
    public boolean musicOn = true;

    // Tutorial (0 = pendiente, 1..5 pasos, -1 completado)
    public int tutorialStep = 0;

    // Misiones diarias
    public long questDay = -1;
    public int qKills, qWaves, qSummons;
    public int qClaimed;

    // Logros permanentes (estadísticas totales)
    public long totalKills, totalSummons, totalGold;
    public long achClaimed;

    private static final String PREF = "grimorio";
    private static final String KEY = "save";

    public void initDefaults() {
        if (mons.isEmpty()) {
            Mon m1 = new Mon(); m1.id = "baba"; m1.cell = -1;
            Mon m2 = new Mon(); m2.id = "baba"; m2.cell = -1;
            mons.add(m1);
            mons.add(m2);
        }
    }

    public void load(Context ctx) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            String s = sp.getString(KEY, null);
            if (s == null) {
                install = System.currentTimeMillis();
                initDefaults();
                return;
            }
            JSONObject o = new JSONObject(s);
            gold = o.getLong("gold");
            gems = o.getInt("gems");
            wave = o.getInt("wave");
            bestWave = o.optInt("bestWave", 0);
            upOro = o.optInt("upOro", 0);
            upDan = o.optInt("upDan", 0);
            upVid = o.optInt("upVid", 0);
            iapOro = o.optBoolean("iapOro", false);
            iapDan = o.optBoolean("iapDan", false);
            install = o.optLong("install", System.currentTimeMillis());
            lastSeen = o.optLong("lastSeen", 0);
            subActive = o.optBoolean("subActive", false);
            subSku = o.optString("subSku", "");
            lastDailyClaim = o.optLong("lastDailyClaim", 0);
            story = o.optInt("story", 0);
            soundOn = o.optBoolean("soundOn", true);
            musicOn = o.optBoolean("musicOn", true);
            tutorialStep = o.optInt("tutorialStep", 0);
            questDay = o.optLong("questDay", -1);
            qKills = o.optInt("qKills", 0);
            qWaves = o.optInt("qWaves", 0);
            qSummons = o.optInt("qSummons", 0);
            qClaimed = o.optInt("qClaimed", 0);
            totalKills = o.optLong("totalKills", 0);
            totalSummons = o.optLong("totalSummons", 0);
            totalGold = o.optLong("totalGold", 0);
            achClaimed = o.optLong("achClaimed", 0);
            mons.clear();
            JSONArray arr = o.optJSONArray("mons");
            if (arr != null) for (int i = 0; i < arr.length(); i++) mons.add(Mon.fromJson(arr.getJSONObject(i)));
            initDefaults();
            Quests.checkDay(this);
        } catch (Exception e) {
            install = System.currentTimeMillis();
            initDefaults();
        }
    }

    public void save(Context ctx) {
        try {
            JSONObject o = new JSONObject();
            o.put("gold", gold);
            o.put("gems", gems);
            o.put("wave", wave);
            o.put("bestWave", bestWave);
            o.put("upOro", upOro);
            o.put("upDan", upDan);
            o.put("upVid", upVid);
            o.put("iapOro", iapOro);
            o.put("iapDan", iapDan);
            o.put("install", install);
            o.put("lastSeen", lastSeen);
            o.put("subActive", subActive);
            o.put("subSku", subSku == null ? "" : subSku);
            o.put("lastDailyClaim", lastDailyClaim);
            o.put("story", story);
            o.put("soundOn", soundOn);
            o.put("musicOn", musicOn);
            o.put("tutorialStep", tutorialStep);
            o.put("questDay", questDay);
            o.put("qKills", qKills);
            o.put("qWaves", qWaves);
            o.put("qSummons", qSummons);
            o.put("qClaimed", qClaimed);
            o.put("totalKills", totalKills);
            o.put("totalSummons", totalSummons);
            o.put("totalGold", totalGold);
            o.put("achClaimed", achClaimed);
            JSONArray arr = new JSONArray();
            for (Mon m : mons) arr.put(m.toJson());
            o.put("mons", arr);
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                    .edit().putString(KEY, o.toString()).apply();
        } catch (Exception ignored) { }
    }

    // ----- Bonificadores -----

    public double goldMult() {
        double m = 1 + 0.10 * upOro;
        if (iapOro) m += 0.25;
        if (paseActivo()) m += 0.5;
        return m;
    }

    public double dmgMult() {
        double m = 1 + 0.10 * upDan;
        if (iapDan) m += 0.25;
        return m;
    }

    public double hpMult() {
        return 1 + 0.10 * upVid;
    }

    /** Días transcurridos desde la instalación. */
    public long diasDesdeInstalacion() {
        return (System.currentTimeMillis() - install) / 86400000L;
    }

    /** Días restantes de la prueba de fundador (7 días desde la instalación). */
    public long diasPruebaRestantes() {
        long d = 7 - diasDesdeInstalacion();
        return Math.max(0, d);
    }

    /** El Pase del Invocador está disponible (suscripción real) tras 7 días de la instalación. */
    public boolean paseDisponible() {
        return diasDesdeInstalacion() >= 7;
    }

    /** Beneficios del Pase activos: suscripción pagada o prueba de fundador (7 días). */
    public boolean paseActivo() {
        return subActive || diasDesdeInstalacion() < 7;
    }

    public boolean pasePagado() {
        return subActive;
    }

    // ----- Economía idle -----

    public long incomePerSec() {
        double s = 0;
        for (Mon m : mons) if (m.cell >= 0) {
            Defs.MDef d = Defs.mon(m.id);
            s += d.oro * (1 + 0.12 * (m.lvl - 1));
        }
        return (long) (s * goldMult());
    }

    public int placedCount() {
        int n = 0;
        for (Mon m : mons) if (m.cell >= 0) n++;
        return n;
    }

    public int maxSlots(int waveN) {
        return Math.min(4 + waveN / 4, 12);
    }

    public ArrayList<Mon> bench() {
        ArrayList<Mon> b = new ArrayList<Mon>();
        for (Mon m : mons) if (m.cell < 0) b.add(m);
        return b;
    }

    public boolean addMonster(String id) {
        if (mons.size() >= 40) return false;
        Mon m = new Mon();
        m.id = id;
        mons.add(m);
        return true;
    }

    /** Devuelve true si ya existía (sube nivel), false si es nuevo. */
    public boolean addOrLevel(String id) {
        for (Mon m : mons) if (m.id.equals(id) && m.cell < 0 && m.lvl < 200) {
            m.lvl++;
            return true;
        }
        addMonster(id);
        return false;
    }

    public void addGems(int n) {
        if (pasePagado()) n = (int) (n * 1.5);
        gems += n;
    }
}
