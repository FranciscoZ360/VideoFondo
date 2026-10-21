# VideoFondo: resumen del proyecto (para retomar con Claude)

> Pega o adjunta este archivo en una conversación nueva con Claude y dile qué quieres cambiar.

## Qué es
App Android que pone un **video guardado en el teléfono como fondo de pantalla animado** (live wallpaper), en bucle.
Hecha en **Kotlin**, sin librerías externas (solo el framework de Android). Android 8.0+ (minSdk 26, targetSdk 35).

- Paquete: `com.videofondo.app`
- Versión actual: **1.1** (versionCode 2)
- Repositorio: **https://github.com/FranciscoZ360/VideoFondo** (público, rama `main`)
- Último APK: https://github.com/FranciscoZ360/VideoFondo/releases/latest

## Cómo se compila
- **GitHub Actions** (`.github/workflows/build-apk.yml`) compila en cada push a cualquier rama.
- Usa `ubuntu-24.04`, que ya trae el SDK de Android (**no** usar `android-actions/setup-android`: falló).
- Java 17 (`actions/setup-java@v5`), `./gradlew assembleRelease` (Gradle 8.14.3, AGP 8.7.3, Kotlin 2.0.21).
- El release se firma con la **clave de depuración**, así se instala directo.
  Ojo: si algún día se compila en otro computador, la clave cambia y habrá que desinstalar la app antes de instalar el nuevo APK.
- El APK se publica solo en **Releases** con la etiqueta `v1.<número de ejecución>`.

## Acceso de Claude a GitHub
- Cuenta GitHub: **FranciscoZ360**.
- La app "Claude" de GitHub está instalada **solo en el repo VideoFondo**. Gracias a eso Claude puede hacer push.
- En una sesión nueva, Claude debe agregar el repo `FranciscoZ360/VideoFondo` (con permiso de escritura) y clonarlo.

## Archivos principales
| Archivo | Para qué sirve |
|---|---|
| `app/src/main/java/com/videofondo/app/VideoWallpaperService.kt` | El fondo animado. Reproduce el video con `MediaPlayer` en bucle, lo recorta para llenar la pantalla (`SCALE_TO_FIT_WITH_CROPPING`), lo pausa cuando no se ve y se recarga al cambiar el video o el sonido. |
| `app/src/main/java/com/videofondo/app/MainActivity.kt` | Pantalla principal: elegir video (`ACTION_OPEN_DOCUMENT` + permiso persistente), vista previa con `TextureView` recortada al centro, interruptor de sonido y botón "Aplicar fondo" (abre el selector de fondos animados de Android). |
| `app/src/main/java/com/videofondo/app/PhoneFrame.kt` | Marco con proporción de teléfono (9:19.5) y esquinas redondeadas para la vista previa. |
| `app/src/main/java/com/videofondo/app/Prefs.kt` | Claves guardadas en SharedPreferences: `video_uri` y `sound`. |
| `app/src/main/res/layout/activity_main.xml` | Diseño de la pantalla. |
| `app/src/main/res/drawable/` | Fondos, degradados, botones e íconos vectoriales. |
| `app/src/main/res/values/` | Textos (`strings.xml`), colores (`colors.xml`) y tema oscuro (`themes.xml`). |
| `app/src/main/res/xml/wallpaper.xml` | Datos del fondo animado para Android. |

## Diseño actual (v1.1)
- Tema oscuro: degradado `#15112B` → `#07070D`, brillos violeta (`#7C6CFF`) y rosa (`#E85DC8`).
- Arriba: "VIDEOFONDO" y el título "Tu video, tu fondo." más un chip de estado con un punto de color.
- Al centro: vista previa con forma de teléfono, con el video y la hora y la fecha encima (`TextClock`). Sin video, muestra "Elige un video" y se puede tocar.
- Tarjeta de sonido con ícono e interruptor. Viene silenciado por defecto.
- Botones tipo píldora: "Elegir video"/"Cambiar" (contorno) y "Aplicar fondo" (degradado violeta→rosa).

## Historial
1. v1.0: primera versión funcional.
2. Arreglo de la compilación en GitHub (se quitó `setup-android`).
3. v1.1: rediseño de la interfaz.

## Pendientes / ideas posibles
- Probar en el teléfono y reportar cualquier error de la nueva interfaz (no se ha probado en un equipo real).
- Ideas: recortar el inicio y el fin del video, ajustar el zoom o la posición, velocidad de reproducción, fondo distinto para la pantalla de bloqueo, modo claro.
- Nota: en algunos teléfonos (por ejemplo varios Samsung) los fondos animados solo se aplican a la pantalla de inicio.
