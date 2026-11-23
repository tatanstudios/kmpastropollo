package com.tatanstudios.astropollocliente.vistas.principal.opciones.password

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.text.input.ImeAction
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.TextoAyudaAuth
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatanstudios.astropollocliente.componentes.LottieAnimation
import com.tatanstudios.astropollocliente.componentes.rememberLottieArchivo
import com.tatanstudios.astropollocliente.componentes.LottieConstants
import com.tatanstudios.astropollocliente.componentes.animateLottieCompositionAsState
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.BloqueTextFieldPassword
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.viewmodel.ActualizarPasswordViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ActualizarPasswordScreen(navController: NavHostController,
                               viewModel: ActualizarPasswordViewModel = viewModel { ActualizarPasswordViewModel() }) {

    val ctx = LocalPlatformContext.current
    val password by viewModel.passowrd.collectAsState("")
    val resultado by viewModel.resultado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(false)
    val keyboardController = LocalSoftwareKeyboardController.current

    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }
    val tokenManager = remember { TokenManager(ctx) }
    var idusuario by remember { mutableStateOf("") }
    val texto4CaracteresMinimo = stringResource(Res.string.minimo_4_caracteres)

    LaunchedEffect(Unit) { idusuario = tokenManager.idUsuario.first() }

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
            else -> viewModel.actualizarContrasenaRetrofit(idusuario)
        }
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Cambiar contraseña", AppColors.colorRojo) },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = Color.White) {
                BotonAuthPrincipal(
                    texto = "Guardar contraseña",
                    onClick = guardar,
                    modifier = Modifier.navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AppColors.colorLetraRoja.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = AppColors.colorLetraRoja, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        "Nueva contraseña",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.colorLetraPrincipal
                    )
                    TextoAyudaAuth("Escribe la contraseña nueva. La próxima vez que inicies sesión usarás esta.")

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
                }
            }
        }

        if (showModal1Boton) {
            CustomModal1Boton(showModal1Boton, modalMensajeString, onDismiss = { showModal1Boton = false })
        }

        if (isLoading) LoadingModal(isLoading = true)
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                // CONTRASENA ACTUALIZADA
                CustomToasty(ctx, "Contraseña actualizada", ToastType.SUCCESS)
                navController.popBackStack()
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
