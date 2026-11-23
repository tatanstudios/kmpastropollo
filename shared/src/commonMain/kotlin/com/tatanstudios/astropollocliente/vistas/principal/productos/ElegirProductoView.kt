package com.tatanstudios.astropollocliente.vistas.principal.productos

import com.tatanstudios.astropollocliente.componentes.DetalleProductoSkeleton
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BarraAgregarProducto
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CampoNotaProducto
import com.tatanstudios.astropollocliente.componentes.CustomModal1BotonTitulo
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.DialogoIniciarSesion
import com.tatanstudios.astropollocliente.componentes.EncabezadoProducto
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ReglasModificadores
import com.tatanstudios.astropollocliente.componentes.SelectorModificadores
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.modelos.ModeloGrupoModificador
import com.tatanstudios.astropollocliente.model.modelos.ModeloInformacionProductoArray
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.EnviarProductoAlCarritoViewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionProductoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ElegirProductoScreen(
    navController: NavHostController,
    idProducto: Int,
    viewModel: InformacionProductoViewModel = viewModel { InformacionProductoViewModel() },
    viewModelEnviar: EnviarProductoAlCarritoViewModel = viewModel { EnviarProductoAlCarritoViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val tokenManager = remember { TokenManager(ctx) }
    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()
    val isLoadingEnviar by viewModelEnviar.isLoading.collectAsState(false)
    val resultadoEnviar by viewModelEnviar.resultado.collectAsState()
    val listaState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // modal con titulo y mensaje (reglas del servidor o validaciones)
    var showModal by remember { mutableStateOf(false) }
    var modalTitulo by remember { mutableStateOf("") }
    var modalMensaje by remember { mutableStateOf("") }

    // datos del producto
    var producto by remember { mutableStateOf<ModeloInformacionProductoArray?>(null) }
    var grupos by remember { mutableStateOf<List<ModeloGrupoModificador>>(emptyList()) }

    // selección del cliente
    var cantidad by remember { mutableStateOf(1) }
    var seleccion by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var nota by remember { mutableStateOf("") }
    var errorNota by remember { mutableStateOf(false) }
    var resaltar by remember { mutableStateOf<Set<Int>>(emptySet()) }

    var idusuario by remember { mutableStateOf("") }
    var pedirSesion by remember { mutableStateOf(false) }

    LaunchedEffect(idProducto) {
        idusuario = tokenManager.idUsuario.first()
        viewModel.informacionProductoRetrofit(idProducto)
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        if (result.success == 1 && result.informacionProducto.isNotEmpty()) {
            producto = result.informacionProducto.first()
            grupos = result.modificadores
            seleccion = ReglasModificadores.seleccionInicial(result.modificadores)
        } else {
            viewModel.marcarError()
        }
    }

    val prod = producto
    val precioBase = prod?.precio?.toDoubleOrNull() ?: 0.0
    val precioUnidad = precioBase + ReglasModificadores.extra(grupos, seleccion)
    val textoAgregar = stringResource(Res.string.agregar_a_la_orden)

    // valida y envia; si falta algo, lleva la lista hasta el bloque pendiente
    val agregar: () -> Unit = agregar@{
        val p = producto ?: return@agregar
        val pendientes = ReglasModificadores.gruposIncompletos(grupos, seleccion)
        if (pendientes.isNotEmpty()) {
            resaltar = pendientes
            modalTitulo = "Faltan opciones"
            modalMensaje = ReglasModificadores.validar(grupos, seleccion) ?: ""
            showModal = true
            val indice = grupos.indexOfFirst { pendientes.contains(it.id) }
            if (indice >= 0) scope.launch { listaState.animateScrollToItem(1 + indice) }
            return@agregar
        }
        if (p.utilizaNota == 1 && nota.isBlank()) {
            errorNota = true
            scope.launch { listaState.animateScrollToItem(1 + grupos.size) }
            return@agregar
        }
        if (idusuario.isEmpty()) {
            pedirSesion = true // visitante: iniciar sesión antes de agregar al carrito
            return@agregar
        }
        viewModelEnviar.enviarProductoCarritoRetrofit(
            idusuario, idProducto, cantidad, nota.trim(), ReglasModificadores.textoApi(seleccion),
        )
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, prod?.nombre ?: stringResource(Res.string.elegir_cantidad), AppColors.colorRojo) },
        bottomBar = {
            if (prod != null) {
                BarraAgregarProducto(
                    cantidad = cantidad,
                    onCantidad = { cantidad = it },
                    total = precioUnidad * cantidad,
                    textoBoton = textoAgregar,
                    onClick = agregar,
                    habilitado = !isLoadingEnviar,
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

                    // un item por bloque para poder desplazarse al que falta
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

                    if (grupos.isNotEmpty() && precioUnidad != precioBase) {
                        item {
                            Text(
                                text = "Precio con tus opciones: $" + com.tatanstudios.astropollocliente.extras.formatoDosDecimales(precioUnidad) + " c/u",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.colorGrisv1,
                            )
                        }
                    }
                }
            }

            ErrorReintentar(viewModel)
            // primera carga: skeleton con la forma del detalle; recargas: modal
            if (isLoading && prod == null) DetalleProductoSkeleton()
            else if (isLoading) LoadingModal(isLoading = true)
            if (isLoadingEnviar) LoadingModal(isLoading = true)

            if (pedirSesion) {
                DialogoIniciarSesion(
                    navController,
                    "Para agregar productos al carrito necesitas iniciar sesión o crear una cuenta.",
                ) { pedirSesion = false }
            }

            if (showModal) {
                CustomModal1BotonTitulo(showModal, modalTitulo, modalMensaje, onDismiss = { showModal = false })
            }
        }
    }

    resultadoEnviar?.getContentIfNotHandled()?.let { result ->
        // 1..5: reglas del servidor (sin dirección, producto/categoría no disponible, fuera de horario)
        // 7: las opciones elegidas no cumplen las reglas actuales del producto
        when (result.success) {
            in 1..5, 7 -> {
                modalTitulo = result.titulo ?: "Nota"
                modalMensaje = result.mensaje ?: ""
                showModal = true
            }
            6 -> {
                CustomToasty(ctx, stringResource(Res.string.agregado_al_carrito), ToastType.SUCCESS)
                navController.popBackStack()
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
