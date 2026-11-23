package com.tatanstudios.astropollocliente.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.tatanstudios.astropollocliente.viewmodel.PoligonoUI
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSError
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygonRenderer
import platform.UIKit.UIColor
import platform.darwin.NSObject
import kotlin.coroutines.resume

// ------------------------------- MAPA (MapKit) -------------------------------

@OptIn(ExperimentalForeignApi::class)
private class DelegadoMapa(val estado: EstadoMapa) : NSObject(), MKMapViewDelegateProtocol {

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        mapView.centerCoordinate.useContents { estado.centro = LatLng(latitude, longitude) }
    }

    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        val poligono = rendererForOverlay as? MKPolygon ?: return MKOverlayRenderer(rendererForOverlay)
        return MKPolygonRenderer(poligono).apply {
            fillColor = UIColor(red = 0.0, green = 1.0, blue = 0.0, alpha = 0.2)
            strokeColor = UIColor(red = 0.098, green = 0.463, blue = 0.824, alpha = 1.0)
            lineWidth = 2.0
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun crearPoligono(puntos: List<LatLng>): MKPolygon = memScoped {
    val coords = allocArray<CLLocationCoordinate2D>(puntos.size)
    puntos.forEachIndexed { i, p ->
        coords[i].latitude = p.latitude
        coords[i].longitude = p.longitude
    }
    MKPolygon.polygonWithCoordinates(coords, puntos.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapaZonas(
    modifier: Modifier,
    poligonos: List<PoligonoUI>,
    estado: EstadoMapa,
    mostrarMiUbicacion: Boolean,
) {
    val delegado = remember(estado) { DelegadoMapa(estado) }
    val mapa = remember { MKMapView().apply { delegate = delegado } }

    LaunchedEffect(estado.moverA) {
        estado.moverA?.let {
            // equivalente al zoom 16 de Google Maps (~500 m de alto)
            val region = MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(it.latitude, it.longitude), 500.0, 500.0,
            )
            mapa.setRegion(region, animated = false)
            estado.moverA = null
        }
    }

    LaunchedEffect(poligonos.toList()) {
        mapa.removeOverlays(mapa.overlays)
        poligonos.filter { it.puntos.size >= 3 }.forEach { mapa.addOverlay(crearPoligono(it.puntos)) }
    }

    UIKitView(
        factory = { mapa },
        modifier = modifier,
        update = { it.showsUserLocation = mostrarMiUbicacion && tienePermisoUbicacion() },
    )
}

// ----------------------------- UBICACION (CoreLocation) -----------------------------

private val gestorUbicacion by lazy { CLLocationManager() }

/** CLLocationManager guarda el delegado como referencia débil: hay que retenerlo. */
private var retenerDelegado: NSObject? = null

private fun estadoAutorizado(estado: CLAuthorizationStatus) =
    estado == kCLAuthorizationStatusAuthorizedWhenInUse || estado == kCLAuthorizationStatusAuthorizedAlways

actual fun tienePermisoUbicacion(): Boolean = estadoAutorizado(gestorUbicacion.authorizationStatus)

@OptIn(ExperimentalForeignApi::class)
actual suspend fun obtenerUbicacionActual(): LatLng? {
    if (!tienePermisoUbicacion()) return null
    gestorUbicacion.location?.let { loc ->
        return loc.coordinate.useContents { LatLng(latitude, longitude) }
    }
    // sin ubicación en caché: pedir una lectura
    return suspendCancellableCoroutine { cont ->
        val delegado = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val loc = didUpdateLocations.lastOrNull() as? CLLocation
                manager.delegate = null
                if (cont.isActive) cont.resume(loc?.coordinate?.useContents { LatLng(latitude, longitude) })
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                manager.delegate = null
                if (cont.isActive) cont.resume(null)
            }
        }
        retenerDelegado = delegado
        gestorUbicacion.delegate = delegado
        gestorUbicacion.requestLocation()
    }
}

@Composable
actual fun SolicitarPermisosUbicacion(
    onPermisosConcedidos: () -> Unit,
    onPermisosDenegados: () -> Unit,
) {
    DisposableEffect(Unit) {
        val estado = gestorUbicacion.authorizationStatus
        when {
            estadoAutorizado(estado) -> onPermisosConcedidos()
            estado == kCLAuthorizationStatusNotDetermined -> {
                val delegado = object : NSObject(), CLLocationManagerDelegateProtocol {
                    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                        val nuevo = manager.authorizationStatus
                        if (nuevo == kCLAuthorizationStatusNotDetermined) return
                        manager.delegate = null
                        if (estadoAutorizado(nuevo)) onPermisosConcedidos() else onPermisosDenegados()
                    }
                }
                retenerDelegado = delegado
                gestorUbicacion.delegate = delegado
                gestorUbicacion.requestWhenInUseAuthorization()
            }
            else -> onPermisosDenegados()
        }
        onDispose { }
    }
}
