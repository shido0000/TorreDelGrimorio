using UnityEngine;

/// <summary>
/// Punto de entrada: crea cámara, canvas virtual 1080x1920, audio, guardado y pantallas.
/// No requiere escena preparada: Unity lo invoca automáticamente al arrancar.
/// </summary>
public static class App {
    public static SaveData Save;
    public static AudioManager Audio;
    public static Purchaser Purchaser;
    public static Canvas Root;
    public static MonoBehaviour Current;
    public static int PendingStoryChapter;

    public static void ShowStory(int chapter) {
        PendingStoryChapter = chapter;
        Show<StoryScreen>();
    }

    public static void Show<T>() where T : MonoBehaviour {
        if (Current != null) Object.Destroy(Current.gameObject);
        var go = new GameObject(typeof(T).Name, typeof(RectTransform));
        go.transform.SetParent(Root.transform, false);
        var rt = (RectTransform)go.transform;
        UiKit.Stretch(rt);
        Current = go.AddComponent<T>();
        Object.DontDestroyOnLoad(Root.gameObject);
    }

    public static void Toast(string msg) {
        var go = new GameObject("Toast", typeof(RectTransform));
        go.transform.SetParent(Root.transform, false);
        var rt = (RectTransform)go.transform;
        UiKit.Stretch(rt);
        var toast = go.AddComponent<ToastView>();
        toast.Show(msg);
    }

    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.AfterSceneLoad)]
    static void Boot() {
        if (GameObject.Find("__APP") != null) return;
        var app = new GameObject("__APP");
        app.AddComponent<AppBoot>();
        Object.DontDestroyOnLoad(app);
    }
}

public class AppBoot : MonoBehaviour {
    void Awake() {
        var camGo = new GameObject("Cam", typeof(Camera));
        var cam = camGo.GetComponent<Camera>();
        cam.tag = "MainCamera";
        cam.clearFlags = CameraClearFlags.SolidColor;
        cam.backgroundColor = new Color32(20, 16, 34, 255);
        cam.orthographic = true;
        cam.transform.position = new Vector3(0, 0, -10);

        var es = new GameObject("EventSystem", typeof(UnityEngine.EventSystems.EventSystem),
            typeof(UnityEngine.EventSystems.StandaloneInputModule));

        var root = new GameObject("Root", typeof(Canvas), typeof(UnityEngine.UI.CanvasScaler),
            typeof(UnityEngine.UI.GraphicRaycaster));
        Root = root.GetComponent<Canvas>();
        Root.renderMode = RenderMode.ScreenSpaceOverlay;
        var scaler = root.GetComponent<UnityEngine.UI.CanvasScaler>();
        scaler.uiScaleMode = UnityEngine.UI.CanvasScaler.ScaleMode.ScaleWithScreenSize;
        scaler.referenceResolution = new Vector2(1080, 1920);
        scaler.matchWidthOrHeight = 0.5f;

        Save = SaveData.Load();
        Audio = root.AddComponent<AudioManager>();
        Purchaser = root.AddComponent<Purchaser>();
        Show<MenuScreen>();
    }

    void Update() {
        if (Input.GetKeyDown(KeyCode.Escape)) {
            var back = Current as IBackHandler;
            if (back != null && back.OnBack()) return;
            Application.Quit();
        }
    }
}

public interface IBackHandler {
    bool OnBack();
}
