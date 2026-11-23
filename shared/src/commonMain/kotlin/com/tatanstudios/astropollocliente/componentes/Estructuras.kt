package com.tatanstudios.astropollocliente.componentes


import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.model.rutas.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraToolbarColor(
    navController: NavController,
    titulo: String,
    backgroundColor: Color,
    estadoBotonAtras: Int = 0,
    acciones: @Composable RowScope.() -> Unit = {}, // íconos a la derecha (ej. carrito)
) {

    var isNavigating by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        },

        navigationIcon = {
            IconButton(
                onClick = {

                    if(estadoBotonAtras == 0){
                        if (!isNavigating) {
                            isNavigating = true
                            navController.popBackStack()
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.atras),
                    tint = Color.White // Color del ícono de navegación
                )
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor, // Color de fondo de la barra
            navigationIconContentColor = Color.White, // Color del ícono de navegación
            titleContentColor = Color.White, // Color del título
            actionIconContentColor = Color.White // Color de las acciones
        ),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 56.dp) // Define una altura mínima
    )
}


@Composable
fun CustomModal2Botones(
    showDialog: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    txtBtnAceptar: String,
    txtBtnCancelar: String,
    titulo: String? = null,
    tipo: TipoAviso = TipoAviso.PREGUNTA,
) {
    if (showDialog) {
        // con [titulo], [message] es el detalle; sin él, "¿Pregunta?\n\nDetalle" se separa solo
        val (t, detalle) = if (titulo != null) titulo to message else separarTituloMensaje(message)
        DialogoAviso(
            titulo = t,
            mensaje = detalle,
            textoPrincipal = txtBtnAceptar,
            onPrincipal = onAccept,
            tipo = tipo,
            textoSecundario = txtBtnCancelar,
            onSecundario = onDismiss,
        )
    }
}




@Composable
fun OtpTextField(codigo: String, longitud: Int = 6, onTextChanged: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier.fillMaxWidth(), // Centrado horizontal
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = codigo,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            onValueChange = { newText ->
                if (newText.length <= longitud && newText.all { it.isDigit() }) {
                    onTextChanged(newText)
                }
            },
            singleLine = true
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp), // Espaciado arriba/abajo
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Separación entre dígitos
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(longitud) { index ->
                    val number = when {
                        index >= codigo.length -> ""
                        else -> codigo[index].toString()
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .size(40.dp)
                                .wrapContentSize(Alignment.Center)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(2.dp)
                                .background(Color.Black)
                        )
                    }
                }
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraToolbarColorMenuPrincipal(
    navController: NavController,
    titulo: String,
    backgroundColor: Color,
    esInvitado: Boolean = false,
) {

    var isNavigating by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        },

        navigationIcon = {
            // visitante: cambiar la sucursal de la que ve el menú
            if (esInvitado) {
                IconButton(onClick = {
                    navController.navigate(Routes.VistaElegirSucursal.createRoute(automatico = false)) {
                        launchSingleTop = true
                    }
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Cambiar sucursal", tint = Color.White)
                }
            }
        },
        actions = {
            IconButton(
                onClick = {
                    // Acción que quieres realizar al presionar el ícono
                    if (!isNavigating) {
                        isNavigating = true

                        // visitante → iniciar sesión; con sesión → perfil
                        navController.navigate(if (esInvitado) Routes.VistaLogin.route else Routes.VistaPerfil.route) {
                            launchSingleTop = true
                        }

                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Person, // Cambia esto por tu ícono deseado
                    contentDescription = "Opciones",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor, // Color de fondo de la barra
            navigationIconContentColor = Color.White, // Color del ícono de navegación
            titleContentColor = Color.White, // Color del título
            actionIconContentColor = Color.White // Color de las acciones
        ),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 56.dp) // Define una altura mínima
    )
}






@Composable
fun CustomModalCerrarSesion(
    showDialog: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    if (showDialog) {
        DialogoAviso(
            titulo = message,
            mensaje = "Podrás volver a entrar con tu usuario y contraseña.",
            textoPrincipal = "Cerrar sesión",
            onPrincipal = onAccept,
            tipo = TipoAviso.PREGUNTA,
            textoSecundario = "Cancelar",
            onSecundario = onDismiss,
            icono = Icons.AutoMirrored.Filled.Logout,
        )
    }
}




@Composable
fun CardHistorialOrden(
    orden: String,
    fecha: String,
    venta: String,
    estado: String?,
    haycupon: Int,
    cupon: String?,
    direccion: String,
    notaOrden: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CampoTexto(stringResource(Res.string.orden_numeral), orden)
            Spacer(modifier = Modifier.height(6.dp))
            CampoTexto(stringResource(Res.string.direccion), direccion)
            Spacer(modifier = Modifier.height(6.dp))
            CampoTexto(stringResource(Res.string.estado), estado)
            Spacer(modifier = Modifier.height(6.dp))
            CampoTexto(stringResource(Res.string.total_a_pagar), venta)
            Spacer(modifier = Modifier.height(6.dp))
            CampoTexto(stringResource(Res.string.fecha), fecha)
            Spacer(modifier = Modifier.height(6.dp))
            CampoTexto(stringResource(Res.string.nota_orden), notaOrden)
            if (haycupon == 1){
                Spacer(modifier = Modifier.height(6.dp))
                CampoTexto(stringResource(Res.string.cupon), cupon, AppColors.colorRojo) }
        }
    }
}



