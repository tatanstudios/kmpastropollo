package com.tatanstudios.astropollocliente.vistas.principal.opciones.horarios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.isoDayNumber
import kotlin.time.Clock
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CardHorarioV1
import com.tatanstudios.astropollocliente.componentes.CardHorarioV2
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.modelos.ModeloHorarioArray
import com.tatanstudios.astropollocliente.viewmodel.ListadoHorariosViewModel
import kotlinx.coroutines.flow.first

@Composable
fun HorariosScreen(navController: NavHostController,
                   viewModel: ListadoHorariosViewModel = viewModel { ListadoHorariosViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val resultado by viewModel.resultado.collectAsState()
    val tokenManager = remember { TokenManager(ctx) }

    var modeloHorariosArray by remember { mutableStateOf(listOf<ModeloHorarioArray>()) }
    var boolDatosCargados by remember { mutableStateOf(false) }
    var nombreRestaurante by remember { mutableStateOf("") }

    // día de hoy con la numeración de la API (1 = domingo ... 7 = sábado)
    val diaHoy = remember {
        val iso = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber // lunes = 1
        iso % 7 + 1
    }

    LaunchedEffect(Unit) {
        viewModel.listadoHorarioRetrofit(tokenManager.idUsuario.first())
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Horarios", AppColors.colorRojo) }
    ) { innerPadding ->

        if (boolDatosCargados) {
            val hoy = modeloHorariosArray.firstOrNull { it.dia == diaHoy }
            // ordenar de lunes a domingo
            val semana = modeloHorariosArray.sortedBy { (it.dia + 5) % 7 }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- restaurante + hoy ----
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.colorLetraRoja),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    nombreRestaurante,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.colorLetraPrincipal
                                )
                                val textoHoy = when {
                                    hoy == null -> "Horario de atención"
                                    hoy.cerrado == 1 -> "Hoy está cerrado"
                                    else -> "Hoy: ${(hoy.fechaformat ?: hoy.horario ?: "").replace(" / ", " – ")}"
                                }
                                Text(
                                    textoHoy,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hoy?.cerrado == 1) AppColors.colorLetraRoja else AppColors.colorVerde
                                )
                            }
                        }
                    }
                }

                // ---- semana ----
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(vertical = 6.dp)) {
                            semana.forEachIndexed { i, h ->
                                FilaHorario(h, esHoy = h.dia == diaHoy)
                                if (i < semana.lastIndex) {
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = AppColors.colorRedondeadoGris)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "El restaurante puede cerrar en días especiales; en ese caso lo verás en el menú.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.colorGrisIcono,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        } else if (isLoading) {
            ContenedorSkeleton(Modifier.padding(innerPadding).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BloqueSkeleton(Modifier.fillMaxWidth().height(88.dp), radio = 18.dp)
                    BloqueSkeleton(Modifier.fillMaxWidth().height(380.dp), radio = 18.dp)
                }
            }
        }

        ErrorReintentar(viewModel, Modifier.padding(innerPadding))
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                nombreRestaurante = result.restaurante
                modeloHorariosArray = result.lista
                boolDatosCargados = true
            }
            else -> viewModel.marcarError()
        }
    }
}

@Composable
private fun FilaHorario(h: ModeloHorarioArray, esHoy: Boolean) {
    val cerrado = h.cerrado == 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (esHoy) AppColors.colorLetraRoja.copy(alpha = 0.06f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            obtenerNombreDia(h.dia),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Medium,
            color = AppColors.colorLetraPrincipal
        )
        if (esHoy) {
            Spacer(Modifier.width(8.dp))
            Text(
                "Hoy",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.colorLetraRoja)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            if (cerrado) "Cerrado" else (h.fechaformat ?: h.horario ?: "").replace(" / ", " – "),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (esHoy) FontWeight.SemiBold else FontWeight.Normal,
            color = if (cerrado) AppColors.colorLetraRoja else AppColors.colorGrisv2
        )
    }
}


fun obtenerNombreDia(dia: Int): String {
    return when (dia) {
        1 -> "Domingo"
        2 -> "Lunes"
        3 -> "Martes"
        4 -> "Miércoles"
        5 -> "Jueves"
        6 -> "Viernes"
        7 -> "Sábado"
        else -> ""
    }
}