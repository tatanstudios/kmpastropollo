# Astro Pollo Cliente — Kotlin Multiplatform (Android + iOS)

Versión multiplataforma de la app cliente. Toda la lógica **y la UI** viven en `shared/commonMain`
con Compose Multiplatform; Android e iOS solo aportan el punto de entrada, el mapa y la ubicación.

El proyecto original `../astroclientecompose` no se modificó.

## Estructura

```
AstroClienteKMP/
├── shared/                          ← módulo KMP (todo el código común)
│   └── src/
│       ├── commonMain/kotlin/…      ← UI, navegación, ViewModels, red, modelos, sesión
│       ├── commonMain/composeResources/  ← strings.xml, imágenes, fuentes, animaciones Lottie (files/)
│       ├── androidMain/             ← Google Maps, ubicación (FusedLocation), DataStore, ajustes
│       ├── iosMain/                 ← MapKit, CoreLocation, MainViewController()
│       └── androidHostTest/         ← pruebas contra la API real (solo lectura)
├── androidApp/                      ← MainActivity, AstroApp (OneSignal), manifest, íconos
└── iosApp/                          ← SwiftUI + OneSignal (proyecto Xcode con XcodeGen)
```

| Capa | Antes (Android) | Ahora (KMP) |
|---|---|---|
| Red | Retrofit + Gson + RxJava3 | Ktor 3 (OkHttp / Darwin) + kotlinx.serialization |
| ViewModels | LiveData + Disposable | `PeticionViewModel<T>` con StateFlow + coroutines |
| Sesión | DataStore con `Context` | DataStore KMP (`TokenManager`, misma ruta de archivo) |
| Imágenes | Coil 2 | Coil 3 |
| Animaciones | Lottie (Airbnb) | Compottie (Lottie multiplataforma) |
| Mapa | Google Maps Compose + PolyUtil | `MapaZonas`: Google Maps (Android) / MapKit (iOS) + `contieneUbicacion` |
| Ubicación | FusedLocationProvider | `obtenerUbicacionActual()`: FusedLocation / CoreLocation |
| Fechas historial | DatePickerDialog + java.time | DatePicker de Material 3 + kotlinx-datetime |
| Recursos | `R.string`, `R.drawable`, `R.color` | `Res.string`, `Res.drawable`, `AppColors` |
| Toast | Toasty | `CustomToasty` + `ToastHost` propio |
| Push | OneSignal (Android) | OneSignal en cada plataforma, `Notificaciones.puente` |

Versiones: Kotlin 2.4.21 · Compose Multiplatform 1.12.2 · AGP 9.5.1 · Gradle 9.9.1.

## Servidor

La URL está en `shared/src/commonMain/kotlin/.../network/ApiClient.kt` (`ApiConfig`).
Ahora apunta al servidor **local**:

```bash
cd ../apiastropollo
php artisan serve --host=192.168.1.4 --port=8080
```

Para producción, comentar las líneas LOCAL y descomentar las de SERVIDOR.

## Android (Windows / Mac)

Abrir la carpeta `AstroClienteKMP` en Android Studio y ejecutar la configuración `androidApp`, o:

```bash
./gradlew :androidApp:assembleDebug
```

Usa el mismo `applicationId` (`com.tatanstudios.astropollocliente`) y la misma ruta de DataStore que
AstroClienteCompose, así que al instalarse encima conserva la sesión (firmar con el mismo keystore).
Versión: 3.0.0 (versionCode 11).

## iOS (requiere Mac con Xcode)

```bash
brew install xcodegen
cd iosApp && xcodegen generate
open iosApp.xcodeproj
```

Bundle ID de iOS: `com.tatanstudios.astropollo` (el de la app ya publicada en App Store; en Android
el `applicationId` es `com.tatanstudios.astropollocliente`).

En Xcode: elegir tu Team en *Signing & Capabilities* y ejecutar. La fase de build llama a
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`, que compila `ComposeApp.framework`.

> El servidor de producción usa HTTPS, así que la app no tiene excepciones de ATS
> (`NSAllowsArbitraryLoads`). Para probar contra el servidor local por HTTP habría que añadirla temporalmente.

## Pruebas

```bash
./gradlew :shared:testAndroidHostTest
```

Llama a 12 endpoints de lectura de la API local (menú, productos, zonas, horario, direcciones,
carrito, órdenes, premios, historial…) y verifica que el JSON se decodifique. Si el servidor no
está levantado, las pruebas se omiten.

## Claves privadas

La clave de Google Maps **no** se sube al repositorio. Antes de compilar, agrega a `local.properties`
(ya está en `.gitignore`):

```
MAPS_API_KEY=tu_clave_de_google_maps
```

En un servidor de CI se puede pasar como variable de entorno `MAPS_API_KEY`.
`androidApp/build.gradle.kts` la inyecta en el `AndroidManifest.xml` (`${MAPS_API_KEY}`).
