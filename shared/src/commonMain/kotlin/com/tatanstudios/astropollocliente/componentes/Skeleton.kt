package com.tatanstudios.astropollocliente.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val GrisSkeleton = Color(0xFFE6E6E6)

/** Bloque gris de skeleton. */
@Composable
fun BloqueSkeleton(modifier: Modifier, radio: Dp = 12.dp) {
    Box(modifier.clip(RoundedCornerShape(radio)).background(GrisSkeleton))
}

/** Contenedor que hace "respirar" (parpadeo suave) a los bloques de adentro. */
@Composable
fun ContenedorSkeleton(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    val transicion = rememberInfiniteTransition(label = "skeleton")
    val alfa by transicion.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alfa"
    )
    Box(modifier.alpha(alfa)) { contenido() }
}

/** Forma de la lista de productos de una categoría (encabezado + tarjetas con imagen a la derecha). */
@Composable
fun ListaProductosSkeleton(modifier: Modifier = Modifier) {
    ContenedorSkeleton(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) { seccion ->
                BloqueSkeleton(Modifier.padding(top = 16.dp).width(if (seccion == 0) 150.dp else 120.dp).height(22.dp), radio = 6.dp)
                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BloqueSkeleton(Modifier.fillMaxWidth(0.7f).height(18.dp), radio = 6.dp)
                            BloqueSkeleton(Modifier.fillMaxWidth(0.9f).height(12.dp), radio = 6.dp)
                            BloqueSkeleton(Modifier.width(60.dp).height(14.dp), radio = 6.dp)
                        }
                        Spacer(Modifier.width(12.dp))
                        BloqueSkeleton(Modifier.size(72.dp))
                    }
                }
            }
        }
    }
}

/** Forma del detalle de un producto (imagen grande, nombre, descripción, precio y un bloque de opciones). */
@Composable
fun DetalleProductoSkeleton(modifier: Modifier = Modifier) {
    ContenedorSkeleton(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BloqueSkeleton(Modifier.fillMaxWidth().height(210.dp), radio = 16.dp)
            Spacer(Modifier.height(4.dp))
            BloqueSkeleton(Modifier.fillMaxWidth(0.65f).height(26.dp), radio = 6.dp)
            BloqueSkeleton(Modifier.fillMaxWidth().height(14.dp), radio = 6.dp)
            BloqueSkeleton(Modifier.fillMaxWidth(0.8f).height(14.dp), radio = 6.dp)
            BloqueSkeleton(Modifier.width(90.dp).height(22.dp), radio = 6.dp)
            Spacer(Modifier.height(8.dp))
            BloqueSkeleton(Modifier.fillMaxWidth().height(120.dp), radio = 14.dp)
            BloqueSkeleton(Modifier.fillMaxWidth().height(90.dp), radio = 14.dp)
        }
    }
}
