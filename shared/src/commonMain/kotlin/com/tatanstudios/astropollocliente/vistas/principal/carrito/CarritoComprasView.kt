package com.tatanstudios.astropollocliente.vistas.principal.carrito

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.sp
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.componentes.TipoAviso
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tatanstudios.astropollocliente.extras.TokenManager
import kotlinx.coroutines.flow.first
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import com.tatanstudios.astropollocliente.componentes.LineasOpciones
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColorCarritoCompras
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import com.tatanstudios.astropollocliente.model.modelos.ModeloCarritoTemporal
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.viewmodel.BorrarCarritoComprasViewModel
import com.tatanstudios.astropollocliente.viewmodel.BorrarFilaCarritoViewModel
import com.tatanstudios.astropollocliente.viewmodel.ListadoCarritoComprasViewModel
import kotlinx.coroutines.launch

@Composable
fun CarritoComprasScreen(
    navController: NavHostController,
    viewModel: ListadoCarritoComprasViewModel = viewModel { ListadoCarritoComprasViewModel() },
    viewModelBorrarCarrito: BorrarCarritoComprasViewModel = viewModel { BorrarCarritoComprasViewModel() },
    viewModelBorrarFila: BorrarFilaCarritoViewModel = viewModel { BorrarFilaCarritoViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingBorrar by viewModelBorrarCarrito.isLoading.collectAsState(true)
    val resultadoBorrar by viewModelBorrarCarrito.resultado.collectAsState()

    val isLoadingFilaBorrar by viewModelBorrarFila.isLoading.collectAsState(true)
    val resultadoFilaBorrar by viewModelBorrarFila.resultado.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope() // Crea el alcance de coroutine

    var idusuario by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }   // usa solo este
    var estadoProductoGlobal by remember { mutableStateOf(false) }

    var subtotal: String by remember { mutableStateOf("") }
    var modeloListaCarritoArray by remember {                 // usa esta lista
        mutableStateOf(listOf<ModeloCarritoTemporal>())
    }

    // MODAL PREGUNTA BOTON
    var showModal2BotonParaBorrarCarrito by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        idusuario = TokenManager(ctx).idUsuario.first()
        viewModel.listadoCarritoComprasRetrofit(idusuario)
    }

    keyboardController?.hide()

    val hayProblemas = datosCargados && modeloListaCarritoArray.isNotEmpty() && !estadoProductoGlobal

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = {
            BarraToolbarColorCarritoCompras(
                navController,
                stringResource(Res.string.carrito),
                AppColors.colorRojo,
                onDeleteClick = {
                    if (datosCargados) {
                        if (modeloListaCarritoArray.isNotEmpty()) {
                            showModal2BotonParaBorrarCarrito = true
                        } else {
                            CustomToasty(ctx, "No hay productos", ToastType.INFO)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (datosCargados && modeloListaCarritoArray.isNotEmpty()) BarraContinuar(
                subtotal = subtotal,
                hayProblemas = hayProblemas,
                onClick = {
                    if (estadoProductoGlobal) {
                        navController.navigate(Routes.VistaEnviarOrden.route) {
                            popUpTo(Routes.VistaEnviarOrden.route) { inclusive = true }
                        }
                    } else {
                        // hay productos marcados: explicar por qué no avanza
                        CustomToasty(ctx, "Revisa los productos marcados antes de continuar", ToastType.INFO)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !datosCargados && isLoading -> ContenedorSkeleton(Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BloqueSkeleton(Modifier.fillMaxWidth(0.7f).height(14.dp), radio = 6.dp)
                        repeat(3) { BloqueSkeleton(Modifier.fillMaxWidth().height(100.dp), radio = 16.dp) }
                    }
                }

                datosCargados && modeloListaCarritoArray.isEmpty() ->
                    CarritoVacio(onVerMenu = {
                        // ir al menú con la pestaña "Menú" activa (aunque se haya venido desde "Órdenes")
                        navController.navigate(Routes.VistaPrincipal.createRoute("menu")) {
                            popUpTo(Routes.VistaPrincipal.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    })

                datosCargados -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val unidades = modeloListaCarritoArray.sumOf { it.cantidad }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$unidades ${if (unidades == 1) "producto" else "productos"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorLetraPrincipal,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.SwipeLeft, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Desliza para quitar", style = MaterialTheme.typography.bodySmall, color = AppColors.colorGrisIcono)
                        }
                    }

                    if (hayProblemas) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFFEBEE))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Algunos productos ya no están disponibles. Quítalos o cámbialos para continuar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }

                    items(
                        items = modeloListaCarritoArray,
                        key = { it.carritoid }
                    ) { p ->
                        ItemCarritoSwipeAction(
                            p = p,
                            onClick = { item ->
                                navController.navigate(Routes.VistaEditarProducto.createRoute(item.carritoid)) {
                                    popUpTo(Routes.VistaInformacionProducto.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onSwipeAction = { item ->
                                scope.launch { viewModelBorrarFila.eliminarFilaCarritoRetrofit(idusuario, item.carritoid) }
                            }
                        )
                    }

                    item {
                        TextButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Agregar más productos", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            ErrorReintentar(viewModel)
            if (isLoading && datosCargados) LoadingModal(true)
            if (isLoadingBorrar) LoadingModal(true)
            if (isLoadingFilaBorrar) LoadingModal(true)

            if (showModal2BotonParaBorrarCarrito) {
                CustomModal2Botones(
                    showDialog = true,
                    titulo = "¿Vaciar el carrito?",
                    message = "Se quitarán todos los productos que agregaste.",
                    tipo = TipoAviso.PELIGRO,
                    onDismiss = { showModal2BotonParaBorrarCarrito = false },
                    onAccept = {
                        showModal2BotonParaBorrarCarrito = false
                        scope.launch { viewModelBorrarCarrito.eliminarCarritoComprasRetrofit(idusuario) }
                    },
                    txtBtnAceptar = "Vaciar",
                    txtBtnCancelar = "Cancelar",
                )
            }
        }
    }


    // Manejo del resultado
    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // SI HAY DATOS

                modeloListaCarritoArray = result.listadoCarritoTemporal

                // producto no disponible
                estadoProductoGlobal = (result.estadoProductoGlobal == 1)

                subtotal = result.subTotal ?: ""
                datosCargados = true
            }
            2 -> {
                // NO HAY DATOS: se muestra el estado vacio
                modeloListaCarritoArray = emptyList()
                datosCargados = true
                subtotal = "$0.00"
            }
            else -> viewModel.marcarError()
        }
    }

    resultadoBorrar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // carrito borrado
                CustomToasty(
                    ctx,
                    stringResource(Res.string.carrito_borrado),
                    ToastType.SUCCESS
                )
                navController.popBackStack()
            }
            2 -> {
                // carrito de compras no encontrado
                CustomToasty(
                    ctx,
                    stringResource(Res.string.carrito_borrado),
                    ToastType.SUCCESS
                )
                navController.popBackStack()
            }
            else -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
            }
        }
    }


    resultadoFilaBorrar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // carrito de compras borrado completamente
                datosCargados = false
                CustomToasty(
                    ctx,
                    stringResource(Res.string.carrito_borrado),
                    ToastType.SUCCESS
                )
                navController.popBackStack()
            }
            2 -> {
                // producto eliminado
                datosCargados = false
                CustomToasty(
                    ctx,
                    stringResource(Res.string.producto_eliminado),
                    ToastType.SUCCESS
                )
                scope.launch {
                    viewModel.listadoCarritoComprasRetrofit(idusuario)
                }
            }
            3 -> { // producto no encontrado

                datosCargados = false
                CustomToasty(
                    ctx,
                    stringResource(Res.string.producto_eliminado),
                    ToastType.SUCCESS
                )
                scope.launch {
                    viewModel.listadoCarritoComprasRetrofit(idusuario)
                }
            }
            else -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCarritoSwipeAction(
    p: ModeloCarritoTemporal,
    onClick: (ModeloCarritoTemporal) -> Unit,          // tap normal: editar
    onSwipeAction: (ModeloCarritoTemporal) -> Unit     // deslizar a la izquierda: quitar
) {
    val forma = RoundedCornerShape(16.dp)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onSwipeAction(p)
            false // la fila no desaparece sola: se recarga la lista al confirmar la API
        },
        positionalThreshold = { distancia -> distancia * 0.4f }
    )

    SwipeToDismissBox(
        modifier = Modifier.fillMaxWidth().clip(forma),
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(forma)
                    .background(AppColors.colorLetraRoja)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quitar", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
            }
        },
        content = { ItemCarritoCard(p = p, onClick = onClick) }
    )
}

