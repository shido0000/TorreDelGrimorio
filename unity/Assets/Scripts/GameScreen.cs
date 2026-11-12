using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

/// <summary>
/// Escena de juego: defensa idle de la Torre del Grimorio.
/// Tablero 9x13 (celda 108 px, origen 54,250), camino fijo, tutorial interactivo,
/// oleadas, banquillo, mejoras, pausa y contadores de misiones.
/// </summary>
public class GameScreen : MonoBehaviour, IBackHandler {
    public const int COLS = 9, ROWS = 13, TILE = 108, X0 = 54, Y0 = 250;
    static readonly int[] PathCells = {
        0,2, 1,2, 2,2, 3,2, 4,2, 4,3, 4,4, 4,5, 3,5, 2,5, 1,5,
        1,6, 1,7, 1,8, 2,8, 3,8, 4,8, 5,8, 6,8, 6,9, 6,10, 6,11, 7,11, 8,11
    };

    class Hero {
        public Defs.HDef d;
        public float dist, hp, maxHp, cd, scale = 1;
        public bool escaped;
        public Image img, hpBg, hpFill;
        public float x, y;
    }

    class RMon {
        public SaveData.Mon src;
        public Defs.MDef d;
        public float hp, maxHp, cd, deadT;
        public Image img, hpBg, hpFill;
        public float x, y;
    }

    class Proj {
        public float x, y, dmg;
        public Hero target;
        public Image img;
        readonly Queue<Vector2> trail = new Queue<Vector2>();
        public Image[] ghosts;
        int col;

        public Proj(Color c) { col = 0; ghosts = new Image[3]; Tint = c; }
        public Color Tint;
    }

    class Fx {
        public Image img;
        public float vx, vy, life, maxLife, size;
    }

    class FText {
        public Text t;
        public float life;
    }

    GameObject _board, _fxLayer, _overlay;
    readonly List<Hero> _heroes = new List<Hero>();
    readonly List<RMon> _placed = new List<RMon>();
    readonly List<Proj> _projs = new List<Proj>();
    readonly List<Fx> _fx = new List<Fx>();
    readonly List<FText> _fts = new List<FText>();
    readonly List<string> _queue = new List<string>();
    readonly float[] _segX = new float[25], _segY = new float[25], _cum = new float[25];
    int _segs;
    float _pathLen, _spawnT, _countdown, _bannerT, _saveT, _speed = 1;
    int _chestHits, _state; // 0 combate, 1 cuenta atrás siguiente, 2 fallo
    string _banner;
    int _chestCell;

    Text _goldTxt, _gemTxt, _incomeTxt, _waveTxt, _aliveTxt, _bannerTxt, _hintTxt, _slotsTxt;
    Button _btnSpeed, _btnEdit;
    GameObject _pausePanel, _infoPanel, _tutPanel;
    Text _tutL1, _tutL2;
    Image _tutDim, _tutHi;
    Button _btnSkip;
    readonly Image[] _validCells = new Image[COLS * ROWS];
    readonly Button[] _benchBtns = new Button[4];
    readonly Image[] _benchHi = new Image[4];
    int _placingBench = -1, _pickedUp = -1, _selected = -1;
    bool _editMode, _paused;
    int _tut, _placedAtTut;
    Image _chestImg;

    void Start() {
        _board = new GameObject("Board", typeof(RectTransform));
        _board.transform.SetParent(gameObject.transform, false);
        UiKit.Stretch((RectTransform)_board.transform);

        _tut = App.Save.tutorialStep;
        if (App.Save.bestWave == 0 && _tut == 0) _tut = 1;

        BuildPath();
        RebuildPlaced();
        BuildHud();
        BuildBench();

        int ch = StoryData.ChapterForWave(App.Save.wave, App.Save.story);
        if (ch >= 0 && App.Save.bestWave == 0) {
            App.Save.story = ch + 1;
            App.Save.Persist();
            App.ShowStory(ch);
            return;
        }
        StartWave(App.Save.wave);
        App.Audio.ApplyMusic();
    }

    // ---------- camino ----------

    void BuildPath() {
        _segX[0] = X0 - TILE * 1.5f;
        _segY[0] = Y0 + PathCells[1] * TILE + TILE / 2f;
        for (int i = 0; i < PathCells.Length / 2; i++) {
            _segX[i + 1] = X0 + PathCells[i * 2] * TILE + TILE / 2f;
            _segY[i + 1] = Y0 + PathCells[i * 2 + 1] * TILE + TILE / 2f;
        }
        _segs = PathCells.Length / 2;
        _chestCell = PathCells[(_segs - 1) * 2 + 1] * COLS + PathCells[(_segs - 1) * 2];
        _cum[0] = 0;
        for (int i = 0; i < _segs; i++)
            _cum[i + 1] = _cum[i] + Vector2.Distance(new Vector2(_segX[i], _segY[i]), new Vector2(_segX[i + 1], _segY[i + 1]));
        _pathLen = _cum[_segs];
    }

    bool IsPathCell(int col, int row) {
        for (int i = 0; i < _segs; i++)
            if (PathCells[i * 2] == col && PathCells[i * 2 + 1] == row) return true;
        return false;
    }

    bool IsValidCell(int col, int row) {
        if (IsPathCell(col, row)) return false;
        int[] dc = { -1, 1, 0, 0 }, dr = { 0, 0, -1, 1 };
        for (int k = 0; k < 4; k++) {
            int c = col + dc[k], r = row + dr[k];
            if (c >= 0 && c < COLS && r >= 0 && r < ROWS && IsPathCell(c, r)) return true;
        }
        return false;
    }

