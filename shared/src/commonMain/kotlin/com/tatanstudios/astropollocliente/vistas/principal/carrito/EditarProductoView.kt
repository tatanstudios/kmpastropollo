package com.tatanstudios.astropollocliente.vistas.principal.carrito

import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BarraAgregarProducto
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CampoNotaProducto
import com.tatanstudios.astropollocliente.componentes.CustomModal1BotonTitulo
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.EncabezadoProducto
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ReglasModificadores
import com.tatanstudios.astropollocliente.componentes.SelectorModificadores
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.model.modelos.ModeloGrupoModificador
import com.tatanstudios.astropollocliente.model.modelos.ModeloInformacionProductoEditarArray
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.ActualizarProductoEditadoViewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionProductoEditadoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditarProductoScreen(
    navController: NavHostController,
    idFilaCarrito: Int,
    viewModel: InformacionProductoEditadoViewModel = viewModel { InformacionProductoEditadoViewModel() },
    viewModelGuardar: ActualizarProductoEditadoViewModel = viewModel { ActualizarProductoEditadoViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val tokenManager = remember { TokenManager(ctx) }
    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()
    val isLoadingGuardar by viewModelGuardar.isLoading.collectAsState(false)
    val resultadoGuardar by viewModelGuardar.resultado.collectAsState()
    val listaState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var producto by remember { mutableStateOf<ModeloInformacionProductoEditarArray?>(null) }
    var grupos by remember { mutableStateOf<List<ModeloGrupoModificador>>(emptyList()) }
    var idusuario by remember { mutableStateOf("") }

    // se inicializan con lo que ya estaba en el carrito
    var cantidad by remember { mutableStateOf(1) }
    var seleccion by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var nota by remember { mutableStateOf("") }
    var errorNota by remember { mutableStateOf(false) }
    var resaltar by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var datosInicializados by remember { mutableStateOf(false) }

    var showModal by remember { mutableStateOf(false) }
    var modalTitulo by remember { mutableStateOf("") }
    var modalMensaje by remember { mutableStateOf("") }

    LaunchedEffect(idFilaCarrito) {
        idusuario = tokenManager.idUsuario.first()
        viewModel.informacionProductoEditarRetrofit(idusuario, idFilaCarrito)
    }

    resultado?.getContentIfNotHandled()?.let { res ->
        if (res.success == 1 && res.producto != null) {
            producto = res.producto
            grupos = res.modificadores
            if (!datosInicializados) {
                cantidad = res.producto.cantidad.coerceAtLeast(1)
                nota = res.producto.notaProducto ?: ""
                seleccion = ReglasModificadores.seleccionInicial(res.modificadores, res.seleccion)
                // si las opciones del producto cambiaron, resaltar lo que ahora falta
                resaltar = ReglasModificadores.gruposIncompletos(res.modificadores, seleccion)
                datosInicializados = true
            }
        } else {
            viewModel.marcarError()
        }
    }

    val prod = producto
    val precioBase = prod?.precio?.toDoubleOrNull() ?: 0.0
    val precioUnidad = precioBase + ReglasModificadores.extra(grupos, seleccion)
    val textoActualizar = stringResource(Res.string.actualizar)

    val guardar: () -> Unit = guardar@{
        val p = producto ?: return@guardar
        val pendientes = ReglasModificadores.gruposIncompletos(grupos, seleccion)
        if (pendientes.isNotEmpty()) {
            resaltar = pendientes
            modalTitulo = "Faltan opciones"
            modalMensaje = ReglasModificadores.validar(grupos, seleccion) ?: ""
            showModal = true
            val indice = grupos.indexOfFirst { pendientes.contains(it.id) }
            if (indice >= 0) scope.launch { listaState.animateScrollToItem(1 + indice) }
            return@guardar
        }
        if (p.utilizaNota == 1 && nota.isBlank()) {
            errorNota = true
            scope.launch { listaState.animateScrollToItem(1 + grupos.size) }
            return@guardar
        }
        viewModelGuardar.actualizarProductoEditadoRetrofit(
            idusuario, cantidad, idFilaCarrito, nota.trim(), ReglasModificadores.textoApi(seleccion),
        )
    }

    Scaffold(
        topBar = { BarraToolbarColor(navController, stringResource(Res.string.editar_cantidad), AppColors.colorRojo) },
        bottomBar = {
            if (prod != null) {
                BarraAgregarProducto(
                    cantidad = cantidad,
                    onCantidad = { cantidad = it },
                    total = precioUnidad * cantidad,
                    textoBoton = textoActualizar,
                    onClick = guardar,
                    habilitado = !isLoadingGuardar,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (prod != null) {
                LazyColumn(
                    state = listaState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        EncabezadoProducto(
                            imagenUrl = if (prod.utilizaImagen == 1 && !prod.imagen.isNullOrBlank()) "${ApiConfig.URL_IMAGENES}${prod.imagen}" else null,
                            nombre = prod.nombre ?: "",
                            descripcion = prod.descripcion,
                            precio = precioBase,
                        )
                    }

                    grupos.forEach { grupo ->
                        item(key = "grupo-${grupo.id}") {
                            SelectorModificadores(
                                grupos = listOf(grupo),
                                seleccion = seleccion,
                                onCambio = {
                                    seleccion = it
                                    resaltar = resaltar.intersect(ReglasModificadores.gruposIncompletos(grupos, it))
                                },
                                resaltar = resaltar,
                            )
                        }
                    }

                    item {
                        CampoNotaProducto(
                            nota = nota,
                            onCambio = { nota = it; errorNota = false },
                            requerida = prod.utilizaNota == 1,
                            indicacion = prod.nota,
                            error = errorNota,
                        )
                    }
                }
            }

            ErrorReintentar(viewModel)
            if (isLoading) LoadingModal(isLoading = true)
            if (isLoadingGuardar) LoadingModal(isLoading = true)

            if (showModal) {
                CustomModal1BotonTitulo(showModal, modalTitulo, modalMensaje, onDismiss = { showModal = false })
            }
        }
    }

    resultadoGuardar?.getContentIfNotHandled()?.let { res ->
        // 1: actualizado · 2: el producto ya no está en el carrito · 3: opciones no válidas
        when (res.success) {
            1 -> {
                CustomToasty(ctx, textoRes(Res.string.actualizado), ToastType.SUCCESS)
                navController.popBackStack()
            }
            2 -> {
                CustomToasty(ctx, "Este producto ya no está en tu carrito", ToastType.ERROR)
                navController.popBackStack()
            }
            3 -> {
                modalTitulo = res.titulo ?: "Opciones"
                modalMensaje = res.mensaje ?: ""
                showModal = true
            }
            else -> CustomToasty(ctx, textoRes(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
