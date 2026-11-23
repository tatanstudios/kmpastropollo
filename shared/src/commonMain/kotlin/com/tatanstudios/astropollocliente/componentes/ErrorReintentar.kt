package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.PeticionViewModel

/**
 * Pantalla completa de "no se pudo cargar" con botón Reintentar.
 * Se dibuja ENCIMA del contenido (tapa lo que haya debajo y bloquea los toques).
 */
@Composable
fun PanelErrorReintentar(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    titulo: String = "No pudimos cargar la información",
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.colorCremaV1)
            // bloquear toques al contenido de abajo
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(AppColors.colorLetraRoja.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = AppColors.colorLetraRoja,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = AppColors.colorLetraPrincipal
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = AppColors.colorGrisIcono
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onReintentar,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.colorLetraRoja,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reintentar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Atajo: muestra [PanelErrorReintentar] cuando la última petición del ViewModel falló
 * y vuelve a lanzarla al tocar "Reintentar". Ponerlo como ÚLTIMO hijo del contenedor
 * de la pantalla (debajo de la toolbar, para que el botón Atrás siga funcionando).
 */
@Composable
fun ErrorReintentar(viewModel: PeticionViewModel<*>, modifier: Modifier = Modifier) {
    val error by viewModel.error.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()
    val mensaje = error
    if (mensaje != null && !cargando) {
        PanelErrorReintentar(mensaje = mensaje, onReintentar = { viewModel.reintentar() }, modifier = modifier)
    }
}
