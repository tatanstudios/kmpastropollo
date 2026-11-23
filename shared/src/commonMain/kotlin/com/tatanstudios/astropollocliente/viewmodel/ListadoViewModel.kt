package com.tatanstudios.astropollocliente.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.Event
import com.tatanstudios.astropollocliente.extras.LatLng
import com.tatanstudios.astropollocliente.model.modelos.ModeloCarrito
import com.tatanstudios.astropollocliente.model.modelos.ModeloDatosBasicos
import com.tatanstudios.astropollocliente.model.modelos.ModeloDirecciones
import com.tatanstudios.astropollocliente.model.modelos.ModeloHistorialOrdenes
import com.tatanstudios.astropollocliente.model.modelos.ModeloHorario
import com.tatanstudios.astropollocliente.model.modelos.ModeloInfoProducto
import com.tatanstudios.astropollocliente.model.modelos.ModeloInformacionOrdenParaEnviar
import com.tatanstudios.astropollocliente.model.modelos.ModeloInformacionProducto
import com.tatanstudios.astropollocliente.model.modelos.ModeloInformacionProductoEditar
import com.tatanstudios.astropollocliente.model.modelos.ModeloMenuPrincipal
import com.tatanstudios.astropollocliente.model.modelos.ModeloOrdenes
import com.tatanstudios.astropollocliente.model.modelos.ModeloOrdenesIndividual
import com.tatanstudios.astropollocliente.model.modelos.ModeloPoligonos
import com.tatanstudios.astropollocliente.model.modelos.ModeloSucursal
import com.tatanstudios.astropollocliente.model.modelos.ModeloSucursales
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductoHistorialOrdenes
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductos
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosDeOrden
import com.tatanstudios.astropollocliente.network.ApiService
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MENSAJE_ERROR_CONEXION = "No se pudo conectar. Revisa tu internet e intenta de nuevo"
private const val MENSAJE_NO_AUTORIZADO = "No autorizado para realizar esta acción"
private const val MENSAJE_INICIAR_SESION = "Inicia sesión para ver esta información"
private const val MENSAJE_ERROR_GENERICO = "Ocurrió un problema al cargar la información"

/**
 * Base para todas las peticiones: reemplaza RxJava (Single + Disposable) y LiveData.
 *
 * - [resultado]: respuesta envuelta en [Event] para consumirla una sola vez.
 * - [isLoading]: estado de carga.
 * - Evita peticiones duplicadas mientras una está en curso (antes: isRequestInProgress).
 * - Reintenta ante fallos de red (antes era `.retry()` infinito; ahora con límite y espera).
 * - Si todos los intentos fallan muestra un aviso (antes el error se perdía en silencio).
 */
