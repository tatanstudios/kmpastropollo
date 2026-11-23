package com.tatanstudios.astropollocliente.extras

import com.tatanstudios.astropollocliente.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Token de Sanctum con el que la app se identifica ante la API.
 *
 * - Se entrega al iniciar sesión o registrarse y se guarda en DataStore (TokenManager).
 * - Cada petición lo envía en "Authorization: Bearer <token>" (ver ApiClient).
 * - No vence: solo deja de servir si se cierra sesión, se cambia la contraseña o el usuario es bloqueado.
 *   En ese caso la API responde 401, se borra la sesión y la app vuelve al login ([sesionExpirada]).
 */
object SesionApi {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // copia en memoria para no leer DataStore en cada petición
    private var tokenEnMemoria: String? = null

    /** true cuando el usuario venia con sesion de una version anterior: el login muestra el aviso. */
    var avisoIniciarSesionDeNuevo = false

    const val MENSAJE_INICIAR_SESION_DE_NUEVO = "Por seguridad actualizamos la app. Inicia sesión de nuevo con tu usuario y contraseña; tus datos y pedidos siguen guardados."

    private val _sesionExpirada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite cuando la API rechaza el token; la navegación principal lleva al login. */
    val sesionExpirada: SharedFlow<Unit> = _sesionExpirada.asSharedFlow()

    suspend fun token(): String =
        tokenEnMemoria ?: TokenManager().tokenGuardado.first().also { tokenEnMemoria = it }

    internal fun actualizarToken(token: String) {
        tokenEnMemoria = token
    }

    /** La API respondió 401: el token ya no sirve. */
    fun notificarSesionExpirada() {
        val habiaSesion = tokenEnMemoria != "" // "" = ya cerrada o visitante; null = aún sin leer
        tokenEnMemoria = ""
        if (!habiaSesion) return
        Notificaciones.desvincular()
        scope.launch { TokenManager().deletePreferences() }
        _sesionExpirada.tryEmit(Unit)
    }

    /** Cerrar sesión por decisión del usuario: revoca el token en la API (si hay red) y borra la sesión local. */
    suspend fun cerrarSesion() {
        runCatching { if (token().isNotEmpty()) ApiService.instance.cerrarSesion() }
        Notificaciones.desvincular()
        TokenManager().deletePreferences()
    }
}