@Composable
fun CampoTexto(etiqueta: String, valor: String?,
               colorEtiqueta: Color = Color.Black
) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$etiqueta: ",
            fontWeight = FontWeight.Bold,
            color = colorEtiqueta,
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = valor ?: "",
            color = colorEtiqueta,
            fontSize = 17.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}




@Composable
fun ProductoListadoHistorialItemCard(
    cantidad: String,
    hayImagen: Int,
    imagenUrl: String,
    titulo: String?,
    descripcion: String?, // lo que el cliente escribe ejemplo (pollo, res)
    precio: String,
    modifier: Modifier = Modifier,
    opciones: List<String> = emptyList(), // modificadores elegidos, una línea por bloque
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cantidad
            Box(
                modifier = Modifier
                    .background(color = AppColors.colorAzul, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${cantidad}x",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Imagen del producto desde URL
            if(hayImagen == 1){
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(imagenUrl)
                        .crossfade(true)
                        .build(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                    contentDescription = stringResource(Res.string.imagen_por_defecto),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }else{
                Image(
                    painter = painterResource(Res.drawable.camaradefecto),
                    contentDescription = stringResource(Res.string.imagen_por_defecto),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }


            Spacer(modifier = Modifier.width(8.dp))

            // Título y descripción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo?: "",
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis
                )
                LineasOpciones(opciones)
                if(!descripcion.isNullOrBlank()){
                    Text(
                        text = "Nota: $descripcion",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.colorLetraRoja,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Precio
            Text(
                text = "$$precio",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}



@Composable
fun CardHorarioV1(nombre: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda
            Image(
                painter = painterResource(Res.drawable.logonegrocirculo), // Reemplaza con tu imagen
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Texto a la derecha
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Composable
fun CardHorarioV2(titulo: String?, descripcion: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen a la izquierda
            Image(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Icono de fecha",
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Texto a la derecha
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = descripcion?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}



@Composable
fun CardMisPuntos(nombre: String, txtPuntos: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente
        ) {
            Text(
                text = nombre,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = txtPuntos?: "",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            )
        }
    }
}




@Composable
fun CardMisDirecciones(
    nombre: String,
    seleccionado: Int,
    minimoCompra: String,
    direccion: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (seleccionado == 1) Color(0xFFF2FFDB) else Color.White

    Box(modifier = modifier
        .fillMaxWidth()
        .padding(8.dp)) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.House,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = nombre,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = direccion,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = minimoCompra,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                )
            }
        }

        if (seleccionado == 1) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Seleccionado",
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(24.dp)
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraToolbarColorDireccion(
    titulo: String,
    backgroundColor: Color,
    onBackClick: (() -> Unit)? = null // nuevo parámetro opcional
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        },
        actions = {
            IconButton(
                onClick = {
                    onBackClick?.invoke()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Opciones",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 56.dp)
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraToolbarColorCarritoCompras(
    navController: NavController,
    titulo: String,
    backgroundColor: Color,
    onDeleteClick: () -> Unit   // 👈 nuevo callback
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onDeleteClick() } // 👈 lo llamas aquí
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar carrito",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 56.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraToolbarColorParaListaOrdenes(titulo: String, backgroundColor: Color) {


    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        },

        actions = {
            // Puedes agregar acciones adicionales aquí si lo necesitas
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor, // Color de fondo de la barra
            navigationIconContentColor = Color.White, // Color del ícono de navegación
            titleContentColor = Color.White, // Color del título
            actionIconContentColor = Color.White // Color de las acciones
        ),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 56.dp) // Define una altura mínima
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraToolbarColorOrdenesEstado(navController: NavController, titulo: String, backgroundColor: Color) {

    var isNavigating by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titulo,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
        },

        navigationIcon = {
            IconButton(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true

                        navController.navigate(Routes.VistaPrincipal.createRoute("ordenes")
                        ) {
                            popUpTo(Routes.VistaEstadoOrden.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.atras),
                    tint = Color.White // Color del ícono de navegación
                )
            }
        },
        actions = {
            // Puedes agregar acciones adicionales aquí si lo necesitas
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor, // Color de fondo de la barra
            navigationIconContentColor = Color.White, // Color del ícono de navegación
            titleContentColor = Color.White, // Color del título
            actionIconContentColor = Color.White // Color de las acciones
        ),
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeightIn(min = 56.dp) // Define una altura mínima
    )
}

