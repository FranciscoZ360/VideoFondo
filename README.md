# VideoFondo

App Android que pone un video de tu teléfono como fondo de pantalla animado, en bucle. Se pausa sola cuando el fondo no se ve (ahorra batería) y viene sin sonido por defecto.

Hecha en **Kotlin**, sin librerías externas (solo el framework de Android). Requiere Android 8.0 o superior.

## Descargar

Baja `VideoFondo.apk` desde la [última versión en Releases](../../releases/latest) directamente en el teléfono.

## Cómo usarla

1. Instala el APK (el teléfono pedirá permitir "instalar apps de origen desconocido").
2. Abre **VideoFondo** → **Elegir video**.
3. **Establecer como fondo de pantalla** → confirma en la pantalla de Android.

Si cambias el video después, el fondo se actualiza solo.

> En algunos teléfonos (por ejemplo varios Samsung) los fondos animados solo se aplican a la pantalla de inicio, no a la de bloqueo.

## Compilar desde el código

**Con Android Studio:** abre la carpeta del proyecto y usa **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

**Con GitHub Actions:** el flujo de `.github/workflows` compila el APK en cada cambio y lo publica en Releases. Si haces un fork, activa Actions en la pestaña correspondiente.
