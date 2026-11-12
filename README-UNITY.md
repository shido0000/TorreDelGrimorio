# La Torre del Grimorio — Versión Unity

Reconstrucción completa del juego con **Unity** como motor. Todo el juego (UI, batalla,
historia, tienda, misiones, logros, suscripción) está hecho 100 % en C#, sin escenas
preparadas: Unity arranca solo gracias a `App.cs`.

## Qué incluye

- **Batalla idle/AFK estilo Summoner Greed**: camino pre-renderizado, monstruos que
  disparan a los héroes, héroes que roban oro del cofre (3 golpes para destruirlo),
  oleadas infinitas con jefes cada 10 oleadas.
- **12 monstruos** invocables (gacha: Común 60 %, Raro 27 %, Épico 10 %, Legendario 3 %),
  con niveles y experiencia.
- **Historia en español** de 10 capítulos con retratos, que se desbloquea por oleadas.
- **Tutorial interactivo** de 5 pasos en la primera partida.
- **Misiones diarias** (3, con recompensa de gemas) y **13 logros permanentes**.
- **3 dificultades**: Normal, Difícil y Pesadilla.
- **Suscripción "Pase del Invocador"**: prueba gratuita de fundador durante 7 días desde
  la instalación; después se requiere suscripción semanal o mensual.
- **Compras dentro de la app** (Unity IAP / Google Play): gemas, mejoras eternas y el Pase.
- **Gráficos**: los mismos sprites cartoon generados del proyecto original
  (`Assets/Resources/Art`) y música/efectos (`Assets/Resources/Audio`).

## Cómo construir el APK (paso a paso)

1. Instala **Unity Hub** (gratis): https://unity.com/download
2. En Unity Hub, instala **Unity 2022.3 LTS** (cualquier 2022.3.x sirve) con los módulos:
   - ✅ **Android Build Support**
   - ✅ **Android SDK & NDK Tools**
   - ✅ **OpenJDK**
3. En Unity Hub pulsa **Add → Add project from disk** y selecciona la carpeta `unity/`
   de este repositorio. Ábrelo (la primera vez tardará en importar).
4. Cuando abra, ve a **File → Build Settings**:
   - Platform: **Android** → pulsa **Switch Platform**.
   - Pulsa **Player Settings**:
     - Company Name: `Grimorio` · Product Name: `La Torre del Grimorio`
     - Other Settings → Identification:
       - Package Name: `com.grimorio.torre`
       - Minimum API Level: `24` · Target API Level: `34 (o la más alta)`
5. **File → Build Settings → Build** → elige dónde guardar → obtienes
   `LaTorreDelGrimorio.apk`.
6. Copia el APK al móvil e instálalo (activa "Instalar apps de fuentes desconocidas").

## Compras dentro de la app (IAP)

El proyecto usa el paquete **Unity IAP** (`com.unity.purchasing`, ya declarado en
`Packages/manifest.json`; Unity lo descarga solo al abrir el proyecto).

| ID de producto | Tipo | Contenido |
|---|---|---|
| `gemas_100` | Consumible | +100 gemas |
| `gemas_550` | Consumible | +550 gemas |
| `gemas_1200` | Consumible | +1200 gemas |
| `mejora_oro_eterna` | No consumible | +25 % oro permanente |
| `mejora_dano_eterna` | No consumible | +25 % daño permanente |
| `pase_semanal` | Suscripción | Pase del Invocador (semanal) |
| `pase_mensual` | Suscripción | Pase del Invocador (mensual) |

> ⚠️ Las compras reales solo funcionan con el APK **firmado y subido a Google Play**
> (o con licencia de pruebas internas). Si el dispositivo no tiene Google Play
> configurado, el juego muestra un aviso en su lugar y el resto funciona igual.

## Lógica de la suscripción (7 días)

- Al instalar la APK se guarda la fecha (`SaveData.install`).
- Durante **7 días** el jugador tiene la "Prueba de fundador" con todas las ventajas.
- Al terminar, el menú avisa y la pantalla **Pase del Invocador** ofrece el plan
  semanal o mensual.

## Estructura del proyecto

```
unity/
├── Assets/
│   ├── Resources/
│   │   ├── Art/      ← 31 sprites PNG + 5 fondos JPG
│   │   └── Audio/    ← 11 WAV (música y efectos)
│   └── Scripts/      ← 16 archivos C# (todo el juego)
├── Packages/manifest.json
└── ProjectSettings/ProjectVersion.txt
```

### Pantallas (cada una es un "screen" en C#)

| Archivo | Pantalla |
|---|---|
| `App.cs` | Arranque automático, gestor de pantallas, guardado |
| `MenuScreen.cs` | Menú principal, ganancias offline, dificultad |
| `GameScreen.cs` | Batalla completa + tutorial interactivo |
| `SummonScreen.cs` | Portal de invocación (gacha) |
| `ShopScreen.cs` | Tienda: gemas y mejoras |
| `SubScreen.cs` | Pase del Invocador (suscripción) |
| `StoryScreen.cs` | Historia por capítulos |
| `DailyScreen.cs` | Misiones diarias |
| `AchieveScreen.cs` | Logros permanentes |
| `StoryData.cs` | Textos de los 10 capítulos |
| `Defs.cs` | Monstruos, héroes, oleadas, economía |
| `SaveData.cs` | Guardado (JSON en PlayerPrefs) |
| `Quests.cs` | Misiones diarias + logros |
| `Purchaser.cs` | Compras IAP reales + avisos de respaldo |
| `AudioManager.cs` | Música y efectos |
| `UiKit.cs` | UI construida por código |

## Versión antigua (Java puro)

La versión anterior (APK compilado, sin Unity) sigue en la raíz del repositorio:
`build/TorreDelGrimorio.apk` con su script `build.sh`. Esta versión Unity es la
oficial a partir de ahora.
