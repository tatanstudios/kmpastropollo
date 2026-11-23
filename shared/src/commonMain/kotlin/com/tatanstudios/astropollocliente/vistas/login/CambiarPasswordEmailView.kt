package com.tatanstudios.astropollocliente.vistas.login

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.IlustracionLottieAuth
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.PantallaAuth
import com.tatanstudios.astropollocliente.componentes.TextoAyudaAuth
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.viewmodel.CambiarPasswordEmailViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CambiarPasswordEmailScreen(navController: NavHostController,
                               idusuario: String,
                               token: String,
                               viewModel: CambiarPasswordEmailViewModel = viewModel { CambiarPasswordEmailViewModel() }) {

    val ctx = LocalPlatformContext.current
    val password by viewModel.passowrd.collectAsState("")

    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)

    val keyboardController = LocalSoftwareKeyboardController.current

    // MODAL 1 BOTON
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }

    val texto4CaracteresMinimo = stringResource(Res.string.minimo_4_caracteres)

    val guardar: () -> Unit = {
        keyboardController?.hide()
        when {
            password.isBlank() -> {
                modalMensajeString = textoRes(Res.string.password_es_requerido)
                showModal1Boton = true
            }
            password.length < 4 -> {
                modalMensajeString = texto4CaracteresMinimo
                showModal1Boton = true
            }
            else -> viewModel.resetearContrasenaEmailRetrofit(idusuario, token)
        }
    }

    // Atrás: el código ya se usó, así que se vuelve al login
    val irAlLogin: () -> Unit = {
        navController.navigate(Routes.VistaLogin.route) {
            popUpTo(Routes.VistaLogin.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    PantallaAuth(
        titulo = "Nueva contraseña",
        subtitulo = "Crea una contraseña que recuerdes",
        onAtras = irAlLogin,
        ilustracion = { IlustracionLottieAuth("files/jsonpassword.json") },
    ) {
        TextoAyudaAuth("Escribe tu nueva contraseña. Con ella vas a iniciar sesión.")

        CampoAuth(
            valor = password,
            onCambio = { viewModel.setPassword(it) },
            etiqueta = stringResource(Res.string.nueva_contrasena),
            icono = Icons.Filled.Lock,
            maxLength = 16,
            esPassword = true,
            imeAction = ImeAction.Done,
            onIme = guardar,
            ayuda = "Mínimo 4 caracteres",
        )

        BotonAuthPrincipal(texto = "Guardar contraseña", onClick = guardar)
    }

    if (showModal1Boton) {
        CustomModal1Boton(showModal1Boton, modalMensajeString, onDismiss = { showModal1Boton = false })
    }

    if (isLoading) LoadingModal(isLoading = true)

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // CONTRASENA ACTUALIZADA
                CustomToasty(ctx, stringResource(Res.string.actualizado), ToastType.SUCCESS)
                navController.navigate(Routes.VistaLogin.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            2, 3 -> {
                // TOKEN INVALIDO O VENCIDO: volver a solicitar el codigo
                CustomToasty(ctx, stringResource(Res.string.recuperacion_vencida), ToastType.INFO)
                navController.navigate(Routes.VistaRecuperarCorreo.route) {
                    popUpTo(Routes.VistaLogin.route)
                    launchSingleTop = true
                }
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
