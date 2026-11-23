package com.tatanstudios.astropollocliente.network

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
import com.tatanstudios.astropollocliente.model.modelos.ModeloSucursales
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductoHistorialOrdenes
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductos
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosDeOrden
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.ParametersBuilder
import io.ktor.http.parameters

/**
 * Endpoints de la API Laravel (routes/api.php). Todos son POST form-urlencoded,
 * igual que en la versión Retrofit, pero como funciones suspend multiplataforma.
 */
class ApiService(private val client: HttpClient = createHttpClient()) {

    private suspend inline fun <reified T> post(
        path: String,
        crossinline campos: ParametersBuilder.() -> Unit,
    ): T = client.submitForm(url = path, formParameters = parameters { campos() }).body()

    /** Retrofit omitía los @Field con valor null; replicamos ese comportamiento. */
    private fun ParametersBuilder.opcional(nombre: String, valor: Any?) {
        if (valor != null) append(nombre, valor.toString())
    }

    // VERIFICACION DE NUMERO
    suspend fun verificarUsuarioPassword(telefono: String, password: String, idfirebase: String?): ModeloDatosBasicos =
        post("cliente/login") {
            append("usuario", telefono); append("password", password); opcional("idfirebase", idfirebase)
            append("externo", "1") // usa External ID de OneSignal
        }

    // REGISTRARSE
    suspend fun registrarme(telefono: String, password: String, correo: String?, tokenFcm: String?, version: String?): ModeloDatosBasicos =
        post("cliente/registro") {
            append("usuario", telefono); append("password", password)
            opcional("correo", correo); opcional("token_fcm", tokenFcm); opcional("version", version)
            append("externo", "1")
        }

    // CERRAR SESION: revoca el token de este dispositivo
    suspend fun cerrarSesion(): ModeloDatosBasicos = post("cliente/cerrar-sesion") { }

    // ENVIAR CORREO PARA QUE LARAVEL ENVIE CORREO CODIGO
    suspend fun enviarCorreoApi(correo: String): ModeloDatosBasicos =
        post("cliente/enviar/codigo-correo") { append("correo", correo) }

    // ACTUALIZAR PASSWORD EMAIL
    suspend fun resetearPasswordEmailApi(id: String, token: String, password: String): ModeloDatosBasicos =
        post("cliente/actualizar/password") { append("id", id); append("token", token); append("password", password) }

    // VERIFICAR CODIGO QUE SE ENVIO AL CORREO
    suspend fun verificarCodigoEmailApi(codigo: String, correo: String): ModeloDatosBasicos =
        post("cliente/verificar/codigo-correo-password") { append("codigo", codigo); append("correo", correo) }

    // LISTA MENU PRINCIPAL
    suspend fun listadoMenuPrincipal(id: String): ModeloMenuPrincipal =
        post("cliente/lista/servicios-bloque") { append("id", id) }

    // VISITANTE (sin sesión): sucursales y menú de una sucursal
    suspend fun listadoSucursalesInvitado(): ModeloSucursales =
        client.get("cliente/invitado/sucursales").body()

    suspend fun listadoMenuInvitado(idSucursal: Int): ModeloMenuPrincipal =
        post("cliente/invitado/menu") { append("id_servicio", idSucursal.toString()) }

    // LISTADO DE ORDENES DE HISTORIAL SEGUN FECHA
    suspend fun listadoHistorialOrdenes(id: String, fecha1: String, fecha2: String): ModeloHistorialOrdenes =
        post("cliente/historial/listado/ordenes") { append("id", id); append("fecha1", fecha1); append("fecha2", fecha2) }

    // LISTADO DE PRODUCTOS DE UNA ORDEN
    suspend fun listadoProductosHistorialOrden(ordenid: Int): ModeloProductoHistorialOrdenes =
        post("cliente/listado/productos/ordenes") { append("ordenid", ordenid.toString()) }

