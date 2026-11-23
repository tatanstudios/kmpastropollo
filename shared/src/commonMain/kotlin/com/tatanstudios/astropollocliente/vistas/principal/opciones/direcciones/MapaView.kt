package com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.EstadoMapa
import com.tatanstudios.astropollocliente.extras.MapaZonas
import com.tatanstudios.astropollocliente.extras.contieneUbicacion
import com.tatanstudios.astropollocliente.extras.obtenerUbicacionActual
import com.tatanstudios.astropollocliente.extras.redireccionarAjustes
import com.tatanstudios.astropollocliente.extras.tienePermisoUbicacion
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.ui.theme.ColorBlanco
import com.tatanstudios.astropollocliente.ui.theme.ColorGris
import com.tatanstudios.astropollocliente.viewmodel.ListadoPoligonosViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val VerdeZona = Color(0xFF2E7D32)

@Composable
fun MapaScreen(
    navController: NavHostController,
    viewModel: ListadoPoligonosViewModel = viewModel { ListadoPoligonosViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    val resultado by viewModel.resultado.collectAsState()
    val poligonos = viewModel.poligonosUI
    val estadoMapa = remember { EstadoMapa() }
    var popPermisoGPS by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    var buscandoUbicacion by remember { mutableStateOf(false) }

    // zona en la que cae el pin ahora mismo (se recalcula al mover el mapa)
    val zonaEnCentro by remember {
        derivedStateOf {
            val centro = estadoMapa.centro ?: return@derivedStateOf null
            poligonos.firstOrNull { contieneUbicacion(centro, it.puntos) }
        }
    }
    val hayZonas = poligonos.isNotEmpty()

    val irAMiUbicacion: () -> Unit = {
        if (tienePermisoUbicacion()) {
            scope.launch {
                buscandoUbicacion = true
                val ubicacion = obtenerUbicacionActual()
                buscandoUbicacion = false
                if (ubicacion != null) estadoMapa.moverA = ubicacion
                else CustomToasty(ctx, "No se pudo obtener tu ubicación", ToastType.INFO)
            }
        } else {
            popPermisoGPS = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.listadoPoligonosRetrofit()
        if (tienePermisoUbicacion()) {
            obtenerUbicacionActual()?.let { estadoMapa.moverA = it }
        }
    }

    val confirmar: () -> Unit = confirmar@{
        val centro = estadoMapa.centro ?: return@confirmar
        val zona = zonaEnCentro ?: run {
            CustomToasty(ctx, "Fuera de zona de entrega", ToastType.INFO)
            return@confirmar
        }
        if (!tienePermisoUbicacion()) {
            popPermisoGPS = true
            return@confirmar
        }
        // zona válida: se guarda también la ubicación real del teléfono
        scope.launch {
            buscandoUbicacion = true
            val ubicacionReal = obtenerUbicacionActual()
            buscandoUbicacion = false
            if (ubicacionReal != null) {
                navController.navigate(
                    Routes.VistaRegistroDireccion.createRoute(
                        zona.id,
                        centro.latitude,
                        centro.longitude,
                        ubicacionReal.latitude,
                        ubicacionReal.longitude
                    ),
                    navOptions { launchSingleTop = true }
                )
            } else {
                CustomToasty(ctx, "No se pudo obtener la ubicación real", ToastType.INFO)
            }
        }
    }

    Scaffold(
        topBar = {
            BarraToolbarColor(navController, "Ubicación de entrega", AppColors.colorRojo)
        }
    ) { innerPadding ->

        Box(Modifier.fillMaxSize().padding(innerPadding)) {

            MapaZonas(
                modifier = Modifier.fillMaxSize(),
                poligonos = poligonos,
                estado = estadoMapa,
                mostrarMiUbicacion = true,
            )

            // ---- indicación arriba ----
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Mueve el mapa hasta que el pin quede sobre tu casa. Las zonas verdes son donde entregamos.",
                    fontSize = 13.sp,
                    color = AppColors.colorLetraPrincipal
                )
            }

            // ---- pin fijo en el centro, con su sombra en el punto exacto ----
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(10.dp, 5.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Icon(
                painter = painterResource(Res.drawable.ic_pin_mapa),
                contentDescription = null,
                tint = AppColors.colorLetraRoja,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .offset(y = (-22).dp)
            )

            // ---- abajo: botón Mi ubicación + tarjeta con el estado de la zona ----
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = irAMiUbicacion,
                    containerColor = Color.White,
                    contentColor = AppColors.colorLetraRoja,
                    shape = CircleShape,
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Mi ubicación")
                }

                Spacer(Modifier.height(10.dp))

                TarjetaEstadoZona(
                    hayZonas = hayZonas,
                    nombreZona = zonaEnCentro?.nombre,
                    dentro = zonaEnCentro != null,
                    onConfirmar = confirmar,
                )
            }
        }

        ErrorReintentar(viewModel, Modifier.padding(innerPadding))

        if (isLoading) LoadingModal(isLoading = true, titulo = "Cargando zonas...")
        if (buscandoUbicacion) LoadingModal(isLoading = true, titulo = "Buscando tu ubicación...")

        if (popPermisoGPS) {
            AlertDialog(
                onDismissRequest = { popPermisoGPS = false },
                title = { Text(stringResource(Res.string.permiso_gps_requerido)) },
                text = { Text(stringResource(Res.string.para_usar_esta_funcion_gps)) },
                confirmButton = {
                    Button(
                        onClick = {
                            popPermisoGPS = false
                            redireccionarAjustes(ctx)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.colorAzul,
                            contentColor = AppColors.colorBlanco
                        )
                    ) { Text(stringResource(Res.string.ir_a_ajustes)) }
                },
                dismissButton = {
                    Button(
                        onClick = { popPermisoGPS = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorGris,
                            contentColor = ColorBlanco
                        )
                    ) { Text(stringResource(Res.string.cancelar)) }
                }
            )
        }
    }

    // Manejo de resultado
    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {}
            else -> viewModel.marcarError("Error al cargar las zonas de entrega")
        }
    }
}

/** Dice en vivo si el pin está dentro de una zona de entrega y permite confirmar solo en ese caso. */
@Composable
private fun TarjetaEstadoZona(
    hayZonas: Boolean,
    nombreZona: String?,
    dentro: Boolean,
    onConfirmar: () -> Unit,
) {
    val colorEstado by animateColorAsState(if (dentro) VerdeZona else AppColors.colorLetraRoja, label = "estado")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (hayZonas) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (dentro) Icons.Filled.CheckCircle else Icons.Filled.WrongLocation,
                        contentDescription = null,
                        tint = colorEstado,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (dentro) "Entregamos aquí" else "Fuera de la zona de entrega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorEstado
                        )
                        Text(
                            text = if (dentro) (nombreZona ?: "Zona de entrega")
                            else "Mueve el pin dentro de una zona verde",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.colorGrisIcono
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            Button(
                onClick = onConfirmar,
                enabled = dentro,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.colorLetraRoja,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF9E9E9E),
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Confirmar ubicación", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
