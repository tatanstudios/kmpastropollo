package com.tatanstudios.astropollocliente.extras

import androidx.compose.runtime.Composable

/**
 * Muestra [contenido] en español aunque el teléfono esté en otro idioma
 * (calendarios de Material: nombres de mes y de día). Ej. "Lu Ma Mi..." en vez de "M T W...".
 */
@Composable
expect fun EnEspanol(contenido: @Composable () -> Unit)
