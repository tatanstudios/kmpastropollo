package com.tatanstudios.astropollocliente.vistas.principal.sucursal

import com.tatanstudios.astropollocliente.componentes.PanelErrorReintentar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.LocalPlatformContext
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.extras.SolicitarPermisosUbicacion
import com.tatanstudios.astropollocliente.extras.TokenManager
import com.tatanstudios.astropollocliente.extras.contieneUbicacion
import com.tatanstudios.astropollocliente.extras.obtenerUbicacionActual
import com.tatanstudios.astropollocliente.extras.redireccionarAjustes
import com.tatanstudios.astropollocliente.model.modelos.ModeloSucursal
import com.tatanstudios.astropollocliente.model.rutas.Routes
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.viewmodel.ListadoPoligonosViewModel
import com.tatanstudios.astropollocliente.viewmodel.ListadoSucursalesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Visitante sin sesión: elige la sucursal (restaurante) cuyo menú quiere ver.
 * Con GPS se detecta sola: ubicación → zona de entrega (polígono) → sucursal de esa zona.
 *
 * @param automatico true = al abrir intenta el GPS y, si cae en una zona, entra directo al menú.
 *                   false = viene de "Cambiar sucursal": solo muestra la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirSucursalScreen(
    navController: NavHostController,
    automatico: Boolean,
    viewModelSucursales: ListadoSucursalesViewModel = viewModel { ListadoSucursalesViewModel() },
    viewModelPoligonos: ListadoPoligonosViewModel = viewModel { ListadoPoligonosViewModel() },
) {
    val ctx = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(ctx) }

    val sucursales = viewModelSucursales.sucursales
    val poligonos = viewModelPoligonos.poligonosUI
    val cargandoLista by viewModelSucursales.isLoading.collectAsState()
    val resultado by viewModelSucursales.resultado.collectAsState()

    var sucursalActual by remember { mutableStateOf<Int?>(null) }
    var buscandoUbicacion by remember { mutableStateOf(false) }
    // cada vez que sube se vuelve a pedir el permiso / leer el GPS (0 = no pedir)
    var solicitudGps by remember { mutableIntStateOf(if (automatico) 1 else 0) }

    fun entrarAlMenu(sucursal: ModeloSucursal) {
        scope.launch {
            tokenManager.saveSucursal(sucursal.id)
            navController.navigate(Routes.VistaPrincipal.createRoute("menu")) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    /** GPS → zona → sucursal. */
    fun buscarPorUbicacion() {
        scope.launch {
            buscandoUbicacion = true
            // esperar a que lleguen sucursales y zonas
            val listos = withTimeoutOrNull(15_000) {
                snapshotFlow { sucursales.isNotEmpty() && poligonos.isNotEmpty() }.first { it }
            } != null
            val ubicacion = if (listos) obtenerUbicacionActual() else null
            buscandoUbicacion = false

            if (!listos) return@launch
            if (ubicacion == null) {
                CustomToasty(ctx, "No se pudo obtener tu ubicación, elige una sucursal", ToastType.INFO)
                return@launch
            }
            val zona = poligonos.firstOrNull { contieneUbicacion(ubicacion, it.puntos) }
            val sucursal = zona?.let { z -> sucursales.firstOrNull { z.id in it.zonas } }
            if (sucursal != null) {
                entrarAlMenu(sucursal)
            } else {
                CustomToasty(ctx, "Estás fuera de nuestras zonas de entrega, elige una sucursal", ToastType.INFO)
            }
        }
    }

    LaunchedEffect(Unit) {
        sucursalActual = tokenManager.idSucursal.first()
        viewModelSucursales.listadoSucursalesRetrofit()
        viewModelPoligonos.listadoPoligonosRetrofit()
    }

    if (solicitudGps > 0) {
        key(solicitudGps) {
            SolicitarPermisosUbicacion(
                onPermisosConcedidos = { buscarPorUbicacion() },
                onPermisosDenegados = {
                    // al abrir solo se muestra la lista; si lo pidió con el botón, llevar a ajustes
                    if (solicitudGps > 1 || !automatico) {
                        CustomToasty(ctx, "Activa el permiso de ubicación", ToastType.INFO)
                        redireccionarAjustes(ctx)
                    }
                },
            )
        }
    }

    LaunchedEffect(resultado) {
        resultado?.getContentIfNotHandled()?.let {
            if (it.success != 1) viewModelSucursales.marcarError("Error al cargar las sucursales")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Elige tu sucursal", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    if (!automatico) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        navController.navigate(Routes.VistaLogin.route) { launchSingleTop = true }
                    }) {
                        Text("Iniciar sesión", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.colorRojo),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.colorCremaV1)
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedButton(
                    onClick = { solicitudGps++ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppColors.colorRojo)
                    Spacer(Modifier.width(8.dp))
                    Text("Usar mi ubicación", color = AppColors.colorRojo, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Text(
                    "O elige la sucursal para ver su menú:",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF555555),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            items(sucursales, key = { it.id }) { sucursal ->
                TarjetaSucursal(
                    sucursal = sucursal,
                    esActual = sucursal.id == sucursalActual,
                    onClick = { entrarAlMenu(sucursal) },
                )
            }
        }

        // Falló la lista de sucursales o las zonas: un solo panel que reintenta lo que falló
        val errorSucursales by viewModelSucursales.error.collectAsState()
        val errorPoligonos by viewModelPoligonos.error.collectAsState()
        val cargandoPoligonos by viewModelPoligonos.isLoading.collectAsState()
        val mensajeError = errorSucursales ?: errorPoligonos
        if (mensajeError != null && !cargandoLista && !cargandoPoligonos) {
            PanelErrorReintentar(
                mensaje = mensajeError,
                onReintentar = {
                    if (errorSucursales != null) viewModelSucursales.reintentar()
                    if (errorPoligonos != null) viewModelPoligonos.reintentar()
                },
                modifier = Modifier.padding(innerPadding),
            )
        }

        if (cargandoLista && sucursales.isEmpty()) LoadingModal(isLoading = true)
        if (buscandoUbicacion) LoadingModal(isLoading = true, titulo = "Buscando tu sucursal...")
    }
}

@Composable
private fun TarjetaSucursal(sucursal: ModeloSucursal, esActual: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (esActual) Modifier.border(2.dp, AppColors.colorRojo, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(AppColors.colorRojo.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppColors.colorRojo)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(sucursal.nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (!sucursal.descripcion.isNullOrBlank()) {
                    Text(
                        "Entrega en: ${sucursal.descripcion}",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (esActual) {
                    Text("Sucursal actual", color = AppColors.colorRojo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
