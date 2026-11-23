package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tatanstudios.astropollocliente.ui.theme.AppColors

/** Tipo de aviso: define el ícono y el color de arriba. */
enum class TipoAviso { INFO, PREGUNTA, PELIGRO }

/**
 * Diálogo común de la app: ícono en círculo, título, mensaje y botones grandes redondeados.
 * Con [textoSecundario] muestra dos botones (secundario con borde a la izquierda, principal a la derecha).
 * No se cierra tocando afuera (igual que los modales anteriores).
 */
@Composable
fun DialogoAviso(
    titulo: String?,
    mensaje: String?,
    textoPrincipal: String,
    onPrincipal: () -> Unit,
    tipo: TipoAviso = TipoAviso.INFO,
    textoSecundario: String? = null,
    onSecundario: () -> Unit = {},
    icono: ImageVector? = null,
) {
    val colorTipo = when (tipo) {
        TipoAviso.PELIGRO -> AppColors.colorLetraRoja
        TipoAviso.PREGUNTA -> AppColors.colorLetraRoja
        TipoAviso.INFO -> AppColors.colorLetraRoja
    }
    val iconoTipo = icono ?: when (tipo) {
        TipoAviso.PELIGRO -> Icons.Filled.DeleteOutline
        TipoAviso.PREGUNTA -> Icons.AutoMirrored.Filled.HelpOutline
        TipoAviso.INFO -> Icons.Filled.Info
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(colorTipo.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconoTipo, contentDescription = null, tint = colorTipo, modifier = Modifier.size(32.dp))
            }

            if (!titulo.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = titulo,
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraPrincipal,
                    textAlign = TextAlign.Center
                )
            }

            if (!mensaje.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = mensaje,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = AppColors.colorGrisIcono,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(22.dp))

            val colorPrincipal = ButtonDefaults.buttonColors(
                containerColor = AppColors.colorLetraRoja,
                contentColor = Color.White
            )

            if (textoSecundario == null) {
                Button(
                    onClick = onPrincipal,
                    shape = RoundedCornerShape(14.dp),
                    colors = colorPrincipal,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text(textoPrincipal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onSecundario,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFD6D6D6)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.colorGrisv2),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) { TextoBotonDialogo(textoSecundario) }

                    Button(
                        onClick = onPrincipal,
                        shape = RoundedCornerShape(14.dp),
                        colors = colorPrincipal,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) { TextoBotonDialogo(textoPrincipal) }
                }
            }
        }
    }
}

/** Texto de los botones lado a lado: centrado y, si no cabe, en dos líneas (antes se cortaba hacia la izquierda). */
@Composable
private fun TextoBotonDialogo(texto: String) {
    Text(
        texto,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        lineHeight = 17.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Separa "¿Pregunta?\n\nExplicación" en título y mensaje; si no hay separación, todo es título. */
internal fun separarTituloMensaje(texto: String): Pair<String, String?> {
    val partes = texto.split("\n\n", limit = 2)
    return partes[0].trim() to partes.getOrNull(1)?.trim()
}
