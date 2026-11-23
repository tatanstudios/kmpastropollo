package com.tatanstudios.astropollocliente.vistas.principal.opciones.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.flow.first
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionUsuarioViewModel
import com.tatanstudios.astropollocliente.extras.SesionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.navOptions
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CustomModalCerrarSesion
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.rutas.Routes


sealed class Opcion(val icon: ImageVector?, open val label: String) {
    object Direcciones : Opcion(Icons.Default.FormatListNumbered, "Direcciones")
    object CambioPassword : Opcion(Icons.Default.Schedule, "Cambio de Contraseña")
    object Perfil : Opcion(Icons.Default.Person, "Perfil")
    object Horarios : Opcion(Icons.Default.History, "Horarios")
    object HistorialCompra : Opcion(Icons.Default.History, "Historial de Compras")
    object CerrarSesion : Opcion(Icons.AutoMirrored.Default.Logout, "Cerrar Sesión")
}


@Composable
fun PerfilScreen(
    navController: NavHostController,
    viewModelInformacion: InformacionUsuarioViewModel = viewModel { InformacionUsuarioViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(ctx) }

    var showModalCerrarSesion by remember { mutableStateOf(false) }

    // datos para el encabezado (si falla, se muestra "Mi cuenta" sin más)
    val resultadoInformacion by viewModelInformacion.resultado.collectAsState()
    var usuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val id = tokenManager.idUsuario.first()
        if (id.isNotEmpty()) viewModelInformacion.informacionUsuarioRetrofit(id)
    }
    resultadoInformacion?.getContentIfNotHandled()?.let { r ->
        if (r.success == 1) {
            usuario = r.usuario ?: ""
            correo = r.correo ?: ""
        }
    }

    fun ir(ruta: String) = navController.navigate(ruta) { launchSingleTop = true }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Mi cuenta", AppColors.colorRojo) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---- encabezado con el usuario ----
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().clickable { ir(Routes.VistaMiUsuario.route) }
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AvatarInicial(usuario)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = usuario.ifBlank { "Mi cuenta" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.colorLetraPrincipal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = correo.ifBlank { "Toca para agregar tu correo" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.colorGrisIcono,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.colorLetraGris)
                }
            }

            GrupoOpciones("Mi cuenta") {
                FilaOpcion(Icons.Filled.LocationOn, "Mis direcciones", "Dónde recibes tus pedidos") {
                    ir(Routes.VistaMisDirecciones.createRoute(0))
                }
                SeparadorOpcion()
                FilaOpcion(Icons.Filled.Lock, "Cambiar contraseña", "Actualiza tu contraseña") {
                    ir(Routes.VistaActualizarContrasena.route)
                }
            }

            GrupoOpciones("Pedidos") {
                FilaOpcion(Icons.Filled.ReceiptLong, "Historial de compras", "Tus órdenes anteriores") {
                    ir(Routes.VistaHistorialFecha.route)
                }
            }

            GrupoOpciones("Restaurante") {
                FilaOpcion(Icons.Filled.Schedule, "Horarios", "Cuándo puedes hacer pedidos") { ir(Routes.VistaHorarios.route) }
            }

            // ---- cerrar sesión ----
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilaOpcion(
                    Icons.AutoMirrored.Filled.Logout,
                    "Cerrar sesión",
                    null,
                    colorAcento = AppColors.colorLetraRoja,
                    mostrarFlecha = false
                ) { showModalCerrarSesion = true }
            }
        }

        if (showModalCerrarSesion) {
            CustomModalCerrarSesion(showModalCerrarSesion,
                "¿Cerrar sesión?",
                onDismiss = { showModalCerrarSesion = false },
                onAccept = {
                    scope.launch {
                        // revoca el token en la API y borra la sesión local
                        SesionApi.cerrarSesion()
                        showModalCerrarSesion = false
                        navigateToLogin(navController)
                    }
                })
        }
    }
}

/** Círculo rojo con la inicial del usuario (o un ícono si aún no se sabe). */
@Composable
internal fun AvatarInicial(nombre: String, tamano: androidx.compose.ui.unit.Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(AppColors.colorLetraRoja),
        contentAlignment = Alignment.Center
    ) {
        val inicial = nombre.trim().firstOrNull()?.uppercaseChar()
        if (inicial != null) {
            Text(inicial.toString(), color = Color.White, fontSize = (tamano.value * 0.42f).sp, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(tamano * 0.55f))
        }
    }
}

@Composable
private fun GrupoOpciones(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.colorGrisIcono,
            modifier = Modifier.padding(start = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Column { contenido() } }
    }
}

@Composable
private fun SeparadorOpcion() {
    HorizontalDivider(Modifier.padding(start = 70.dp), color = AppColors.colorRedondeadoGris)
}

@Composable
private fun FilaOpcion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String?,
    colorAcento: Color = AppColors.colorGrisIcono,
    mostrarFlecha: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorAcento.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = colorAcento, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (colorAcento == AppColors.colorGrisIcono) AppColors.colorLetraPrincipal else colorAcento
            )
            if (subtitulo != null) {
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = AppColors.colorGrisIcono)
            }
        }
        if (mostrarFlecha) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.colorLetraGris)
        }
    }
}



// al cerrar sesión vuelve al menú como visitante (usa la zona guardada)
private fun navigateToLogin(navController: NavHostController) {
    navController.navigate(Routes.VistaPrincipal.createRoute("menu")) {
        popUpTo(0) {
            inclusive = true
        }
        launchSingleTop = true // Asegura que no se creen múltiples instancias de VistaLogin
    }
}


