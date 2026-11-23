package com.tatanstudios.astropollocliente.vistas.principal.opciones.usuario

import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.tatanstudios.astropollocliente.componentes.BotonAuthPrincipal
import com.tatanstudios.astropollocliente.componentes.CampoAuth
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import com.tatanstudios.astropollocliente.vistas.principal.opciones.perfil.AvatarInicial
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.BloqueTextFieldCorreoUsuario
import com.tatanstudios.astropollocliente.componentes.BloqueTextFieldUsuario
import com.tatanstudios.astropollocliente.componentes.CustomModal1Boton
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.viewmodel.ActualizarCorreoUsuarioViewModel
import com.tatanstudios.astropollocliente.viewmodel.InformacionUsuarioViewModel
import com.tatanstudios.astropollocliente.vistas.login.esCorreoValido
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MiUsuarioScreen(navController: NavHostController,
                   viewModelInformacion: InformacionUsuarioViewModel = viewModel { InformacionUsuarioViewModel() },
                   viewModelActualizar: ActualizarCorreoUsuarioViewModel = viewModel { ActualizarCorreoUsuarioViewModel() },
) {
    val ctx = LocalPlatformContext.current

    val resultadoInformacion by viewModelInformacion.resultado.collectAsState()
    val isLoadingInformacion by viewModelInformacion.isLoading.collectAsState(false)

    val resultadoActualizar by viewModelActualizar.resultado.collectAsState()
    val isLoadingActualizar by viewModelActualizar.isLoading.collectAsState(false)

    var idusuario by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val tokenManager = remember { TokenManager(ctx) }
    val scope = rememberCoroutineScope()

    // el usuario no se edita aquí (se elige al registrarse); se reenvía tal cual a la API
    var usuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var correoGuardado by remember { mutableStateOf("") }
    var datosCargados by remember { mutableStateOf(false) }

    var showModal1Boton by remember { mutableStateOf(false) }
    var modalMensajeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        idusuario = tokenManager.idUsuario.first()
        viewModelInformacion.informacionUsuarioRetrofit(idusuario)
    }

    val hayCambios = correo.trim() != correoGuardado

    val guardar: () -> Unit = {
        keyboardController?.hide()
        when {
            correo.isBlank() -> {
                modalMensajeString = textoRes(Res.string.correo_es_requerido)
                showModal1Boton = true
            }
            !esCorreoValido(correo.trim()) -> {
                modalMensajeString = textoRes(Res.string.correo_ingresado_no_es_valido)
                showModal1Boton = true
            }
            else -> scope.launch { viewModelActualizar.actualizarUsuarioRetrofit(idusuario, usuario, correo.trim()) }
        }
    }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Mi perfil", AppColors.colorRojo) },
        bottomBar = {
            if (datosCargados) {
                Surface(shadowElevation = 12.dp, color = Color.White) {
                    Button(
                        onClick = guardar,
                        enabled = hayCambios,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.colorLetraRoja,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE0E0E0),
                            disabledContentColor = Color(0xFF9E9E9E),
                        ),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .height(52.dp)
                    ) { Text("Guardar correo", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (datosCargados) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ---- usuario (solo lectura) ----
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarInicial(usuario, tamano = 84.dp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                usuario,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorLetraPrincipal
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Tu usuario para iniciar sesión",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.colorGrisIcono
                                )
                            }
                        }
                    }

                    // ---- correo de recuperación ----
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Correo de recuperación",
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.colorLetraPrincipal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (correoGuardado.isBlank()) {
                                    Text(
                                        "Sin agregar",
                                        color = Color(0xFF8D6E00),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFFFF8E1))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                "Si olvidas tu contraseña, te enviaremos un código a este correo para crear una nueva.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.colorGrisIcono
                            )
                            CampoAuth(
                                valor = correo,
                                onCambio = { correo = it },
                                etiqueta = stringResource(Res.string.correo_electronico),
                                icono = Icons.Filled.Email,
                                maxLength = 100,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done,
                                onIme = { if (hayCambios) guardar() },
                            )
                        }
                    }
                }
            } else if (isLoadingInformacion) {
                ContenedorSkeleton(Modifier.padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        BloqueSkeleton(Modifier.fillMaxWidth().height(190.dp), radio = 18.dp)
                        BloqueSkeleton(Modifier.fillMaxWidth().height(170.dp), radio = 18.dp)
                    }
                }
            }

            ErrorReintentar(viewModelInformacion)
        }

        if (showModal1Boton) {
            CustomModal1Boton(showModal1Boton, modalMensajeString, onDismiss = { showModal1Boton = false })
        }
        if (isLoadingActualizar) LoadingModal(isLoading = true)
    }

    resultadoInformacion?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                usuario = result.usuario ?: ""
                correo = result.correo ?: ""
                correoGuardado = correo
                datosCargados = true
            }
            else -> viewModelInformacion.marcarError()
        }
    }

    resultadoActualizar?.getContentIfNotHandled()?.let { result ->
        // API v2: 1 = correo ya usado por otro cliente, 2 = usuario ya usado, 3 = actualizado
        when (result.success) {
            1 -> {
                modalMensajeString = stringResource(Res.string.otro_correo_ya_tiene)
                showModal1Boton = true
            }
            2 -> {
                modalMensajeString = stringResource(Res.string.otro_usuario_ya_tiene)
                showModal1Boton = true
            }
            3 -> {
                correoGuardado = correo.trim()
                CustomToasty(ctx, "Correo guardado", ToastType.SUCCESS)
            }
            else -> CustomToasty(ctx, stringResource(Res.string.error_reintentar_de_nuevo), ToastType.ERROR)
        }
    }
}
