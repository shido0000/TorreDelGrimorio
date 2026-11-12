# La Torre del Grimorio — Idle · AFK · Tower Defense

Juego Android 100 % en español, inspirado en el estilo de *Summoner's Greed: Idle AFK Tower*:
gráficos cartoon chibi (contornos gruesos, colores vibrantes), defensa de torre idle con oleadas
infinitas, invocación tipo gacha, historia con capítulos, compras dentro de la app y un Pase de
suscripción que se desbloquea a los 7 días de instalar la APK.

## Versión Unity (oficial)

El juego fue **rehecho desde cero con Unity** como motor. El proyecto completo está en la
carpeta [`unity/`](unity/) y las instrucciones para abrirlo y generar el APK están en
**[README-UNITY.md](README-UNITY.md)**.

La versión antigua en Java puro (carpeta `app/` + `build/TorreDelGrimorio.apk`) se conserva
como respaldo.

## La historia

Eres el último aprendiz del maestro **Orin**, Guardián del **Grimorio Codicioso**, un grimorio
que convierte la magia en oro. Tu compañero de estudios, **Sir Aldric**, traicionó al maestro y
ahora es el **Rey Héroe**: el reino entero viene en oleadas a robar el Grimorio de tu Torre.
Con ayuda de **Mocha** (la gata de Orin) invocarás monstruos —limos, lobos, avispas, gólems,
ojos místicos, dragones y fénix— para que cada caballero que se acerque pague en oro.

**Capítulos**: 1) El Grimorio Codicioso · 2) Los estandartes del traidor · 3) La capitana Elara ·
4) El oro es poder · 5) El asalto final · Epílogo: La Torre eterna.
Se desbloquean en las oleadas 1, 10, 20, 30, 40 y 50.

## Características

- **Defensa idle AFK**: los monstruos colocados junto al camino generan oro por segundo,
  incluso sin conexión (hasta 8 h acumuladas, se reclaman al volver).
- **Oleadas infinitas** con jefes cada 10 oleadas (¡el Rey Héroe Aldric en persona!),
  velocidad x1/x2/x4 y oleadas automáticas.
- **9 monstruos invocables** en 4 rarezas (Común, Raro, Épico, Legendario) con portal gacha:
  100 gemas x1, 900 gemas x10; los repetidos suben de nivel. Pantalla de revelación con ráfaga
  radial estilo "¡Nuevo Monstruo Raro!".
- **Mejoras**: subir de nivel cada monstruo con oro (daño/vida), XP por baja, mejoras globales
  con gemas y modos edición (mover/vender monstruos).
- **Héroes invasores**: escuderos, caballeros, arqueras, magos renegados, guardianes de hierro
  y jefes. Roban oro si llegan al cofre; si golpean 3 veces, pierdes la oleada.
- **Historia completa en español** con diálogos y retratos.
- **Compras en la app** (Google Play Billing v7).
- **Suscripción "Pase del Invocador"** con prueba de fundador de 7 días.

## Compras dentro de la app

| Producto | Tipo | Efecto |
|---|---|---|
| `gemas_100` | Consumible | +100 gemas |
| `gemas_550` | Consumible | +550 gemas |
| `gemas_1200` | Consumible | +1200 gemas |
| `mejora_oro_eterna` | No consumible | +25 % de oro permanente |
| `mejora_dano_eterna` | No consumible | +25 % de daño permanente |
| `pase_semanal` | Suscripción | Pase del Invocador semanal |
| `pase_mensual` | Suscripción | Pase del Invocador mensual |

### Beneficios del Pase del Invocador

- Oro x1,5 en todo el juego.
- +50 % de gemas en cada compra/pack.
- Recompensa diaria de 100 gemas.
- Insignia exclusiva de Invocador.

### Lógica de los 7 días

- Al instalar se guarda la fecha ( SharedPreferences). Durante los **primeros 7 días** el
  jugador disfruta gratis los beneficios del Pase ("Prueba de fundador") y la escena del Pase
  muestra una cuenta atrás.
- **A partir del día 7**, la suscripción se desbloquea y se ofrecen los planes semanal y
  mensual reales de Google Play. Si la prueba termina sin suscribirse, se avisa al jugador.
- Las compras se restauran con `queryPurchasesAsync` (botón "Restaurar compras").

> Antes de publicar: crea estos IDs de producto en Play Console (Monetizar → Productos) con
> exactamente esos identificadores. Sin ellos la tienda mostrará "Cargando...".

## Compilar

### Opción A: Android Studio (recomendado)

1. Abre la carpeta del proyecto en Android Studio (Gradle 8.5+, AGP 8.5.2, SDK 34).
2. `Build > Build APK(s)`. Genera `app/build/outputs/apk/debug/app-debug.apk`.

### Opción B: script sin Gradle (toolchain incluido en el sandbox)

```bash
./build.sh          # genera build/TorreDelGrimorio.apk (firmada con debug.keystore)
```

Requisitos del script (ya presentes en `/workspace/tc`): JRE, `ecj.jar`, build-tools 34
(`aapt2`, `d8`, `apksigner`, `zipalign`), `android.jar` (plataforma 34) y
`billing-7.1.1.aar`.

## Instalar en un dispositivo

```bash
adb install build/TorreDelGrimorio.apk
```

## Estructura del código (Java, sin dependencias de UI externas)

```
app/src/main/java/com/grimorio/torre/
├── MainActivity.java   Actividad fullscreen inmersiva
├── GameView.java       Bucle de juego, escalado 1080x1920, gestión de escenas
├── GameScene.java      Tablero, camino, oleadas, combate, colocación, HUD
├── MenuScene.java      Menú principal + ganancias offline + recompensa diaria
├── SummonScene.java    Portal gacha + revelación
├── ShopScene.java      Tienda (IAP de dinero real + mejoras con gemas)
├── SubScene.java       Pase del Invocador (suscripción, prueba de 7 días)
├── StoryScene.java     Diálogos de la historia
├── Story.java          Textos de los capítulos
├── Defs.java           Balance: monstruos, héroes, oleadas, economía
├── Save.java           Partida guardada (JSON en SharedPreferences)
├── Billing.java        Google Play Billing v7 (pagos, suscripciones, restaurar)
├── Art.java            Todos los gráficos dibujados por código (estilo chibi)
└── Ui.java             Botones, paneles y texto cartoon
```

## Notas

- Los gráficos son 100 % procedurales (Canvas): no hace falta descargar assets y el peso de la
  APK es mínimo (~260 KB). Todos los textos están en español.
- La APK de prueba está firmada con `debug.keystore` (contraseña `grimorio`). Para subir a
  Play Store, genera tu propio keystore de release.
- El pago real solo funciona con la app instalada desde Play (o testers con licencia); en
  dispositivos sin Google Play la tienda avisa y el juego sigue siendo 100 % jugable.
