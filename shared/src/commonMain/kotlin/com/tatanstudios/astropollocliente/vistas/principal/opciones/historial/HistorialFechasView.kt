package com.tatanstudios.astropollocliente.vistas.principal.opciones.historial

import com.tatanstudios.astropollocliente.extras.EnEspanol
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.BotonAuthSecundario
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.modelos.ModeloHistorialOrdenesArray
import com.tatanstudios.astropollocliente.viewmodel.HistorialFechasBuscarViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavHostController
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.navOptions
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.model.rutas.Routes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.formatoDosDecimales
/** dd/MM/yyyy (antes DateTimeFormatter.ofPattern("dd/MM/yyyy")). */
private fun LocalDate.formatoVista() =
    "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}/$year"

/** yyyy-MM-dd, el formato que espera la API. */
private fun LocalDate.formatoApi() = toString()

/** Periodos rápidos del historial (días hacia atrás desde hoy, incluyendo hoy). */
private enum class PeriodoHistorial(val texto: String, val diasAtras: Int) {
    HOY("Hoy", 0), SEMANA("7 días", 6), MES("30 días", 29), TRIMESTRE("3 meses", 89), PERSONALIZADO("Fechas", -1)
}

/**
 * Historial en una sola pantalla: al entrar muestra los últimos 30 días y cada periodo que se toca
 * vuelve a cargar al instante (sin botón "Buscar"). "Fechas" abre un calendario de rango.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialFechaScreen(
    navController: NavHostController,
    viewModel: HistorialFechasBuscarViewModel = viewModel { HistorialFechasBuscarViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val hoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val tokenManager = remember { TokenManager(ctx) }

    var periodo by rememberSaveable { mutableStateOf(PeriodoHistorial.MES) }
    var desde by remember { mutableStateOf(hoy.minus(PeriodoHistorial.MES.diasAtras, DateTimeUnit.DAY)) }
    var hasta by remember { mutableStateOf(hoy) }
    var calendarioAbierto by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()
    var ordenes by remember { mutableStateOf(listOf<ModeloHistorialOrdenesArray>()) }
    var datosCargados by remember { mutableStateOf(false) }

    // cargar cada vez que cambia el rango
    LaunchedEffect(desde, hasta) {
        datosCargados = false
        viewModel.historialListadonRetrofit(tokenManager.idUsuario.first(), desde.formatoApi(), hasta.formatoApi())
    }

    fun elegir(p: PeriodoHistorial) {
        if (p == PeriodoHistorial.PERSONALIZADO) { calendarioAbierto = true; return }
        periodo = p
        desde = hoy.minus(p.diasAtras, DateTimeUnit.DAY)
        hasta = hoy
    }

    if (calendarioAbierto) {
        EnEspanol {
            val millisHoy = hoy.toEpochDays() * 86_400_000L
            val estado = rememberDateRangePickerState(
                initialSelectedStartDateMillis = desde.toEpochDays() * 86_400_000L,
                initialSelectedEndDateMillis = hasta.toEpochDays() * 86_400_000L,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= millisHoy
                },
            )
            val coloresCalendario = DatePickerDefaults.colors(
                containerColor = Color.White,
                selectedDayContainerColor = AppColors.colorLetraRoja,
                selectedDayContentColor = Color.White,
                dayInSelectionRangeContainerColor = AppColors.colorLetraRoja.copy(alpha = 0.15f),
                dayInSelectionRangeContentColor = AppColors.colorLetraPrincipal,
                todayDateBorderColor = AppColors.colorLetraRoja,
                todayContentColor = AppColors.colorLetraRoja,
            )
                DatePickerDialog(
                    colors = coloresCalendario,
                    onDismissRequest = { calendarioAbierto = false },
                    confirmButton = {
                        TextButton(
                            enabled = estado.selectedStartDateMillis != null,
                            onClick = {
                                val ini = estado.selectedStartDateMillis
                                if (ini != null) {
                                    val fin = estado.selectedEndDateMillis ?: ini
                                    desde = Instant.fromEpochMilliseconds(ini).toLocalDateTime(TimeZone.UTC).date
                                    hasta = Instant.fromEpochMilliseconds(fin).toLocalDateTime(TimeZone.UTC).date
                                    periodo = PeriodoHistorial.PERSONALIZADO
                                }
                                calendarioAbierto = false
                            }
                        ) { Text("Ver órdenes", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { calendarioAbierto = false }) { Text(stringResource(Res.string.cancelar), color = AppColors.colorGrisv2) }
                    },
                ) {
                    DateRangePicker(
                        state = estado,
                        title = { Text("Elige el periodo", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
                        headline = {
                            val i = estado.selectedStartDateMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.formatoVista() } ?: "Desde"
                            val f = estado.selectedEndDateMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.formatoVista() } ?: "Hasta"
                            Text("$i – $f", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 24.dp, bottom = 8.dp))
                        },
                        showModeToggle = false,
                        colors = coloresCalendario,
                        modifier = Modifier.height(480.dp)
                    )
                }
        }
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Historial de compras", AppColors.colorRojo) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {

            // ---- periodos ----
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PeriodoHistorial.entries) { p ->
                    FilterChip(
                        selected = periodo == p,
                        onClick = { elegir(p) },
                        label = { Text(p.texto) },
                        leadingIcon = if (p == PeriodoHistorial.PERSONALIZADO) {
                            { Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = AppColors.colorLetraPrincipal,
                            iconColor = AppColors.colorGrisIcono,
                            selectedContainerColor = AppColors.colorLetraRoja,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                        )
                    )
                }
            }

            // ---- rango y conteo ----
            val conteo = if (datosCargados) {
                " · ${ordenes.size} ${if (ordenes.size == 1) "orden" else "órdenes"}"
            } else ""
            Text(
                text = (if (desde == hasta) desde.formatoVista() else "${desde.formatoVista()} – ${hasta.formatoVista()}") + conteo,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.colorGrisIcono,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Box(Modifier.fillMaxSize()) {
                when {
                    !datosCargados && isLoading -> ContenedorSkeleton(Modifier.padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(3) { BloqueSkeleton(Modifier.fillMaxWidth().height(150.dp), radio = 16.dp) }
                        }
                    }

                    datosCargados && ordenes.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
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
                            "No hay órdenes en este periodo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.colorLetraPrincipal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Toca otro periodo arriba para buscar más atrás.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.colorGrisIcono,
                            textAlign = TextAlign.Center
                        )
                        if (periodo != PeriodoHistorial.TRIMESTRE) {
                            Spacer(Modifier.height(20.dp))
                            BotonAuthSecundario(
                                "Ver últimos 3 meses",
                                onClick = { elegir(PeriodoHistorial.TRIMESTRE) },
                                modifier = Modifier.fillMaxWidth(0.75f)
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ordenes, key = { it.id }) { orden ->
                            TarjetaOrdenHistorial(orden) {
                                navController.navigate(
                                    Routes.VistaListadoProductosHistorialOrden.createRoute(idorden = orden.id.toString()),
                                    navOptions { launchSingleTop = true }
                                )
                            }
                        }
                    }
                }

                ErrorReintentar(viewModel)
            }
        }
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                ordenes = result.lista
                datosCargados = true
            }
            else -> viewModel.marcarError()
        }
    }
}
