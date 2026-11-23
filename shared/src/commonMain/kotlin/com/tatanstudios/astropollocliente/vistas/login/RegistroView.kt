package com.tatanstudios.astropollocliente.vistas.login

import com.tatanstudios.astropollocliente.extras.Notificaciones
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomModal1BotonTitulo
import com.tatanstudios.astropollocliente.componentes.CustomModal2Botones
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.IlustracionLottieAuth
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.PantallaAuth
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.extras.getOneSignalUserId
import com.tatanstudios.astropollocliente.extras.getVersionName
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.RegistroViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegistroScreen(navController: NavHostController, viewModel: RegistroViewModel = viewModel { RegistroViewModel() }) {

    val ctx = LocalPlatformContext.current
    val usuario by viewModel.usuario.collectAsState("")
    val password by viewModel.password.collectAsState("")
    val correo by viewModel.correo.collectAsState("")

    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)

    val keyboardController = LocalSoftwareKeyboardController.current
    val tokenManager = remember { TokenManager(ctx) }
    val scope = rememberCoroutineScope()

    // MODAL 1 BOTON
    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }

    // titulo y mensaje de respuestas
    var textoTituloApi by remember { mutableStateOf("") }
    var textoMensajeApi by remember { mutableStateOf("") }
    var showDialogApi by remember { mutableStateOf(false) }

    var showModal2Boton by remember { mutableStateOf(false) }
    var idonesignal by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { idonesignal = getOneSignalUserId() }

    val texto4CaracteresMinimo = stringResource(Res.string.minimo_4_caracteres)

    val registrarse: () -> Unit = {
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
            password.length < 4 -> {
                modalMensajeString = texto4CaracteresMinimo
                showModal1Boton = true
            }
            !correo.isNullOrBlank() && !esCorreoValido(correo!!) -> {
                modalMensajeString = textoRes(Res.string.correo_ingresado_no_es_valido)
                showModal1Boton = true
            }
            else -> showModal2Boton = true
        }
    }

    PantallaAuth(
        titulo = "Crear cuenta",
        subtitulo = "Regístrate para pedir y seguir tus órdenes",
        onAtras = { irAlLoginDesdeRegistro(navController) },
        ilustracion = { IlustracionLottieAuth("files/jsonhotdog.json") },
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
            ayuda = "Mínimo 4 caracteres",
        )

        CampoAuth(
            valor = correo ?: "",
            onCambio = { viewModel.setCorreo(it) },
            etiqueta = stringResource(Res.string.correo_opcional),
            icono = Icons.Filled.Email,
            maxLength = 100,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onIme = registrarse,
            ayuda = "Te sirve para recuperar tu contraseña",
        )

        BotonAuthPrincipal(texto = "Crear cuenta", onClick = registrarse)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta?", color = AppColors.colorGrisIcono, fontSize = 14.sp)
            TextButton(onClick = { irAlLoginDesdeRegistro(navController) }) {
                Text("Inicia sesión", color = AppColors.colorLetraRoja, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showDialogApi) {
        CustomModal1BotonTitulo(
            showDialog = showDialogApi,
            title = textoTituloApi,
            message = textoMensajeApi,
            onDismiss = { showDialogApi = false }
        )
    }

    if (showModal1Boton) {
        CustomModal1Boton(showModal1Boton, modalMensajeString, onDismiss = { showModal1Boton = false })
    }

    // CONFIRMAR PARA REGISTRARSE
    if (showModal2Boton) {
        CustomModal2Botones(
            showDialog = true,
            titulo = "¿Crear tu cuenta?",
            message = "Con tu usuario y contraseña podrás hacer pedidos y seguir tus órdenes.",
            onDismiss = { showModal2Boton = false },
            onAccept = {
                showModal2Boton = false
                scope.launch {
                    viewModel.registroRetrofit(null, version = getVersionName(ctx))
                }
            },
            txtBtnAceptar = "Crear cuenta",
            txtBtnCancelar = "Cancelar",
        )
    }

    if (isLoading) LoadingModal(isLoading = true)

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1, 2 -> {
                // USUARIO O CORREO YA REGISTRADO
                textoTituloApi = result.titulo ?: ""
                textoMensajeApi = result.mensaje ?: ""
                showDialogApi = true
            }
            3 -> {
                // REGISTRADO CORRECTAMENTE
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
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}

/** Regresa al login (normalmente es la pantalla anterior; si no, lo abre). */
private fun irAlLoginDesdeRegistro(navController: NavHostController) {
    if (navController.previousBackStackEntry?.destination?.route == Routes.VistaLogin.route) {
        navController.popBackStack()
    } else {
        navController.navigate(Routes.VistaLogin.route) {
            popUpTo(Routes.VistaRegistro.route) { inclusive = true }
            launchSingleTop = true
        }
    }
}

/** Mismo patrón que android.util.Patterns.EMAIL_ADDRESS. */
private val PATRON_CORREO = Regex(
    """[a-zA-Z0-9+._%-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9-]{0,64}(.[a-zA-Z0-9][a-zA-Z0-9-]{0,25})+"""
)

fun esCorreoValido(correo: String): Boolean = PATRON_CORREO.matches(correo)
