package com.tatanstudios.astropollocliente.vistas.login

import com.tatanstudios.astropollocliente.extras.Notificaciones
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.BotonAuthSecundario
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.PantallaAuth
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.SesionApi
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.extras.getOneSignalUserId
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel = viewModel { LoginViewModel() }) {

    val ctx = LocalPlatformContext.current
    val usuario by viewModel.usuario.collectAsState("")
    val password by viewModel.password.collectAsState("")
    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)

    val keyboardController = LocalSoftwareKeyboardController.current
    val tokenManager = remember { TokenManager(ctx) }
    val scope = rememberCoroutineScope()

    // MODAL 1 BOTON
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }

    // venia con sesion de la version anterior (sin token): explicar por que debe entrar de nuevo
    LaunchedEffect(Unit) {
        if (SesionApi.avisoIniciarSesionDeNuevo) {
            SesionApi.avisoIniciarSesionDeNuevo = false
            modalMensajeString = SesionApi.MENSAJE_INICIAR_SESION_DE_NUEVO
            showModal1Boton = true
        }
    }

    var idonesignal by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { idonesignal = getOneSignalUserId() }

    val iniciarSesion: () -> Unit = {
        keyboardController?.hide()
        when {
            usuario.isBlank() -> {
                modalMensajeString = textoRes(Res.string.usuario_es_requerido)
                showModal1Boton = true
            }
            password.isBlank() -> {
                modalMensajeString = textoRes(Res.string.password_es_requerido)
                showModal1Boton = true
            }
            else -> viewModel.verificarUsuarioPasssword(idonesignal = "")
        }
    }

    // Atrás: volver a donde venía (menú de invitado, perfil...). Si el login es la primera pantalla
    // (sesión vencida o venía de una versión anterior), ir al menú como invitado.
    val regresar: () -> Unit = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            scope.launch {
                val idSucursal = tokenManager.idSucursal.first()
                val destino = if (idSucursal != null) Routes.VistaPrincipal.createRoute("menu")
                else Routes.VistaElegirSucursal.createRoute(automatico = true)
                navController.navigate(destino) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    PantallaAuth(
        titulo = stringResource(Res.string.astro_pollo),
        subtitulo = "Inicia sesión para hacer tu pedido",
        onAtras = regresar,
        ilustracion = {
            Image(
                painter = painterResource(Res.drawable.logonegrocirculo),
                contentDescription = stringResource(Res.string.logotipo),
                modifier = Modifier.size(104.dp).clip(CircleShape)
            )
        },
    ) {
        CampoAuth(
            valor = usuario,
            onCambio = { viewModel.setUsuario(it) },
            etiqueta = stringResource(Res.string.usuario),
            icono = Icons.Filled.Person,
            maxLength = 20,
        )

        CampoAuth(
            valor = password,
            onCambio = { viewModel.setPassword(it) },
            etiqueta = stringResource(Res.string.contrasena),
            icono = Icons.Filled.Lock,
            maxLength = 16,
            esPassword = true,
            imeAction = ImeAction.Done,
            onIme = iniciarSesion,
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                navController.navigate(Routes.VistaRecuperarCorreo.route) { launchSingleTop = true }
            }) {
                Text("¿Olvidaste tu contraseña?", color = AppColors.colorLetraRoja, fontWeight = FontWeight.Medium)
            }
        }

        BotonAuthPrincipal(texto = stringResource(Res.string.iniciar_sesion), onClick = iniciarSesion)

        // separador "¿No tienes cuenta?"
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(Modifier.weight(1f), color = AppColors.colorRedondeadoGris)
            Text(
                "  ¿No tienes cuenta?  ",
                color = AppColors.colorGrisIcono,
                fontSize = 14.sp
            )
            HorizontalDivider(Modifier.weight(1f), color = AppColors.colorRedondeadoGris)
        }

        BotonAuthSecundario(texto = "Crear cuenta", onClick = {
            navController.navigate(Routes.VistaRegistro.route) { launchSingleTop = true }
        })

        Spacer(Modifier.height(2.dp))
    }

    if (showModal1Boton) {
        CustomModal1Boton(showModal1Boton, modalMensajeString, onDismiss = { showModal1Boton = false })
    }

    if (isLoading) LoadingModal(isLoading = true)

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // USUARIO BLOQUEADO
                modalMensajeString = textoRes(Res.string.usuario_bloqueado)
                showModal1Boton = true
            }
            2 -> {
                // INICIO SESION CORRECTO
                val id = result.id.toString()
                Notificaciones.vincular(id)
                scope.launch {
                    tokenManager.saveID(id)
                    tokenManager.saveToken(result.token ?: "")
                    navController.navigate(Routes.VistaPrincipal.createRoute("menu")) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            3 -> CustomToasty(ctx, stringResource(Res.string.password_incorrecto), ToastType.INFO)
            4 -> CustomToasty(ctx, stringResource(Res.string.usuario_incorrecto), ToastType.INFO)
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
