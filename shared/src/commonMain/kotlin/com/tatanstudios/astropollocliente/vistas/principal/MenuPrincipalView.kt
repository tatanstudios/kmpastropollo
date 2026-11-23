package com.tatanstudios.astropollocliente.vistas.opciones.menu

import com.tatanstudios.astropollocliente.extras.SesionApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.tatanstudios.astropollocliente.model.modelos.ModeloMenuPrincipalSliderArray
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.extras.redireccionarAjustes
import androidx.compose.ui.platform.LocalLayoutDirection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.extras.SolicitarPermisosUbicacion
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.modelos.ModeloMenuPrincipalCategoriasArray
import com.tatanstudios.astropollocliente.model.modelos.ModeloMenuPrincipalPopularesArray
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductoBuscable
import androidx.compose.runtime.saveable.rememberSaveable
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.ui.theme.ColorBlanco
import com.tatanstudios.astropollocliente.ui.theme.ColorGris
import com.tatanstudios.astropollocliente.viewmodel.ListadoMenuPrincipal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuPrincipalScreen(
    navController: NavHostController,
    viewModel: ListadoMenuPrincipal = viewModel { ListadoMenuPrincipal() },
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current

    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()

    val tokenManager = remember { TokenManager(ctx) }
    var idusuario by remember { mutableStateOf("") }
    var esInvitado by remember { mutableStateOf(false) }

    var boolDatosCargados by remember { mutableStateOf(false) }
    var popPermisoGPS by remember { mutableStateOf(false) }

    var listaSlider by remember { mutableStateOf(listOf<ModeloMenuPrincipalSliderArray>()) }
    var modeloListaCategoriasArray by remember { mutableStateOf(listOf<ModeloMenuPrincipalCategoriasArray>()) }
    var modeloListaPopularesArray by remember { mutableStateOf(listOf<ModeloMenuPrincipalPopularesArray>()) }
    var productosBuscables by remember { mutableStateOf(listOf<ModeloProductoBuscable>()) }
    var entregaNombre by remember { mutableStateOf<String?>(null) }
    var entregaDireccion by remember { mutableStateOf<String?>(null) }
    var sucursal by remember { mutableStateOf<String?>(null) }

    var busqueda by rememberSaveable { mutableStateOf("") }
    val resultadosBusqueda = remember(busqueda, productosBuscables) { buscarProductos(productosBuscables, busqueda) }
    val listaState = rememberLazyListState()
    // al escribir (o borrar) la búsqueda, mostrar desde arriba
    LaunchedEffect(busqueda) { listaState.scrollToItem(0) }

    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }
    var showModal1BotonUsuarioBloqueado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        idusuario = tokenManager.idUsuario.first()
        if (idusuario.isNotEmpty()) {
            viewModel.listadoMenuPrincipalRetrofit(idusuario)
        } else {
            // VISITANTE: el menú es el de la sucursal que eligió
            val idSucursal = tokenManager.idSucursal.first()
            if (idSucursal == null) {
                navigateToElegirSucursal(navController)
            } else {
                esInvitado = true
                viewModel.listadoMenuInvitadoRetrofit(idSucursal)
            }
        }
    }

    if (boolDatosCargados && !esInvitado) {
        SolicitarPermisosUbicacion(
            onPermisosConcedidos = { },
            onPermisosDenegados = { }
        )
    }

    Scaffold(
        topBar = {
            EncabezadoMenu(
                esInvitado = esInvitado,
                entregaNombre = entregaNombre,
                entregaDireccion = entregaDireccion,
                sucursal = sucursal,
                busqueda = busqueda,
                onBusquedaChange = { busqueda = it },
                mostrarBuscador = productosBuscables.isNotEmpty(),
                onCambiarEntrega = {
                    if (esInvitado) {
                        navController.navigate(Routes.VistaElegirSucursal.createRoute(automatico = false)) {
                            launchSingleTop = true
                        }
                    } else {
                        navigateToDirecciones(navController)
                    }
                },
                onPerfil = {
                    // visitante → iniciar sesión; con sesión → perfil
                    navController.navigate(if (esInvitado) Routes.VistaLogin.route else Routes.VistaPerfil.route) {
                        launchSingleTop = true
                    }
                },
            )
        },
        // 👇 evita que este Scaffold agregue insets extra que se sumen al padre
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->

        // 👇 combinamos el top padding del topBar local con el padding del padre (FAB/bottom bar)
        val totalPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection)
        )

        LazyColumn(
            state = listaState,
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.colorCremaV1),
            contentPadding = totalPadding
        ) {
          if (busqueda.isNotBlank()) {
            // ---- modo búsqueda: solo resultados ----
            if (resultadosBusqueda.isEmpty()) {
                item { SinResultadosBusqueda(busqueda) }
            } else {
                item { Spacer(Modifier.height(7.dp)) }
                items(resultadosBusqueda, key = { it.id }) { prod ->
                    FilaResultadoBusqueda(prod) {
                        navController.navigate(Routes.VistaInformacionProducto.createRoute(prod.id)) {
                            launchSingleTop = true
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
          } else if (isLoading && !boolDatosCargados) {
            // ---- cargando por primera vez: skeleton con la forma del menú ----
            item { MenuSkeleton() }
          } else {
            // 1) Banners (la API manda la lista vacía y hayslider = 0 cuando no hay ninguno)
            if (listaSlider.isNotEmpty()) {
                item {
                    CarruselBanners(
                        banners = listaSlider,
                        onAbrirProducto = { idProducto ->
                            navController.navigate(Routes.VistaInformacionProducto.createRoute(idProducto)) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }

            // 2) Categorías: todas visibles en cuadrícula (ya no hay "ver más")
            if (modeloListaCategoriasArray.isNotEmpty()) {
                item { TituloSeccionMenu("Categorías") }

                items(modeloListaCategoriasArray.chunked(3)) { fila ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        fila.forEach { categoria ->
                            TarjetaCategoria(
                                nombre = categoria.nombre ?: "",
                                imagenUrl = "${ApiConfig.URL_IMAGENES}${categoria.imagen}",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    navController.navigate(Routes.VistaListadoProductos.createRoute(categoria.id)) {
                                        popUpTo(Routes.VistaListadoProductos.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                        // completar la fila para que las tarjetas mantengan el mismo ancho
                        repeat(3 - fila.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // 3) Populares
            if (modeloListaPopularesArray.isNotEmpty()) {
                item { Spacer(Modifier.height(6.dp)) }
                item { TituloSeccionMenu("Populares") }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        items(modeloListaPopularesArray) { prod ->
                            TarjetaPopular(
                                nombre = prod.nombre ?: "",
                                precio = prod.precio ?: "",
                                imagenUrl = if (prod.utilizaImagen == 1 && !prod.imagen.isNullOrBlank()) "${ApiConfig.URL_IMAGENES}${prod.imagen}" else null,
                                onClick = {
                                    navController.navigate(Routes.VistaInformacionProducto.createRoute(prod.id)) {
                                        popUpTo(Routes.VistaInformacionProducto.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                    }
                }
            }

            // 4) Sin nada que mostrar (restaurante sin categorías disponibles a esta hora)
            if (boolDatosCargados && modeloListaCategoriasArray.isEmpty() && modeloListaPopularesArray.isEmpty()) {
                item { MenuVacio() }
            }
          }
        }

        // Falló la carga: panel con "Reintentar" (debajo de la toolbar)
        ErrorReintentar(viewModel, Modifier.padding(totalPadding))

        // ======= tus diálogos/modales abajo tal cual =======
        // la primera carga usa el skeleton; el modal queda solo para recargas con datos ya visibles
        if (isLoading && boolDatosCargados) LoadingModal(true)

        LaunchedEffect(resultado) {
            resultado?.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    1 -> showModal1BotonUsuarioBloqueado = true
                    2 -> navigateToDirecciones(navController, btnBloqueoAtras = 1)
                    3 -> {
                        listaSlider = if (result.haySlider == 0) emptyList() else result.arraySlider
                        modeloListaCategoriasArray = result.arrayCategorias
                        modeloListaPopularesArray = result.arrayPopulares
                        productosBuscables = result.productosBuscables
                        entregaNombre = result.entregaNombre
                        entregaDireccion = result.entregaDireccion
                        sucursal = result.sucursal
                        boolDatosCargados = true
                    }
                    4 -> if (esInvitado) {
                        // la sucursal guardada ya no está disponible: volver a elegir
                        CustomToasty(ctx, result.mensaje ?: "Sucursal no disponible", ToastType.INFO)
                        navigateToElegirSucursal(navController)
                    } else {
                        viewModel.marcarError(result.mensaje ?: "El menú no está disponible en este momento")
                    }
                    5 -> navigateToDirecciones(navController, btnBloqueoAtras = 1)
                    else -> viewModel.marcarError()
                }
            }
        }

        if (showModal1Boton) {
            CustomModal1Boton(showModal1Boton, modalMensajeString) { showModal1Boton = false }
        }

        if (showModal1BotonUsuarioBloqueado) {
            CustomModal1Boton(showModal1BotonUsuarioBloqueado, textoRes(Res.string.usuario_bloqueado)) {
                scope.launch {
                    SesionApi.cerrarSesion()
                    showModal1BotonUsuarioBloqueado = false
                    navigateToLogin(navController)
                }
            }
        }

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
    } // end Scaffold
}



private fun navigateToLogin(navController: NavHostController) {
    navController.navigate(Routes.VistaLogin.route) {
        popUpTo(Routes.VistaPrincipal.route) {
            inclusive = true // Elimina VistaPrincipal de la pila
        }
        launchSingleTop = true // Asegura que no se creen múltiples instancias de VistaLogin
    }
}


private fun navigateToElegirSucursal(navController: NavHostController) {
    navController.navigate(Routes.VistaElegirSucursal.createRoute(automatico = true)) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}


private fun navigateToDirecciones(
    navController: NavHostController,
    btnBloqueoAtras: Int = 0
) {

    navController.navigate(
        Routes.VistaMisDirecciones.createRoute(btnBloqueoAtras)
    ) {
        popUpTo(Routes.VistaMisDirecciones.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}


// ======================= piezas del menú principal =======================

@Composable
private fun TituloSeccionMenu(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = AppColors.colorLetraPrincipal,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp)
    )
}

/** Carrusel de banners: avanza solo cada 5 s y, si el banner abre un producto, lo abre al tocarlo. */
@Composable
private fun CarruselBanners(
    banners: List<ModeloMenuPrincipalSliderArray>,
    onAbrirProducto: (Int) -> Unit,
) {
    val ctx = LocalPlatformContext.current
    val pagerState = rememberPagerState(pageCount = { banners.size })

    if (banners.size > 1) {
        LaunchedEffect(pagerState) {
            while (true) {
                kotlinx.coroutines.delay(5_000)
                if (!pagerState.isScrollInProgress) {
                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
                }
            }
        }
    }

    Column(modifier = Modifier.padding(top = 12.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val banner = banners[page]
            val abreProducto = banner.redireccionamiento == 1 && banner.idProducto > 0
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data("${ApiConfig.URL_IMAGENES}${banner.imagen}")
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(Res.drawable.spinloading),
                error = painterResource(Res.drawable.camaradefecto),
                contentDescription = banner.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.colorRedondeadoGris)
                    .clickable(enabled = abreProducto) { onAbrirProducto(banner.idProducto) }
            )
        }

        if (banners.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { i ->
                    val activo = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(if (activo) AppColors.colorLetraRoja else Color(0xFFD5D5D5))
                            .size(width = if (activo) 18.dp else 7.dp, height = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCategoria(nombre: String, imagenUrl: String, modifier: Modifier, onClick: () -> Unit) {
    val ctx = LocalPlatformContext.current
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(imagenUrl).crossfade(true).build(),
                placeholder = painterResource(Res.drawable.spinloading),
                error = painterResource(Res.drawable.camaradefecto),
                contentDescription = nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.colorLetraPrincipal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TarjetaPopular(nombre: String, precio: String, imagenUrl: String?, onClick: () -> Unit) {
    val ctx = LocalPlatformContext.current
    Card(
        modifier = Modifier.width(156.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.colorRedondeadoGris),
                contentAlignment = Alignment.Center
            ) {
                if (imagenUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(imagenUrl).crossfade(true).build(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                        contentDescription = nombre,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(6.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.camaradefecto),
                        contentDescription = nombre,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = precio,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraRoja,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AppColors.colorLetraRoja),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MenuVacio() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.camaradefecto),
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "El menú no está disponible en este momento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = AppColors.colorLetraPrincipal
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Vuelve a intentarlo más tarde o revisa el horario del restaurante.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = AppColors.colorGrisIcono
        )
    }
}
