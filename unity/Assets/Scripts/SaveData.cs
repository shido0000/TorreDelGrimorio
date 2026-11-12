using System;
using System.Collections.Generic;
using UnityEngine;

/// <summary>Guardado completo de la partida (JSON en PlayerPrefs), economía y lógica del Pase.</summary>
[Serializable]
public class SaveData {
    [Serializable]
    public class Mon {
        public string id;
        public int cell = -1;
        public int lvl = 1;
        public int xp;
    }

    public long gold, gems;
    public int wave = 1, bestWave;
    public int upOro, upDan, upVid;
    public bool iapOro, iapDan;
    public long install, lastSeen;
    public bool subActive;
    public string subSku = "";
    public long lastDailyClaim;
    public int story;
    public bool soundOn = true, musicOn = true;
    public int tutorialStep; // 0 pendiente, 1..5 pasos, -1 completado
    public long questDay = -1;
    public int qKills, qWaves, qSummons, qClaimed;
    public long totalKills, totalSummons, totalGold;
    public long achClaimed;
    public int difficulty;
    public List<Mon> mons = new List<Mon>();

    public static SaveData Load() {
        var s = new SaveData();
        try {
            string json = PlayerPrefs.GetString("save", null);
            if (json != null) JsonUtility.FromJsonOverwrite(json, s);
        } catch (Exception) { }
        if (s.install == 0) s.install = Now();
        if (s.mons.Count == 0) {
            s.mons.Add(new Mon { id = "baba", cell = -1 });
            s.mons.Add(new Mon { id = "baba", cell = -1 });
        }
        Quests.CheckDay(s);
        return s;
    }

    public void Persist() {
        lastSeen = Now();
        PlayerPrefs.SetString("save", JsonUtility.ToJson(this));
        PlayerPrefs.Save();
    }

    public static long Now() => (long)(DateTime.UtcNow - new DateTime(1970, 1, 1)).TotalMilliseconds;

    public float GoldMult() => (1 + upOro * 0.10f) * (iapOro ? 1.25f : 1f);
    public float DmgMult() => (1 + upDan * 0.10f) * (iapDan ? 1.25f : 1f);
    public float HpMult() => 1 + upVid * 0.10f;

    public long IncomePerSec() {
        double inc = 0;
        foreach (var m in mons) {
            if (m.cell < 0) continue;
            var d = Defs.Mon(m.id);
            inc += d.oro * (1 + 0.12 * (m.lvl - 1));
        }
        return (long)(inc * GoldMult());
    }

    public int MaxSlots(int waveN) => Mathf.Min(4 + waveN / 4, 12);
    public int PlacedCount() { int n = 0; foreach (var m in mons) if (m.cell >= 0) n++; return n; }

    public List<Mon> Bench() {
        var b = new List<Mon>();
        foreach (var m in mons) if (m.cell < 0) b.Add(m);
        return b;
    }

    public void AddGems(int n) { gems += n; }

    public bool AddOrLevel(string id) {
        foreach (var m in mons) if (m.id == id) { m.lvl++; return true; }
        mons.Add(new Mon { id = id, cell = -1, lvl = 1 });
        return false;
    }

    public int MonLevel(string id) {
        foreach (var m in mons) if (m.id == id) return m.lvl;
        return 1;
    }

    // ---------- Pase del Invocador (suscripción a los 7 días) ----------

    public int DiasPruebaRestantes() {
        long elapsed = Now() - install;
        int days = (int)(elapsed / 86400000L);
        return Mathf.Max(0, 7 - days);
    }

    public bool PaseDisponible() => DiasPruebaRestantes() <= 0;

    public bool PasePagado() => subActive;

    public bool PaseActivo() => !PaseDisponible() || subActive;
}
