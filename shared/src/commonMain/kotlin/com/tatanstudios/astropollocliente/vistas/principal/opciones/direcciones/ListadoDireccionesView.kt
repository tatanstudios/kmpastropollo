package com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.extras.tienePermisoUbicacion
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CardMisDirecciones
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.extras.SolicitarPermisosUbicacion
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.modelos.ModeloDireccionesArray
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.ui.theme.ColorBlanco
import com.tatanstudios.astropollocliente.ui.theme.ColorGris
import com.tatanstudios.astropollocliente.viewmodel.ListadoDireccionesViewModel
import com.tatanstudios.astropollocliente.extras.redireccionarAjustes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MisDireccionesScreen(navController: NavHostController,
                         estadoBotonAtras: Int = 0, // defecto, para boton Atras
                         viewModel: ListadoDireccionesViewModel = viewModel { ListadoDireccionesViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    // listado de productos
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val resultado by viewModel.resultado.collectAsState()
    var boolDatosCargados by remember { mutableStateOf(false) }

    var modeloListaDireccionesArray by remember { mutableStateOf(listOf<ModeloDireccionesArray>()) }

    val tokenManager = remember { TokenManager(ctx) }
    var idusuario by remember { mutableStateOf("") }
    var popPermisoGPS by remember { mutableStateOf(false) }

    //val keyboardController = LocalSoftwareKeyboardController.current

    // Lanzar la solicitud cuando se carga la pantalla
    LaunchedEffect(Unit) {
        scope.launch {
            idusuario = tokenManager.idUsuario.first()
            viewModel.listadoDireccionesRetrofit(idusuario)
        }
    }

    if(boolDatosCargados){
        SolicitarPermisosUbicacion (
            onPermisosConcedidos = {
            },
            onPermisosDenegados = {
            }
        )
    }


    // agregar dirección: el mapa necesita el permiso de ubicación
    val agregarDireccion: () -> Unit = {
        if (tienePermisoUbicacion()) {
            navController.navigate(Routes.VistaMapa.route) {
                popUpTo(Routes.VistaMapa.route) { inclusive = true }
            }
        } else {
            popPermisoGPS = true
        }
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.mis_direcciones),
                AppColors.colorRojo,
                estadoBotonAtras = estadoBotonAtras
            )
        },
        floatingActionButton = {
            if (modeloListaDireccionesArray.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = agregarDireccion,
                    containerColor = AppColors.colorLetraRoja,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Agregar dirección", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->

        when {
            // primera carga: skeleton
            isLoading && !boolDatosCargados -> ContenedorSkeleton(Modifier.padding(innerPadding)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BloqueSkeleton(Modifier.fillMaxWidth(0.8f).height(16.dp), radio = 6.dp)
                    repeat(3) { BloqueSkeleton(Modifier.fillMaxWidth().height(104.dp), radio = 16.dp) }
                }
            }

            // sin direcciones
            modeloListaDireccionesArray.isEmpty() && boolDatosCargados -> Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(AppColors.colorLetraRoja.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.map),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.no_hay_direccion_registrada),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraPrincipal,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Agrega dónde quieres recibir tus pedidos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.colorGrisIcono,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = agregarDireccion,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.colorLetraRoja, contentColor = Color.White),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar dirección", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // listado
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Toca una dirección para usarla en tus pedidos o eliminarla.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.colorGrisIcono
                    )
                }

                items(modeloListaDireccionesArray, key = { it.id }) { dir ->
                    TarjetaDireccion(
                        direccion = dir,
                        onClick = {
                            navController.navigate(
                                Routes.VistaSeleccionarDireccion.createRoute(
                                    dir.id,
                                    dir.nombre ?: "",
                                    dir.telefono,
                                    dir.direccion,
                                    dir.punto_referencia,
                                    dir.etiqueta
                                ),
                                navOptions { launchSingleTop = true }
                            )
                        }
                    )
                }
            }
        }

        ErrorReintentar(viewModel, Modifier.padding(innerPadding))

        if (isLoading && boolDatosCargados) {
            LoadingModal(isLoading = true)
        }

        if(popPermisoGPS){
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
                    ) {
                        Text(stringResource(Res.string.ir_a_ajustes))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { popPermisoGPS = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorGris,
                            contentColor = ColorBlanco
                        )
                    ) {
                        Text(stringResource(Res.string.cancelar))
                    }
                }
            )
        }

    } // end-scalfold


    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // NO HAY DIRECCION REGISTRADA
                boolDatosCargados = true
            }
            2 -> {
                // SI HAY DIRECCIONES
                boolDatosCargados = true
                modeloListaDireccionesArray = result.lista
            }
            else -> viewModel.marcarError()
        }
    }


}


/** Tarjeta de una dirección: ícono, nombre, dirección, referencia y mínimo de compra; la que está en uso va resaltada. */
@Composable
private fun TarjetaDireccion(direccion: ModeloDireccionesArray, onClick: () -> Unit) {
    val enUso = direccion.seleccionado == 1
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enUso) 4.dp else 2.dp),
        border = if (enUso) BorderStroke(2.dp, AppColors.colorLetraRoja) else null
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (enUso) AppColors.colorLetraRoja else AppColors.colorRedondeadoGris),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = if (enUso) Color.White else AppColors.colorGrisIcono
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = direccion.etiqueta?.takeIf { it.isNotBlank() } ?: direccion.nombre ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorLetraPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (enUso) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "En uso",
                            color = AppColors.colorLetraRoja,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AppColors.colorLetraRoja.copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!direccion.etiqueta.isNullOrBlank() && !direccion.nombre.isNullOrBlank()) {
                    Text(
                        text = "Recibe: ${direccion.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.colorLetraPrincipal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = direccion.direccion ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.colorGrisv2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!direccion.punto_referencia.isNullOrBlank()) {
                    Text(
                        text = "Ref: ${direccion.punto_referencia}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.colorGrisIcono,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!direccion.minimocompra.isNullOrBlank()) {
                    Text(
                        text = direccion.minimocompra,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.colorVerde
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.colorLetraGris)
        }
    }
}
