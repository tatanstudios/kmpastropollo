package com.tatanstudios.astropollocliente.extras

import androidx.compose.runtime.Composable

// En iOS el calendario usa el idioma del sistema; la app se distribuye en español.
@Composable
actual fun EnEspanol(contenido: @Composable () -> Unit) = contenido()
