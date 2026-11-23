package com.tatanstudios.astropollocliente.vistas.login

import com.tatanstudios.astropollocliente.extras.Notificaciones
import androidx.savedstate.read
import com.tatanstudios.astropollocliente.extras.SesionApi
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.ToastType
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tatanstudios.astropollocliente.componentes.LottieAnimation
import com.tatanstudios.astropollocliente.componentes.rememberLottieArchivo
import com.tatanstudios.astropollocliente.componentes.animateLottieCompositionAsState
import com.tatanstudios.astropollocliente.componentes.LottieConstants
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.vistas.principal.opciones.perfil.PerfilScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.historial.HistorialFechaScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.historial.HistorialOrdenScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.historial.InfoProductoHistorialScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.historial.ListadoProductosHistorialScreen
import com.tatanstudios.astropollocliente.vistas.principal.PrincipalScreen
import com.tatanstudios.astropollocliente.vistas.principal.sucursal.ElegirSucursalScreen
import com.tatanstudios.astropollocliente.vistas.principal.carrito.CarritoComprasScreen
import com.tatanstudios.astropollocliente.vistas.principal.carrito.EditarProductoScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones.MapaScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones.MisDireccionesScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones.RegistrarNuevaDireccionScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.direcciones.SeleccionarDireccionScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.horarios.HorariosScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.password.ActualizarPasswordScreen
import com.tatanstudios.astropollocliente.vistas.principal.opciones.usuario.MiUsuarioScreen
import com.tatanstudios.astropollocliente.vistas.principal.ordenes.EstadoOrdenScreen
import com.tatanstudios.astropollocliente.vistas.principal.productos.ElegirProductoScreen
import com.tatanstudios.astropollocliente.vistas.principal.productos.EnviarOrdenScreen
import com.tatanstudios.astropollocliente.vistas.principal.productos.ListadoProductosDeUnaOrdenScreen
import com.tatanstudios.astropollocliente.vistas.principal.productos.ListadoProductosScreen

