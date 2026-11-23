package com.tatanstudios.astropollocliente.vistas.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.IlustracionLottieAuth
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.OtpTextField
import com.tatanstudios.astropollocliente.componentes.PantallaAuth
import com.tatanstudios.astropollocliente.componentes.TextoAyudaAuth
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.EnviarCorreoApiViewModel
import com.tatanstudios.astropollocliente.viewmodel.VerificarCodigoCorreoViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecuperarPasswordScreen(navController: NavHostController,
                            viewModel: EnviarCorreoApiViewModel = viewModel { EnviarCorreoApiViewModel() },
                            viewModelVerificar: VerificarCodigoCorreoViewModel = viewModel { VerificarCodigoCorreoViewModel() }
) {
    val ctx = LocalPlatformContext.current
    val correo by viewModel.correo.collectAsState("")

    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)

    val resultadoVerificar by viewModelVerificar.resultado.collectAsState()
    val isLoadingVerificar by viewModelVerificar.isLoading.collectAsState(false)

    val keyboardController = LocalSoftwareKeyboardController.current

    val textoCorreoRequerido = stringResource(Res.string.correo_es_requerido)
    val textoCorreoInvalido = stringResource(Res.string.correo_ingresado_no_es_valido)
    val textoCodigoEnviado = stringResource(Res.string.codigo_enviado)
    val textoCorreoNoEncontrado = stringResource(Res.string.correo_no_encontrado)
    val textoCodigoRequerido = stringResource(Res.string.codigo_es_requerido)
    val textoCodigoIncorrecto = stringResource(Res.string.codigo_incorrecto)
    val textoCodigoExpirado = stringResource(Res.string.codigo_expirado)
    val textoDemasiadosIntentos = stringResource(Res.string.codigo_demasiados_intentos)
    val textoDemasiadasSolicitudes = stringResource(Res.string.codigo_demasiadas_solicitudes)

    // paso 2: ya se envió el código y se pide escribirlo
    var boolOTP by remember { mutableStateOf(false) }
    var txtFieldCodigo by remember { mutableStateOf("") }

    val enviarCorreo: () -> Unit = {
        keyboardController?.hide()
        when {
            correo.isBlank() -> CustomToasty(ctx, textoCorreoRequerido, ToastType.INFO)
            !esCorreoValido(correo) -> CustomToasty(ctx, textoCorreoInvalido, ToastType.INFO)
            else -> viewModel.enviarCorreoRetrofit()
        }
    }

    val verificarCodigo: () -> Unit = {
        keyboardController?.hide()
        if (txtFieldCodigo.length < 6) {
            CustomToasty(ctx, textoCodigoRequerido, ToastType.INFO)
        } else {
            viewModelVerificar.verificarCodigoRetrofit(txtFieldCodigo, correo)
        }
    }

    // Atrás: en el paso del código vuelve a pedir el correo; si no, regresa al login
    val regresar: () -> Unit = {
        when {
            boolOTP -> { boolOTP = false; txtFieldCodigo = "" }
            navController.previousBackStackEntry != null -> navController.popBackStack()
            else -> navController.navigate(Routes.VistaLogin.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // flecha del sistema en el paso del código: volver a pedir el correo (no salir de la pantalla)
    @OptIn(ExperimentalComposeUiApi::class)
    BackHandler(enabled = boolOTP) { boolOTP = false; txtFieldCodigo = "" }

    PantallaAuth(
        titulo = "Recuperar contraseña",
        subtitulo = if (boolOTP) "Revisa tu correo" else "Te enviaremos un código a tu correo",
        onAtras = regresar,
        ilustracion = { IlustracionLottieAuth("files/jsonemail.json") },
    ) {
        if (boolOTP) {
            TextoAyudaAuth("Escribe el código de 6 dígitos que enviamos a\n$correo")

            OtpTextField(
                codigo = txtFieldCodigo,
                onTextChanged = { txtFieldCodigo = it }
            )

            BotonAuthPrincipal(texto = "Verificar código", onClick = verificarCodigo)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { viewModel.enviarCorreoRetrofit() }) {
                    Text("Reenviar código", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { boolOTP = false; txtFieldCodigo = "" }) {
                    Text("Cambiar correo", color = AppColors.colorGrisIcono, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            TextoAyudaAuth("Escribe el correo con el que te registraste.")

            CampoAuth(
                valor = correo ?: "",
                onCambio = { viewModel.setCorreo(it) },
                etiqueta = stringResource(Res.string.correo_electronico),
                icono = Icons.Filled.Email,
                maxLength = 100,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Send,
                onIme = enviarCorreo,
            )

            BotonAuthPrincipal(texto = stringResource(Res.string.enviar_codigo), onClick = enviarCorreo)
        }
    }

    if (isLoading) LoadingModal(isLoading = true)
    if (isLoadingVerificar) LoadingModal(isLoading = true)

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // CODIGO ENVIADO
                CustomToasty(ctx, textoCodigoEnviado, ToastType.INFO)
                boolOTP = true
            }
            2 -> CustomToasty(ctx, textoCorreoNoEncontrado, ToastType.INFO)
            4 -> CustomToasty(ctx, textoDemasiadasSolicitudes, ToastType.INFO)
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }

    resultadoVerificar?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // se obtiene id del usuario y token de un solo uso -> pantalla cambiar contraseña
                navController.navigate(
                    Routes.VistaCambiarPasswordCorreo.createRoute(result.id.toString(), result.token ?: "")
                ) { launchSingleTop = true }
            }
            2 -> CustomToasty(ctx, textoCodigoIncorrecto, ToastType.INFO)
            3, 4 -> {
                // CODIGO VENCIDO O DEMASIADOS INTENTOS: volver a pedir el codigo
                CustomToasty(
                    ctx,
                    if (result.success == 3) textoCodigoExpirado else textoDemasiadosIntentos,
                    ToastType.INFO
                )
                txtFieldCodigo = ""
                boolOTP = false
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
