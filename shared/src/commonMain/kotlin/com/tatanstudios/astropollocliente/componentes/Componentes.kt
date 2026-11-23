package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes


@Composable
fun BloqueTextFieldLogin(text: String, onTextChanged: (String) -> Unit, maxLength: Int) {
    // Color común para el texto del placeholder y la línea
    val commonColor = Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White) // Fondo blanco
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Ícono al inicio con color gris claro
        Icon(
            imageVector = Icons.Filled.Person,  // Aquí puedes poner el ícono que prefieras
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp) // Espacio entre el ícono y el TextField
                .size(24.dp), // Tamaño del ícono
            tint = commonColor // Usamos el color común para el ícono
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = commonColor, // Usamos el mismo color para la línea
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            TextField(
                value = text,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                onValueChange = { newText ->
                    if (newText.length <= maxLength) {
                        onTextChanged(newText)
                    }
                },
                textStyle = TextStyle(
                    fontSize = 17.sp, // Tamaño del texto
                    fontWeight = FontWeight.Normal // Negrita
                ),
                placeholder = { Text(text = stringResource(Res.string.usuario), color = commonColor) }, // Color del placeholder
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White, // Fondo blanco cuando está enfocado
                    unfocusedContainerColor = Color.White, // Fondo blanco cuando no está enfocado
                    disabledContainerColor = Color.White, // Fondo blanco cuando está deshabilitado
                    errorContainerColor = Color.White, // Fondo blanco si hay error
                    focusedIndicatorColor = commonColor, // Color de la línea cuando está enfocado
                    unfocusedIndicatorColor = commonColor  // Color de la línea cuando no está enfocado
                ),
            )
        }
    }
}


@Composable
fun BloqueTextFieldCorreo(text: String, textoPlaceholder: String, onTextChanged: (String) -> Unit, maxLength: Int) {
    // Color común para el texto del placeholder y la línea
    val commonColor = Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White) // Fondo blanco
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Ícono al inicio con color gris claro
        Icon(
            imageVector = Icons.Filled.Email,  // Aquí puedes poner el ícono que prefieras
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp) // Espacio entre el ícono y el TextField
                .size(24.dp), // Tamaño del ícono
            tint = commonColor // Usamos el color común para el ícono
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = commonColor, // Usamos el mismo color para la línea
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            TextField(
                value = text,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                onValueChange = { newText ->
                    if (newText.length <= maxLength) {
                        onTextChanged(newText)
                    }
                },
                textStyle = TextStyle(
                    fontSize = 17.sp, // Tamaño del texto
                    fontWeight = FontWeight.Normal // Negrita
                ),
                placeholder = { Text(text = textoPlaceholder, color = commonColor) }, // Color del placeholder
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White, // Fondo blanco cuando está enfocado
                    unfocusedContainerColor = Color.White, // Fondo blanco cuando no está enfocado
                    disabledContainerColor = Color.White, // Fondo blanco cuando está deshabilitado
                    errorContainerColor = Color.White, // Fondo blanco si hay error
                    focusedIndicatorColor = commonColor, // Color de la línea cuando está enfocado
                    unfocusedIndicatorColor = commonColor  // Color de la línea cuando no está enfocado
                ),
            )
        }
    }
}


@Composable
fun BloqueTextFieldPassword(
    text: String,
    onTextChanged: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChanged: (Boolean) -> Unit,
    maxLength: Int
) {
    // Color común para el texto del placeholder y la línea
    val commonColor = Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White) // Fondo blanco
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Ícono al inicio con color gris claro
        Icon(
            imageVector = Icons.Filled.Lock,  // Puedes cambiarlo por otro ícono si lo prefieres
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp) // Espacio entre el ícono y el TextField
                .size(24.dp), // Tamaño del ícono
            tint = commonColor // Usamos el color común para el ícono
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = commonColor, // Usamos el mismo color para la línea
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            TextField(
                value = text,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                onValueChange = { newText ->
                    if (newText.length <= maxLength) {  // Limitar la longitud de la contraseña si lo deseas
                        onTextChanged(newText)
                    }
                },
                textStyle = TextStyle(
                    fontSize = 17.sp, // Tamaño del texto
                    fontWeight = FontWeight.Normal // Negrita
                ),
                placeholder = { Text(text = stringResource(Res.string.contrasena), color = commonColor) }, // Color del placeholder
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibilityChanged(!isPasswordVisible) }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = commonColor
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White, // Fondo blanco cuando está enfocado
                    unfocusedContainerColor = Color.White, // Fondo blanco cuando no está enfocado
                    disabledContainerColor = Color.White, // Fondo blanco cuando está deshabilitado
                    errorContainerColor = Color.White, // Fondo blanco si hay error
                    focusedIndicatorColor = commonColor, // Color de la línea cuando está enfocado
                    unfocusedIndicatorColor = commonColor  // Color de la línea cuando no está enfocado
                ),
            )
        }
    }
}



