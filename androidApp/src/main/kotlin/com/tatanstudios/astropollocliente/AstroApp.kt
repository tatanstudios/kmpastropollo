package com.tatanstudios.astropollocliente

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.tatanstudios.astropollocliente.extras.AndroidPlatform
import com.tatanstudios.astropollocliente.extras.Notificaciones
import com.tatanstudios.astropollocliente.extras.PuenteNotificaciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** App de OneSignal de Astro Pollo (no es secreta: va dentro de la app). */
private const val ONESIGNAL_APP_ID = "f86a2ee4-a10b-4a86-a063-151be6845bce"

/** Antes SistemaController. */
class AstroApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.init(this)

        if (BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        Notificaciones.puente = object : PuenteNotificaciones {
            override fun idSuscripcion(): String? = OneSignal.User.pushSubscription.id

            override fun pedirPermiso() {
                scope.launch { OneSignal.Notifications.requestPermission(false) }
            }

            override fun iniciarSesion(externalId: String) {
                if (OneSignal.User.externalId != externalId) OneSignal.login(externalId)
            }

            override fun cerrarSesion() {
                OneSignal.logout()
            }
        }

        // igual que antes: se pide el permiso de notificaciones al abrir la app
        Notificaciones.pedirPermiso()
    }
}