    void PosAt(float d, out float ox, out float oy) {
        if (d <= 0) { ox = _segX[0]; oy = _segY[0]; return; }
        if (d >= _pathLen) { ox = _segX[_segs]; oy = _segY[_segs]; return; }
        int i = 1;
        while (i < _cum.Length && _cum[i] < d) i++;
        float t = (d - _cum[i - 1]) / Vector2.Distance(
            new Vector2(_segX[i - 1], _segY[i - 1]), new Vector2(_segX[i], _segY[i]));
        ox = _segX[i - 1] + (_segX[i] - _segX[i - 1]) * t;
        oy = _segY[i - 1] + (_segY[i] - _segY[i - 1]) * t;
    }

    // ---------- HUD ----------

    void BuildHud() {
        UiKit.Image(_board, "bg" + (((App.Save.wave - 1) / 10) % 4), 0, 0, 1080, 1920);

        var top = UiKit.Panel(_board, 30, 30, 1020, 100, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        top.raycastTarget = false;
        UiKit.Image(top.gameObject, "coin", 32, 20, 60, 60);
        _goldTxt = UiKit.Text(top.gameObject, "", 105, 20, 42, UiKit.C(0x9C6A00), true);
        UiKit.Image(top.gameObject, "gem", 370, 25, 52, 52);
        _gemTxt = UiKit.Text(top.gameObject, "", 435, 20, 42, UiKit.C(0x00838F), true);
        _incomeTxt = UiKit.Text(top.gameObject, "", 570, 30, 32, UiKit.C(0x2E7D32), true);
        _waveTxt = UiKit.Text(top.gameObject, "", 850, 18, 40, UiKit.C(0x3A2E20), true);
        _aliveTxt = UiKit.Text(top.gameObject, "", 850, 62, 26, UiKit.C(0x6D4C41));

        _btnSpeed = UiKit.Btn(_board, 640, 150, 130, 78, "x1", UiKit.C(0x42A5F5), UiKit.C(0x1565C0), () => {
            _speed = _speed == 1 ? 2 : _speed == 2 ? 4 : 1;
            _btnSpeed.GetComponentInChildren<Text>().text = "x" + (int)_speed;
        });
        UiKit.Btn(_board, 790, 150, 130, 78, "II", UiKit.C(0x78909C), UiKit.C(0x455A64), () => ShowPause(true));

        var bottom = UiKit.Panel(_board, 30, 1660, 760, 230, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        bottom.raycastTarget = false;
        _hintTxt = UiKit.Text(bottom.gameObject, "Toca un monstruo del banquillo", 30, 14, 28, UiKit.C(0x6D4C41), true);
        _slotsTxt = UiKit.Text(bottom.gameObject, "", 30, 185, 26, UiKit.C(0x2E7D32), true);

        for (int i = 0; i < 4; i++) {
            int idx = i;
            var slotGo = new GameObject("bench" + i, typeof(RectTransform), typeof(Image), typeof(Button));
            slotGo.transform.SetParent(_board.transform, false);
            UiKit.RT(slotGo, 80 + i * 170, 1720, 140, 140);
            var im = slotGo.GetComponent<Image>();
            im.color = new Color(0.92f, 0.85f, 0.67f);
            var b = slotGo.GetComponent<Button>();
            b.onClick.AddListener(() => TapBench(idx));
            _benchBtns[i] = b;
            _benchHi[i] = UiKit.Panel(slotGo, -6, -6, 152, 152, new Color(1f, 0.96f, 0.55f, 0.25f), new Color(0, 0, 0, 0));
            _benchHi[i].raycastTarget = false;
            _benchHi[i].gameObject.SetActive(false);
        }

        _btnEdit = UiKit.Btn(_board, 60, 1712, 200, 92, "Editar", UiKit.C(0x8D6E63), UiKit.C(0x5D4037), () => {
            _editMode = !_editMode;
            _selected = -1;
            _btnEdit.image.color = _editMode ? UiKit.C(0x66BB6A) : UiKit.C(0x8D6E63);
            _btnEdit.GetComponentInChildren<Text>().text = _editMode ? "Listo" : "Editar";
            _hintTxt.text = _editMode ? "Modo edición: toca un monstruo" : "Toca un monstruo del banquillo";
        });
        UiKit.Btn(_board, 300, 1712, 230, 92, "Invocar", UiKit.C(0xAB47BC), UiKit.C(0x6A1B9A), () => {
            App.Save.Persist();
            App.Show<SummonScreen>();
        });
        UiKit.Btn(_board, 570, 1712, 230, 92, "Tienda", UiKit.C(0xFFB300), UiKit.C(0xE65100), () => {
            App.Save.Persist();
            App.Show<ShopScreen>();
        });

        // cofre (el Grimorio)
        _chestImg = UiKit.Image(_board, "chest", 0, 0, 100, 100);

        _bannerTxt = UiKit.TextC(_board, "", 540, 430, 44, UiKit.C(0xFFC93C), true, true);
        _bannerTxt.gameObject.SetActive(false);
    }

    void BuildBench() {
        var bench = App.Save.Bench();
        for (int i = 0; i < 4; i++) {
            var slot = _benchBtns[i].GetComponent<Image>().transform;
            foreach (Transform child in slot)
                if (child.name == "mon" || child.name == "lvl") Destroy(child.gameObject);
            if (i >= bench.Count) { _benchBtns[i].interactable = false; _benchBtns[i].image.color = new Color(0.8f, 0.78f, 0.72f, 0.4f); continue; }
            _benchBtns[i].interactable = true;
            _benchBtns[i].image.color = new Color(0.92f, 0.85f, 0.67f);
            var m = bench[i];
            var mon = UiKit.Image(_benchBtns[i].gameObject, "mon" + Defs.Mon(m.id).art, 30, 18, 80, 80);
            mon.name = "mon";
            mon.raycastTarget = false;
            var lvl = UiKit.TextC(_benchBtns[i].gameObject, "Nv " + m.lvl, 70, 100, 24, UiKit.C(0x3A2E20), true);
            lvl.name = "lvl";
            _benchHi[i].gameObject.SetActive(_placingBench == i);
        }
        _slotsTxt.text = "Huecos: " + App.Save.PlacedCount() + "/" + App.Save.MaxSlots(App.Save.wave);
    }

    // ---------- entidades ----------

    Image MakeSprite(GameObject parent, string art, float size) {
        var go = new GameObject("e_" + art, typeof(RectTransform), typeof(Image));
        go.transform.SetParent(parent.transform, false);
        var rt = (RectTransform)go.transform;
        rt.sizeDelta = new Vector2(size * 1.5f, size * 1.5f);
        var im = go.GetComponent<Image>();
        im.sprite = UiKit.Art(art);
        im.preserveAspect = true;
        im.raycastTarget = false;
        return im;
    }

    void RebuildPlaced() {
        foreach (var m in _placed) { Destroy(m.img.gameObject); if (m.hpBg != null) Destroy(m.hpBg.gameObject); }
        _placed.Clear();
        foreach (var src in App.Save.mons) {
            if (src.cell < 0) continue;
            var r = new RMon { src = src, d = Defs.Mon(src.id) };
            r.maxHp = Defs.MonHp(r.d, src.lvl) * App.Save.HpMult();
            r.hp = r.maxHp;
            r.x = X0 + (src.cell % COLS) * TILE + TILE / 2f;
            r.y = Y0 + (src.cell / COLS) * TILE + TILE / 2f;
            r.img = MakeSprite(_board, "mon" + r.d.art, TILE * 0.85f);
            r.hpBg = new GameObject("hpb", typeof(RectTransform), typeof(Image)).GetComponent<Image>();
            r.hpBg.transform.SetParent(_board.transform, false);
            r.hpBg.rectTransform.sizeDelta = new Vector2(TILE * 0.6f, 10);
            r.hpBg.color = new Color32(51, 50, 58, 255);
            r.hpBg.raycastTarget = false;
            r.hpFill = new GameObject("hpf", typeof(RectTransform), typeof(Image)).GetComponent<Image>();
            r.hpFill.transform.SetParent(r.hpBg.transform, false);
            r.hpFill.rectTransform.anchorMin = Vector2.zero;
            r.hpFill.rectTransform.anchorMax = Vector2.zero;
            r.hpFill.rectTransform.pivot = new Vector2(0, 0.5f);
            r.hpFill.rectTransform.sizeDelta = new Vector2(TILE * 0.6f, 10);
            r.hpFill.color = new Color32(102, 187, 106, 255);
            r.hpFill.raycastTarget = false;
            _placed.Add(r);
        }
    }

    void StartWave(int n) {
        App.Save.wave = n;
        if (n > App.Save.bestWave) App.Save.bestWave = n;
        _queue.Clear();
        _queue.AddRange(Defs.BuildWave(n));
        _spawnT = 0.6f;
        _chestHits = 0;
        _state = 0;
        foreach (var h in _heroes) { Destroy(h.img.gameObject); Destroy(h.hpBg != null ? h.hpBg.gameObject : null); }
        _heroes.Clear();
        int ch = StoryData.ChapterForWave(n, App.Save.story);
        if (ch >= 0) {
            App.Save.story = ch + 1;
            App.Save.Persist();
            App.ShowStory(ch);
        }
    }

    void SpawnHero(string id) {
        var h = new Hero { d = Defs.Hero(id) };
        float s = Defs.WaveScale(App.Save.wave);
        h.maxHp = h.d.hp * s * Defs.DifEnemigo(App.Save.difficulty);
        h.hp = h.maxHp;
        if (h.d.boss) h.scale = 1.5f;
        h.img = MakeSprite(_board, "hero" + h.d.art, TILE * 0.72f * h.scale);
        h.hpBg = new GameObject("hpb", typeof(RectTransform), typeof(Image)).GetComponent<Image>();
        h.hpBg.transform.SetParent(_board.transform, false);
        h.hpBg.rectTransform.sizeDelta = new Vector2(TILE * 0.6f, 10);
        h.hpBg.color = new Color32(51, 50, 58, 255);
        h.hpBg.raycastTarget = false;
        h.hpFill = new GameObject("hpf", typeof(RectTransform), typeof(Image)).GetComponent<Image>();
        h.hpFill.transform.SetParent(h.hpBg.transform, false);
        h.hpFill.rectTransform.anchorMin = Vector2.zero;
        h.hpFill.rectTransform.anchorMax = Vector2.zero;
        h.hpFill.rectTransform.pivot = new Vector2(0, 0.5f);
        h.hpFill.rectTransform.sizeDelta = new Vector2(TILE * 0.6f, 10);
        h.hpFill.color = new Color32(239, 83, 80, 255);
        h.hpFill.raycastTarget = false;
        _heroes.Add(h);
        if (h.d.boss) {
            _banner = "¡" + h.d.name + " ataca!";
            _bannerT = 2.5f;
        }
    }

    // ---------- bucle ----------

    void Update() {
        float dt = _paused ? 0 : Time.deltaTime * _speed;
        if (_bannerT > 0) _bannerT -= Time.deltaTime;

        if (dt > 0) {
            long inc = (long)(App.Save.IncomePerSec() * dt);
            App.Save.gold += inc;
            App.Save.totalGold += inc;
        }

        if (dt > 0 && _state == 0) {
            _spawnT -= dt;
            if (_tut >= 1 && _tut <= 4) _spawnT = Mathf.Max(_spawnT, 1f);
            if (_spawnT <= 0 && _queue.Count > 0) { SpawnHero(_queue[0]); _queue.RemoveAt(0); _spawnT = 0.9f; }
            if (_queue.Count == 0 && _heroes.Count == 0) WaveCleared();
        } else if (dt > 0 && (_state == 1 || _state == 2)) {
            _countdown -= dt;
            if (_countdown <= 0) StartWave(_state == 1 ? App.Save.wave + 1 : App.Save.wave);
        }

        if (dt > 0) { UpdateHeroes(dt); UpdateMonsters(dt); UpdateProjs(dt); }
        UpdateFx(Time.deltaTime);

        if (_tut == 1 && _placingBench >= 0) { _tut = 2; _placedAtTut = _placed.Count; }
        if (_tut == 2 && _placed.Count > _placedAtTut) _tut = 3;
        if (_tut == 4 && _speed > 1) _tut = 5;

        _saveT += Time.deltaTime;
        if (_saveT > 10) { _saveT = 0; App.Save.Persist(); }

        RefreshHud();
        HandleBoardInput();
        UpdateTutorial();
    }

    void RefreshHud() {
        _goldTxt.text = UiKit.Fmt(App.Save.gold);
        _gemTxt.text = App.Save.gems.ToString();
        _incomeTxt.text = "+" + UiKit.Fmt(App.Save.IncomePerSec()) + "/s";
        _waveTxt.text = "Oleada " + App.Save.wave;
        int alive = _heroes.Count + _queue.Count;
        _aliveTxt.text = alive > 0 ? "Héroes: " + alive : "Camino despejado";
        if (_bannerT > 0 && _banner != null) {
            _bannerTxt.gameObject.SetActive(true);
            _bannerTxt.text = _banner;
        } else _bannerTxt.gameObject.SetActive(false);

        float chestX = X0 + (_chestCell % COLS) * TILE + TILE / 2f;
        float chestY = Y0 + (_chestCell / COLS) * TILE + TILE / 2f;
        _chestImg.rectTransform.anchoredPosition = new Vector2(chestX, -(chestY - 10));

        for (int i = 0; i < _placed.Count; i++) {
            var m = _placed[i];
            m.img.rectTransform.anchoredPosition = new Vector2(m.x, -(m.y - TILE * 0.3f));
            bool dead = m.deadT > 0;
            m.img.color = dead ? new Color(0.6f, 0.6f, 0.6f, 0.6f) : Color.white;
            bool showHp = !dead && m.hp < m.maxHp;
            m.hpBg.gameObject.SetActive(showHp);
            if (showHp) {
                m.hpBg.rectTransform.anchoredPosition = new Vector2(m.x - TILE * 0.3f, -(m.y - TILE * 0.62f));
                m.hpFill.rectTransform.sizeDelta = new Vector2(TILE * 0.6f * (m.hp / m.maxHp), 10);
            }
        }
        for (int i = 0; i < _heroes.Count; i++) {
            var h = _heroes[i];
            h.img.rectTransform.anchoredPosition = new Vector2(h.x, -(h.y - TILE * 0.25f));
            bool showHp = h.hp < h.maxHp;
            h.hpBg.gameObject.SetActive(showHp);
            if (showHp) {
                h.hpBg.rectTransform.anchoredPosition = new Vector2(h.x - TILE * 0.3f, -(h.y - TILE * 0.55f * h.scale));
                h.hpFill.rectTransform.sizeDelta = new Vector2(TILE * 0.6f * (h.hp / h.maxHp), 10);
            }
        }
        if (_placingBench >= 0 || _pickedUp >= 0) ShowValidCells();
        else for (int i = 0; i < _validCells.Length; i++) if (_validCells[i] != null) _validCells[i].gameObject.SetActive(false);
    }

    void ShowValidCells() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cell = row * COLS + col;
                bool show = IsValidCell(col, row) && !Occupied(cell);
                if (show && _validCells[cell] == null) {
                    var im = UiKit.Panel(_board, X0 + col * TILE + 6, Y0 + row * TILE + 6, TILE - 12, TILE - 12,
                        new Color(0.4f, 0.73f, 0.42f, 0.35f), new Color(0, 0, 0, 0));
                    im.raycastTarget = false;
                    im.name = "valid";
                    _validCells[cell] = im;
                }
                if (_validCells[cell] != null) _validCells[cell].gameObject.SetActive(show);
            }
        }
    }

    bool Occupied(int cell) {
        foreach (var m in App.Save.mons) if (m.cell == cell) return true;
        return false;
    }

    void UpdateHeroes(float dt) {
        for (int i = _heroes.Count - 1; i >= 0; i--) {
            var h = _heroes[i];
            RMon tgt = null;
            float best = float.MaxValue;
            float range = h.d.range * TILE;
            foreach (var m in _placed) {
                if (m.deadT > 0) continue;
                float dd = Vector2.SqrMagnitude(new Vector2(m.x - h.x, m.y - h.y));
                if (dd < best && dd <= range * range) { best = dd; tgt = m; }
            }
            if (tgt != null) {
                h.cd -= dt;
                if (h.cd <= 0) {
                    h.cd = h.d.atkSpd;
                    float dmg = h.d.dmg * Defs.WaveScale(App.Save.wave) * Defs.DifEnemigo(App.Save.difficulty);
                    tgt.hp -= dmg;
                    App.Audio.Sfx("hit");
                    SpawnFx(tgt.x, tgt.y - 20, new Color32(120, 144, 156, 255), 3, 130);
                    if (tgt.hp <= 0) {
                        tgt.deadT = 12;
                        tgt.hp = 0;
                        App.Audio.Sfx("die");
                        SpawnFx(tgt.x, tgt.y, new Color32(176, 190, 197, 255), 10, 220);
                        AddFt(tgt.x, tgt.y, "¡Caído!", UiKit.C(0xEF5350));
                    }
                }
            } else {
                h.dist += h.d.speed * TILE * dt;
                PosAt(h.dist, out h.x, out h.y);
                if (h.dist >= _pathLen) {
                    long steal = Mathf.Max(10, (long)(App.Save.gold * 0.08f));
                    App.Save.gold = Mathf.Max(0, App.Save.gold - steal);
                    AddFt(_segX[_segs], _segY[_segs] - 80, "-" + UiKit.Fmt(steal) + " oro", UiKit.C(0xEF5350));
                    _chestHits++;
                    h.escaped = true;
                    _banner = "¡Un héroe robó oro del cofre!";
                    _bannerT = 1.8f;
                    App.Audio.Sfx("steal");
                    SpawnFx(h.x, h.y - 40, UiKit.C(0xEF5350), 12, 260);
                    if (_chestHits >= 3) { WaveFailed(); return; }
                }
            }
        }
        for (int i = _heroes.Count - 1; i >= 0; i--)
            if (_heroes[i].escaped) { Destroy(_heroes[i].img.gameObject); Destroy(_heroes[i].hpBg.gameObject); _heroes.RemoveAt(i); }
    }

    void UpdateMonsters(float dt) {
        foreach (var m in _placed) {
            if (m.deadT > 0) {
                m.deadT -= dt;
                if (m.deadT <= 0) m.hp = m.maxHp;
                continue;
            }
            m.cd -= dt;
            if (m.cd > 0) continue;
            Hero tgt = null;
            float best = float.MaxValue;
            float range = m.d.range * TILE;
            foreach (var h in _heroes) {
                float dd = Vector2.SqrMagnitude(new Vector2(h.x - m.x, h.y - m.y));
                if (dd < best && dd <= range * range) { best = dd; tgt = h; }
            }
            if (tgt != null) {
                m.cd = m.d.atkSpd;
                var p = new Proj(ProjectColor(m.d.art)) { x = m.x, y = m.y - 20, dmg = Defs.MonDmg(m.d, m.src.lvl) * App.Save.DmgMult(), target = tgt };
                p.img = new GameObject("proj", typeof(RectTransform), typeof(Image)).GetComponent<Image>();
                p.img.transform.SetParent(_fxLayer != null ? _fxLayer.transform : _board.transform, false);
                p.img.sprite = UiKit.Circle();
                p.img.color = ProjectColor(m.d.art);
                p.img.rectTransform.sizeDelta = new Vector2(24, 24);
                p.img.raycastTarget = false;
                p.ghosts = new Image[3];
                for (int g = 0; g < 3; g++) {
                    p.ghosts[g] = new GameObject("trail", typeof(RectTransform), typeof(Image)).GetComponent<Image>();
                    p.ghosts[g].transform.SetParent(p.img.transform.parent, false);
                    p.ghosts[g].sprite = UiKit.Circle();
                    p.ghosts[g].color = new Color(p.img.color.r, p.img.color.g, p.img.color.b, 0.3f - g * 0.08f);
                    p.ghosts[g].rectTransform.sizeDelta = new Vector2(18 - g * 4, 18 - g * 4);
                    p.ghosts[g].raycastTarget = false;
                }
                _projs.Add(p);
                App.Audio.Sfx("shoot");
            }
        }
    }

    Color ProjectColor(int art) {
        switch (art) {
            case 0: return UiKit.C(0x7ED957);
            case 1: return UiKit.C(0xBDBDBD);
            case 2: return UiKit.C(0xFFD54F);
            case 3: return UiKit.C(0xE53935);
            case 4: return UiKit.C(0xB39DDB);
            case 5: return UiKit.C(0x9E9E9E);
            case 6: return UiKit.C(0xCE93D8);
            case 7: return UiKit.C(0x66BB6A);
            default: return UiKit.C(0xFF8A65);
        }
    }

    void UpdateProjs(float dt) {
        for (int i = _projs.Count - 1; i >= 0; i--) {
            var p = _projs[i];
            var h = p.target;
            if (h.escaped || h.hp <= 0 || !_heroes.Contains(h)) {
                Destroy(p.img.gameObject);
                foreach (var g in p.ghosts) if (g != null) Destroy(g.gameObject);
                _projs.RemoveAt(i);
                continue;
            }
            var dir = new Vector2(h.x - p.x, h.y - p.y);
            float step = 950 * dt;
            if (dir.magnitude <= step + 12) {
                h.hp -= p.dmg;
                App.Audio.Sfx("hit");
                SpawnFx(h.x, h.y - 30, p.img.color, 4, 160);
                if (h.hp <= 0) {
                    long rw = (long)(Defs.KillReward(h.d, App.Save.wave) * App.Save.GoldMult() * Defs.DifRecompensa(App.Save.difficulty));
                    App.Save.gold += rw;
                    App.Save.totalGold += rw;
                    App.Save.qKills++;
                    App.Save.totalKills++;
                    App.Audio.Sfx("coin");
                    SpawnFx(h.x, h.y - 30, UiKit.C(0xFFC93C), 12, 260);
                    AddFt(h.x, h.y - 70, "+" + UiKit.Fmt(rw), UiKit.C(0xFFC93C));
                    foreach (var m in _placed) {
                        if (m.src.cell < 0) continue;
                        float dd = Vector2.SqrMagnitude(new Vector2(m.x - h.x, m.y - h.y));
                        if (dd <= (m.d.range * TILE) * (m.d.range * TILE)) {
                            m.src.xp += h.d.boss ? 25 : 5;
                            if (m.src.xp >= Defs.XpNeed(m.src.lvl)) {
                                m.src.xp -= (int)Defs.XpNeed(m.src.lvl);
                                m.src.lvl++;
                                m.maxHp = Defs.MonHp(m.d, m.src.lvl) * App.Save.HpMult();
                                m.hp = m.maxHp;
                                App.Audio.Sfx("levelup");
                                AddFt(m.x, m.y - 90, "¡Nivel " + m.src.lvl + "!", UiKit.C(0x66BB6A));
                            }
                            break;
                        }
                    }
                    Destroy(h.img.gameObject);
                    Destroy(h.hpBg.gameObject);
                    _heroes.Remove(h);
                }
                Destroy(p.img.gameObject);
                foreach (var g in p.ghosts) if (g != null) Destroy(g.gameObject);
                _projs.RemoveAt(i);
                continue;
            }
            p.x += dir.x / dir.magnitude * step;
            p.y += dir.y / dir.magnitude * step;
            p.img.rectTransform.anchoredPosition = new Vector2(p.x, -p.y);
            for (int g = 0; g < 3; g++) {
                float f = (g + 1) / 4f;
                p.ghosts[g].rectTransform.anchoredPosition =
                    new Vector2(p.x - dir.normalized.x * 30 * f, -(p.y - dir.normalized.y * 30 * f));
            }
        }
    }

    void SpawnFx(float x, float y, Color col, int n, float spd) {
        for (int i = 0; i < n; i++) {
            var go = new GameObject("fx", typeof(RectTransform), typeof(Image));
            go.transform.SetParent(_board.transform, false);
            var im = go.GetComponent<Image>();
            im.sprite = UiKit.Circle();
            im.color = col;
            im.raycastTarget = false;
            float ang = Random.value * Mathf.PI * 2;
            float v = spd * Random.Range(0.4f, 1.2f);
            _fx.Add(new Fx {
                img = im, vx = Mathf.Cos(ang) * v, vy = Mathf.Sin(ang) * v - spd * 0.3f,
                life = Random.Range(0.4f, 0.85f), maxLife = 0.85f, size = Random.Range(8, 18)
            });
            im.rectTransform.sizeDelta = new Vector2(14, 14);
            im.rectTransform.anchoredPosition = new Vector2(x, -y);
        }
    }

    void UpdateFx(float dt) {
        for (int i = _fx.Count - 1; i >= 0; i--) {
            var p = _fx[i];
            p.life -= dt;
            if (p.life <= 0) { Destroy(p.img.gameObject); _fx.RemoveAt(i); continue; }
            p.vy += 700 * dt;
            var pos = p.img.rectTransform.anchoredPosition;
            pos.x += p.vx * dt;
            pos.y -= p.vy * dt;
            p.img.rectTransform.anchoredPosition = pos;
            float a = p.life / p.maxLife;
            p.img.color = new Color(p.img.color.r, p.img.color.g, p.img.color.b, a);
        }
        for (int i = _fts.Count - 1; i >= 0; i--) {
            var f = _fts[i];
            f.life -= dt;
            if (f.life <= 0) { Destroy(f.t.gameObject); _fts.RemoveAt(i); continue; }
            var pos = f.t.rectTransform.anchoredPosition;
            pos.y += 60 * dt;
            f.t.rectTransform.anchoredPosition = pos;
        }
    }

    void AddFt(float x, float y, string msg, Color col) {
        var t = UiKit.TextC(_board, msg, x, y, 40, col, true, true);
        _fts.Add(new FText { t = t, life = 1.1f });
    }

    void WaveCleared() {
        int n = App.Save.wave;
        App.Save.qWaves++;
        long bonus = (long)((60 + 25 * n) * App.Save.GoldMult() * Defs.DifRecompensa(App.Save.difficulty));
        App.Save.gold += bonus;
        App.Save.totalGold += bonus;
        int gems = n % 10 == 0 ? 5 : n % 5 == 0 ? 2 : 0;
        if (gems > 0) App.Save.AddGems(gems);
        _banner = "¡Oleada " + n + " superada! +" + UiKit.Fmt(bonus) + " oro" + (gems > 0 ? "  +" + gems + " gemas" : "");
        _bannerT = 3;
        _state = 1;
        _countdown = 4;
        App.Audio.Sfx("wave");
        App.Save.Persist();
    }

    void WaveFailed() {
        long lost = (long)(App.Save.gold * 0.15f);
        App.Save.gold -= lost;
        _banner = "¡El cofre cayó! Pierdes " + UiKit.Fmt(lost) + " de oro";
        _bannerT = 3;
        _state = 2;
        _countdown = 3.5f;
        foreach (var h in _heroes) { Destroy(h.img.gameObject); if (h.hpBg != null) Destroy(h.hpBg.gameObject); }
        _heroes.Clear();
        App.Audio.Sfx("steal");
        App.Save.Persist();
    }

    // ---------- entrada ----------

    void HandleBoardInput() {
        if (_paused || _selected >= 0) return;
        if (_tut > 0 && _tut != 1 && _tut != 2) return;
        if (!Input.GetMouseButtonDown(0)) return;
        if (UnityEngine.EventSystems.EventSystem.current != null &&
            UnityEngine.EventSystems.EventSystem.current.IsPointerOverGameObject()) return;
        var pos = ToVirtual(Input.mousePosition);
        float x = pos.x, y = pos.y;

        int col = (int)((x - X0) / TILE), row = (int)((y - Y0) / TILE);
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return;
        int cell = row * COLS + col;
        if (_placingBench >= 0) {
            if (IsValidCell(col, row) && !Occupied(cell) && App.Save.PlacedCount() < App.Save.MaxSlots(App.Save.wave)) {
                App.Save.Bench()[_placingBench].cell = cell;
                _placingBench = -1;
                RebuildPlaced();
                BuildBench();
                App.Save.Persist();
            }
        } else if (_pickedUp >= 0) {
            if (IsValidCell(col, row) && !Occupied(cell)) {
                _placed[_pickedUp].src.cell = cell;
                _pickedUp = -1;
                RebuildPlaced();
                App.Save.Persist();
            }
        } else if (_editMode) {
            for (int i = 0; i < _placed.Count; i++) {
                var m = _placed[i];
                if (Mathf.Abs(m.x - x) < TILE / 2 && Mathf.Abs(m.y - y) < TILE / 2) {
                    ShowInfoPanel(i);
                    return;
                }
            }
        }
    }

    void TapBench(int idx) {
        if (_tut > 0 && _tut != 1 && _tut != 2) return;
        var bench = App.Save.Bench();
        if (idx >= bench.Count) return;
        if (App.Save.PlacedCount() >= App.Save.MaxSlots(App.Save.wave)) {
            _banner = "¡No hay huecos! Sube de oleada o vende un monstruo";
            _bannerT = 2.5f;
            return;
        }
        _placingBench = idx;
    }

    Vector2 ToVirtual(Vector3 screen) {
        float scale = App.Root.scaleFactor;
        return new Vector2(screen.x / scale, Screen.height / scale - screen.y / scale);
    }

    // ---------- paneles ----------

    void ShowInfoPanel(int idx) {
        _selected = idx;
        var m = _placed[idx];
        _infoPanel = new GameObject("Info", typeof(RectTransform));
        _infoPanel.transform.SetParent(gameObject.transform, false);
        UiKit.Stretch((RectTransform)_infoPanel.transform);
        var dim = UiKit.Panel(_infoPanel, 0, 0, 1080, 1920, new Color(0, 0, 0, 0.45f), new Color(0, 0, 0, 0));
        dim.raycastTarget = true;
        var panel = UiKit.Panel(_infoPanel, 540, 1150, 520, 500, UiKit.C(0xF5E6C8), UiKit.C(0x8D6E63));
        UiKit.TextC(panel.gameObject, m.d.name, 260, 30, 44, UiKit.C(Defs.RarityCol[m.d.rarity]), true, true);
        UiKit.TextC(panel.gameObject, "Nivel " + m.src.lvl + " · " + Defs.Rarity[m.d.rarity], 260, 85, 30, UiKit.C(0x3A2E20));
        UiKit.TextC(panel.gameObject, "Daño: " + (long)Defs.MonDmg(m.d, m.src.lvl) + "  Vida: " + (long)m.maxHp, 260, 130, 30, UiKit.C(0x3A2E20));
        UiKit.TextC(panel.gameObject, "Alcance: " + m.d.range + "  Oro: +" + UiKit.Fmt((long)(m.d.oro * (1 + 0.12 * (m.src.lvl - 1)) * App.Save.GoldMult())) + "/s", 260, 172, 26, UiKit.C(0x2E7D32));
        long cost = Defs.MonUpCost(m.d, m.src.lvl);
        UiKit.Image(panel.gameObject, "mon" + m.d.art, 30, 210, 140, 140);
        UiKit.Btn(panel.gameObject, 200, 250, 150, 90, "Mejorar", UiKit.C(0x66BB6A), UiKit.C(0x2E7D32), () => {
            if (App.Save.gold < cost) { App.Toast("¡Oro insuficiente!"); return; }
            App.Save.gold -= cost;
            m.src.lvl++;
            m.maxHp = Defs.MonHp(m.d, m.src.lvl) * App.Save.HpMult();
            m.hp = m.maxHp;
            App.Save.Persist();
            CloseInfo();
            RebuildPlaced();
        }, UiKit.Fmt(cost) + " oro");
        UiKit.Btn(panel.gameObject, 370, 250, 120, 90, "Vender", UiKit.C(0xEF5350), UiKit.C(0xB71C1C), () => {
            long refund = (long)(Defs.MonUpCost(m.d, m.src.lvl) * 0.4f);
            App.Save.gold += refund;
            m.src.cell = -1;
            App.Save.Persist();
            CloseInfo();
            RebuildPlaced();
            BuildBench();
        });
        UiKit.Btn(panel.gameObject, 200, 360, 150, 90, "Mover", UiKit.C(0x42A5F5), UiKit.C(0x1565C0), () => {
            _pickedUp = _selected;
            CloseInfo();
        });
        UiKit.Btn(panel.gameObject, 370, 360, 120, 90, "Cerrar", UiKit.C(0x90A4AE), UiKit.C(0x455A64), CloseInfo);
    }

    void CloseInfo() {
        if (_infoPanel != null) Destroy(_infoPanel);
        _infoPanel = null;
        _selected = -1;
    }

    void ShowPause(bool on) {
        _paused = on;
        if (!on) { if (_pausePanel != null) Destroy(_pausePanel); _pausePanel = null; return; }
        _pausePanel = new GameObject("Pause", typeof(RectTransform));
        _pausePanel.transform.SetParent(gameObject.transform, false);
        UiKit.Stretch((RectTransform)_pausePanel.transform);
        UiKit.Panel(_pausePanel, 0, 0, 1080, 1920, new Color(0, 0, 0, 0.6f), new Color(0, 0, 0, 0)).raycastTarget = true;
        var panel = UiKit.Panel(_pausePanel, 240, 700, 600, 620, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
        UiKit.TextC(panel.gameObject, "PAUSA", 300, 40, 60, UiKit.C(0x3A2E20), true);
        UiKit.TextC(panel.gameObject, "Oleada " + App.Save.wave + " · Récord: " + App.Save.bestWave +
            "\nDificultad: " + Defs.Dif[App.Save.difficulty], 300, 120, 30, UiKit.C(0x6D4C41));
        UiKit.Btn(panel.gameObject, 100, 240, 400, 110, "Reanudar", UiKit.C(0x66BB6A), UiKit.C(0x2E7D32), () => ShowPause(false));
        UiKit.Btn(panel.gameObject, 100, 370, 400, 110, "Menú principal", UiKit.C(0xEF5350), UiKit.C(0xB71C1C), () => {
            App.Save.Persist();
            App.Show<MenuScreen>();
        });
        UiKit.Btn(panel.gameObject, 100, 500, 195, 90, "Sonido: " + (App.Save.soundOn ? "SÍ" : "NO"),
            UiKit.C(0x78909C), UiKit.C(0x455A64), () => {
                App.Save.soundOn = !App.Save.soundOn;
                App.Save.Persist();
                ShowPause(false); ShowPause(true);
            });
        UiKit.Btn(panel.gameObject, 305, 500, 195, 90, "Música: " + (App.Save.musicOn ? "SÍ" : "NO"),
            UiKit.C(0x78909C), UiKit.C(0x455A64), () => {
                App.Save.musicOn = !App.Save.musicOn;
                App.Save.Persist();
                App.Audio.ApplyMusic();
                ShowPause(false); ShowPause(true);
            });
    }

    // ---------- tutorial ----------

    void UpdateTutorial() {
        bool active = _tut > 0;
        if (active && _tutPanel == null) {
            _tutPanel = new GameObject("Tut", typeof(RectTransform));
            _tutPanel.transform.SetParent(gameObject.transform, false);
            UiKit.Stretch((RectTransform)_tutPanel.transform);
            _tutDim = UiKit.Panel(_tutPanel, 0, 0, 1080, 1920, new Color(0, 0, 0, 0.53f), new Color(0, 0, 0, 0));
            _tutDim.raycastTarget = true;
            var trigger = _tutPanel.AddComponent<TutTap>();
            trigger.Screen = this;
            var panel = UiKit.Panel(_tutPanel, 70, 260, 940, 170, UiKit.C(0xF7EBC9), UiKit.C(0x8D6E63));
            _tutL1 = UiKit.TextC(panel.gameObject, "", 470, 30, 34, UiKit.C(0x3A2E20), true);
            _tutL2 = UiKit.TextC(panel.gameObject, "", 470, 90, 34, UiKit.C(0x2E7D32), true);
            _tutHi = UiKit.Panel(_tutPanel, 0, 0, 100, 100, new Color(1f, 0.92f, 0.23f, 0.16f), UiKit.C(0xFFEB3B));
            _tutHi.raycastTarget = false;
            UiKit.Btn(_tutPanel, 820, 1740, 220, 90, "Saltar >", UiKit.C(0xEF5350), UiKit.C(0xB71C1C), FinishTutorial);
        } else if (!active && _tutPanel != null) {
            Destroy(_tutPanel);
            _tutPanel = null;
        }
        if (!active) return;

        string l1 = "", l2 = "";
        Vector2 hi = Vector2.zero, hiSize = new Vector2(200, 120);
        if (_tut == 1) {
            l1 = "¡Bienvenido, Invocador!";
            l2 = "Toca un monstruo del banquillo para elegirlo";
            hi = new Vector2(80, 1720); hiSize = new Vector2(660, 152);
        } else if (_tut == 2) {
            l1 = "¡Buena elección!";
            l2 = "Toca una celda verde junto al camino";
            hi = new Vector2(X0, Y0); hiSize = new Vector2(COLS * TILE, ROWS * TILE);
        } else if (_tut == 3) {
            l1 = "¡Perfecto! Tus monstruos defienden el Grimorio";
            l2 = "Toca la pantalla para continuar";
            hi = new Vector2(X0 + (_chestCell % COLS) * TILE - 20, Y0 + (_chestCell / COLS) * TILE - 20);
            hiSize = new Vector2(TILE + 40, TILE + 40);
        } else if (_tut == 4) {
            l1 = "¿Muy lento? Acelera la batalla";
            l2 = "Toca el botón de velocidad (x2 / x4)";
            hi = new Vector2(630, 142); hiSize = new Vector2(150, 94);
        } else if (_tut == 5) {
            l1 = "¡Ya sabes jugar! Gasta gemas para";
            l2 = "invocar monstruos más fuertes. Toca para empezar";
            hi = new Vector2(290, 1702); hiSize = new Vector2(250, 112);
        }
        _tutL1.text = l1;
        _tutL2.text = l2;
        float pulse = 4 + Mathf.Sin(Time.time * 6) * 4;
        _tutHi.rectTransform.anchoredPosition = new Vector2(hi.x - pulse, -(hi.y - pulse));
        _tutHi.rectTransform.sizeDelta = hiSize + new Vector2(pulse * 2, pulse * 2);
    }

    public void TutTap() {
        if (_tut == 3) _tut = 4;
        else if (_tut == 5) {
            App.Save.tutorialStep = -1;
            _tut = -1;
            App.Save.Persist();
            App.Toast("¡Tutorial completado! ¡Suerte, Invocador!");
        }
    }

    void FinishTutorial() {
        App.Save.tutorialStep = -1;
        _tut = -1;
        App.Save.Persist();
    }

    class TutTap : MonoBehaviour, UnityEngine.EventSystems.IPointerClickHandler {
        public GameScreen Screen;
        public void OnPointerClick(UnityEngine.EventSystems.PointerEventData e) => Screen.TutTap();
    }

    public bool OnBack() {
        if (_tut > 0) { FinishTutorial(); return true; }
        if (_selected >= 0) { CloseInfo(); return true; }
        if (_placingBench >= 0) { _placingBench = -1; return true; }
        if (_editMode) { _btnEdit.image.color = UiKit.C(0x8D6E63); _editMode = false; return true; }
        ShowPause(!_paused);
        return true;
    }
}
