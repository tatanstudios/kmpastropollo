package com.tatanstudios.astropollocliente.componentes

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import kotlinx.coroutines.flow.first

/**
 * true si hay sesión iniciada, false si es visitante, null mientras se lee DataStore.
 * El visitante puede ver el menú; carrito, órdenes y perfil piden iniciar sesión.
 */
@Composable
fun rememberSesionIniciada(): Boolean? {
    var sesion by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) { sesion = TokenManager().idUsuario.first().isNotEmpty() }
    return sesion
}

@Composable
fun DialogoIniciarSesion(
    navController: NavController,
    mensaje: String = "Para continuar necesitas iniciar sesión o crear una cuenta.",
    onDismiss: () -> Unit,
) {
    DialogoAviso(
        titulo = "Inicia sesión",
        mensaje = mensaje,
        textoPrincipal = "Iniciar sesión",
        onPrincipal = {
            onDismiss()
            navController.navigate(Routes.VistaLogin.route) { launchSingleTop = true }
        },
        tipo = TipoAviso.INFO,
        textoSecundario = "Ahora no",
        onSecundario = onDismiss,
        icono = Icons.Filled.Person,
    )
}
