package com.tatanstudios.astropollocliente.vistas.principal.opciones.historial


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.BotonAuthSecundario
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.astropollocliente.extras.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.navOptions
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CardHistorialOrden
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.model.modelos.ModeloHistorialOrdenesArray
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.viewmodel.HistorialFechasBuscarViewModel

@Composable
fun HistorialOrdenScreen(navController: NavHostController,
                         _fecha1: String, _fecha2: String,
                         viewModel: HistorialFechasBuscarViewModel = viewModel { HistorialFechasBuscarViewModel() }
) {
    val ctx = LocalPlatformContext.current
    var boolDatosCargados by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState(true)
    val tokenManager = remember { TokenManager(ctx) }
    val resultado by viewModel.resultado.collectAsState()

    var modeloListaHistorialArray by remember { mutableStateOf(listOf<ModeloHistorialOrdenesArray>()) }

    LaunchedEffect(Unit) {
        viewModel.historialListadonRetrofit(tokenManager.idUsuario.first(), _fecha1, _fecha2)
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Mis órdenes", AppColors.colorRojo) }
    ) { innerPadding ->

        when {
            !boolDatosCargados && isLoading -> ContenedorSkeleton(Modifier.padding(innerPadding).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BloqueSkeleton(Modifier.fillMaxWidth(0.6f).height(16.dp), radio = 6.dp)
                    repeat(3) { BloqueSkeleton(Modifier.fillMaxWidth().height(150.dp), radio = 16.dp) }
                }
            }

            boolDatosCargados && modeloListaHistorialArray.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(AppColors.colorLetraRoja.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(52.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    "No hay órdenes en estas fechas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraPrincipal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Prueba con un periodo más amplio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.colorGrisIcono,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                BotonAuthSecundario("Cambiar fechas", onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth(0.7f))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val n = modeloListaHistorialArray.size
                    Text(
                        "${formatoFechaCorta(_fecha1)} – ${formatoFechaCorta(_fecha2)} · $n ${if (n == 1) "orden" else "órdenes"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.colorGrisIcono
                    )
                }
                items(modeloListaHistorialArray, key = { it.id }) { orden ->
                    TarjetaOrdenHistorial(orden) {
                        navController.navigate(
                            Routes.VistaListadoProductosHistorialOrden.createRoute(idorden = orden.id.toString()),
                            navOptions { launchSingleTop = true }
                        )
                    }
                }
            }
        }

        ErrorReintentar(viewModel, Modifier.padding(innerPadding))
        if (isLoading && boolDatosCargados) LoadingModal(isLoading = true)
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                modeloListaHistorialArray = result.lista
                boolDatosCargados = true
            }
            else -> viewModel.marcarError()
        }
    }
}

/** "2026-11-01" -> "01/11/2026" (si no tiene ese formato se deja igual). */
private fun formatoFechaCorta(api: String): String {
    val p = api.split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else api
}

/** Tarjeta de una orden del historial: número, estado, fecha, dirección, total y cupón. */
@Composable
internal fun TarjetaOrdenHistorial(orden: ModeloHistorialOrdenesArray, onClick: () -> Unit) {
    val estado = orden.estado ?: ""
    val cancelada = estado.contains("cancel", ignoreCase = true)
    val (fondoEstado, textoEstado) = when {
        cancelada -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        estado.contains("entreg", ignoreCase = true) || estado.contains("complet", ignoreCase = true) ->
            Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> Color(0xFFFFF8E1) to Color(0xFF8D6E00)
    }

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
                if (estado.isNotBlank()) {
                    Text(
                        estado,
                        color = textoEstado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(fondoEstado)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (!orden.fechaOrden.isNullOrBlank()) {
                DatoOrden(Icons.Filled.CalendarMonth, orden.fechaOrden)
            }
            if (!orden.direccion.isNullOrBlank()) {
                DatoOrden(Icons.Filled.LocationOn, orden.direccion)
            }
            if (!orden.notaOrden.isNullOrBlank()) {
                DatoOrden(Icons.Filled.EditNote, "Nota: ${orden.notaOrden}")
            }
            if (orden.haycupon == 1 && !orden.mensajeCupon.isNullOrBlank()) {
                Text(
                    "Cupón: ${orden.mensajeCupon}",
                    color = AppColors.colorLetraRoja,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.colorLetraRoja.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
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
                Text("Ver productos", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.colorLetraRoja)
            }
        }
    }
}

@Composable
private fun DatoOrden(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv2)
    }
}