abstract class PeticionViewModel<T>(
    private val reintentos: Int = 3,
) : ViewModel() {

    protected val api: ApiService get() = ApiService.instance

    private val _resultado = MutableStateFlow<Event<T>?>(null)
    val resultado: StateFlow<Event<T>?> = _resultado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Mensaje de error de la ÚLTIMA petición (null = sin error). Las pantallas que cargan datos
     * lo muestran con [com.tatanstudios.astropollocliente.componentes.PanelErrorReintentar]
     * y llaman a [reintentar].
     */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var job: Job? = null

    // Última petición lanzada, para poder repetirla con reintentar()
    private var ultimaAlRecibir: ((T) -> Unit)? = null
    private var ultimaLlamada: (suspend ApiService.() -> T)? = null

    /** Repite la última petición (la que falló). */
    fun reintentar() {
        val llamada = ultimaLlamada ?: return
        ejecutar(ultimaAlRecibir ?: {}, llamada = llamada)
    }

    /** La pantalla recibió una respuesta que no sabe manejar: mostrar el panel de reintentar. */
    fun marcarError(mensaje: String = MENSAJE_ERROR_GENERICO) { _error.value = mensaje }

    /**
     * @param reemplazar true = si hay una petición en curso se cancela y vale la nueva
     *   (ej. filtros que el usuario cambia rápido); false = se ignora la nueva (evita dobles envíos).
     */
    protected fun ejecutar(
        alRecibir: (T) -> Unit = {},
        reemplazar: Boolean = false,
        llamada: suspend ApiService.() -> T,
    ) {
        if (job?.isActive == true) {
            if (reemplazar) job?.cancel() else return
        }
        ultimaAlRecibir = alRecibir
        ultimaLlamada = llamada
        _error.value = null
        _isLoading.value = true
        job = viewModelScope.launch {
            val esteJob = coroutineContext[Job]
            try {
                val respuesta = conReintentos { api.llamada() }
                _isLoading.value = false
                _resultado.value = Event(respuesta)
                alRecibir(respuesta)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClientRequestException) {
                // 401: sesión inválida -> SesionApi ya lleva al login (con sesión). Sin sesión (visitante)
                // no hay a dónde llevarlo: se muestra el panel para que la pantalla no quede en blanco.
                // 403 u otro 4xx: no autorizado.
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    _error.value = MENSAJE_INICIAR_SESION
                } else {
                    _error.value = MENSAJE_NO_AUTORIZADO
                    CustomToasty(null, MENSAJE_NO_AUTORIZADO, ToastType.ERROR)
                }
            } catch (e: Exception) {
                _error.value = MENSAJE_ERROR_CONEXION
                CustomToasty(null, MENSAJE_ERROR_CONEXION, ToastType.ERROR)
            } finally {
                // si otra petición la reemplazó, el estado de carga lo maneja la nueva
                if (job === esteJob) _isLoading.value = false
            }
        }
    }

    private suspend fun <R> conReintentos(bloque: suspend () -> R): R {
        var espera = 1_000L
        repeat(reintentos) {
            try {
                return bloque()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClientRequestException) {
                throw e // 4xx (sin sesión, no autorizado): reintentar no sirve
            } catch (_: Exception) {
                delay(espera)
                espera *= 2
            }
        }
        return bloque()
    }
}

// ============================ LOGIN / REGISTRO ============================

class LoginViewModel : PeticionViewModel<ModeloDatosBasicos>(reintentos = 0) {
    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun setUsuario(usuario: String) { _usuario.value = usuario }
    fun setPassword(password: String) { _password.value = password }

    fun verificarUsuarioPasssword(idonesignal: String) = ejecutar {
        verificarUsuarioPassword(_usuario.value, _password.value, idonesignal)
    }
}

class RegistroViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _correo = MutableStateFlow<String?>(null)
    val correo: StateFlow<String?> = _correo.asStateFlow()

    fun setUsuario(usuario: String) { _usuario.value = usuario }
    fun setPassword(password: String) { _password.value = password }
    fun setCorreo(correo: String) { _correo.value = correo }

    fun registroRetrofit(idonesignal: String?, version: String?) = ejecutar {
        registrarme(_usuario.value, _password.value, _correo.value, idonesignal, version)
    }
}

class EnviarCorreoApiViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _correo = MutableStateFlow("")
    val correo: StateFlow<String> = _correo.asStateFlow()

    fun setCorreo(correo: String) { _correo.value = correo }

    fun enviarCorreoRetrofit() = ejecutar { enviarCorreoApi(_correo.value) }
}

class CambiarPasswordEmailViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _password = MutableStateFlow("")
    val passowrd: StateFlow<String> = _password.asStateFlow()

    fun setPassword(password: String) { _password.value = password }

    fun resetearContrasenaEmailRetrofit(idusuario: String, token: String) = ejecutar {
        resetearPasswordEmailApi(idusuario, token, _password.value)
    }
}

class VerificarCodigoCorreoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun verificarCodigoRetrofit(codigo: String, correo: String) = ejecutar { verificarCodigoEmailApi(codigo, correo) }
}

// ============================ MENU / HISTORIAL ============================

class ListadoMenuPrincipal : PeticionViewModel<ModeloMenuPrincipal>() {
    fun listadoMenuPrincipalRetrofit(id: String) = ejecutar { listadoMenuPrincipal(id) }
    fun listadoMenuInvitadoRetrofit(idSucursal: Int) = ejecutar { listadoMenuInvitado(idSucursal) }
}

class HistorialFechasBuscarViewModel : PeticionViewModel<ModeloHistorialOrdenes>() {
    fun historialListadonRetrofit(id: String, fecha1: String, fecha2: String) =
        ejecutar(reemplazar = true) { listadoHistorialOrdenes(id, fecha1, fecha2) }
}

