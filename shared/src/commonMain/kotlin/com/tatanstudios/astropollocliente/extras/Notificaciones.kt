package com.tatanstudios.astropollocliente.extras

/**
 * Notificaciones push con OneSignal.
 *
 * El SDK se inicia en cada plataforma (Android: AstroApp, iOS: iOSApp.swift) y se registra aquí
 * con [puente]. Cada teléfono se vincula con el cliente que inició sesión usando el External ID
 * "cliente_{idCliente}" (OneSignal.login), y la API le envía los avisos de su orden (iniciada,
 * cancelada...) por ese mismo id. Así funciona en varios teléfonos y al cerrar sesión deja de recibir.
 * El servidor usa el mismo formato (App\Services\OneSignalService::externalIdCliente).
 */
interface PuenteNotificaciones {
    /** Id de la suscripción push de este teléfono (OneSignal.User.pushSubscription.id). */
    fun idSuscripcion(): String?

    /** Pide permiso para mostrar notificaciones (solo pregunta si aún no se ha respondido). */
    fun pedirPermiso()

    /** Vincula el teléfono con el cliente (OneSignal.login). */
    fun iniciarSesion(externalId: String)

    /** Desvincula el teléfono (OneSignal.logout). */
    fun cerrarSesion()
}

object Notificaciones {

    /** Lo asigna cada plataforma al iniciar la app. Si es null (ej. pruebas), no se hace nada. */
    var puente: PuenteNotificaciones? = null

    /** Lleva prefijo porque OneSignal rechaza en silencio ids genéricos como "1". */
    fun externalId(idCliente: String) = "cliente_$idCliente"

    fun idSuscripcion(): String = runCatching { puente?.idSuscripcion() }.getOrNull() ?: ""

    fun pedirPermiso() {
        runCatching { puente?.pedirPermiso() }
    }

    /** Al iniciar sesión, registrarse o abrir la app con sesión. */
    fun vincular(idCliente: String) {
        if (idCliente.isBlank()) return
        runCatching { puente?.iniciarSesion(externalId(idCliente)) }
            .onFailure { println("AstroPush: no se pudo vincular: $it") }
    }

    /** Al cerrar sesión o si la sesión expira. */
    fun desvincular() {
        runCatching { puente?.cerrarSesion() }
    }
}

/** Mismo nombre que la función de la versión Android (LoginView.getOneSignalUserId). */
fun getOneSignalUserId(): String = Notificaciones.idSuscripcion()
