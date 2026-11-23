package com.tatanstudios.astropollocliente.vistas.principal.ordenes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.vector.ImageVector
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.TipoAviso
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.astropollocliente.extras.TokenManager
import kotlinx.coroutines.flow.first
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColorParaListaOrdenes
import com.tatanstudios.astropollocliente.model.modelos.ModeloOrdenesArray
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.viewmodel.ListadoOrdenesViewModel
import com.tatanstudios.astropollocliente.viewmodel.OcultarOrdenViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListadoOrdenesScreen(
    navController: NavHostController,
    viewModel: ListadoOrdenesViewModel = viewModel { ListadoOrdenesViewModel() },
    viewModelOcultarOrden: OcultarOrdenViewModel = viewModel { OcultarOrdenViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingOcultar by viewModelOcultarOrden.isLoading.collectAsState(true)
    val resultadoOcultar by viewModelOcultarOrden.resultado.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    var idusuario by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }

    var modeloOrdenesArray: List<ModeloOrdenesArray> by remember { mutableStateOf(listOf<ModeloOrdenesArray>()) }

    // orden cancelada que el cliente quiere quitar de la lista (se confirma antes)
    var ordenPorBorrar by remember { mutableStateOf<Int?>(null) }

    // 🔄 Estado de "pull to refresh"
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            // Se dispara al hacer swipe
            isRefreshing = true
            viewModel.listadoOrdenesRetrofit(idusuario)
        }
    )

    // si la petición falla por red no llega resultado: cortar el "refresh" igual
    LaunchedEffect(isLoading) { if (!isLoading) isRefreshing = false }

    LaunchedEffect(Unit) {
        idusuario = TokenManager(ctx).idUsuario.first()
        viewModel.listadoOrdenesRetrofit(idusuario)
    }

    keyboardController?.hide()

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = {
            BarraToolbarColorParaListaOrdenes(
                "Mis órdenes",
                AppColors.colorRojo,
            )
        },
    ) { innerPadding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                !datosCargados && isLoading -> ContenedorSkeleton(Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) { BloqueSkeleton(Modifier.fillMaxWidth().height(160.dp), radio = 16.dp) }
                    }
                }

                datosCargados && modeloOrdenesArray.isEmpty() -> Column(
                    // scrolleable para que funcione "jalar para recargar"
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(60.dp))
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(AppColors.colorLetraRoja.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DeliveryDining, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(56.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("No tienes órdenes activas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppColors.colorLetraPrincipal)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Cuando hagas un pedido podrás seguir su estado aquí. Desliza hacia abajo para actualizar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.colorGrisIcono,
                        textAlign = TextAlign.Center
                    )
                }

                datosCargados -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Desliza hacia abajo para actualizar",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.colorGrisIcono
                        )
                    }
                    items(items = modeloOrdenesArray, key = { it.id }) { orden ->
                        val esCancelada = orden.estadoCancelada == 1
                        TarjetaOrdenActiva(
                            orden = orden,
                            onClick = {
                                navController.navigate(Routes.VistaEstadoOrden.createRoute(orden.id)) { launchSingleTop = true }
                            },
                            // cancelada: se puede quitar de la lista (se confirma antes)
                            onQuitar = if (esCancelada) ({ ordenPorBorrar = orden.id }) else null,
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            ErrorReintentar(viewModel)
            if (isLoading && datosCargados && !isRefreshing) LoadingModal(true)
            if (isLoadingOcultar) LoadingModal(true)

            ordenPorBorrar?.let { id ->
                CustomModal2Botones(
                    showDialog = true,
                    titulo = "¿Quitar la orden #$id?",
                    message = "Solo se oculta de tu lista.",
                    tipo = TipoAviso.PELIGRO,
                    onDismiss = { ordenPorBorrar = null },
                    onAccept = {
                        ordenPorBorrar = null
                        viewModelOcultarOrden.ocultarOrdenRetrofit(id)
                    },
                    txtBtnAceptar = "Quitar",
                    txtBtnCancelar = "Cancelar",
                )
            }
        }
    }

    // ==== Manejo de resultados (resetea isRefreshing) ====
    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                datosCargados = true
                modeloOrdenesArray = result.ordenes
                isRefreshing = false // ✅ termina refresh
            }
            else -> {
                isRefreshing = false // ✅ termina refresh también en error
                viewModel.marcarError()
            }
        }
    }

    resultadoOcultar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // tras ocultar, refresca lista
                viewModel.listadoOrdenesRetrofit(idusuario)
            }
            else -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
            }
        }
    }
}

/** Colores del estado de una orden: cancelada (rojo), en preparación (verde), esperando (ámbar). */
internal fun coloresEstadoOrden(cancelada: Boolean, iniciada: Boolean): Pair<Color, Color> = when {
    cancelada -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    iniciada -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    else -> Color(0xFFFFF8E1) to Color(0xFF8D6E00)
}

@Composable
private fun TarjetaOrdenActiva(orden: ModeloOrdenesArray, onClick: () -> Unit, onQuitar: (() -> Unit)?) {
    val cancelada = orden.estadoCancelada == 1
    val iniciada = orden.estadoIniciada == 1
    val (fondoEstado, textoEstado) = coloresEstadoOrden(cancelada, iniciada)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Orden #${orden.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraPrincipal,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    orden.estado ?: "",
                    color = textoEstado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(fondoEstado)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (!orden.fechaOrden.isNullOrBlank()) DatoOrdenActiva(Icons.Filled.CalendarMonth, orden.fechaOrden)
            if (!orden.direccion.isNullOrBlank()) DatoOrdenActiva(Icons.Filled.LocationOn, orden.direccion)
            if (!orden.notaOrden.isNullOrBlank()) DatoOrdenActiva(Icons.Filled.EditNote, "Nota: ${orden.notaOrden}")

            if (orden.hayCupon == 1 && !orden.mensajeCupon.isNullOrBlank()) {
                Text(
                    "Cupón: ${orden.mensajeCupon}",
                    color = Color(0xFF2E7D32),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            if (cancelada && !orden.notaCancelada.isNullOrBlank()) {
                Text(
                    "Motivo: ${orden.notaCancelada}",
                    color = Color(0xFFC62828),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = AppColors.colorRedondeadoGris)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total", style = MaterialTheme.typography.labelMedium, color = AppColors.colorGrisIcono)
                    Text(
                        orden.totalFormat ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorLetraRoja
                    )
                }
                if (onQuitar != null) {
                    TextButton(onClick = onQuitar) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Quitar", color = AppColors.colorGrisIcono, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text("Ver estado", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.colorLetraRoja)
            }
        }
    }
}

@Composable
private fun DatoOrdenActiva(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv2)
    }
}
