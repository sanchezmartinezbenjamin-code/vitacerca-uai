# VitaCerca — Smartwatch y red de apoyo (demo UAI)

Prototipo académico para el Taller de Innovación y Emprendimiento Tecnológico (TEI301, UAI).

**El objetivo es visualizar dos perspectivas:**

1. **Pulsera/reloj:** botón rojo SOS, botón verde Estoy bien, posible caída y cuenta regresiva.
2. **Red de apoyo:** estado de Marta (usuario ficticio), alerta por prioridad, respuesta Voy en camino e historial.

> **Advertencia:** no detecta caídas ni signos vitales reales, no envía notificaciones externas, no conecta dispositivos entre sí, no contacta emergencias y no puede utilizarse para seguridad o atención médica real.

## Qué contiene el repositorio

- `docs/`: web adaptable a computador, móvil y pantalla pequeña de reloj; publicable mediante GitHub Pages. No necesita Node ni base de datos.
- `wearos-android/`: código de una app **nativa para Wear OS** en Kotlin, con ambos modos y simulación local. Requiere compilar un APK.
- `.github/workflows/build-wearos.yml`: flujo para generar un APK de demostración desde GitHub Actions (sin necesidad de instalar Android Studio para compilar).

## A. Subir a GitHub y publicar la demo web

1. En GitHub, crea un repositorio (por ejemplo, `vitacerca-uai`).
2. Sube **todo el contenido** de esta carpeta, manteniendo los nombres y rutas.
3. En **Settings → Pages**, selecciona **Deploy from a branch**.
4. Elige `main` y la carpeta `/docs`; guarda los cambios.
5. GitHub mostrará la URL pública, habitualmente `https://TU-USUARIO.github.io/vitacerca-uai/`.
6. En computador o móvil abre la URL, selecciona «Red de apoyo» o «Smartwatch». Puedes usar `?vista=familia` y `?vista=reloj`.

La app web es instalable como PWA en navegadores compatibles, pero **Wear OS no garantiza soporte para instalar PWAs**. En algunos relojes solo se puede abrir desde un navegador compatible. Para una instalación auténtica usa el proyecto Wear OS de la sección B.

### Demostración web

- Abre en **dos pestañas del mismo navegador**: `?vista=reloj` y `?vista=familia`.
- En el reloj, pulsa `Simular caída`. Hay 12 segundos para presionar «Estoy bien».
- Si no se confirma, la vista familiar muestra una alerta naranja.
- Pulsa «Voy en camino» y «Cerrar evento».
- Consulta el historial.

**Límite:** las pestañas comparten `localStorage` y `BroadcastChannel` del **mismo origen y perfil de navegador**. No sincronizan relojes y celulares distintos. Abrir GitHub Pages desde un móvil y desde un reloj distinto **no enlaza los datos**.

## B. Instalación real en smartwatch Wear OS

Este código está preparado para **Google Wear OS** (p. ej., algunos Galaxy Watch y Pixel Watch). **No es compatible con Apple Watch/watchOS ni con relojes propietarios sin Wear OS.** Revisa el sistema del reloj antes de intentar instalar.

### Opción B1: Crear APK desde GitHub Actions

1. Sube el repositorio completo a GitHub.
2. Abre **Actions → Compilar APK de demostración Wear OS** y selecciona **Run workflow** (o revisa el resultado de la compilación automática tras subir archivos).
3. Si la compilación termina correctamente, abre la ejecución y descarga el artefacto `VitaCerca-WearOS-APK-Debug`.
4. Extrae el APK. En el reloj activa **Opciones de desarrollador → Depuración por Wi-Fi** y conecta mediante Android Debug Bridge (ADB) o Android Studio.
5. Instala el APK con `adb install app-debug.apk` después de vincular el reloj con ADB.

### Opción B2: Android Studio

1. Instala [Android Studio](https://developer.android.com/studio).
2. **Open** → selecciona `wearos-android/`.
3. Configura el SDK Android 35 y sincroniza Gradle (JDK 17).
4. Conecta un reloj Wear OS compatible o inicia un emulador Wear OS.
5. Ejecuta el proyecto con el botón **Run ▶**. Android Studio compila e instala la aplicación.

La app Wear OS alterna entre **Pulsera** y **Red de apoyo** en el **mismo reloj** para demostrar los flujos. La app Android no tiene aún comunicación con el panel web; las acciones del reloj no llegarán al celular familiar.

## Próxima etapa: conexión real entre dos dispositivos

El proyecto necesitará:

- comunicación segura por BLE/Wi-Fi/móvil, según el hardware;
- aplicación o backend que reciba los eventos del reloj;
- cuentas y permisos de familiares, cifrado y gestión de datos personales;
- notificaciones push, confirmaciones, reintentos y registro de fallos de conectividad;
- pruebas rigurosas de falsas alarmas, pérdida de conexión, accesibilidad y batería.

No publiques datos reales de adultos mayores en un sitio estático ni utilices un canal sin autenticación para emergencias.

## Nota sobre el proyecto

La propuesta sigue el concepto de VitaCerca presentado en el Avance 2: pulsera negra sin pantalla, dos botones, clasificación preventiva/urgente y coordinación familiar. Se omiten métricas médicas reales porque el prototipo no tiene sensores.
