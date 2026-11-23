package com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.RegistroNuevaDireccionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val VerdeZona = Color(0xFF2E7D32)

@Composable
fun RegistrarNuevaDireccionScreen(
    navController: NavHostController,
    idzona: Int,
    latitud: String,
    longitud: String,
    latitudreal: String?,
    longitudreal: String?,
    viewModel: RegistroNuevaDireccionViewModel = viewModel { RegistroNuevaDireccionViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)

    val tokenManager = remember { TokenManager(ctx) }
    var idusuario by remember { mutableStateOf("") }

    val nombre by viewModel.nombre.collectAsState("")
    val etiqueta by viewModel.etiqueta.collectAsState("")
    // "Otro": se muestra el campo para escribir una etiqueta propia
    var etiquetaPropia by remember { mutableStateOf(false) }
    val telefono by viewModel.telefono.collectAsState("")
    val puntoReferencia by viewModel.puntoReferencia.collectAsState("")
    val direccion by viewModel.direccion.collectAsState("")

    val keyboardController = LocalSoftwareKeyboardController.current

    // MODALES
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }
    var showModal2Boton by remember { mutableStateOf(false) }

    val texto8CaracteresTelefono = stringResource(Res.string.telefono_son_8_digitos)

    LaunchedEffect(Unit) { idusuario = tokenManager.idUsuario.first() }

    val guardar: () -> Unit = {
        keyboardController?.hide()
        when {
            nombre.isBlank() -> {
                modalMensajeString = "Escribe el nombre de quien recibe el pedido"
                showModal1Boton = true
            }
            telefono.isBlank() -> {
                modalMensajeString = textoRes(Res.string.telefono_es_requerido)
                showModal1Boton = true
            }
            telefono.count { it.isDigit() } < 8 -> {
                modalMensajeString = texto8CaracteresTelefono
                showModal1Boton = true
            }
            direccion.isBlank() -> {
                modalMensajeString = textoRes(Res.string.direccion_es_requerido)
                showModal1Boton = true
            }
            else -> showModal2Boton = true
        }
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Nueva dirección", AppColors.colorRojo) },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                BotonAuthPrincipal(
                    texto = "Guardar dirección",
                    onClick = guardar,
                    modifier = Modifier.navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                // el teclado ya lo descuenta la barra de abajo (sube con él): no repetir imePadding aquí
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ---- ubicación elegida en el mapa ----
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, VerdeZona.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VerdeZona.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeZona)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ubicación marcada", fontWeight = FontWeight.Bold, color = AppColors.colorLetraPrincipal)
                        Text(
                            "Está dentro de la zona de entrega",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.colorGrisIcono
                        )
                    }
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cambiar", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ---- datos de la dirección ----
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Text("¿Cómo quieres llamarla? (opcional)", fontWeight = FontWeight.SemiBold, color = AppColors.colorLetraPrincipal)

                    // etiqueta de la dirección: tocar de nuevo la elegida la quita
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipEtiqueta("Casa", Icons.Filled.Home, seleccionado = !etiquetaPropia && etiqueta == "Casa") {
                            etiquetaPropia = false
                            viewModel.setEtiqueta(if (etiqueta == "Casa") "" else "Casa")
                        }
                        ChipEtiqueta("Trabajo", Icons.Filled.Work, seleccionado = !etiquetaPropia && etiqueta == "Trabajo") {
                            etiquetaPropia = false
                            viewModel.setEtiqueta(if (etiqueta == "Trabajo") "" else "Trabajo")
                        }
                        ChipEtiqueta("Otro", Icons.Filled.Apartment, seleccionado = etiquetaPropia) {
                            etiquetaPropia = !etiquetaPropia
                            viewModel.setEtiqueta("")
                        }
                    }

                    if (etiquetaPropia) {
                        CampoAuth(
                            valor = etiqueta,
                            onCambio = { viewModel.setEtiqueta(it) },
                            etiqueta = "Nombre de la dirección",
                            icono = Icons.Filled.Badge,
                            maxLength = 50,
                            ayuda = "Ej. Casa de mamá, Oficina",
                        )
                    }

                    HorizontalDivider(color = AppColors.colorRedondeadoGris)

                    Text("¿Quién recibe el pedido?", fontWeight = FontWeight.SemiBold, color = AppColors.colorLetraPrincipal)

                    CampoAuth(
                        valor = nombre,
                        onCambio = { viewModel.setNombre(it) },
                        etiqueta = "Nombre de quien recibe",
                        icono = Icons.Filled.Person,
                        maxLength = 100,
                    )

                    CampoAuth(
                        valor = telefono,
                        onCambio = { viewModel.setTelefono(limpiarTelefono(it)) },
                        etiqueta = stringResource(Res.string.telefono),
                        icono = Icons.Filled.Phone,
                        // 8 digitos locales, o numero de otro pais con codigo (hasta 15 digitos + "+")
                        maxLength = 16,
                        keyboardType = KeyboardType.Phone,
                        ayuda = "El repartidor llamará a este número.",
                    )

                    CampoAuth(
                        valor = direccion,
                        onCambio = { viewModel.setDireccion(it) },
                        etiqueta = stringResource(Res.string.direccion),
                        icono = Icons.Filled.Map,
                        maxLength = 400,
                        ayuda = "Colonia, calle, pasaje y número de casa",
                    )

                    CampoAuth(
                        valor = puntoReferencia ?: "",
                        onCambio = { viewModel.setPuntoReferencia(it) },
                        etiqueta = "Punto de referencia (opcional)",
                        icono = Icons.Filled.Place,
                        maxLength = 400,
                        imeAction = ImeAction.Done,
                        onIme = guardar,
                        ayuda = "Ej. portón negro, frente a la tienda",
                    )
                }
            }
        }

        if (showModal1Boton) {
            CustomModal1Boton(
                showDialog = showModal1Boton,
                message = modalMensajeString,
                onDismiss = { showModal1Boton = false }
            )
        }

        // Confirmación registrar dirección
        if (showModal2Boton) {
            CustomModal2Botones(
                showDialog = true,
                titulo = "¿Guardar esta dirección?",
                message = "La usaremos para tus próximos pedidos.",
                onDismiss = { showModal2Boton = false },
                onAccept = {
                    showModal2Boton = false
                    scope.launch {
                        viewModel.registrarNuevaDireccionRetrofit(
                            idusuario,
                            idzona.toString(),
                            latitud,
                            longitud,
                            latitudreal,
                            longitudreal
                        )
                    }
                },
                txtBtnAceptar = "Guardar",
                txtBtnCancelar = "Cancelar",
            )
        }

        if (isLoading) LoadingModal(isLoading = true)
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                CustomToasty(ctx, stringResource(Res.string.direccion_registrada), ToastType.SUCCESS)
                navController.navigate(Routes.VistaPrincipal.createRoute("menu")) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}

/** Atajo para la etiqueta de la dirección (Casa, Trabajo, Otro). */
@Composable
private fun ChipEtiqueta(texto: String, icono: ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        leadingIcon = { Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            iconColor = AppColors.colorGrisIcono,
            labelColor = AppColors.colorLetraPrincipal,
            selectedContainerColor = AppColors.colorLetraRoja,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
        )
    )
}


/** Solo dígitos y un "+" opcional al inicio (para números de otro país). */
private fun limpiarTelefono(texto: String): String {
    val digitos = texto.filter { it.isDigit() }
    return if (texto.trimStart().startsWith("+")) "+$digitos" else digitos
}
