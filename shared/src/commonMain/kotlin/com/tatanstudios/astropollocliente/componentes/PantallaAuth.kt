package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatanstudios.astropollocliente.resources.Res
import com.tatanstudios.astropollocliente.resources.arthura_medium
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import org.jetbrains.compose.resources.Font

/**
 * Estructura común de las pantallas de acceso (login, registro, recuperar y cambiar contraseña):
 * encabezado rojo con flecha atrás, ilustración y título; debajo una tarjeta blanca con el formulario.
 */
@Composable
fun PantallaAuth(
    titulo: String,
    subtitulo: String?,
    onAtras: () -> Unit,
    ilustracion: @Composable () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.colorCremaV1)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- encabezado rojo ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.colorRojo, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .statusBarsPadding()
                    .padding(bottom = 44.dp)
            ) {
                IconButton(
                    onClick = onAtras,
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ilustracion()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = titulo,
                        fontFamily = FontFamily(Font(Res.font.arthura_medium)),
                        color = Color.White,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                    if (!subtitulo.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitulo,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ---- tarjeta con el formulario (se monta un poco sobre el encabezado) ----
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-28).dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = contenido
                )
            }

            Spacer(Modifier.navigationBarsPadding().height(8.dp))
        }
    }
}

/** Campo de texto redondeado con ícono; si [esPassword] agrega el botón de ver/ocultar. */
@Composable
fun CampoAuth(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    maxLength: Int,
    modifier: Modifier = Modifier,
    esPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onIme: () -> Unit = {},
    ayuda: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= maxLength) onCambio(it) },
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = if (esPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        } else null,
        supportingText = ayuda?.let { { Text(it) } },
        visualTransformation = if (esPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onAny = { onIme() }),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.colorLetraRoja,
            focusedLabelColor = AppColors.colorLetraRoja,
            focusedLeadingIconColor = AppColors.colorLetraRoja,
            cursorColor = AppColors.colorLetraRoja,
            unfocusedBorderColor = Color(0xFFD6D6D6),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFAFAFA),
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BotonAuthPrincipal(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.colorLetraRoja, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(texto, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BotonAuthSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, AppColors.colorLetraRoja),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.colorLetraRoja),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(texto, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Texto de ayuda centrado en gris (ej. instrucciones arriba del formulario). */
@Composable
fun TextoAyudaAuth(texto: String) {
    Text(
        text = texto,
        color = AppColors.colorGrisIcono,
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Animación Lottie del encabezado (registro, recuperar contraseña...) dentro de un círculo blanco. */
@Composable
fun IlustracionLottieAuth(ruta: String) {
    val composicion by rememberLottieArchivo(ruta)
    val progreso by animateLottieCompositionAsState(composition = composicion, iterations = LottieConstants.IterateForever)
    Box(
        modifier = Modifier
            .size(104.dp)
            .background(Color.White, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composicion,
            progress = { progreso },
            modifier = Modifier.size(90.dp)
        )
    }
}