class ListadoProductosHistorialOrdenViewModel : PeticionViewModel<ModeloProductoHistorialOrdenes>() {
    fun listaProductosHistorialRetrofit(idorden: Int) = ejecutar { listadoProductosHistorialOrden(idorden) }
}

class InfoProductoViewModel : PeticionViewModel<ModeloInfoProducto>() {
    fun infoProductoRetrofit(idproducto: Int) = ejecutar { infoProductosHistorialOrden(idproducto) }
}

class ActualizarPasswordViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _password = MutableStateFlow("")
    val passowrd: StateFlow<String> = _password.asStateFlow()

    fun setPassword(password: String) { _password.value = password }

    fun actualizarContrasenaRetrofit(idusuario: String) = ejecutar { actualizarPassword(idusuario, _password.value) }
}

class ListadoHorariosViewModel : PeticionViewModel<ModeloHorario>() {
    fun listadoHorarioRetrofit(idusuario: String) = ejecutar { listadoHorario(idusuario) }
}


// ============================ DIRECCIONES ============================

class ListadoDireccionesViewModel : PeticionViewModel<ModeloDirecciones>() {
    fun listadoDireccionesRetrofit(idusuario: String) = ejecutar { listadoDirecciones(idusuario) }
}

data class PoligonoUI(
    val id: Int,
    val nombre: String?,
    val puntos: List<LatLng>,
)

class ListadoSucursalesViewModel : PeticionViewModel<ModeloSucursales>() {
    val sucursales = mutableStateListOf<ModeloSucursal>()

    fun listadoSucursalesRetrofit() = ejecutar(
        alRecibir = { if (it.success == 1) { sucursales.clear(); sucursales.addAll(it.lista) } },
    ) { listadoSucursalesInvitado() }
}

class ListadoPoligonosViewModel : PeticionViewModel<ModeloPoligonos>() {

    private val _poligonosUI = mutableStateListOf<PoligonoUI>()
    val poligonosUI: List<PoligonoUI> get() = _poligonosUI

    fun listadoPoligonosRetrofit() = ejecutar(
        alRecibir = { response ->
            if (response.success == 1) {
                _poligonosUI.clear()
                response.lista.forEach { poligono ->
                    val puntos = poligono.listado.mapNotNull { coord ->
                        val lat = coord.latitud.toDoubleOrNull()
                        val lng = coord.longitud.toDoubleOrNull()
                        if (lat != null && lng != null) LatLng(lat, lng) else null
                    }
                    _poligonosUI.add(PoligonoUI(id = poligono.id, nombre = poligono.nombre, puntos = puntos))
                }
            }
        },
    ) { listadoPoligonos() }
}

class RegistroNuevaDireccionViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion.asStateFlow()

    // UNICO OPCIONAL
    private val _puntoReferencia = MutableStateFlow<String?>(null)
    val puntoReferencia: StateFlow<String?> = _puntoReferencia.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    fun setNombre(nombre: String) { _nombre.value = nombre }

    // Casa, Trabajo u otra (opcional); "nombre" es quien recibe
    private val _etiqueta = MutableStateFlow("")
    val etiqueta: StateFlow<String> = _etiqueta.asStateFlow()
    fun setEtiqueta(etiqueta: String) { _etiqueta.value = etiqueta }
    fun setDireccion(direccion: String) { _direccion.value = direccion }
    fun setPuntoReferencia(puntoReferencia: String) { _puntoReferencia.value = puntoReferencia }
    fun setTelefono(telefono: String) { _telefono.value = telefono }

    fun registrarNuevaDireccionRetrofit(
        idusuario: String, idzona: String, latitud: String, longitud: String,
        latitudreal: String?, longitudreal: String?,
    ) = ejecutar {
        registrarNuevaDireccion(
            idusuario, _nombre.value, _direccion.value, _puntoReferencia.value ?: "",
            idzona, latitud, longitud, latitudreal, longitudreal, _telefono.value,
            _etiqueta.value.trim().ifBlank { null },
        )
    }
}

class SeleccionarDireccionViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun seleccionarDireccionRetrofit(idusuario: String, dirid: Int) = ejecutar { seleccionarDireccion(idusuario, dirid) }
}

class BorrarDireccionViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun borrarDireccionRetrofit(idusuario: String, dirid: Int) = ejecutar { borrarDireccion(idusuario, dirid) }
}

