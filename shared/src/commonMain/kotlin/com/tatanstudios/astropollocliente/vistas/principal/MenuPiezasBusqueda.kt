package com.tatanstudios.astropollocliente.vistas.opciones.menu

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.model.modelos.ModeloProductoBuscable
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.resources.Res
import com.tatanstudios.astropollocliente.resources.camaradefecto
import com.tatanstudios.astropollocliente.resources.spinloading
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import org.jetbrains.compose.resources.painterResource

// ======================= encabezado: "Entregar en" + buscador =======================

/**
 * Encabezado rojo del menú (reemplaza la toolbar con título "Menú"):
 * - fila superior: a dónde se entrega (cliente) o de qué sucursal es el menú (invitado); tocarla
 *   lleva a cambiarla. A la derecha, perfil / iniciar sesión.
 * - debajo: buscador de productos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EncabezadoMenu(
    esInvitado: Boolean,
    entregaNombre: String?,
    entregaDireccion: String?,
    sucursal: String?,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    mostrarBuscador: Boolean,
    onCambiarEntrega: () -> Unit,
    onPerfil: () -> Unit,
) {
    val etiqueta = if (esInvitado) "Viendo el menú de" else "Entregar en"
    val titulo = if (esInvitado) {
        sucursal ?: "Elegir sucursal"
    } else {
        listOfNotNull(entregaNombre?.takeIf { it.isNotBlank() }, entregaDireccion?.takeIf { it.isNotBlank() })
            .joinToString(" · ")
            .ifBlank { "Mi dirección" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.colorRojo)
            .windowInsetsPadding(TopAppBarDefaults.windowInsets)
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onCambiarEntrega)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = etiqueta,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = titulo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Cambiar", tint = Color.White)
                }
            }
            IconButton(onClick = onPerfil) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = if (esInvitado) "Iniciar sesión" else "Perfil",
                    tint = Color.White
                )
            }
        }

        if (mostrarBuscador) {
            Spacer(Modifier.height(10.dp))
            BuscadorMenu(
                texto = busqueda,
                onTextoChange = onBusquedaChange,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

@Composable
private fun BuscadorMenu(texto: String, onTextoChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val teclado = LocalSoftwareKeyboardController.current
    BasicTextField(
        value = texto,
        onValueChange = { onTextoChange(it.take(60)) },
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, color = AppColors.colorLetraPrincipal),
        cursorBrush = SolidColor(AppColors.colorLetraRoja),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { teclado?.hide() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { campo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = AppColors.colorGrisIcono, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (texto.isEmpty()) {
                        Text("Buscar en el menú", fontSize = 15.sp, color = AppColors.colorLetraGris)
                    }
                    campo()
                }
                if (texto.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Borrar búsqueda",
                        tint = AppColors.colorGrisIcono,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { onTextoChange("") }
                    )
                }
            }
        }
    )
}

// ======================= búsqueda =======================

/** minúsculas y sin tildes, para que "pina" encuentre "Piña" y "cafe" encuentre "Café". */
private fun normalizar(texto: String): String = buildString(texto.length) {
    for (c in texto.lowercase()) {
        append(
            when (c) {
                'á', 'à', 'ä', 'â' -> 'a'
                'é', 'è', 'ë', 'ê' -> 'e'
                'í', 'ì', 'ï', 'î' -> 'i'
                'ó', 'ò', 'ö', 'ô' -> 'o'
                'ú', 'ù', 'ü', 'û' -> 'u'
                'ñ' -> 'n'
                else -> c
            }
        )
    }
}

/**
 * Filtra por nombre, categoría y descripción. Cada palabra escrita debe aparecer en alguno de ellos.
 * Primero los que coinciden por nombre.
 */
internal fun buscarProductos(productos: List<ModeloProductoBuscable>, consulta: String): List<ModeloProductoBuscable> {
    val palabras = normalizar(consulta).split(' ').filter { it.isNotBlank() }
    if (palabras.isEmpty()) return emptyList()
    return productos
        .map { p ->
            val nombre = normalizar(p.nombre ?: "")
            val resto = normalizar("${p.categoria ?: ""} ${p.descripcion ?: ""}")
            val coincide = palabras.all { it in nombre || it in resto }
            val porNombre = palabras.all { it in nombre }
            Triple(p, coincide, porNombre)
        }
        .filter { it.second }
        .sortedByDescending { it.third }
        .map { it.first }
}

@Composable
internal fun FilaResultadoBusqueda(producto: ModeloProductoBuscable, onClick: () -> Unit) {
    val ctx = LocalPlatformContext.current
    val imagenUrl = if (producto.utilizaImagen == 1 && !producto.imagen.isNullOrBlank()) {
        "${ApiConfig.URL_IMAGENES}${producto.imagen}"
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.colorRedondeadoGris),
                contentAlignment = Alignment.Center
            ) {
                if (imagenUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(imagenUrl).crossfade(true).build(),
                        placeholder = painterResource(Res.drawable.spinloading),
                        error = painterResource(Res.drawable.camaradefecto),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(painterResource(Res.drawable.camaradefecto), contentDescription = null, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = producto.nombre ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.colorLetraPrincipal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!producto.categoria.isNullOrBlank()) {
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.colorGrisIcono,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = producto.precio ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.colorLetraRoja
                )
            }
        }
    }
}

@Composable
internal fun SinResultadosBusqueda(consulta: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = AppColors.colorLetraGris, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No encontramos \"${consulta.trim()}\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = AppColors.colorLetraPrincipal
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Prueba con otra palabra, por ejemplo \"pollo\" o \"combo\".",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = AppColors.colorGrisIcono
        )
    }
}

// ======================= skeleton de carga =======================

/** Bloques grises que "respiran" con la forma del menú mientras llega la información. */
@Composable
internal fun MenuSkeleton() {
    val transicion = rememberInfiniteTransition(label = "skeleton")
    val alfa by transicion.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alfa"
    )
    val gris = Color(0xFFE6E6E6)

    @Composable
    fun Bloque(modifier: Modifier, radio: Int = 12) =
        Box(modifier.clip(RoundedCornerShape(radio.dp)).background(gris))

    Column(Modifier.fillMaxWidth().alpha(alfa)) {
        // banner
        Bloque(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp).fillMaxWidth().aspectRatio(16f / 9f), radio = 16)

        // título "Categorías"
        Bloque(Modifier.padding(start = 16.dp, top = 22.dp, bottom = 10.dp).width(130.dp).height(22.dp), radio = 6)

        repeat(2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(3) { Bloque(Modifier.weight(1f).height(118.dp), radio = 16) }
            }
        }

        // título "Populares"
        Bloque(Modifier.padding(start = 16.dp, top = 22.dp, bottom = 10.dp).width(110.dp).height(22.dp), radio = 6)

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) { Bloque(Modifier.width(156.dp).height(190.dp), radio = 16) }
        }
    }
}
