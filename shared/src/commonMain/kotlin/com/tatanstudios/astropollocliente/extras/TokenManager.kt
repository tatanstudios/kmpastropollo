package com.tatanstudios.astropollocliente.extras

import kotlinx.coroutines.flow.first
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal const val DATASTORE_FILE_NAME = "user_prefs.preferences_pb"

/** Ruta absoluta del archivo de preferencias; cada plataforma decide dónde guardarlo. */
internal expect fun dataStoreFilePath(): String

private val dataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(produceFile = { dataStoreFilePath().toPath() })
}

private val ID_KEY = stringPreferencesKey("ID")
/** Token de Sanctum de la sesión (ver SesionApi). */
private val TOKEN_KEY = stringPreferencesKey("TOKEN_API")
private val DEDOS_KEY = stringPreferencesKey("DEDOS")
private val MAPA_KEY = stringPreferencesKey("MAPA")
/** Sucursal (restaurante) elegida por el visitante sin sesión (no se borra al cerrar sesión). */
private val SUCURSAL_KEY = stringPreferencesKey("SUCURSAL_INVITADO")

/**
 * Sesión del usuario guardada con DataStore multiplataforma.
 * Mismo API que la versión Android: `TokenManager(ctx).idUsuario`, `saveID`, `deletePreferences`...
 * El parámetro [context] se conserva solo por compatibilidad con las vistas; no se usa.
 */
class TokenManager(@Suppress("UNUSED_PARAMETER") context: Any? = null) {

    suspend fun saveID(id: String) { dataStore.edit { it[ID_KEY] = id } }

    /** Guarda el token que entrega la API al iniciar sesión o registrarse. */
    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
        SesionApi.actualizarToken(token)
    }

    val tokenGuardado: Flow<String> = dataStore.data.map { it[TOKEN_KEY] ?: "" }

    /**
     * Sesion de una version anterior de la app: tiene id guardado pero no token de la API.
     * Si la encuentra, borra ese id (ya no sirve) y devuelve true para avisar una sola vez.
     */
    suspend fun limpiarSesionAnteriorSinToken(): Boolean {
        val prefs = dataStore.data.first()
        val sesionVieja = !prefs[ID_KEY].isNullOrEmpty() && prefs[TOKEN_KEY].isNullOrEmpty()
        if (sesionVieja) dataStore.edit { it.remove(ID_KEY) }
        return sesionVieja
    }

    suspend fun saveDEDOS(id: String) { dataStore.edit { it[DEDOS_KEY] = id } }

    suspend fun saveMAPA(id: String) { dataStore.edit { it[MAPA_KEY] = id } }

    // Sin token no hay sesión: quien venga de una versión anterior (solo id guardado)
    // debe iniciar sesión una vez para obtener su token (el splash lo lleva al login con un aviso).
    val idUsuario: Flow<String> = dataStore.data.map {
        if (it[TOKEN_KEY].isNullOrEmpty()) "" else it[ID_KEY] ?: ""
    }

    val idDedos: Flow<String> = dataStore.data.map { it[DEDOS_KEY] ?: "" }

    val idMapa: Flow<String> = dataStore.data.map { it[MAPA_KEY] ?: "" }

    suspend fun saveSucursal(idSucursal: Int) { dataStore.edit { it[SUCURSAL_KEY] = idSucursal.toString() } }

    /** Sucursal del visitante; null si todavía no ha elegido. */
    val idSucursal: Flow<Int?> = dataStore.data.map { it[SUCURSAL_KEY]?.toIntOrNull() }

    suspend fun deletePreferences() {
        dataStore.edit {
            it.remove(ID_KEY)
            it.remove(TOKEN_KEY)
            it.remove(DEDOS_KEY)
            it.remove(MAPA_KEY)
        }
        SesionApi.actualizarToken("")
    }

    /** Copia única de SharedPreferences → DataStore (solo hace algo en Android). */
    suspend fun migrateFromSharedPreferencesIfNeeded() = migrarPreferenciasAntiguas(this)
}

/** Envuelve un resultado para consumirlo una sola vez (respuestas de red). */
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? =
        if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }

    fun peekContent(): T = content
}
