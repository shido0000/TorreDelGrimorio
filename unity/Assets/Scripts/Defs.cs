using System.Collections.Generic;

/// <summary>Definiciones de monstruos, héroes invasores, oleadas y economía.</summary>
public static class Defs {
    public static readonly string[] Rarity = { "Común", "Raro", "Épico", "Legendario" };
    public static readonly int[] RarityCol = { 0xFF8BC34A, 0xFF42A5F5, 0xFFAB47BC, 0xFFFFB300 };
    public static readonly int[] RarityEdge = { 0xFF33691E, 0xFF1565C0, 0xFF6A1B9A, 0xFFE65100 };
    public static readonly string[] Dif = { "Normal", "Difícil", "Pesadilla" };

    public class MDef {
        public string id, name, desc;
        public int rarity, art;
        public float dmg, hp, atkSpd, range, oro;
    }

    public class HDef {
        public string id, name;
        public int art;
        public float hp, dmg, atkSpd, speed, range, reward;
        public bool boss;
    }

    public static readonly MDef[] Monsters = {
        Mk("baba", "Baba", "Un limo adorable que escupe ácido pegajoso.", 0, 0, 8, 60, 1.0f, 1.6f, 0.5f),
        Mk("colmillo", "Colmillo", "Lobo de las sombras, rápido y fiel.", 0, 1, 12, 45, 0.8f, 1.4f, 0.6f),
        Mk("ponzona", "Ponzoña", "Avispa invocada que nunca pierde el humor.", 0, 2, 10, 42, 0.6f, 1.5f, 0.7f),
        Mk("mocha", "Mocha", "La gata del maestro Orin. Ronronea tormentas.", 1, 3, 22, 90, 0.7f, 1.8f, 1.5f),
        Mk("chispa", "Chispa", "Murciélago eléctrico de las cuevas del norte.", 1, 4, 18, 70, 0.5f, 1.7f, 1.4f),
        Mk("pengui", "Pengüi", "Caballero pingüino del hielo eterno del norte.", 1, 9, 24, 100, 0.65f, 1.9f, 1.6f),
        Mk("rocoso", "Rocoso", "Gólem de la montaña. Lento pero imparable.", 2, 5, 45, 350, 1.6f, 1.3f, 3.5f),
        Mk("ojo", "Ojo Místico", "Vigía ancestral que ve todo el camino.", 2, 6, 60, 80, 1.2f, 3.2f, 3.2f),
        Mk("espectro", "Espectro", "Alma del grimorio que susurra maldiciones.", 2, 10, 65, 95, 1.0f, 3.0f, 3.4f),
        Mk("draco", "Draco", "Dragón chibi heredero del Grimorio.", 3, 7, 110, 300, 0.9f, 2.4f, 8),
        Mk("fenix", "Fénix", "Ave renacida del último hechizo de Orin.", 3, 8, 90, 260, 0.7f, 2.8f, 7),
        Mk("quimera", "Quimera", "Bestia de tres cabezas, guardiana del portal final.", 3, 11, 125, 330, 0.85f, 2.3f, 9),
    };

    static MDef Mk(string id, string name, string desc, int rarity, int art,
                   float dmg, float hp, float atkSpd, float range, float oro) {
        return new MDef { id = id, name = name, desc = desc, rarity = rarity, art = art,
            dmg = dmg, hp = hp, atkSpd = atkSpd, range = range, oro = oro };
    }

    public static MDef Mon(string id) {
        foreach (var m in Monsters) if (m.id == id) return m;
        return Monsters[0];
    }

    public static readonly HDef[] Heroes = {
        Hk("escudero", "Escudero", 0, 60, 6, 1.0f, 1.1f, 0.9f, 12, false),
        Hk("caballero", "Caballero", 1, 160, 12, 1.1f, 0.9f, 0.9f, 25, false),
        Hk("arquera", "Arquera", 2, 90, 10, 0.9f, 1.0f, 2.4f, 30, false),
        Hk("mago", "Mago Renegado", 3, 80, 26, 1.6f, 0.8f, 2.6f, 40, false),
        Hk("guardian", "Guardián de Hierro", 4, 600, 20, 1.5f, 0.55f, 0.9f, 80, false),
        Hk("rey", "Rey Héroe Aldric", 5, 3000, 80, 1.2f, 0.5f, 1.1f, 500, true),
    };

    static HDef Hk(string id, string name, int art, float hp, float dmg,
                   float atkSpd, float speed, float range, float reward, bool boss) {
        return new HDef { id = id, name = name, art = art, hp = hp, dmg = dmg,
            atkSpd = atkSpd, speed = speed, range = range, reward = reward, boss = boss };
    }

    public static HDef Hero(string id) {
        foreach (var h in Heroes) if (h.id == id) return h;
        return Heroes[0];
    }

    public static float WaveScale(int n) => Mathf.Pow(1.16f, n - 1) * (1 + n * 0.03f);

    public static float DifEnemigo(int d) => d == 1 ? 1.5f : d == 2 ? 2.3f : 1f;
    public static float DifRecompensa(int d) => d == 1 ? 1.4f : d == 2 ? 2f : 1f;

    public static List<string> BuildWave(int n) {
        var outp = new List<string>();
        if (n % 10 == 0) {
            outp.Add("rey");
            int e = 3 + n / 15;
            for (int i = 0; i < e; i++) outp.Add(i % 2 == 0 ? "caballero" : "arquera");
            return outp;
        }
        int count = Mathf.Min(4 + n / 2, 14);
        for (int i = 0; i < count; i++) {
            string pick = "escudero";
            if (n >= 3 && i % 3 == 1) pick = "caballero";
            if (n >= 5 && i % 4 == 2) pick = "arquera";
            if (n >= 8 && i % 5 == 3) pick = "mago";
            if (n >= 12 && i % 6 == 4) pick = "guardian";
            outp.Add(pick);
        }
        return outp;
    }

    public static long KillReward(HDef h, int n) => (long)(h.reward * (1 + n * 0.18f));

    public static long MonUpCost(MDef d, int lvl) => (long)(30 * Mathf.Pow(lvl, 1.6f) * (1 + d.rarity * 0.7f));

    public static float MonDmg(MDef d, int lvl) => d.dmg * (1 + 0.12f * (lvl - 1));
    public static float MonHp(MDef d, int lvl) => d.hp * (1 + 0.12f * (lvl - 1));

    public static long XpNeed(int lvl) => (long)(10 * Mathf.Pow(lvl, 1.5f));

    public static string RollMonster(System.Random rnd) {
        int r = rnd.Next(100);
        int rar = r < 60 ? 0 : r < 87 ? 1 : r < 97 ? 2 : 3;
        var pool = new List<string>();
        foreach (var m in Monsters) if (m.rarity == rar) pool.Add(m.id);
        return pool[rnd.Next(pool.Count)];
    }
}
