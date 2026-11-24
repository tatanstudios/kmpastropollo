package com.tatanstudios.astropollocliente.vistas.principal.productos


import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.window.Dialog
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColorMenuPrincipal
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomModal1BotonTitulo
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import com.tatanstudios.astropollocliente.model.modelos.ModeloInformacionProductoArray
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductosTerceraArray
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.viewmodel.EnviarOrdenFinalViewModel
import com.tatanstudios.astropollocliente.viewmodel.EnviarProductoAlCarritoViewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionOrdenParaEnviarViewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionProductoViewModel
import com.tatanstudios.astropollocliente.viewmodel.VerificarCuponViewModel
import com.tatanstudios.astropollocliente.extras.getOneSignalUserId
import com.tatanstudios.astropollocliente.extras.getVersionName
import com.tatanstudios.astropollocliente.extras.formatoDosDecimales

@Composable
fun EnviarOrdenScreen(
    navController: NavHostController,
    viewModel: InformacionOrdenParaEnviarViewModel = viewModel { InformacionOrdenParaEnviarViewModel() },
    viewModelVerificarCupon: VerificarCuponViewModel = viewModel { VerificarCuponViewModel() },
    viewModelEnviarOrden: EnviarOrdenFinalViewModel = viewModel { EnviarOrdenFinalViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val focusManager = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val tokenManager = remember { TokenManager(ctx) }
    val isLoading by viewModel.isLoading.collectAsState(true)
    val resultado by viewModel.resultado.collectAsState()

    val isLoadingVerificarCupon by viewModelVerificarCupon.isLoading.collectAsState(true)
    val resultadoVerificarCupon by viewModelVerificarCupon.resultado.collectAsState()

    val isLoadingEnviarOrden by viewModelEnviarOrden.isLoading.collectAsState(true)
    val resultadoEnviarOrden by viewModelEnviarOrden.resultado.collectAsState()

    var idusuario by remember { mutableStateOf("") }

    // 1: SI PUEDE ORDENAR   0: NO PUEDE ORDENAR
    var minimoInt0NoPuede by remember { mutableStateOf(0) }
    var mensajeMinimoConsumoString by remember { mutableStateOf("") }
    var direccionString by remember { mutableStateOf("") }
    var clienteString by remember { mutableStateOf("") }
    var totalString by remember { mutableStateOf("") }
    var usaCuponPermitoServerInt by remember { mutableStateOf(0) }
    var nota by remember { mutableStateOf("") }

    // === Estado del modal de cupón ===
    var mostrarDialogCupon by remember { mutableStateOf(false) }

    var datosCargados by remember { mutableStateOf(false) }


    var textoCuponEscrito by remember { mutableStateOf("") }


    var tengoCupon by remember { mutableStateOf(0) }
    // MENSAJE DE LO QUE APLICA EL CUPON
    var textoCuponGanado by remember { mutableStateOf("") }

    // LA PRIMERA PALABRA (TOTAL) QUE CAMBIARA A SUB TOTAL CUANDO SE APLIQUE CUPON DINERO O DESCUENTO
    var txtSubTotalLetra by remember { mutableStateOf("Total") }

    val versionLocal = getVersionName(ctx)

    // PARA SABER SI ES CUPON DE DINERO O PORCENTAJE

    // CUANDO SE APLICA CUPON DINERO O PORCENTAJE, SABER CUANTO SE PAGARA AL FINAL

    // PARA MOSTRAR MODAL PARA ENVIAR ORDEN SI OR NO
    var showModal2BotonParaEnviarOrden by remember { mutableStateOf(false) }

    // MOSTRAR RESPUESTA DE ERROR PARA CUANDO ENVIAMOS LA ORDEN
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalTituloString by remember { mutableStateOf("") }
    var modalMensajeString by remember { mutableStateOf("") }

    // PARA CUANDO SE ENVIO ORDEN FINAL Y REDIRECCIONA A ORDENES
    var showModal1BotonFinal by remember { mutableStateOf(false) }
    var modalTituloStringFinal by remember { mutableStateOf("") }
    var modalMensajeStringFinal by remember { mutableStateOf("") }

    // NOTA FINAL PARA REDIRECCIONARME A ORDENES
    // PARA CUANDO SE ENVIO ORDEN FINAL Y REDIRECCIONA A ORDENES
    var showModal1BotonRedireccionar by remember { mutableStateOf(false) }
    var modalTituloStringRedireccionar by remember { mutableStateOf("") }
    var modalMensajeStringRedireccionar by remember { mutableStateOf("") }

    var idonesignal by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope() // Crea el alcance de coroutine

    // cargar datos
    LaunchedEffect(Unit) {
        idusuario = tokenManager.idUsuario.first()

        scope.launch {
            idonesignal = getOneSignalUserId()
        }
        // para que le lleguen los avisos de esta orden (iniciada, cancelada...)
        com.tatanstudios.astropollocliente.extras.Notificaciones.vincular(idusuario)

        viewModel.informacionOrdenParaEnviarRetrofit(idusuario)
    }


    val sinMinimo = minimoInt0NoPuede == 0

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Confirmar orden", AppColors.colorRojo) },
        bottomBar = {
            if (datosCargados) {
                Surface(shadowElevation = 12.dp, color = Color.White) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Total a pagar",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.colorGrisIcono,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                totalString,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorLetraPrincipal
                            )
                        }
                        Button(
                            onClick = {
                                if (sinMinimo) {
                                    CustomToasty(ctx, "Mínimo de Compra es Requerido", ToastType.INFO)
                                } else {
                                    showModal2BotonParaEnviarOrden = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                // gris si no se alcanza el mínimo (sigue tocable para explicar por qué)
                                containerColor = if (sinMinimo) Color(0xFFBDBDBD) else AppColors.colorLetraRoja,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                if (sinMinimo) "Mínimo de compra no alcanzado" else "Enviar orden",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!sinMinimo) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (datosCargados) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ---- mínimo de compra no alcanzado ----
                    if (sinMinimo) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(Res.string.minimo_de_consumo),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Text(mensajeMinimoConsumoString, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E2424))
                                TextButton(
                                    onClick = { navController.popBackStack() },
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text("Agregar más productos", color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }

                    // ---- entrega ----
                    SeccionEnvio(titulo = "Entregar a") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.colorLetraRoja),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    clienteString,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.colorLetraPrincipal
                                )
                                Text(direccionString, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv2)
                            }
                            TextButton(onClick = {
                                navController.navigate(Routes.VistaMisDirecciones.createRoute(0)) { launchSingleTop = true }
                            }) { Text("Cambiar", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold) }
                        }
                    }

                    // ---- nota ----
                    SeccionEnvio(titulo = "Nota para el repartidor (opcional)") {
                        OutlinedTextField(
                            value = nota,
                            onValueChange = { nota = if (it.length <= 300) it else it.take(300) },
                            placeholder = { Text("Ej. Tocar el timbre, portón negro") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.colorLetraRoja,
                                cursorColor = AppColors.colorLetraRoja,
                                unfocusedBorderColor = Color(0xFFD6D6D6),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                            ),
                            supportingText = {
                                Text("${nota.length}/300", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                            }
                        )
                    }

                    // ---- cupón (solo si el restaurante los permite) ----
                    if (usaCuponPermitoServerInt == 1) {
                        if (tengoCupon == 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(26.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Cupón aplicado: $textoCuponEscrito", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text(textoCuponGanado, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B5E20))
                                }
                                TextButton(onClick = {
                                    tengoCupon = 0
                                    textoCuponEscrito = ""
                                    txtSubTotalLetra = "Total"
                                    viewModel.informacionOrdenParaEnviarRetrofit(idusuario)
                                }) { Text(stringResource(Res.string.borrar_cupon), color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold) }
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    textoCuponEscrito = ""
                                    mostrarDialogCupon = true
                                }
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "¿Tienes un cupón?",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.colorLetraPrincipal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(stringResource(Res.string.agregar_cupon), color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // ---- resumen ----
                    SeccionEnvio(titulo = "Resumen") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(txtSubTotalLetra, style = MaterialTheme.typography.bodyLarge, color = AppColors.colorGrisIcono, modifier = Modifier.weight(1f))
                            Text(totalString, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppColors.colorLetraRoja)
                        }
                        Text(
                            "Revisa tu dirección antes de enviar. Al confirmar, el restaurante recibe tu orden.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.colorGrisIcono
                        )
                    }
                }

                // ===== Diálogo para escribir el cupón =====
                if (mostrarDialogCupon) {
                    val habilitado = textoCuponEscrito.trim().isNotEmpty()
                    Dialog(onDismissRequest = { mostrarDialogCupon = false }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Agregar cupón", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AppColors.colorLetraPrincipal)
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = textoCuponEscrito,
                                onValueChange = { textoCuponEscrito = if (it.length <= 100) it else it.take(100) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { teclado?.hide() }),
                                placeholder = { Text("Escribe tu cupón") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2E7D32),
                                    cursorColor = Color(0xFF2E7D32),
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(18.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { mostrarDialogCupon = false },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) { Text("Cancelar", color = AppColors.colorGrisv2, fontWeight = FontWeight.SemiBold) }
                                Button(
                                    onClick = {
                                        if (textoCuponEscrito.trim().isNotEmpty()) {
                                            mostrarDialogCupon = false
                                            viewModelVerificarCupon.verificarCuponRetrofit(idusuario, textoCuponEscrito)
                                        }
                                    },
                                    enabled = habilitado,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) { Text("Verificar", fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                ContenedorSkeleton(Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        BloqueSkeleton(Modifier.fillMaxWidth().height(110.dp), radio = 16.dp)
                        BloqueSkeleton(Modifier.fillMaxWidth().height(130.dp), radio = 16.dp)
                        BloqueSkeleton(Modifier.fillMaxWidth().height(70.dp), radio = 16.dp)
                    }
                }
            }

            // === Modales y Loadings globales ===
            if (showModal1Boton) {
                CustomModal1BotonTitulo(
                    showModal1Boton,
                    modalTituloString,
                    modalMensajeString,
                    onDismiss = { showModal1Boton = false }
                )
            }

            if (showModal1BotonFinal) {
                CustomModal1BotonTitulo(
                    showModal1BotonFinal,
                    modalTituloStringFinal,
                    modalMensajeStringFinal,
                    onDismiss = {
                        showModal1BotonFinal = false
                        navController.navigate(Routes.VistaPrincipal.createRoute("ordenes")) {
                            popUpTo(Routes.VistaEnviarOrden.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            ErrorReintentar(viewModel)
            if (isLoading && datosCargados) LoadingModal(isLoading = true)
            if (isLoadingVerificarCupon) LoadingModal(isLoading = true, titulo = "Verificando cupón...")
            if (isLoadingEnviarOrden) LoadingModal(isLoading = true, titulo = "Enviando tu orden...")

            if (showModal2BotonParaEnviarOrden) {
                CustomModal2Botones(
                    showDialog = true,
                    message = "¿Enviar tu orden por $totalString?\n\nSe entregará a $clienteString en: $direccionString",
                    onDismiss = { showModal2BotonParaEnviarOrden = false },
                    onAccept = {
                        showModal2BotonParaEnviarOrden = false
                        viewModelEnviarOrden.enviarOrdenRetrofit(idusuario, nota, textoCuponEscrito, tengoCupon, versionLocal,
                            idonesignal)
                    },
                    txtBtnAceptar = "Enviar orden",
                    txtBtnCancelar = "Revisar",
                )
            }

            if (showModal1BotonRedireccionar) {
                CustomModal1BotonTitulo(
                    showModal1BotonRedireccionar,
                    modalTituloStringRedireccionar,
                    modalMensajeStringRedireccionar,
                    onDismiss = {
                        showModal1BotonRedireccionar = false
                        navController.navigate(Routes.VistaPrincipal.createRoute("ordenes")) {
                            popUpTo(Routes.VistaSplash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }



    // Manejo del resultado del ViewModel
    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // SIN DIRECCION EL CLIENTE
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
                navController.popBackStack()
            }
            2 -> {
                // Datos correctos
                minimoInt0NoPuede = result.minimo
                mensajeMinimoConsumoString = result.mensaje ?: ""
                totalString = result.total ?: ""
                direccionString = result.direccion ?: ""
                clienteString = result.cliente ?: ""
                usaCuponPermitoServerInt = result.usacupon
                datosCargados = true
            }
            3 -> {
                // CARRITO NO ENCONTRADO
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
                navController.popBackStack()
            }
            else -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
                navController.popBackStack()
            }
        }
    }

    resultadoVerificarCupon?.getContentIfNotHandled()?.let { result ->
        when (result.success) {

            // RETORNOS
            // 1: carrito de compras no encontrado
            // 1: cupon no valido (estado igual)

            // 2: cupon producto gratis (unico tipo de cupon)

            1 -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.cupon_no_valido),
                    ToastType.INFO
                )
            }
            2 -> {
                // CUPON PARA PRODUCTO GRATIS
                textoCuponGanado = result.mensaje ?: ""
                tengoCupon = 1
            }
            else -> {
                CustomToasty(
                    ctx,
                    stringResource(Res.string.error_reintentar_de_nuevo),
                    ToastType.ERROR
                )
                navController.popBackStack()
            }
        }
    }




    resultadoEnviarOrden?.getContentIfNotHandled()?.let { result ->
        when (result.success) {

            // REGLAS
            1 -> {
               // titulo y mensaje
                val titulo = result.titulo ?: ""
                val mensaje = result.mensaje ?: ""
                modalTituloString = titulo
                modalMensajeString = mensaje
                showModal1Boton = true
            }
            10 -> {
                // ORDEN ENVIADA (el servidor ya avisa al restaurante al guardarla)
                val titulo = result.titulo ?: ""
                val mensaje = result.mensaje ?: ""
                modalTituloStringFinal = titulo
                modalMensajeStringFinal = mensaje
                showModal1BotonFinal = true
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


/** Bloque blanco con título pequeño (secciones de la pantalla de confirmar orden). */
@Composable
private fun SeccionEnvio(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            titulo.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.colorGrisIcono,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = contenido)
        }
    }
}
