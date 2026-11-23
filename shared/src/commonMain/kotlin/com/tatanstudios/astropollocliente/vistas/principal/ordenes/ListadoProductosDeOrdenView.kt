package com.tatanstudios.astropollocliente.vistas.principal.productos

import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.tatanstudios.astropollocliente.model.modelos.ModeloDireccionesArray
import com.tatanstudios.astropollocliente.model.modelos.ModeloHistorialOrdenesArray
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductos
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosArray
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.viewmodel.HistorialFechasBuscarViewModel
import com.tatanstudios.astropollocliente.viewmodel.ListadoProductosViewModel
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosDeOrden
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosDeOrdenArray
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosTerceraArray
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.viewmodel.ListadoProductosDeUnaOrdenViewModel

/**
 * Productos de una orden activa: es el mismo endpoint que el historial, así que se usa la misma
 * pantalla (tarjetas con opciones, nota y detalle al tocar).
 */
@Composable
fun ListadoProductosDeUnaOrdenScreen(navController: NavHostController, idorden: Int) {
    com.tatanstudios.astropollocliente.vistas.principal.opciones.historial.ListadoProductosHistorialScreen(
        navController = navController,
        _idorden = idorden,
    )
}


@Composable
fun ProductoItemCardDeOrden(
    producto: ModeloProductosDeOrdenArray,
    onClick: () -> Unit
) {
    val baseUrlImagenes = "${ApiConfig.URL_IMAGENES}${producto.imagen}"
    var traeImagen = (producto.utiliza_imagen == 1)
    if (producto.imagen.isNullOrBlank()) traeImagen = false

    val imageSlot = 96.dp // ancho reservado para la imagen + padding

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // Texto: reservamos espacio a la derecha para la imagen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .padding(end = imageSlot),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = producto.nombreproducto ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.colorNegro
                )

                // opciones elegidas (modificadores), una línea por bloque
                com.tatanstudios.astropollocliente.componentes.LineasOpciones(producto.opciones)

                Text(
                    text = "Precio: $${producto.precio}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorNegro
                )

                Text(
                    text = "Cantidad: ${producto.cantidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorNegro
                )

                Text(
                    text = "Total: ${producto.multiplicado}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorNegro
                )

                if(!producto.nota.isNullOrBlank()){
                    Text(
                        text = "Nota: ${producto.nota}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorRojo
                    )
                }

            }

            // Imagen: CENTRADA verticalmente a la derecha
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)   // <-- clave
                    .padding(end = 10.dp)
                    .size(72.dp)
            ) {
                if (traeImagen) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(baseUrlImagenes)
                            .crossfade(true)
                            .build(),
                            placeholder = painterResource(Res.drawable.spinloading),
                            error = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombreproducto,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombreproducto,
                        modifier = Modifier
                            .size(72.dp)                 // 👈 define tamaño cuadrado
                            .clip(CircleShape)           // 👈 recorta en forma de círculo
                            .border(2.dp, Color.LightGray, CircleShape), // 👈 borde opcional
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