// ============================ USUARIO ============================

class InformacionUsuarioViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun informacionUsuarioRetrofit(idusuario: String) = ejecutar { informacionUsuario(idusuario) }
}

class ActualizarCorreoUsuarioViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun actualizarUsuarioRetrofit(idusuario: String, usuario: String, correo: String) =
        ejecutar { actualizarCorreoUsuario(idusuario, usuario, correo) }
}

// ============================ PRODUCTOS / CARRITO ============================

class ListadoProductosViewModel : PeticionViewModel<ModeloProductos>() {
    fun listadoProductosRetrofit(idcategoria: Int) = ejecutar { listadoProductos(idcategoria) }
}

class InformacionProductoViewModel : PeticionViewModel<ModeloInformacionProducto>() {
    fun informacionProductoRetrofit(idproducto: Int) = ejecutar { informacionProducto(idproducto) }
}

class EnviarProductoAlCarritoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun enviarProductoCarritoRetrofit(clienteid: String, productoid: Int, cantidad: Int, notaproducto: String?, opciones: String) =
        ejecutar { enviarProductosAlCarrito(clienteid, productoid, cantidad, notaproducto, opciones) }
}

class ListadoCarritoComprasViewModel : PeticionViewModel<ModeloCarrito>() {
    fun listadoCarritoComprasRetrofit(idcliente: String) = ejecutar { listadoCarritoCompras(idcliente) }
}

class BorrarCarritoComprasViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun eliminarCarritoComprasRetrofit(idcliente: String) = ejecutar { borrarCarritoCompras(idcliente) }
}

class InformacionProductoEditadoViewModel : PeticionViewModel<ModeloInformacionProductoEditar>() {
    fun informacionProductoEditarRetrofit(idcliente: String, idfilacarrito: Int) =
        ejecutar { informacionProductoParaEditar(idcliente, idfilacarrito) }
}

class BorrarFilaCarritoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun eliminarFilaCarritoRetrofit(idcliente: String, idcarritofila: Int) =
        ejecutar { eliminarFilaCarrito(idcliente, idcarritofila) }
}

class ActualizarProductoEditadoViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun actualizarProductoEditadoRetrofit(idcliente: String, cantidad: Int, idcarritofila: Int, nota: String?, opciones: String) =
        ejecutar { actualizarProductoEditado(idcliente, cantidad, idcarritofila, nota, opciones) }
}

// ============================ ORDEN ============================

class InformacionOrdenParaEnviarViewModel : PeticionViewModel<ModeloInformacionOrdenParaEnviar>() {
    fun informacionOrdenParaEnviarRetrofit(idcliente: String) = ejecutar { informacionOrdenParaEnviar(idcliente) }
}

class VerificarCuponViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun verificarCuponRetrofit(idcliente: String, cupon: String) = ejecutar { verificarCupon(idcliente, cupon) }
}

class EnviarOrdenFinalViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun enviarOrdenRetrofit(
        idcliente: String, nota: String?, cupon: String?, aplicacupon: Int,
        version: String?, idfirebase: String?,
    ) = ejecutar { enviarOrdenFinal(idcliente, nota, cupon, aplicacupon, version, idfirebase) }
}

class ListadoOrdenesViewModel : PeticionViewModel<ModeloOrdenes>() {
    fun listadoOrdenesRetrofit(idcliente: String) = ejecutar { listadoOrdenes(idcliente) }
}

class InformacionDeUnaOrdenViewModel : PeticionViewModel<ModeloOrdenesIndividual>() {
    fun informacionOrdenIndividualRetrofit(ordenid: Int) = ejecutar { informacionOrdenIndividual(ordenid) }
}

class ListadoProductosDeUnaOrdenViewModel : PeticionViewModel<ModeloProductosDeOrden>() {
    fun listadoProductosDeUnaOrdenRetrofit(idorden: Int) = ejecutar { listadoProductosOrden(idorden) }
}

class CancelarOrdenViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun cancelarOrdenRetrofit(ordenid: Int) = ejecutar { cancelarOrden(ordenid) }
}

class OcultarOrdenViewModel : PeticionViewModel<ModeloDatosBasicos>() {
    fun ocultarOrdenRetrofit(ordenid: Int) = ejecutar { ocultarOrden(ordenid) }
}

