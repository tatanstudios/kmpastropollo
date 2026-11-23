package com.tatanstudios.astropollocliente.network

import com.tatanstudios.astropollocliente.extras.SesionApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Configuración central de red (equivalente al antiguo RetrofitBuilder).
 * El motor HTTP lo pone cada plataforma: OkHttp en Android, Darwin (NSURLSession) en iOS.
 */
object ApiConfig {
    //** LOCAL: php artisan serve --host=192.168.1.4 --port=8080
    const val BASE_URL = "http://192.168.1.4:8080/api/"
    const val URL_IMAGENES = "http://192.168.1.4:8080/storage/imagenes/"

    //** SERVIDOR (producción)
    // const val BASE_URL = "https://astropollo.tatancloud.com/api/"
    // const val URL_IMAGENES = "https://astropollo.tatancloud.com/storage/imagenes/"
}

/** JSON tolerante, igual de permisivo que Gson con las respuestas del backend Laravel. */
val ApiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

/**
 * Agrega el token de Sanctum a cada petición y avisa si la API lo rechaza (401).
 * Las rutas públicas (login, registro, invitado) funcionan igual sin token.
 */
private val AutenticacionApi = createClientPlugin("AutenticacionApi") {
    onRequest { request, _ ->
        val token = SesionApi.token()
        if (token.isNotEmpty()) request.headers.append(HttpHeaders.Authorization, "Bearer $token")
    }
}

fun createHttpClient(): HttpClient = HttpClient {
    expectSuccess = true
    install(AutenticacionApi)
    HttpResponseValidator {
        validateResponse { respuesta ->
            if (respuesta.status == HttpStatusCode.Unauthorized) SesionApi.notificarSesionExpirada()
        }
        handleResponseExceptionWithRequest { error, _ ->
            if (error is ClientRequestException && error.response.status == HttpStatusCode.Unauthorized) {
                SesionApi.notificarSesionExpirada()
            }
        }
    }
    install(ContentNegotiation) {
        json(ApiJson)
        // Laravel a veces responde con text/html cuando hay arrays planos
        json(ApiJson, ContentType.Text.Html)
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    install(Logging) {
        level = LogLevel.NONE // Cambiar a LogLevel.BODY para depurar
    }
    defaultRequest {
        url(ApiConfig.BASE_URL)
        headers.append(HttpHeaders.Accept, "application/json")
        headers.append(HttpHeaders.Connection, "close")
    }
}
