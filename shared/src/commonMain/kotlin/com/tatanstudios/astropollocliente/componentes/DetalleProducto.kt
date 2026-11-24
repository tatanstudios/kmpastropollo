package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.extras.formatoDosDecimales
import com.tatanstudios.astropollocliente.resources.Res
import com.tatanstudios.astropollocliente.resources.camaradefecto
import com.tatanstudios.astropollocliente.resources.spinloading
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource

/** Encabezado del detalle: imagen (si la usa), nombre, descripción y precio base. */
@Composable
fun EncabezadoProducto(
    imagenUrl: String?,
    nombre: String,
    descripcion: String?,
    precio: Double,
) {
    val ctx = LocalPlatformContext.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (imagenUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.colorRedondeadoGris),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(imagenUrl).crossfade(true).build(),
                    contentDescription = nombre,
                    modifier = Modifier.fillMaxWidth().height(210.dp).padding(8.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(Res.drawable.spinloading),
                    error = painterResource(Res.drawable.camaradefecto),
                )
            }
        }

        Text(
            text = nombre,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.colorLetraPrincipal,
            modifier = Modifier.padding(top = 6.dp),
        )

        val desc = (descripcion ?: "").replace("\r\n", "\n").replace("\\r\\n", "\n").trim()
        if (desc.isNotEmpty()) {
            Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv1)
        }

        Text(
            text = "$" + formatoDosDecimales(precio),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.colorLetraRoja,
        )
    }
}

/**
 * Nota para el producto. Si el producto pide nota ([requerida]), se muestra la indicación
 * del restaurante como título (ej. "Indica el sabor de tu bebida") y es obligatoria.
 */
@Composable
fun CampoNotaProducto(
    nota: String,
    onCambio: (String) -> Unit,
    requerida: Boolean,
    indicacion: String?,
    error: Boolean,
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (requerida && !indicacion.isNullOrBlank()) indicacion else "Nota para este producto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.colorLetraPrincipal,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (requerida) "Obligatorio" else "Opcional",
                style = MaterialTheme.typography.labelSmall,
                color = if (requerida) AppColors.colorLetraRoja else AppColors.colorLetraGris,
            )
        }
        OutlinedTextField(
            value = nota,
            onValueChange = { onCambio(it.take(300)) },
            placeholder = { Text(if (requerida) "Escribe aquí" else "") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            isError = error,
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                Row(Modifier.fillMaxWidth()) {
                    if (error) Text("Este dato es obligatorio", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    else Box(Modifier.weight(1f))
                    Text("${nota.length}/300", color = AppColors.colorLetraGris)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.colorAzul,
                unfocusedBorderColor = Color(0xFFD6D6DB),
            ),
        )
    }
}

/**
 * Barra inferior fija: cantidad (− n +) y botón con el total, siempre visible aunque
 * el producto tenga muchas opciones.
 */
@Composable
fun BarraAgregarProducto(
    cantidad: Int,
    onCantidad: (Int) -> Unit,
    total: Double,
    textoBoton: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    maximo: Int = 50,
) {
    Surface(shadowElevation = 12.dp, color = Color.White) {
        Column {
            HorizontalDivider(color = Color(0xFFEDEDF0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AppColors.colorRedondeadoGris)
                        .padding(4.dp),
                ) {
                    BotonCantidad(Icons.Default.Remove, "Menos", enabled = cantidad > 1) { onCantidad(cantidad - 1) }
                    Text(
                        text = cantidad.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                    BotonCantidad(Icons.Default.Add, "Más", enabled = cantidad < maximo) { onCantidad(cantidad + 1) }
                }

                Button(
                    onClick = onClick,
                    enabled = habilitado,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.colorLetraRoja,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(textoBoton, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text("$" + formatoDosDecimales(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BotonCantidad(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = AppColors.colorLetraRoja,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFDADADF),
            disabledContentColor = Color.White,
        ),
    ) { Icon(icono, contentDescription = descripcion) }
}