    // INFO DE UN PRODUCTO DE UNA ORDEN
    suspend fun infoProductosHistorialOrden(idordendescrip: Int): ModeloInfoProducto =
        post("cliente/listado/productos/ordenes-individual") { append("idordendescrip", idordendescrip.toString()) }

    // ACTUALIZAR CONTRASENA
    suspend fun actualizarPassword(id: String, password: String): ModeloDatosBasicos =
        post("cliente/perfil/actualizar/contrasena") { append("id", id); append("password", password) }

    // LISTADO DE HORARIOS
    suspend fun listadoHorario(id: String): ModeloHorario =
        post("cliente/informacion/restaurante/horario") { append("id", id) }

    // LISTADO DE DIRECCIONES
    suspend fun listadoDirecciones(id: String): ModeloDirecciones =
        post("cliente/listado/direcciones") { append("id", id) }

    // LISTADO ZONA POLIGONOS (GET)
    suspend fun listadoPoligonos(): ModeloPoligonos =
        client.get("cliente/listado/zonas/poligonos").body()

    // REGISTRAR NUEVA DIRECCION
    suspend fun registrarNuevaDireccion(
        idusuario: String, nombre: String, direccion: String, puntoReferencia: String?, idzona: String,
        latitud: String, longitud: String, latitudreal: String?, longitudreal: String?, telefono: String,
        etiqueta: String? = null,
    ): ModeloDatosBasicos = post("cliente/nueva/direccion") {
        append("id", idusuario); append("nombre", nombre); append("direccion", direccion)
        opcional("punto_referencia", puntoReferencia); append("id_zona", idzona)
        append("latitud", latitud); append("longitud", longitud)
        opcional("latitudreal", latitudreal); opcional("longitudreal", longitudreal)
        append("telefono", telefono)
        opcional("etiqueta", etiqueta)
    }

    // SELECCIONAR DIRECCION
    suspend fun seleccionarDireccion(id: String, dirid: Int): ModeloDatosBasicos =
        post("cliente/direcciones/elegir/direccion") { append("id", id); append("dirid", dirid.toString()) }

    // BORRAR DIRECCION
    suspend fun borrarDireccion(id: String, dirid: Int): ModeloDatosBasicos =
        post("cliente/eliminar/direccion/seleccionada") { append("id", id); append("dirid", dirid.toString()) }

    // INFORMACION DEL USUARIO
    suspend fun informacionUsuario(id: String): ModeloDatosBasicos =
        post("cliente/informacion/personal") { append("id", id) }

    // ACTUALIZAR CORREO DEL USUARIO
    suspend fun actualizarCorreoUsuario(id: String, usuario: String, correo: String): ModeloDatosBasicos =
        post("cliente/actualizar/correo/v2") { append("id", id); append("usuario", usuario); append("correo", correo) }

    // LISTADO DE PRODUCTOS DE UNA CATEGORIA
    suspend fun listadoProductos(idcategoria: Int): ModeloProductos =
        post("cliente/listado/productos/servicios") { append("id", idcategoria.toString()) }

    // INFORMACION DE UN PRODUCTO PARA METER AL CARRITO
    suspend fun informacionProducto(idproducto: Int): ModeloInformacionProducto =
        post("cliente/informacion/producto/individual") { append("id", idproducto.toString()) }

    // AGREGAR PRODUCTO A CARRITO TEMPORAL
    // opciones: modificadores elegidos como "idOpcion:cantidad,idOpcion:cantidad" ("" si no hay)
    suspend fun enviarProductosAlCarrito(clienteid: String, productoid: Int, cantidad: Int, notaproducto: String?, opciones: String): ModeloDatosBasicos =
        post("cliente/carrito/producto/agregar") {
            append("clienteid", clienteid); append("productoid", productoid.toString())
            append("cantidad", cantidad.toString()); opcional("notaproducto", notaproducto)
            append("opciones", opciones)
        }