/** "3.00" -> "$3.00" (si ya trae signo se deja igual). */
private fun conSignoCarrito(monto: String?): String {
    val m = monto?.trim().orEmpty()
    return if (m.isEmpty() || m.startsWith("$")) m else "$$m"
}

@Composable
private fun ItemCarritoCard(
    p: ModeloCarritoTemporal,
    onClick: (ModeloCarritoTemporal) -> Unit
) {
    val conProblema = p.estadoLocal == 1
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(p) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = if (conProblema) Color(0xFFFFF4F4) else Color.White),
        border = if (conProblema) BorderStroke(1.5.dp, Color(0xFFE57373)) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // imagen
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.colorRedondeadoGris),
                contentAlignment = Alignment.Center
            ) {
                if (p.utilizaImagen == 1 && !p.imagen.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data("${ApiConfig.URL_IMAGENES}${p.imagen}")
                            .crossfade(true)
                            .build(),
                        contentDescription = p.nombre,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = AppColors.colorLetraGris, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = p.nombre ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.colorLetraPrincipal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                LineasOpciones(p.opciones)

                if (!p.titulo.isNullOrBlank() || !p.mensaje.isNullOrBlank()) {
                    Text(
                        text = listOf(p.titulo.orEmpty(), p.mensaje.orEmpty()).filter { it.isNotBlank() }.joinToString(": "),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFC62828),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        "${p.cantidad} × ${conSignoCarrito(p.precioUnitario ?: p.precio)}",
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
                        conSignoCarrito(p.precioformat),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorLetraRoja
                    )
                }
            }

            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Edit, contentDescription = "Cambiar", tint = AppColors.colorLetraGris, modifier = Modifier.size(18.dp))
        }
    }
}

/* ---------- Carrito vacío ---------- */

@Composable
private fun CarritoVacio(onVerMenu: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(110.dp).clip(CircleShape).background(AppColors.colorLetraRoja.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(stringResource(Res.string.carrito_vacio), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppColors.colorLetraPrincipal)
        Spacer(Modifier.height(6.dp))
        Text("Agrega productos del menú para hacer tu pedido.", style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisIcono, textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        BotonAuthPrincipal(texto = "Ver menú", onClick = onVerMenu, modifier = Modifier.fillMaxWidth(0.7f))
    }
}

/* ---------- Barra inferior: subtotal + continuar ---------- */

@Composable
private fun BarraContinuar(subtotal: String, hayProblemas: Boolean, onClick: () -> Unit) {
    Surface(shadowElevation = 12.dp, color = Color.White) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.subtotal), style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisIcono)
                    Text(
                        "El total final lo ves en el siguiente paso",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.colorLetraGris
                    )
                }
                Text(
                    conSignoCarrito(subtotal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraPrincipal
                )
            }
            androidx.compose.material3.Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (hayProblemas) Color(0xFFBDBDBD) else AppColors.colorLetraRoja,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Continuar con la orden", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}