// *** RUTAS DE NAVEGACION ***
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // la API rechazó el token (sesión cerrada en otro lado, contraseña cambiada o usuario bloqueado)
    LaunchedEffect(Unit) {
        SesionApi.sesionExpirada.collect {
            CustomToasty(null, "Tu sesión terminó, inicia sesión de nuevo", ToastType.INFO)
            navController.navigate(Routes.VistaLogin.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.VistaSplash.route) {

        composable(Routes.VistaSplash.route) { SplashScreen(navController) }
        composable(Routes.VistaLogin.route) { LoginScreen(navController) }

        // VISITANTE: detectar o elegir sucursal para ver el menú sin sesión
        composable(
            route = Routes.VistaElegirSucursal.route,
            arguments = listOf(navArgument("auto") { type = NavType.BoolType; defaultValue = true })
        ) { backStackEntry ->
            val auto = backStackEntry.arguments?.read { getBooleanOrNull("auto") } ?: true
            ElegirSucursalScreen(navController = navController, automatico = auto)
        }
        composable(Routes.VistaRegistro.route) { RegistroScreen(navController) }
        composable(Routes.VistaRecuperarCorreo.route) { RecuperarPasswordScreen(navController) }

        composable(Routes.VistaCambiarPasswordCorreo.route) { backStackEntry ->
            val idusuario = backStackEntry.arguments?.read { getStringOrNull("idusuario") } ?: ""
            val token = backStackEntry.arguments?.read { getStringOrNull("token") } ?: ""

            CambiarPasswordEmailScreen(navController = navController, idusuario = idusuario, token = token)
        }



        composable(
            route = Routes.VistaPrincipal.route, // "principal/{selectedScreenVar}"
            arguments = listOf(
                navArgument("selectedScreenVar") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val selected = backStackEntry.arguments?.read { getStringOrNull("selectedScreenVar") } ?: "menu"
            PrincipalScreen(
                navController = navController,
                selectedScreenVar = selected      // 👈 pásalo POR NOMBRE
            )
        }




        composable(Routes.VistaCarrito.route) { CarritoComprasScreen(navController) }
        composable(Routes.VistaPerfil.route) { PerfilScreen(navController) }
        composable(Routes.VistaHistorialFecha.route) { HistorialFechaScreen(navController) }

        // HISTORIAL LISTADO ORDENES
        composable(Routes.VistaHistorialListadoOrden.route) { backStackEntry ->
            val fecha1 = backStackEntry.arguments?.read { getStringOrNull("fecha1") } ?: ""
            val fecha2 = backStackEntry.arguments?.read { getStringOrNull("fecha2") } ?: ""

            HistorialOrdenScreen(navController = navController, _fecha1 = fecha1, _fecha2 = fecha2)
        }

        // LISTADO DE PRODUCTOS HISTORIAL ORDEN
        composable(Routes.VistaListadoProductosHistorialOrden.route) { backStackEntry ->
            val idordenStr = backStackEntry.arguments?.read { getStringOrNull("idorden") } ?: "0"
            val idorden = idordenStr.toIntOrNull() ?: 0

            ListadoProductosHistorialScreen(navController = navController, _idorden = idorden)
        }

        // VISTA INFO PRODUCTO DE HISTORIAL
        composable(Routes.VistaInfoProductoHistorialOrden.route) { backStackEntry ->
            val idproductoStr = backStackEntry.arguments?.read { getStringOrNull("idproducto") } ?: "0"
            val idproducto = idproductoStr.toIntOrNull() ?: 0

            InfoProductoHistorialScreen(navController = navController, _idproducto = idproducto)
        }

        // VISTA ACTUALIZAR CONTRASENA
        composable(Routes.VistaActualizarContrasena.route) { ActualizarPasswordScreen(navController) }

        // VISTA HORARIOS
        composable(Routes.VistaHorarios.route) { HorariosScreen(navController) }


        // VISTA MIS DIRECCIONES
        composable(
            route = Routes.VistaMisDirecciones.route,
            arguments = listOf(
                navArgument("estadoBoton") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val estadoBoton = backStackEntry.arguments?.read { getIntOrNull("estadoBoton") } ?: 0

            MisDireccionesScreen(
                navController = navController,
                estadoBotonAtras = estadoBoton
            )
        }

        // VISTA MAPA
        composable(Routes.VistaMapa.route) { MapaScreen(navController) }

        // VISTA REGISTRAR NUEVA DIRECCION
        composable(Routes.VistaRegistroDireccion.route) { backStackEntry ->

            val id = backStackEntry.arguments?.read { getStringOrNull("id") }?.toIntOrNull() ?: 0
            val latitud = backStackEntry.arguments?.read { getStringOrNull("latitud") } ?: "0"
            val longitud = backStackEntry.arguments?.read { getStringOrNull("longitud") } ?: "0"
            val latitudrealStr = backStackEntry.arguments?.read { getStringOrNull("latitudreal") }
            val longitudrealStr = backStackEntry.arguments?.read { getStringOrNull("longitudreal") }

            val latitudreal = if (latitudrealStr == "none") null else latitudrealStr
            val longitudreal = if (longitudrealStr == "none") null else longitudrealStr

            RegistrarNuevaDireccionScreen(
                navController = navController,
                idzona = id,
                latitud = latitud,
                longitud = longitud,
                latitudreal = latitudreal,
                longitudreal = longitudreal
            )
        }

        // VISTA SELECCIONAR DIRECCION
        composable(
            route = Routes.VistaSeleccionarDireccion.route,
            arguments = listOf(navArgument("etiqueta") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->

            val id = backStackEntry.arguments?.read { getStringOrNull("id") }?.toIntOrNull() ?: 0
            val nombre = backStackEntry.arguments?.read { getStringOrNull("nombre") } ?: ""
            val telefono = backStackEntry.arguments?.read { getStringOrNull("telefono") } ?: ""
            val direccion = backStackEntry.arguments?.read { getStringOrNull("direccion") } ?: ""
            val referencia = backStackEntry.arguments?.read { getStringOrNull("referencia") } ?: ""
            val etiqueta = backStackEntry.arguments?.read { getStringOrNull("etiqueta") }

            SeleccionarDireccionScreen(
                navController = navController,
                id = id,
                nombre,
                telefono,
                direccion,
                referencia,
                etiqueta = etiqueta,
            )
        }

        // VISTA MI USUARIO
        composable(Routes.VistaMiUsuario.route) { MiUsuarioScreen(navController) }

        // VISTA LISTADO DE PRODUCTOS
        composable(
            route = Routes.VistaListadoProductos.route,
            arguments = listOf(
                navArgument("idcategoria") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val idCategoria = backStackEntry.arguments?.read { getIntOrNull("idcategoria") } ?: 0

            ListadoProductosScreen(
                navController = navController,
                idCategoria = idCategoria
            )
        }

        // VISTA INFORMACION PRODUCTO
        composable(Routes.VistaInformacionProducto.route) { backStackEntry ->
            val idproductoStr = backStackEntry.arguments?.read { getStringOrNull("idproducto") } ?: "0"
            val idproducto = idproductoStr.toIntOrNull() ?: 0

            ElegirProductoScreen(navController = navController, idProducto = idproducto)
        }

        // VISTA INFORMACION PRODUCTO PARA EDITARLO
        composable(Routes.VistaEditarProducto.route) { backStackEntry ->
            val idfilaCarritoStr = backStackEntry.arguments?.read { getStringOrNull("idfilacarrito") } ?: "0"
            val idfilaCarrito = idfilaCarritoStr.toIntOrNull() ?: 0

            EditarProductoScreen(navController = navController, idfilaCarrito)
        }

        // VISTA ENVIAR ORDEN
        composable(Routes.VistaEnviarOrden.route) { EnviarOrdenScreen(navController) }

        // VISTA ESTADO ORDENES
        composable(Routes.VistaEstadoOrden.route) { backStackEntry ->

            val idorden = backStackEntry.arguments?.read { getStringOrNull("idorden") }?.toIntOrNull() ?: 0

            EstadoOrdenScreen(
                navController = navController,
                idorden = idorden,
            )
        }

        // VISTA LISTADO PRODUCTOS DE UNA ORDEN
        composable(Routes.VistaListaProductosDeOrden.route) { backStackEntry ->

            val idorden = backStackEntry.arguments?.read { getStringOrNull("idorden") }?.toIntOrNull() ?: 0

            ListadoProductosDeUnaOrdenScreen(
                navController = navController,
                idorden = idorden,
            )
        }

    }
}

/**
 * Pantalla de arranque SIN espera ni animación: solo decide a dónde entrar y navega de inmediato.
 * (Antes mostraba una animación 3 s.) Se queda en blanco crema los milisegundos que tarda en leer la sesión.
 */
@Composable
fun SplashScreen(navController: NavHostController) {
    val ctx = LocalPlatformContext.current
    val tokenManager = remember { TokenManager(ctx) }

    // Evitar que el usuario vuelva al splash con el botón atrás
    DisposableEffect(Unit) {
        onDispose {
            navController.popBackStack(Routes.VistaSplash.route, true)
        }
    }

    // Con sesión o con sucursal guardada va al menú; un visitante nuevo primero elige su sucursal.
    LaunchedEffect(Unit) {
        // primero la migración desde SharedPreferences (antes la cubría la espera de 3 s)
        tokenManager.migrateFromSharedPreferencesIfNeeded()

        // sesion de la version anterior (sin token): al login con aviso
        if (tokenManager.limpiarSesionAnteriorSinToken()) {
            SesionApi.avisoIniciarSesionDeNuevo = true
            navController.navigate(Routes.VistaLogin.route) {
                popUpTo(Routes.VistaSplash.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        val idusuario = tokenManager.idUsuario.first()
        val idSucursal = tokenManager.idSucursal.first()
        // con sesion: asegurar que el telefono quede vinculado para recibir avisos de sus ordenes
        Notificaciones.vincular(idusuario)

        val destino = if (idusuario.isNotEmpty() || idSucursal != null) {
            Routes.VistaPrincipal.createRoute("menu")
        } else {
            Routes.VistaElegirSucursal.createRoute(automatico = true)
        }

        navController.navigate(destino) {
            popUpTo(Routes.VistaSplash.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.colorCremaV1))
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    val navController = rememberNavController()
    SplashScreen(navController = navController)
}