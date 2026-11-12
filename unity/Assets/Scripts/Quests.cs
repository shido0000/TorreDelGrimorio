using System;

/// <summary>Misiones diarias: se reinician cada día, recompensan gemas.</summary>
public static class Quests {
    // [título, descripción, meta, recompensa]
    public static readonly object[][] List = {
        new object[] { "Cazador de héroes", "Derrota 20 héroes hoy", 20, 50 },
        new object[] { "Defensa firme", "Supera 3 oleadas hoy", 3, 30 },
        new object[] { "Portal activo", "Invoca 1 monstruo hoy", 1, 40 },
    };

    public static long Today() => SaveData.Now() / 86400000L;

    public static void CheckDay(SaveData s) {
        if (s.questDay != Today()) {
            s.questDay = Today();
            s.qKills = s.qWaves = s.qSummons = 0;
            s.qClaimed = 0;
        }
    }

    public static int Progress(SaveData s, int i) =>
        i == 0 ? s.qKills : i == 1 ? s.qWaves : s.qSummons;

    public static int Goal(int i) => (int)List[i][2];
    public static bool Done(SaveData s, int i) => Progress(s, i) >= Goal(i);
    public static bool Claimed(SaveData s, int i) => (s.qClaimed & (1 << i)) != 0;

    public static bool Claim(SaveData s, int i) {
        if (!Done(s, i) || Claimed(s, i)) return false;
        s.qClaimed |= 1 << i;
        s.AddGems((int)List[i][3]);
        return true;
    }
}

/// <summary>Logros permanentes: se cumplen una sola vez y dan gemas.</summary>
public static class Achievements {
    // tipo: 0 oleada, 1 kills, 2 invocaciones, 3 oro total (en miles), 4 monstruos distintos
    public static readonly object[][] List = {
        new object[] { "Primeras sangre", "Derrota 10 héroes", 1, 10, 20 },
        new object[] { "Cazador veterano", "Derrota 100 héroes", 1, 100, 50 },
        new object[] { "Cazador legendario", "Derrota 500 héroes", 1, 500, 150 },
        new object[] { "Aprendiz valiente", "Alcanza la oleada 10", 0, 10, 20 },
        new object[] { "Defensor del Grimorio", "Alcanza la oleada 25", 0, 25, 40 },
        new object[] { "Campeón del reino", "Alcanza la oleada 50", 0, 50, 100 },
        new object[] { "Leyenda viviente", "Alcanza la oleada 100", 0, 100, 300 },
        new object[] { "Coleccionista", "Reúne 4 monstruos distintos", 4, 4, 30 },
        new object[] { "Maestro invocador", "Reúne los 12 monstruos", 4, 12, 150 },
        new object[] { "Puerta al portal", "Invoca 25 veces", 2, 25, 40 },
        new object[] { "Devoto del portal", "Invoca 100 veces", 2, 100, 100 },
        new object[] { "Erario lleno", "Gana 100.000 de oro en total", 3, 100, 40 },
        new object[] { "Millonario", "Gana 1.000.000 de oro en total", 3, 1000, 100 },
    };

    static int Distinct(SaveData s) {
        int n = 0;
        foreach (var d in Defs.Monsters)
            foreach (var m in s.mons) if (m.id == d.id) { n++; break; }
        return n;
    }

    public static int Progress(SaveData s, int i) {
        int type = (int)List[i][2], meta = (int)List[i][3];
        switch (type) {
            case 0: return Mathf.Max(s.bestWave, s.wave);
            case 1: return (int)Math.Min(int.MaxValue, s.totalKills);
            case 2: return (int)Math.Min(int.MaxValue, s.totalSummons);
            case 3: return (int)Math.Min(int.MaxValue, s.totalGold / 1000);
            default: return Distinct(s);
        }
    }

    public static int Goal(int i) => (int)List[i][3];
    public static bool Done(SaveData s, int i) => Progress(s, i) >= Goal(i);
    public static bool Claimed(SaveData s, int i) => (s.achClaimed & (1L << i)) != 0;

    public static bool Claim(SaveData s, int i) {
        if (!Done(s, i) || Claimed(s, i)) return false;
        s.achClaimed |= 1L << i;
        s.AddGems((int)List[i][4]);
        return true;
    }

    public static bool AnyClaimable(SaveData s) {
        for (int i = 0; i < List.Length; i++)
            if (Done(s, i) && !Claimed(s, i)) return true;
        return false;
    }
}