@Composable
fun CustomModal1Boton(showDialog: Boolean, message: String, onDismiss: () -> Unit) {
    if (showDialog) {
        // mensaje corto: va como título; si trae "\n\n" se separa en título y detalle
        val (titulo, detalle) = separarTituloMensaje(message)
        DialogoAviso(
            titulo = titulo,
            mensaje = detalle,
            textoPrincipal = stringResource(Res.string.aceptar),
            onPrincipal = onDismiss,
            tipo = TipoAviso.INFO,
        )
    }
}

@Composable
fun CustomModal1BotonTitulo(
    showDialog: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        DialogoAviso(
            titulo = title,
            mensaje = message,
            textoPrincipal = stringResource(Res.string.aceptar),
            onPrincipal = onDismiss,
            tipo = TipoAviso.INFO,
        )
    }
}



@Composable
fun LoadingModal(isLoading: Boolean, titulo:String = "Cargando...") {
    if (isLoading) {
        Dialog(onDismissRequest = { /* Evitar que se cierre el modal */ }) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = AppColors.colorAzul)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = titulo,
                        fontSize = 18.sp,
                        color = AppColors.colorNegro
                    )
                }
            }
        }
    }
}




val MontserratFont: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.arthura_regular, FontWeight.Normal),
    Font(Res.font.arthura_medium, FontWeight.Bold)
)




@Composable
fun BloqueEntradaGeneral(
    text: String,
    onTextChanged: (String) -> Unit,
    maxLength: Int,
    placeholderResId: org.jetbrains.compose.resources.StringResource,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text // ← aquí
) {
    val commonColor = Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp),
            tint = commonColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = commonColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            TextField(
                value = text,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                onValueChange = { newText ->
                    if (newText.length <= maxLength) {
                        onTextChanged(newText)
                    }
                },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                ),
                placeholder = {
                    Text(
                        text = stringResource(placeholderResId),
                        color = commonColor
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedIndicatorColor = commonColor,
                    unfocusedIndicatorColor = commonColor
                )
            )
        }
    }
}






@Composable
fun BloqueTextFieldUsuario(
    text: String,
    onTextChanged: (String) -> Unit,
    maxLength: Int,
    labelText: String // Nuevo parámetro
) {
    val commonColor = Color.Gray

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = labelText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                tint = commonColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = commonColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                TextField(
                    value = text,
                    onValueChange = { newText ->
                        if (newText.length <= maxLength) {
                            onTextChanged(newText)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.usuario),
                            color = commonColor
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        errorContainerColor = Color.White,
                        focusedIndicatorColor = commonColor,
                        unfocusedIndicatorColor = commonColor
                    )
                )
            }
        }
    }
}





@Composable
fun BloqueTextFieldCorreoUsuario(
    text: String,
    textoPlaceholder: String,
    onTextChanged: (String) -> Unit,
    maxLength: Int,
    labelText: String // Nuevo parámetro para el texto superior
) {
    val commonColor = Color.Gray

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = labelText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                tint = commonColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = commonColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                TextField(
                    value = text,
                    onValueChange = { newText ->
                        if (newText.length <= maxLength) {
                            onTextChanged(newText)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    placeholder = { Text(text = textoPlaceholder, color = commonColor) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        errorContainerColor = Color.White,
                        focusedIndicatorColor = commonColor,
                        unfocusedIndicatorColor = commonColor
                    )
                )
            }
        }
    }
}
