package com.grimorio.torre;

/** Logros permanentes: se cumplen una sola vez y dan gemas. */
public final class Achievements {

    /** [título, descripción, tipo, meta, recompensa en gemas]
     *  tipo: 0 = oleada máxima, 1 = héroes derrotados, 2 = invocaciones,
     *        3 = oro total ganado, 4 = monstruos distintos */
    public static final Object[][] LIST = {
            {"Primeras sangre", "Derrota 10 h\u00e9roes", 1, 10, 20},
            {"Cazador veterano", "Derrota 100 h\u00e9roes", 1, 100, 50},
            {"Cazador legendario", "Derrota 500 h\u00e9roes", 1, 500, 150},
            {"Aprendiz valiente", "Alcanza la oleada 10", 0, 10, 20},
            {"Defensor del Grimorio", "Alcanza la oleada 25", 0, 25, 40},
            {"Campe\u00f3n del reino", "Alcanza la oleada 50", 0, 50, 100},
            {"Leyenda viviente", "Alcanza la oleada 100", 0, 100, 300},
            {"Coleccionista", "Re\u00fane 4 monstruos distintos", 4, 4, 30},
            {"Maestro invocador", "Re\u00fane los 12 monstruos", 4, 12, 150},
            {"Puerta al portal", "Invoca 25 veces", 2, 25, 40},
            {"Devoto del portal", "Invoca 100 veces", 2, 100, 100},
            {"Erario lleno", "Gana 100.000 de oro en total", 3, 100000, 40},
            {"Millonario", "Gana 1.000.000 de oro en total", 3, 1000000, 100},
    };

    public static int progress(Save s, int i) {
        int type = (Integer) LIST[i][2];
        switch (type) {
            case 0: return Math.max(s.bestWave, s.wave);
            case 1: return (int) Math.min(Integer.MAX_VALUE, s.totalKills);
            case 2: return (int) Math.min(Integer.MAX_VALUE, s.totalSummons);
            case 3: return (int) Math.min(Integer.MAX_VALUE, s.totalGold / 1000);
            default: return distinct(s);
        }
    }

    private static int distinct(Save s) {
        int n = 0;
        for (int k = 0; k < Defs.MONSTERS.length; k++) {
            String id = Defs.MONSTERS[k].id;
            for (Save.Mon m : s.mons) if (m.id.equals(id)) { n++; break; }
        }
        return n;
    }

    public static int goal(int i) {
        int type = (Integer) LIST[i][2];
        int meta = (Integer) LIST[i][3];
        return type == 3 ? meta / 1000 : meta;
    }

    public static boolean done(Save s, int i) {
        return progress(s, i) >= goal(i);
    }

    public static boolean claimed(Save s, int i) {
        return (s.achClaimed & (1L << i)) != 0;
    }

    public static boolean claim(Save s, int i) {
        if (!done(s, i) || claimed(s, i)) return false;
        s.achClaimed |= (1L << i);
        s.addGems((Integer) LIST[i][4]);
        return true;
    }

    /** Hay alg\u00fan logro completado sin reclamar. */
    public static boolean anyClaimable(Save s) {
        for (int i = 0; i < LIST.length; i++)
            if (done(s, i) && !claimed(s, i)) return true;
        return false;
    }
}
