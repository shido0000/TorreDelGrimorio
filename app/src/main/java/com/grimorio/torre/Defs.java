package com.grimorio.torre;

import java.util.ArrayList;

/** Definiciones de monstruos, héroes invasores y generación de oleadas. */
public final class Defs {

    public static final String[] RARITY = {"Común", "Raro", "Épico", "Legendario"};
    public static final int[] RARITY_COL = {0xFF8BC34A, 0xFF42A5F5, 0xFFAB47BC, 0xFFFFB300};
    public static final int[] RARITY_EDGE = {0xFF33691E, 0xFF1565C0, 0xFF6A1B9A, 0xFFE65100};

    public static class MDef {
        public String id, name, desc;
        public int rarity, art;
        public float dmg, hp, atkSpd, range, oro;
    }

    public static class HDef {
        public String id, name;
        public int art;
        public float hp, dmg, atkSpd, speed, range, reward;
        public boolean boss;
    }

    public static final MDef[] MONSTERS = {
            mk("baba", "Baba", "Un limo adorable que escupe ácido pegajoso.", 0, 0, 8, 60, 1.0f, 1.6f, 0.5f),
            mk("colmillo", "Colmillo", "Lobo de las sombras, rápido y fiel.", 0, 1, 12, 45, 0.8f, 1.4f, 0.6f),
            mk("ponzona", "Ponzoña", "Avispa invocada que nunca pierde el humor.", 0, 2, 10, 42, 0.6f, 1.5f, 0.7f),
            mk("mocha", "Mocha", "La gata del maestro Orin. Ronronea tormentas.", 1, 3, 22, 90, 0.7f, 1.8f, 1.5f),
            mk("chispa", "Chispa", "Murciélago eléctrico de las cuevas del norte.", 1, 4, 18, 70, 0.5f, 1.7f, 1.4f),
            mk("rocoso", "Rocoso", "Gólem de la montaña. Lento pero imparable.", 2, 5, 45, 350, 1.6f, 1.3f, 3.5f),
            mk("ojo", "Ojo Místico", "Vigía ancestral que ve todo el camino.", 2, 6, 60, 80, 1.2f, 3.2f, 3.2f),
            mk("draco", "Draco", "Dragón chibi heredero del Grimorio.", 3, 7, 110, 300, 0.9f, 2.4f, 8),
            mk("fenix", "Fénix", "Ave renacida del último hechizo de Orin.", 3, 8, 90, 260, 0.7f, 2.8f, 7),
    };

    private static MDef mk(String id, String name, String desc, int rarity, int art,
                           float dmg, float hp, float atkSpd, float range, float oro) {
        MDef m = new MDef();
        m.id = id; m.name = name; m.desc = desc; m.rarity = rarity; m.art = art;
        m.dmg = dmg; m.hp = hp; m.atkSpd = atkSpd; m.range = range; m.oro = oro;
        return m;
    }

    public static MDef mon(String id) {
        for (MDef m : MONSTERS) if (m.id.equals(id)) return m;
        return MONSTERS[0];
    }

    public static final HDef[] HEROES = {
            hk("escudero", "Escudero", 0, 60, 6, 1.0f, 1.1f, 0.9f, 12, false),
            hk("caballero", "Caballero", 1, 160, 12, 1.1f, 0.9f, 0.9f, 25, false),
            hk("arquera", "Arquera", 2, 90, 10, 0.9f, 1.0f, 2.4f, 30, false),
            hk("mago", "Mago Renegado", 3, 80, 26, 1.6f, 0.8f, 2.6f, 40, false),
            hk("guardian", "Guardián de Hierro", 4, 600, 20, 1.5f, 0.55f, 0.9f, 80, false),
            hk("rey", "Rey Héroe Aldric", 5, 3000, 80, 1.2f, 0.5f, 1.1f, 500, true),
    };

    private static HDef hk(String id, String name, int art, float hp, float dmg,
                           float atkSpd, float speed, float range, float reward, boolean boss) {
        HDef h = new HDef();
        h.id = id; h.name = name; h.art = art; h.hp = hp; h.dmg = dmg;
        h.atkSpd = atkSpd; h.speed = speed; h.range = range; h.reward = reward; h.boss = boss;
        return h;
    }

    public static HDef hero(String id) {
        for (HDef h : HEROES) if (h.id.equals(id)) return h;
        return HEROES[0];
    }

    /** Escala de dificultad de la oleada n (n >= 1). */
    public static float waveScale(int n) {
        return (float) Math.pow(1.16, n - 1) * (1 + n * 0.03f);
    }

    /** Lista de héroes que atacan en la oleada n. */
    public static ArrayList<String> buildWave(int n) {
        ArrayList<String> out = new ArrayList<String>();
        if (n % 10 == 0) {
            out.add("rey");
            int e = 3 + n / 15;
            for (int i = 0; i < e; i++) out.add(i % 2 == 0 ? "caballero" : "arquera");
            return out;
        }
        int count = Math.min(4 + n / 2, 14);
        for (int i = 0; i < count; i++) {
            String pick = "escudero";
            if (n >= 3 && i % 3 == 1) pick = "caballero";
            if (n >= 5 && i % 4 == 2) pick = "arquera";
            if (n >= 8 && i % 5 == 3) pick = "mago";
            if (n >= 12 && i % 6 == 4) pick = "guardian";
            out.add(pick);
        }
        return out;
    }

    /** Recompensa en oro por derrotar a un héroe en la oleada n. */
    public static long killReward(HDef h, int n) {
        return (long) (h.reward * (1 + n * 0.18f));
    }

    public static long monUpCost(MDef d, int lvl) {
        double r = 30 * Math.pow(lvl, 1.6) * (1 + d.rarity * 0.7);
        return (long) r;
    }

    public static float monDmg(MDef d, int lvl) {
        return d.dmg * (1 + 0.12f * (lvl - 1));
    }

    public static float monHp(MDef d, int lvl) {
        return d.hp * (1 + 0.12f * (lvl - 1));
    }

    public static long xpNeed(int lvl) {
        return (long) (10 * Math.pow(lvl, 1.5));
    }

    /** Invocación: elige un monstruo según rareza (60/27/10/3). */
    public static String rollMonster(java.util.Random rnd) {
        int r = rnd.nextInt(100);
        int rar = r < 60 ? 0 : (r < 87 ? 1 : (r < 97 ? 2 : 3));
        ArrayList<String> pool = new ArrayList<String>();
        for (MDef m : MONSTERS) if (m.rarity == rar) pool.add(m.id);
        return pool.get(rnd.nextInt(pool.size()));
    }
}
