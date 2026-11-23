package com.tatanstudios.astropollocliente.extras

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.withTimeoutOrNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.tatanstudios.astropollocliente.viewmodel.PoligonoUI
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.model.LatLng as GLatLng

private fun LatLng.google() = GLatLng(latitude, longitude)

@Composable
actual fun MapaZonas(
    modifier: Modifier,
    poligonos: List<PoligonoUI>,
    estado: EstadoMapa,
    mostrarMiUbicacion: Boolean,
) {
    val cameraPositionState = rememberCameraPositionState()

    // mover la cámara cuando el código común lo pide (zoom 16, igual que antes)
    LaunchedEffect(estado.moverA) {
        estado.moverA?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it.google(), 16f))
            estado.moverA = null
        }
    }
    // informar el centro actual al código común
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.target }.collect {
            estado.centro = LatLng(it.latitude, it.longitude)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = mostrarMiUbicacion && tienePermisoUbicacion()),
        // botones propios en la pantalla (Mi ubicación); se ocultan los de Google para que no tapen
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        ),
    ) {
        poligonos.forEach { poligono ->
            Polygon(
                points = poligono.puntos.map { it.google() },
                fillColor = Color(0x3300FF00),
                strokeColor = Color(0xFF1976D2),
                strokeWidth = 2f,
            )
        }
    }
}

actual fun tienePermisoUbicacion(): Boolean =
    ContextCompat.checkSelfPermission(AndroidPlatform.appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
actual suspend fun obtenerUbicacionActual(): LatLng? {
    if (!tienePermisoUbicacion()) return null
    val cliente = LocationServices.getFusedLocationProviderClient(AndroidPlatform.appContext)
    return try {
        // lectura actual (la de caché puede ser vieja); si tarda, usar la última conocida
        val actual = withTimeoutOrNull(8_000) {
            cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        }
        (actual ?: cliente.lastLocation.await())?.let { LatLng(it.latitude, it.longitude) }
    } catch (_: Exception) {
        null
    }
}

@Composable
actual fun SolicitarPermisosUbicacion(
    onPermisosConcedidos: () -> Unit,
    onPermisosDenegados: () -> Unit,
) {
    val context = LocalContext.current
    val permisoUbicacion = Manifest.permission.ACCESS_FINE_LOCATION

    val solicitudPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) onPermisosConcedidos() else onPermisosDenegados()
    }

    val permisosConcedidos = ContextCompat.checkSelfPermission(context, permisoUbicacion) ==
        PackageManager.PERMISSION_GRANTED

    if (!permisosConcedidos) {
        LaunchedEffect(Unit) { solicitudPermisos.launch(permisoUbicacion) }
    } else {
        // una sola vez, no en cada recomposición
        LaunchedEffect(Unit) { onPermisosConcedidos() }
    }
}
