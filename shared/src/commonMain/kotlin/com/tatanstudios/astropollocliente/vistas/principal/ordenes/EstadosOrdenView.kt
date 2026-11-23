package com.tatanstudios.astropollocliente.vistas.principal.ordenes

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.TipoAviso
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.astropollocliente.extras.TokenManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.model.rutas.Routes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColorOrdenesEstado
import com.tatanstudios.astropollocliente.componentes.CustomModal1BotonTitulo
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import com.tatanstudios.astropollocliente.model.modelos.ModeloOrdenesIndividualArray
import com.tatanstudios.astropollocliente.viewmodel.CancelarOrdenViewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionDeUnaOrdenViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EstadoOrdenScreen(
    navController: NavHostController,
    idorden: Int,
    viewModel: InformacionDeUnaOrdenViewModel = viewModel { InformacionDeUnaOrdenViewModel() },
    viewModelCancelar: CancelarOrdenViewModel = viewModel { CancelarOrdenViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val tokenManager = remember { TokenManager(ctx) }

    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingCancelar by viewModelCancelar.isLoading.collectAsState(false)
    val resultadoCancelar by viewModelCancelar.resultado.collectAsState()

    var modeloOrdenesArray by remember { mutableStateOf(listOf<ModeloOrdenesIndividualArray>()) }

    // cargar datos cuando cambia el id
    LaunchedEffect(idorden) {
        viewModel.informacionOrdenIndividualRetrofit(idorden)

        // mientras el restaurante no la inicia, revisar en silencio cada 20 s
        while (true) {
            kotlinx.coroutines.delay(20_000)
            val orden = modeloOrdenesArray.firstOrNull() ?: continue
            if (orden.estadoIniciada == 0 && orden.estadoCancelada == 0) {
                viewModel.informacionOrdenIndividualRetrofit(idorden)
            }
        }
    }

    var showModalCancelarOrden by rememberSaveable { mutableStateOf(false) }

    var showModalOrdenYaFueIniciada by rememberSaveable { mutableStateOf(false) }
    var tituloOrdenYaIniciada by rememberSaveable { mutableStateOf("") }
    var mensajeOrdenYaIniciada by rememberSaveable { mutableStateOf("") }


    // estado de refresco
    var refreshing by remember { mutableStateOf(false) }

    fun recargar() {
        refreshing = true
        viewModel.informacionOrdenIndividualRetrofit(idorden)
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, { recargar() })

    // si la petición falla por red no llega resultado: cortar el "refresh" igual
    LaunchedEffect(isLoading) { if (!isLoading) refreshing = false }

    val ordenActual = modeloOrdenesArray.firstOrNull()
    val sePuedeCancelar = ordenActual != null && ordenActual.estadoIniciada == 0 && ordenActual.estadoCancelada == 0

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = {
            BarraToolbarColorOrdenesEstado(
                navController,
                stringResource(Res.string.estado_de_orden),
                AppColors.colorRojo
            )
        },
        bottomBar = {
            if (ordenActual != null) {
                Surface(shadowElevation = 12.dp, color = Color.White) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (sePuedeCancelar) {
                            OutlinedButton(
                                onClick = { showModalCancelarOrden = true },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, AppColors.colorLetraRoja),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.colorLetraRoja),
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) { Text("Cancelar orden", fontWeight = FontWeight.SemiBold) }
                        }
                        Button(
                            onClick = {
                                navController.navigate(Routes.VistaListaProductosDeOrden.createRoute(ordenActual.id)) {
                                    launchSingleTop = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.colorLetraRoja, contentColor = Color.White),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ver productos", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            val orden = ordenActual
            if (orden == null) {
                if (isLoading) {
                    ContenedorSkeleton(Modifier.padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            BloqueSkeleton(Modifier.fillMaxWidth().height(150.dp), radio = 18.dp)
                            BloqueSkeleton(Modifier.fillMaxWidth().height(220.dp), radio = 18.dp)
                        }
                    }
                }
            } else {
                val cancelada = orden.estadoCancelada == 1
                val iniciada = orden.estadoIniciada == 1
                val (fondoEstado, colorEstado) = coloresEstadoOrden(cancelada, iniciada)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ---- estado actual destacado ----
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(fondoEstado),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when {
                                        cancelada -> Icons.Filled.Cancel
                                        iniciada -> Icons.Filled.Restaurant
                                        else -> Icons.Filled.HourglassTop
                                    },
                                    contentDescription = null,
                                    tint = colorEstado,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Orden #${orden.id}", style = MaterialTheme.typography.labelLarge, color = AppColors.colorGrisIcono)
                            Text(
                                when {
                                    cancelada -> "Orden cancelada"
                                    iniciada -> orden.textoIniciada?.takeIf { it.isNotBlank() } ?: "Tu orden se está preparando"
                                    else -> "Esperando al restaurante"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorLetraPrincipal,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                when {
                                    cancelada -> orden.notaCancelada?.takeIf { it.isNotBlank() } ?: "Esta orden ya no se preparará."
                                    iniciada -> orden.fechaEstimadaTxt?.takeIf { it.isNotBlank() }?.let { "Tiempo estimado: $it" }
                                        ?: "El restaurante ya aceptó tu orden."
                                    else -> "Te avisaremos cuando el restaurante acepte tu orden."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (cancelada) Color(0xFFC62828) else AppColors.colorGrisIcono,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // ---- línea de tiempo ----
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Seguimiento", fontWeight = FontWeight.SemiBold, color = AppColors.colorLetraPrincipal)
                            Spacer(Modifier.height(12.dp))
                            PasoEstado(
                                titulo = "Orden enviada",
                                detalle = orden.fechaOrden,
                                hecho = true,
                                ultimo = false,
                            )
                            if (cancelada) {
                                PasoEstado(
                                    titulo = "Cancelada",
                                    detalle = orden.fechaCancelada,
                                    hecho = true,
                                    ultimo = true,
                                    color = Color(0xFFC62828),
                                )
                            } else {
                                PasoEstado(
                                    titulo = if (iniciada) (orden.textoIniciada?.takeIf { it.isNotBlank() } ?: "Orden iniciada")
                                             else "Esperando que el restaurante inicie la orden",
                                    detalle = if (iniciada) orden.fechaEstimadaTxt else null,
                                    hecho = iniciada,
                                    ultimo = true,
                                )
                            }
                        }
                    }

                    // ---- datos de la orden ----
                    val total = orden.totalOrden?.takeIf { it.isNotBlank() }
                    if (total != null || !orden.notaOrden.isNullOrBlank() || !orden.mensajeCupon.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!orden.notaOrden.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Filled.EditNote, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Nota: ${orden.notaOrden}", style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv2)
                                    }
                                }
                                if (!orden.mensajeCupon.isNullOrBlank()) {
                                    Text("Cupón: ${orden.mensajeCupon}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                                }
                                if (total != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Total", style = MaterialTheme.typography.bodyLarge, color = AppColors.colorGrisIcono, modifier = Modifier.weight(1f))
                                        Text(
                                            if (total.startsWith("$")) total else "$$total",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.colorLetraRoja
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!cancelada && !iniciada) {
                        Text(
                            "Desliza hacia abajo para actualizar",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.colorGrisIcono,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (modeloOrdenesArray.isEmpty()) ErrorReintentar(viewModel)
            if (isLoadingCancelar) LoadingModal(isLoading = true, titulo = "Cancelando orden...")

            if (showModalCancelarOrden) {
                CustomModal2Botones(
                    showDialog = true,
                    titulo = "¿Cancelar tu orden?",
                    message = "El restaurante ya no la preparará.",
                    tipo = TipoAviso.PELIGRO,
                    onDismiss = { showModalCancelarOrden = false },
                    onAccept = {
                        showModalCancelarOrden = false
                        viewModelCancelar.cancelarOrdenRetrofit(idorden)
                    },
                    txtBtnAceptar = "Sí, cancelar",
                    txtBtnCancelar = "No",
                )
            }

            if (showModalOrdenYaFueIniciada) {
                CustomModal1BotonTitulo(showModalOrdenYaFueIniciada, tituloOrdenYaIniciada, mensajeOrdenYaIniciada, onDismiss = {
                    showModalOrdenYaFueIniciada = false
                    viewModel.informacionOrdenIndividualRetrofit(idorden)
                })
            }
        }
    }


    // manejar resultado retrofit
    resultado?.getContentIfNotHandled()?.let { result ->
        refreshing = false
        if (result.success == 1) {
            modeloOrdenesArray = result.ordenes

        } else {
            viewModel.marcarError()
        }
    }


    // manejar resultado retrofit
    resultadoCancelar?.getContentIfNotHandled()?.let { result ->

        when (result.success) {

            1 -> {
                // ORDEN YA FUE INICIADA POR RESTAURANTE
                val titulo = result.titulo ?: ""
                val mensaje = result.mensaje ?: ""
                tituloOrdenYaIniciada = titulo
                mensajeOrdenYaIniciada = mensaje
                showModalOrdenYaFueIniciada = true
            }
            2 -> {
                // ORDEN CANCELADA CORRECTAMENTE
               // val titulo = result.titulo ?: ""
              //  val mensaje = result.mensaje ?: ""

                CustomToasty(
                    ctx,
                    stringResource(Res.string.orden_cancelada),
                    ToastType.SUCCESS
                )

                navController.popBackStack()
            }
            else -> {
                // Error, mostrar Toast
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
            }
        }
    }


}


/** Un paso de la línea de tiempo: círculo (lleno si ya pasó) unido al siguiente por una línea. */
@Composable
private fun PasoEstado(
    titulo: String,
    detalle: String?,
    hecho: Boolean,
    ultimo: Boolean,
    color: Color = Color(0xFF2E7D32),
) {
    val colorPunto = if (hecho) color else Color(0xFFBDBDBD)
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (hecho) colorPunto else Color.White)
                    .border(2.dp, colorPunto, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hecho) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            if (!ultimo) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (hecho) colorPunto.copy(alpha = 0.5f) else Color(0xFFE0E0E0))
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (ultimo) 0.dp else 18.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (hecho) FontWeight.SemiBold else FontWeight.Normal,
                color = if (hecho) AppColors.colorLetraPrincipal else AppColors.colorGrisIcono
            )
            if (!detalle.isNullOrBlank()) {
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = AppColors.colorGrisIcono)
            }
        }
    }
}
