import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Claves privadas: se leen de local.properties (no se sube al repositorio) o de variables
// de entorno (para compilar en un servidor de CI). Ver README.
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
fun clavePrivada(nombre: String): String =
    propiedadesLocales.getProperty(nombre) ?: System.getenv(nombre) ?: ""

android {
    namespace = "com.tatanstudios.astropollocliente"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // mismo applicationId que AstroClienteCompose: se instala como actualización
        applicationId = "com.tatanstudios.astropollocliente"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 11
        versionName = "3.0.0"

        // Google Maps: el manifest usa ${MAPS_API_KEY}
        manifestPlaceholders["MAPS_API_KEY"] = clavePrivada("MAPS_API_KEY")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.onesignal)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
}
