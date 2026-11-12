using UnityEngine;

/// <summary>Música en bucle (menú/batalla) y efectos de sonido.</summary>
public class AudioManager : MonoBehaviour {
    AudioSource _music;
    float _lastSfx;
    string _current;

    void Awake() {
        _music = gameObject.AddComponent<AudioSource>();
        _music.loop = true;
        _music.volume = 0.5f;
        _lastSfx = -1f;
    }

    public void Music(string name) {
        string want = App.Save.musicOn ? name : null;
        if (want == _current) return;
        _current = want;
        if (want == null) { _music.Stop(); return; }
        var clip = Resources.Load<AudioClip>("Audio/" + want);
        if (clip == null) { _music.Stop(); return; }
        _music.clip = clip;
        _music.Play();
    }

    public void Sfx(string name) {
        if (!App.Save.soundOn) return;
        if (Time.unscaledTime - _lastSfx < 0.06f) return;
        _lastSfx = Time.unscaledTime;
        var clip = Resources.Load<AudioClip>("Audio/" + name);
        if (clip != null) {
            var src = gameObject.AddComponent<AudioSource>();
            src.PlayOneShot(clip);
            Destroy(src, clip.length + 0.1f);
        }
    }

    public void ApplyMusic() => Music(IsBattle() ? "battle" : "menu");

    static bool IsBattle() => App.Current is GameScreen;
}
