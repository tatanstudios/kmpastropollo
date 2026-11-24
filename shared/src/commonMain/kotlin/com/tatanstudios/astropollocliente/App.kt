package com.tatanstudios.astropollocliente

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.componentes.ToastHost
import com.tatanstudios.astropollocliente.vistas.login.AppNavigation

/** Punto de entrada común: lo llaman MainActivity (Android) y MainViewController (iOS). */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }

    val focusManager = LocalFocusManager.current
    // Tocar cualquier zona que no consuma el toque (fuera de campos/botones) cierra el teclado.
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    ) {
        AppNavigation()
        ToastHost()
    }
}
