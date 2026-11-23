package com.tatanstudios.astropollocliente.extras

import coil3.PlatformContext
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/** Versión de la app instalada (versionName en Android, CFBundleShortVersionString en iOS). */
expect fun getVersionName(context: PlatformContext): String

/** Abre los ajustes del sistema para esta app (permisos). */
expect fun redireccionarAjustes(context: PlatformContext)

/** Copia única de las SharedPreferences de versiones muy antiguas. En iOS no hace nada. */
internal expect suspend fun migrarPreferenciasAntiguas(tokenManager: TokenManager)

/**
 * Reemplazo de `context.getString(R.string.x)` para usar fuera de @Composable (onClick, callbacks).
 * Los textos de compose resources se leen de forma suspendida; aquí se esperan de forma síncrona.
 */
fun textoRes(res: StringResource): String = runBlocking { getString(res) }

/** Reemplazo multiplataforma de String.format(Locale.US, "%.2f", monto). */
fun formatoDosDecimales(monto: Double): String {
    val centavos = kotlin.math.round(kotlin.math.abs(monto) * 100).toLong()
    val signo = if (monto < 0 && centavos != 0L) "-" else ""
    val decimales = (centavos % 100).toString().padStart(2, '0')
    return "$signo${centavos / 100}.$decimales"
}
