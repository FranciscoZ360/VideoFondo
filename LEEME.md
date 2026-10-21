# VideoFondo

App Android que pone un video de tu teléfono como fondo de pantalla animado, en bucle.
Se pausa sola cuando el fondo no se ve (ahorra batería) y viene sin sonido por defecto.

## Cómo obtener el APK

### Opción A: GitHub (sin instalar nada)
1. Crea una cuenta en github.com y un repositorio nuevo (puede ser privado).
2. Sube todo el contenido de esta carpeta (incluida la carpeta `.github`).
3. Ve a la pestaña **Actions**: se compila solo en unos 5 minutos.
4. Al terminar, en **Releases** aparece `VideoFondo.apk` para descargarlo desde el celular.

### Opción B: Android Studio
1. Abre esta carpeta con Android Studio.
2. Menú **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

## Cómo usarla
1. Instala el APK (el teléfono pedirá permitir "instalar apps de origen desconocido").
2. Abre **VideoFondo** → **Elegir video**.
3. **Establecer como fondo de pantalla** → confirma en la pantalla de Android.

Si cambias el video después, el fondo se actualiza solo.

Nota: en algunos teléfonos (por ejemplo varios Samsung) los fondos animados
solo se aplican a la pantalla de inicio, no a la de bloqueo.
