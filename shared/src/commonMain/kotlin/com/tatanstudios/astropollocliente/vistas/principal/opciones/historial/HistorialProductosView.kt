package com.tatanstudios.astropollocliente.vistas.principal.opciones.historial

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.HorizontalDivider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.painterResource
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import androidx.compose.material.icons.filled.ShoppingBag
import com.tatanstudios.astropollocliente.extras.formatoDosDecimales
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
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
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ProductoListadoHistorialItemCard
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductoHistorialOrdenesArray
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.viewmodel.ListadoProductosHistorialOrdenViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ListadoProductosHistorialScreen(navController: NavHostController, _idorden: Int,
                                    viewModelProductosListaHistorial: ListadoProductosHistorialOrdenViewModel = viewModel { ListadoProductosHistorialOrdenViewModel() },
) {
    val isLoading by viewModelProductosListaHistorial.isLoading.collectAsState(initial = true)
    val resultado by viewModelProductosListaHistorial.resultado.collectAsState()
    var boolDatosCargados by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf(listOf<ModeloProductoHistorialOrdenesArray>()) }

    LaunchedEffect(Unit) { viewModelProductosListaHistorial.listaProductosHistorialRetrofit(idorden = _idorden) }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Orden #$_idorden", AppColors.colorRojo) }
    ) { innerPadding ->

        if (boolDatosCargados) {
            val unidades = productos.sumOf { it.cantidad }
            val total = productos.sumOf { montoDe(it.multiplicado) }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---- resumen ----
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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.colorLetraRoja.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = AppColors.colorLetraRoja)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "$unidades ${if (unidades == 1) "producto" else "productos"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.colorLetraPrincipal
                                )
                                Text(
                                    "Toca un producto para ver el detalle",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.colorGrisIcono
                                )
                            }
                            if (total > 0.0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Productos", style = MaterialTheme.typography.labelSmall, color = AppColors.colorGrisIcono)
                                    Text(
                                        "$" + formatoDosDecimales(total),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.colorLetraRoja
                                    )
                                }
                            }
                        }
                    }
                }

                items(productos, key = { it.id }) { p ->
                    TarjetaProductoHistorial(p) {
                        navController.navigate(
                            Routes.VistaInfoProductoHistorialOrden.createRoute(p.id.toString()),
                            navOptions { launchSingleTop = true }
                        )
                    }
                }
            }
        } else if (isLoading) {
            ContenedorSkeleton(Modifier.padding(innerPadding).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BloqueSkeleton(Modifier.fillMaxWidth().height(80.dp), radio = 18.dp)
                    repeat(3) { BloqueSkeleton(Modifier.fillMaxWidth().height(96.dp), radio = 16.dp) }
                }
            }
        }

        ErrorReintentar(viewModelProductosListaHistorial, Modifier.padding(innerPadding))
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                productos = result.lista
                boolDatosCargados = true
            }
            else -> viewModelProductosListaHistorial.marcarError()
        }
    }
}

/** Agrega "$" si la API mandó el monto sin signo ("3.00" -> "$3.00"). */
private fun conSigno(monto: String?): String {
    val m = monto?.trim().orEmpty()
    return if (m.isEmpty() || m.startsWith("$")) m else "$$m"
}

/** "$1,234.50" -> 1234.5 (0 si no se puede leer). */
internal fun montoDe(texto: String?): Double =
    texto?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull() ?: 0.0

/** Imagen del producto (o cámara por defecto) en un cuadro redondeado. */
@Composable
internal fun ImagenProductoHistorial(utilizaImagen: Int, imagen: String?, tamano: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (utilizaImagen == 1 && !imagen.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data("${ApiConfig.URL_IMAGENES}$imagen")
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(Res.drawable.spinloading),
                error = painterResource(Res.drawable.camaradefecto),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(painterResource(Res.drawable.camaradefecto), contentDescription = null, modifier = Modifier.size(tamano * 0.5f))
        }
    }
}

@Composable
private fun TarjetaProductoHistorial(p: ModeloProductoHistorialOrdenesArray, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenProductoHistorial(p.utilizaImagen, p.imagen, 72.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    p.nombreProducto ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.colorLetraPrincipal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // opciones elegidas (modificadores), una línea por bloque
                p.opciones.forEach { linea ->
                    Text(
                        linea,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.colorGrisv2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!p.nota.isNullOrBlank()) {
                    Text(
                        "Nota: ${p.nota}",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = AppColors.colorGrisIcono,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // cantidad x precio unitario  ·  total
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        "${p.cantidad} × ${conSigno(p.precio)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.colorGrisIcono,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.colorRedondeadoGris)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        conSigno(p.multiplicado ?: p.precio),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorLetraRoja
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.colorLetraGris)
        }
    }
}
