package com.tatanstudios.astropollocliente.vistas.principal.productos

import kotlinx.coroutines.delay
import com.tatanstudios.astropollocliente.componentes.rememberSesionIniciada
import com.tatanstudios.astropollocliente.componentes.DialogoIniciarSesion
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.Icons
import com.tatanstudios.astropollocliente.componentes.ListaProductosSkeleton
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
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
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosTerceraArray
import com.tatanstudios.astropollocliente.network.ApiConfig

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListadoProductosScreen(navController: NavHostController,
                           idCategoria: Int,
                           viewModel: ListadoProductosViewModel = viewModel { ListadoProductosViewModel() }
) {

    val ctx = LocalPlatformContext.current
    var boolDatosCargados by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState(true)
    val tokenManager = remember { TokenManager(ctx) }
    val resultado by viewModel.resultado.collectAsState()
    val scope = rememberCoroutineScope() // Crea el alcance de coroutine
    val keyboardController = LocalSoftwareKeyboardController.current

    var modeloListaProductosArray: List<ModeloProductosArray> by remember { mutableStateOf(listOf<ModeloProductosArray>()) }


    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.listadoProductosRetrofit(idCategoria)
        }
    }

    // carrito: el visitante debe iniciar sesión; con sesión se abre (sin abrirlo dos veces)
    val sesionIniciada = rememberSesionIniciada()
    var pedirSesion by remember { mutableStateOf(false) }
    var puedeAbrirCarrito by remember { mutableStateOf(true) }
    val abrirCarrito: () -> Unit = {
        when (sesionIniciada) {
            false -> pedirSesion = true
            true -> if (puedeAbrirCarrito) {
                puedeAbrirCarrito = false
                navController.navigate(Routes.VistaCarrito.route) { launchSingleTop = true }
                scope.launch {
                    delay(600)
                    puedeAbrirCarrito = true
                }
            }
            null -> {} // todavía se está leyendo la sesión
        }
    }

    // ocultar teclado
    keyboardController?.hide()





    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = {
            BarraToolbarColor(
                navController,
                stringResource(Res.string.productos),
                AppColors.colorRojo,
                acciones = {
                    IconButton(onClick = abrirCarrito) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Ver carrito", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // sub categorías que sí tienen productos
            val secciones = modeloListaProductosArray.filter { it.listaProductoCategoriaTercera.isNotEmpty() }
            val listaState = rememberLazyListState()

            // índice en la lista donde empieza cada sub categoría (encabezado + productos)
            val inicioSeccion = remember(secciones) {
                var acumulado = 0
                secciones.map { s -> acumulado.also { acumulado += 1 + s.listaProductoCategoriaTercera.size } }
            }
            val seccionActual by remember(secciones) {
                derivedStateOf {
                    val primero = listaState.firstVisibleItemIndex
                    inicioSeccion.indexOfLast { it <= primero }.coerceAtLeast(0)
                }
            }

            if (boolDatosCargados && secciones.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(Res.drawable.camaradefecto), contentDescription = null, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay productos disponibles en esta categoría", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Pueden estar agotados o fuera de horario.", style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisIcono, textAlign = TextAlign.Center)
                }
            } else if (boolDatosCargados) {
              Column(Modifier.fillMaxSize()) {
                // chips para saltar a cada sub categoría (solo si hay más de una)
                if (secciones.size > 1) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().background(Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(secciones) { i, s ->
                            val activo = i == seccionActual
                            Text(
                                text = s.nombre ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                color = if (activo) Color.White else AppColors.colorLetraPrincipal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (activo) AppColors.colorLetraRoja else AppColors.colorRedondeadoGris)
                                    .clickable { scope.launch { listaState.animateScrollToItem(inicioSeccion[i]) } }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listaState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    secciones.forEach { categoria ->
                        // encabezado fijo de la sub categoría mientras se recorre su lista
                        stickyHeader {
                            CategoriaHeader(title = categoria.nombre ?: "")
                        }

                        // Productos de la categoría
                        items(categoria.listaProductoCategoriaTercera) { producto ->
                            ProductoItemCard(
                                producto = producto,
                                onClick = {

                                    navController.navigate(
                                        Routes.VistaInformacionProducto.createRoute(producto.id)
                                    ) {
                                        popUpTo(Routes.VistaInformacionProducto.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
              }
            }
        }

        ErrorReintentar(viewModel, Modifier.padding(innerPadding))

        // primera carga: skeleton con la forma de la lista; recargas: modal
        if (pedirSesion) {
            DialogoIniciarSesion(
                navController,
                "Para ver tu carrito y hacer pedidos necesitas iniciar sesión o crear una cuenta.",
            ) { pedirSesion = false }
        }

        if (isLoading && !boolDatosCargados) ListaProductosSkeleton(Modifier.padding(innerPadding))
        else if (isLoading) LoadingModal(isLoading = true)

        resultado?.getContentIfNotHandled()?.let { result ->
            when (result.success) {
                1 -> {
                    modeloListaProductosArray = result.listaProductoCategoria
                    boolDatosCargados = true
                }

                else -> viewModel.marcarError()
            }
        }
    }




}

// Encabezado de categoría con estilo solicitado
@Composable
fun CategoriaHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.colorCremaV1) // fondo opaco: es encabezado fijo
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            color = AppColors.colorLetraPrincipal,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Box(
            Modifier
                .padding(start = 4.dp, top = 4.dp)
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(AppColors.colorLetraRoja)
        )
    }
}

// Card del producto con imagen abajo-derecha y fallback
@Composable
fun ProductoItemCard(
    producto: ModeloProductosTerceraArray,
    onClick: () -> Unit
) {
    val baseUrlImagenes = "${ApiConfig.URL_IMAGENES}${producto.imagen}"
    var traeImagen = (producto.utilizaImagen == 1)
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
                    text = producto.nombre ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (!producto.descripcion.isNullOrBlank()) {
                    Text(
                        text = producto.descripcion.replace("\\r\\n|\\n".toRegex(), " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!producto.precio.isNullOrBlank()) {
                    Text(
                        text = "$${producto.precio}",
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
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.colorRedondeadoGris),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.camaradefecto),
                            contentDescription = producto.nombre,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}