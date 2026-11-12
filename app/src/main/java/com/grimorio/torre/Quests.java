package com.grimorio.torre;

/** Misiones diarias: se reinician cada día, recompensan gemas. */
public final class Quests {

    /** [título, descripción, meta, recompensa en gemas] */
    public static final Object[][] QUESTS = {
            {"Cazador de héroes", "Derrota 20 héroes hoy", 20, 50},
            {"Defensa firme", "Supera 3 oleadas hoy", 3, 30},
            {"Portal activo", "Invoca 1 monstruo hoy", 1, 40},
    };

    public static long today() {
        return System.currentTimeMillis() / 86400000L;
    }

    /** Reinicia los contadores si cambió el día. */
    public static void checkDay(Save s) {
        if (s.questDay != today()) {
            s.questDay = today();
            s.qKills = 0;
            s.qWaves = 0;
            s.qSummons = 0;
            s.qClaimed = 0;
        }
    }

    public static int progress(Save s, int i) {
        if (i == 0) return s.qKills;
        if (i == 1) return s.qWaves;
        return s.qSummons;
    }

    public static int goal(int i) {
        return (Integer) QUESTS[i][2];
    }

    public static boolean done(Save s, int i) {
        return progress(s, i) >= goal(i);
    }

    public static boolean claimed(Save s, int i) {
        return (s.qClaimed & (1 << i)) != 0;
    }

    public static boolean claim(Save s, int i) {
        if (!done(s, i) || claimed(s, i)) return false;
        s.qClaimed |= (1 << i);
        s.addGems((Integer) QUESTS[i][3]);
        return true;
    }
}
