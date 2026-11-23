package com.tatanstudios.astropollocliente.extras

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
actual fun EnEspanol(contenido: @Composable () -> Unit) {
    val actual = LocalConfiguration.current
    // el DatePicker de Material toma el idioma de la configuración
    val configuracion = remember(actual) {
        Configuration(actual).apply { setLocale(Locale.forLanguageTag("es-SV")) }
    }
    CompositionLocalProvider(LocalConfiguration provides configuracion) { contenido() }
}
