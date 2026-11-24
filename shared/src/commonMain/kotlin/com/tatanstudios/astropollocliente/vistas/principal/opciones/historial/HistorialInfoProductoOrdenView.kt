package com.tatanstudios.astropollocliente.vistas.principal.opciones.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.HorizontalDivider
import androidx.navigation.navOptions
import com.tatanstudios.astropollocliente.componentes.BloqueSkeleton
import com.tatanstudios.astropollocliente.componentes.ContenedorSkeleton
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import com.tatanstudios.astropollocliente.model.modelos.ModeloInfoProductoArray
import com.tatanstudios.astropollocliente.componentes.DetalleProductoSkeleton
import com.tatanstudios.astropollocliente.componentes.ErrorReintentar
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tatanstudios.astropollocliente.resources.*
import com.tatanstudios.astropollocliente.ui.theme.AppColors
import com.tatanstudios.astropollocliente.extras.textoRes
import com.tatanstudios.astropollocliente.componentes.BarraToolbarColor
import com.tatanstudios.astropollocliente.componentes.CustomToasty
import com.tatanstudios.astropollocliente.componentes.LoadingModal
import com.tatanstudios.astropollocliente.componentes.ToastType
import com.tatanstudios.astropollocliente.network.ApiConfig
import com.tatanstudios.astropollocliente.viewmodel.InfoProductoViewModel
import kotlinx.coroutines.launch

@Composable
fun InfoProductoHistorialScreen(navController: NavHostController, _idproducto: Int,
                       viewModel: InfoProductoViewModel = viewModel { InfoProductoViewModel() },
) {
    val isLoading by viewModel.isLoading.collectAsState(initial = true)
    val resultado by viewModel.resultado.collectAsState()
    var producto by remember { mutableStateOf<ModeloInfoProductoArray?>(null) }

    LaunchedEffect(Unit) { viewModel.infoProductoRetrofit(idproducto = _idproducto) }

    Scaffold(
        containerColor = AppColors.colorCremaV1,
        topBar = { BarraToolbarColor(navController, "Producto", AppColors.colorRojo) }
    ) { innerPadding ->

        val p = producto
        if (p != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- imagen + nombre ----
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (p.utilizaImagen == 1 && !p.imagen.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalPlatformContext.current)
                                        .data("${ApiConfig.URL_IMAGENES}${p.imagen}")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = p.nombreProducto,
                                    contentScale = ContentScale.Fit,
                                    placeholder = painterResource(Res.drawable.spinloading),
                                    error = painterResource(Res.drawable.camaradefecto),
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            }
                        }
                        Text(
                            p.nombreProducto ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.colorLetraPrincipal
                        )
                        val desc = (p.descripcion ?: "").replace("\r\n", "\n").trim()
                        if (desc.isNotEmpty()) {
                            Text(desc, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv1)
                        }
                    }
                }

                // ---- opciones elegidas ----
                if (p.opciones.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Lo que elegiste", fontWeight = FontWeight.SemiBold, color = AppColors.colorLetraPrincipal)
                            p.opciones.forEach { linea ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = AppColors.colorVerde, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(linea, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv2)
                                }
                            }
                        }
                    }
                }

                // ---- nota ----
                if (!p.nota.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF8E1))
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Filled.EditNote, contentDescription = null, tint = Color(0xFF8D6E00), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Tu nota", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF8D6E00))
                            Text(p.nota, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisv2)
                        }
                    }
                }

                // ---- precio ----
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilaPrecio("Precio unitario", p.precio ?: "")
                        FilaPrecio("Cantidad", "${p.cantidad}")
                        HorizontalDivider(color = AppColors.colorRedondeadoGris)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorLetraPrincipal,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                p.multiplicado ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.colorLetraRoja
                            )
                        }
                    }
                }
            }
        } else if (isLoading) {
            DetalleProductoSkeleton(Modifier.padding(innerPadding))
        }

        ErrorReintentar(viewModel, Modifier.padding(innerPadding))
    }

    resultado?.getContentIfNotHandled()?.let { result ->
        when (result.success) {
            1 -> {
                val item = result.lista.firstOrNull()
                if (item != null) producto = item else viewModel.marcarError()
            }
            else -> viewModel.marcarError()
        }
    }
}

@Composable
private fun FilaPrecio(etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = AppColors.colorGrisIcono, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = AppColors.colorLetraPrincipal)
    }
}
