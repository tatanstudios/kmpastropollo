package com.tatanstudios.astropollocliente.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tatanstudios.astropollocliente.viewmodel.PoligonoUI

/** Coordenada multiplataforma (reemplaza com.google.android.gms.maps.model.LatLng en el código común). */
data class LatLng(val latitude: Double, val longitude: Double)

/**
 * Estado del mapa compartido entre el código común y el mapa nativo.
 * - [centro]: punto que está en el centro de la pantalla (lo actualiza el mapa al moverse).
 * - [moverA]: pedir al mapa que se mueva a esa coordenada.
 */
class EstadoMapa {
    var centro by mutableStateOf<LatLng?>(null)
    var moverA by mutableStateOf<LatLng?>(null)
}

/** Mapa nativo (Google Maps en Android, MapKit en iOS) con las zonas de entrega dibujadas. */
@Composable
expect fun MapaZonas(
    modifier: Modifier,
    poligonos: List<PoligonoUI>,
    estado: EstadoMapa,
    mostrarMiUbicacion: Boolean,
)

/** true si la app ya tiene permiso de ubicación. */
expect fun tienePermisoUbicacion(): Boolean

/** Última ubicación conocida del teléfono, o null si no hay permiso o no está disponible. */
expect suspend fun obtenerUbicacionActual(): LatLng?

/** Pide el permiso de ubicación (si falta) y avisa el resultado. */
@Composable
expect fun SolicitarPermisosUbicacion(
    onPermisosConcedidos: () -> Unit,
    onPermisosDenegados: () -> Unit,
)

/**
 * Reemplazo de PolyUtil.containsLocation(punto, poligono, geodesic = false):
 * algoritmo de "ray casting" sobre latitud/longitud.
 */
fun contieneUbicacion(punto: LatLng, poligono: List<LatLng>): Boolean {
    if (poligono.size < 3) return false
    var dentro = false
    var j = poligono.lastIndex
    for (i in poligono.indices) {
        val a = poligono[i]
        val b = poligono[j]
        if ((a.latitude > punto.latitude) != (b.latitude > punto.latitude)) {
            val lngCruce = (b.longitude - a.longitude) * (punto.latitude - a.latitude) /
                (b.latitude - a.latitude) + a.longitude
            if (punto.longitude < lngCruce) dentro = !dentro
        }
        j = i
    }
    return dentro
}
