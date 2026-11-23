package com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones

import com.tatanstudios.astropollocliente.componentes.TipoAviso
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColorDireccion
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.viewmodel.BorrarDireccionViewModel
import com.tatanstudios.astropollocliente.viewmodel.SeleccionarDireccionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SeleccionarDireccionScreen(navController: NavHostController,
                                  id: Int, // id direccion
                                  _nombre: String,
                                  _telefono: String?,
                                  _direccion: String?,
                                  _puntoReferencia: String?,
                                  etiqueta: String? = null,
                                  viewModelSeleccionar: SeleccionarDireccionViewModel = viewModel { SeleccionarDireccionViewModel() },
                                  viewModelBorrar: BorrarDireccionViewModel = viewModel { BorrarDireccionViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    val resultadoSeleccionar by viewModelSeleccionar.resultado.collectAsState()
    val isLoadingSeleccionar by viewModelSeleccionar.isLoading.collectAsState(false)

    val resultadoBorrar by viewModelBorrar.resultado.collectAsState()
    val isLoadingBorrar by viewModelBorrar.isLoading.collectAsState(false)

    val tokenManager = remember { TokenManager(ctx) }
    var idusuario by remember { mutableStateOf("") }

    var showModal2BotonBorrar by remember { mutableStateOf(false) }
    // confirmar antes de cambiar: si la nueva dirección es de otro restaurante, la API vacía el carrito
    var showModalSeleccionar by remember { mutableStateOf(false) }
    var showModal1Boton by remember { mutableStateOf(false) }

    val textoNosePuedeBorrar = stringResource(Res.string.no_se_puede_eliminar_direccion_ultima)

    LaunchedEffect(Unit) { idusuario = tokenManager.idUsuario.first() }

    val telefono = _telefono.nullSiLiteral()
    val direccion = _direccion.nullSiLiteral()
    val referencia = _puntoReferencia.nullSiLiteral()

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, stringResource(Res.string.direccion), AppColors.colorRojo) },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                Column(
                    modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotonAuthPrincipal(texto = "Usar esta dirección", onClick = { showModalSeleccionar = true })
                    OutlinedButton(
                        onClick = { showModal2BotonBorrar = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, AppColors.colorLetraRoja),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.colorLetraRoja),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar dirección", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ---- encabezado: nombre de la dirección ----
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
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = etiqueta?.takeIf { it.isNotBlank() } ?: _nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.colorLetraPrincipal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Dirección de entrega",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.colorGrisIcono
                        )
                    }
                }
            }

            // ---- detalle ----
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    FilaDatoDireccion(Icons.Filled.Person, "Recibe", _nombre)
                    HorizontalDivider(Modifier.padding(start = 68.dp), color = AppColors.colorRedondeadoGris)
                    FilaDatoDireccion(Icons.Filled.Phone, "Teléfono", telefono ?: "Sin teléfono")
                    HorizontalDivider(Modifier.padding(start = 68.dp), color = AppColors.colorRedondeadoGris)
                    FilaDatoDireccion(Icons.Filled.Map, "Dirección", direccion ?: "Sin dirección")
                    if (referencia != null) {
                        HorizontalDivider(Modifier.padding(start = 68.dp), color = AppColors.colorRedondeadoGris)
                        FilaDatoDireccion(Icons.Filled.Place, "Punto de referencia", referencia)
                    }
                }
            }

            // ---- aviso ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.colorLetraRoja.copy(alpha = 0.06f))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Al usar esta dirección, el menú será el del restaurante que la atiende. Si cambia de restaurante, tu carrito se vacía.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.colorGrisv2
                )
            }
        }

        if (isLoadingSeleccionar) LoadingModal(isLoading = true)
        if (isLoadingBorrar) LoadingModal(isLoading = true)

        if (showModal1Boton) {
            CustomModal1Boton(showModal1Boton, textoNosePuedeBorrar, onDismiss = { showModal1Boton = false })
        }

        // CONFIRMAR PARA USAR ESTA DIRECCION
        if (showModalSeleccionar) {
            CustomModal2Botones(
                showDialog = true,
                message = "¿Usar esta dirección para tus pedidos?\n\nSi la atiende otro restaurante, el menú cambiará y tu carrito se vaciará.",
                onDismiss = { showModalSeleccionar = false },
                onAccept = {
                    showModalSeleccionar = false
                    scope.launch { viewModelSeleccionar.seleccionarDireccionRetrofit(idusuario, id) }
                },
                txtBtnAceptar = "Usar",
                txtBtnCancelar = stringResource(Res.string.cancelar),
            )
        }

        // CONFIRMAR PARA BORRAR DIRECCION
        if (showModal2BotonBorrar) {
            CustomModal2Botones(
                showDialog = true,
                titulo = "¿Eliminar esta dirección?",
                message = "Ya no aparecerá en tu lista. No se puede deshacer.",
                tipo = TipoAviso.PELIGRO,
                onDismiss = { showModal2BotonBorrar = false },
                onAccept = {
                    showModal2BotonBorrar = false
                    scope.launch { viewModelBorrar.borrarDireccionRetrofit(idusuario, id) }
                },
                txtBtnAceptar = "Eliminar",
                txtBtnCancelar = "Cancelar",
            )
        }
    }

    resultadoSeleccionar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                CustomToasty(ctx, stringResource(Res.string.direccion_seleccionada), ToastType.SUCCESS)
                navController.popBackStack()
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }

    resultadoBorrar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // DIRECCION BORRADA
                CustomToasty(ctx, stringResource(Res.string.direccion_borrada), ToastType.SUCCESS)
                navController.popBackStack()
            }
            2 -> showModal1Boton = true // NO SE PUEDE BORRAR, MINIMO 1 DIRECCION
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}

/** Fila del detalle: ícono en círculo, etiqueta pequeña y valor. */
@Composable
private fun FilaDatoDireccion(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AppColors.colorRedondeadoGris),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = AppColors.colorGrisIcono)
            Text(valor, style = MaterialTheme.typography.bodyLarge, color = AppColors.colorLetraPrincipal)
        }
    }
}

fun String?.nullSiLiteral(): String? =
    this?.trim()?.let { if (it.isEmpty() || it.equals("null", true)) null else it }