    // LISTADO CARRITO DE COMPRAS
    suspend fun listadoCarritoCompras(idcliente: String): ModeloCarrito =
        post("cliente/carrito/ver/orden") { append("clienteid", idcliente) }

    // PETICION PARA BORRAR CARRITO DE COMPRAS
    suspend fun borrarCarritoCompras(idcliente: String): ModeloDatosBasicos =
        post("cliente/carrito/borrar/orden") { append("clienteid", idcliente) }

    // ELIMINAR PRODUCTO INDIVIDUAL DEL CARRITO COMPRAS
    suspend fun eliminarFilaCarrito(idcliente: String, idcarrito: Int): ModeloDatosBasicos =
        post("cliente/carrito/eliminar/producto") { append("clienteid", idcliente); append("carritoid", idcarrito.toString()) }

    // INFORMACION DE PRODUCTO PARA EDITARLO
    suspend fun informacionProductoParaEditar(idcliente: String, idcarrito: Int): ModeloInformacionProductoEditar =
        post("cliente/carrito/ver/producto") { append("clienteid", idcliente); append("carritoid", idcarrito.toString()) }

    // ACTUALIZAR PRODUCTO EDITADO
    // opciones: se reemplazan las elegidas de esa fila (la API las valida)
    suspend fun actualizarProductoEditado(idcliente: String, cantidad: Int, idcarrito: Int, nota: String?, opciones: String): ModeloDatosBasicos =
        post("cliente/carrito/cambiar/cantidad") {
            append("clienteid", idcliente); append("cantidad", cantidad.toString())
            append("carritoid", idcarrito.toString()); opcional("nota", nota)
            append("opciones", opciones)
        }

    // INFORMACION DE ORDEN PARA ENVIAR
    suspend fun informacionOrdenParaEnviar(idcliente: String): ModeloInformacionOrdenParaEnviar =
        post("cliente/carrito/ver/proceso-orden") { append("clienteid", idcliente) }

    // VERIFICAR CUPON
    suspend fun verificarCupon(idcliente: String, cupon: String): ModeloDatosBasicos =
        post("cliente/verificar/cupon") { append("clienteid", idcliente); append("cupon", cupon) }

    // ENVIAR ORDEN FINAL
    suspend fun enviarOrdenFinal(
        idcliente: String, nota: String?, cupon: String?, aplicacupon: Int?, version: String?, idfirebase: String?,
    ): ModeloDatosBasicos = post("cliente/proceso/enviar/orden") {
        append("clienteid", idcliente); opcional("nota", nota); opcional("cupon", cupon)
        opcional("aplicacupon", aplicacupon); opcional("version", version); opcional("idfirebase", idfirebase)
        append("externo", "1")
    }

    // LISTADO DE ORDENES
    suspend fun listadoOrdenes(clienteid: String): ModeloOrdenes =
        post("cliente/ordenes/listado/activas") { append("clienteid", clienteid) }

    // INFORMACION DE UNA ORDEN PARA VER LOS ESTADOS
    suspend fun informacionOrdenIndividual(ordenid: Int): ModeloOrdenesIndividual =
        post("cliente/orden/informacion/estado") { append("ordenid", ordenid.toString()) }

    // INFORMACION LISTADO DE UN PRODUCTO DE UNA ORDEN
    suspend fun listadoProductosOrden(ordenid: Int): ModeloProductosDeOrden =
        post("cliente/listado/productos/ordenes") { append("ordenid", ordenid.toString()) }

    // CANCELAR ORDEN ANTES DE INICIARSE
    suspend fun cancelarOrden(idorden: Int): ModeloDatosBasicos =
        post("cliente/proceso/cancelar/orden") { append("idorden", idorden.toString()) }

    // OCULTAR UNA ORDEN COMPLETADA
    suspend fun ocultarOrden(idorden: Int): ModeloDatosBasicos =
        post("cliente/ocultar/mi/orden") { append("ordenid", idorden.toString()) }


    companion object {
        val instance: ApiService by lazy { ApiService() }
    }
}
