package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatanstudios.astropollocliente.extras.formatoDosDecimales
import com.tatanstudios.astropollocliente.model.modelos.ModeloGrupoModificador
import com.tatanstudios.astropollocliente.model.modelos.ModeloOpcionModificador
import com.tatanstudios.astropollocliente.model.modelos.ModeloSeleccionOpcion
import com.tatanstudios.astropollocliente.ui.theme.AppColors

/**
 * Modificadores de producto (bloques de opciones). Las reglas son las mismas que valida la API
 * (App\Services\Modificadores), para que el cliente no pueda armar algo que el servidor rechace:
 *
 *  - bloque con máximo 1: se comporta como radio (tocar otra opción la cambia)
 *  - opción con máximo > 1: contador (+ / −)
 *  - obligatoria: siempre incluida, no se puede quitar
 *  - "una sola": si se elige, no se combina con otras del bloque
 *
 * La selección es un mapa idOpcion -> cantidad.
 */
object ReglasModificadores {

    /** Selección inicial: lo que ya estaba elegido (al editar) + las obligatorias. */
    fun seleccionInicial(
        grupos: List<ModeloGrupoModificador>,
        previa: List<ModeloSeleccionOpcion> = emptyList(),
    ): Map<Int, Int> {
        val validas = grupos.flatMap { it.opciones }.associateBy { it.id }
        val seleccion = previa.filter { validas.containsKey(it.idOpcion) && it.cantidad > 0 }
            .associate { it.idOpcion to it.cantidad }
            .toMutableMap()
        grupos.flatMap { it.opciones }.filter { it.obligatorio == 1 }.forEach { o ->
            if ((seleccion[o.id] ?: 0) < 1) seleccion[o.id] = 1
        }
        return seleccion
    }

    fun totalGrupo(grupo: ModeloGrupoModificador, seleccion: Map<Int, Int>): Int =
        grupo.opciones.sumOf { seleccion[it.id] ?: 0 }

    private fun hayObligatoria(grupo: ModeloGrupoModificador) = grupo.opciones.any { it.obligatorio == 1 }

    /** ¿Se puede elegir (o sumar una unidad a) esta opción ahora? */
    fun puedeAgregar(grupo: ModeloGrupoModificador, opcion: ModeloOpcionModificador, seleccion: Map<Int, Int>): Boolean {
        val actual = seleccion[opcion.id] ?: 0
        if (actual >= maxOf(1, opcion.maximo)) return false
        if (opcion.unaSola == 1 && hayObligatoria(grupo)) return false // chocaría con la obligatoria
        if (grupo.maximo <= 1) return true // radio: reemplaza
        val total = totalGrupo(grupo, seleccion)
        // "una sola" reemplaza todo lo del bloque; lo demás respeta el máximo del bloque
        if (opcion.unaSola == 1) return true
        val quitaUnaSola = grupo.opciones.filter { it.unaSola == 1 }.sumOf { seleccion[it.id] ?: 0 }
        return total - quitaUnaSola < grupo.maximo
    }

    /** Suma una unidad (o elige) la opción, aplicando las reglas del bloque. */
    fun agregar(grupo: ModeloGrupoModificador, opcion: ModeloOpcionModificador, seleccion: Map<Int, Int>): Map<Int, Int> {
        if (!puedeAgregar(grupo, opcion, seleccion)) return seleccion
        val nueva = seleccion.toMutableMap()
        val idsGrupo = grupo.opciones.map { it.id }
        when {
            grupo.maximo <= 1 -> {
                // radio: queda solo esta (las obligatorias no se tocan; en un bloque de 1 no hay otra)
                idsGrupo.filter { it != opcion.id }.forEach { id ->
                    if (grupo.opciones.first { o -> o.id == id }.obligatorio != 1) nueva.remove(id)
                }
                nueva[opcion.id] = 1
            }
            opcion.unaSola == 1 -> {
                idsGrupo.forEach { nueva.remove(it) }
                nueva[opcion.id] = 1
            }
            else -> {
                // al elegir otra, se quita la "una sola" que hubiera
                grupo.opciones.filter { it.unaSola == 1 }.forEach { nueva.remove(it.id) }
                nueva[opcion.id] = (nueva[opcion.id] ?: 0) + 1
            }
        }
        return nueva
    }

    /** Resta una unidad (o quita) la opción. Las obligatorias no bajan de 1. */
    fun quitar(grupo: ModeloGrupoModificador, opcion: ModeloOpcionModificador, seleccion: Map<Int, Int>): Map<Int, Int> {
        val actual = seleccion[opcion.id] ?: 0
        if (actual <= 0) return seleccion
        if (opcion.obligatorio == 1 && actual <= 1) return seleccion
        // radio requerido: no se puede dejar vacío tocando la misma (hay que elegir otra)
        if (grupo.maximo <= 1 && grupo.minimo >= 1 && actual <= 1) return seleccion
        val nueva = seleccion.toMutableMap()
        if (actual <= 1) nueva.remove(opcion.id) else nueva[opcion.id] = actual - 1
        return nueva
    }

    /** Mismo criterio que la API. Devuelve el primer error o null si se puede agregar. */
    fun validar(grupos: List<ModeloGrupoModificador>, seleccion: Map<Int, Int>): String? {
        for (g in grupos) {
            val total = totalGrupo(g, seleccion)
            g.opciones.firstOrNull { it.obligatorio == 1 && (seleccion[it.id] ?: 0) < 1 }?.let {
                return "«${it.nombre}» es obligatorio en «${g.nombre}»"
            }
            val elegidas = g.opciones.filter { (seleccion[it.id] ?: 0) > 0 }
            if (elegidas.size > 1 && elegidas.any { it.unaSola == 1 }) {
                return "«${elegidas.first { it.unaSola == 1 }.nombre}» no se puede combinar con otras opciones de «${g.nombre}»"
            }
            if (total > maxOf(1, g.maximo)) return "«${g.nombre}»: máximo ${g.maximo}"
            if ((g.requerido == 1 || total > 0) && total < g.minimo) return "«${g.nombre}»: ${textoRegla(g)}"
        }
        return null
    }

    /** Ids de los bloques que aún no cumplen su regla (para resaltarlos). */
    fun gruposIncompletos(grupos: List<ModeloGrupoModificador>, seleccion: Map<Int, Int>): Set<Int> =
        grupos.filter { validar(listOf(it), seleccion) != null }.map { it.id }.toSet()

    /** Precio extra por unidad de producto. */
    fun extra(grupos: List<ModeloGrupoModificador>, seleccion: Map<Int, Int>): Double =
        grupos.flatMap { it.opciones }.sumOf { (it.precio.toDoubleOrNull() ?: 0.0) * (seleccion[it.id] ?: 0) }

    /** Texto para la API: "idOpcion:cantidad,idOpcion:cantidad". */
    fun textoApi(seleccion: Map<Int, Int>): String =
        seleccion.filter { it.value > 0 }.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun textoRegla(g: ModeloGrupoModificador): String {
        val min = g.minimo
        val max = maxOf(1, g.maximo)
        return when {
            min == max -> if (max == 1) "elige 1" else "elige $max"
            min <= 0 -> "elige hasta $max"
            else -> "elige de $min a $max"
        }
    }
}

/** Lista de bloques con sus opciones. [resaltar] = ids de bloques a marcar como pendientes. */
@Composable
fun SelectorModificadores(
    grupos: List<ModeloGrupoModificador>,
    seleccion: Map<Int, Int>,
    onCambio: (Map<Int, Int>) -> Unit,
    resaltar: Set<Int> = emptySet(),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        grupos.forEach { grupo ->
            BloqueModificador(
                grupo = grupo,
                seleccion = seleccion,
                onCambio = onCambio,
                pendiente = resaltar.contains(grupo.id),
            )
        }
    }
}

@Composable
private fun BloqueModificador(
    grupo: ModeloGrupoModificador,
    seleccion: Map<Int, Int>,
    onCambio: (Map<Int, Int>) -> Unit,
    pendiente: Boolean,
) {
    val esRadio = grupo.maximo <= 1
    val total = ReglasModificadores.totalGrupo(grupo, seleccion)
    val borde = if (pendiente) AppColors.colorLetraRoja else Color(0xFFE6E6E9)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(BorderStroke(if (pendiente) 1.5.dp else 1.dp, borde), RoundedCornerShape(14.dp))
            .background(Color.White)
    ) {
        // encabezado del bloque: nombre, regla y si es obligatorio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.colorRedondeadoGris)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(grupo.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = AppColors.colorLetraPrincipal)
                Text(
                    text = ReglasModificadores.textoRegla(grupo).replaceFirstChar { it.uppercase() } +
                        if (!esRadio) " · $total de ${grupo.maximo}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pendiente) AppColors.colorLetraRoja else AppColors.colorGrisIcono,
                )
            }
            EtiquetaBloque(
                texto = if (grupo.requerido == 1) "Obligatorio" else "Opcional",
                fondo = if (grupo.requerido == 1) (if (pendiente) AppColors.colorLetraRoja else AppColors.colorLetraPrincipal) else Color(0xFFDADADF),
                letra = if (grupo.requerido == 1) Color.White else AppColors.colorGrisv2,
            )
        }

        grupo.opciones.forEachIndexed { i, opcion ->
            if (i > 0) Spacer(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(Color(0xFFF0F0F2)))
            FilaOpcion(grupo, opcion, seleccion, esRadio, onCambio)
        }
    }
}

@Composable
private fun FilaOpcion(
    grupo: ModeloGrupoModificador,
    opcion: ModeloOpcionModificador,
    seleccion: Map<Int, Int>,
    esRadio: Boolean,
    onCambio: (Map<Int, Int>) -> Unit,
) {
    val cantidad = seleccion[opcion.id] ?: 0
    val elegida = cantidad > 0
    val bloqueada = opcion.obligatorio == 1
    val puedeAgregar = ReglasModificadores.puedeAgregar(grupo, opcion, seleccion)
    val usaContador = !esRadio && opcion.maximo > 1
    val precio = opcion.precio.toDoubleOrNull() ?: 0.0
    val habilitada = elegida || puedeAgregar

    val alTocar: () -> Unit = {
        when {
            bloqueada -> Unit
            usaContador -> if (!elegida) onCambio(ReglasModificadores.agregar(grupo, opcion, seleccion))
            elegida -> onCambio(ReglasModificadores.quitar(grupo, opcion, seleccion))
            else -> onCambio(ReglasModificadores.agregar(grupo, opcion, seleccion))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !bloqueada && (habilitada || esRadio), onClick = alTocar)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            bloqueada -> Icon(
                Icons.Default.Lock, contentDescription = "Incluido",
                tint = AppColors.colorGrisIcono, modifier = Modifier.padding(12.dp).size(20.dp),
            )
            esRadio -> RadioButton(
                selected = elegida, onClick = alTocar,
                colors = RadioButtonDefaults.colors(selectedColor = AppColors.colorLetraRoja),
            )
            !usaContador -> Checkbox(
                checked = elegida, onCheckedChange = { alTocar() }, enabled = habilitada,
                colors = CheckboxDefaults.colors(checkedColor = AppColors.colorLetraRoja),
            )
            else -> Spacer(Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp)) {
            Text(
                text = opcion.nombre,
                style = MaterialTheme.typography.bodyLarge,
                color = if (habilitada || bloqueada) AppColors.colorNegro else AppColors.colorLetraGris,
            )
            val notas = buildList {
                if (bloqueada) add("Incluido")
                if (opcion.unaSola == 1) add("No se combina con otras")
                if (usaContador) add("Hasta ${opcion.maximo}")
            }
            if (notas.isNotEmpty()) {
                Text(notas.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = AppColors.colorGrisIcono)
            }
        }

        if (precio > 0) {
            Text(
                text = "+$" + formatoDosDecimales(precio),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.colorGrisv1,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        if (usaContador) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                if (elegida) {
                    BotonContadorMini(Icons.Default.Remove, "Quitar", enabled = !(bloqueada && cantidad <= 1)) {
                        onCambio(ReglasModificadores.quitar(grupo, opcion, seleccion))
                    }
                    Text(cantidad.toString(), modifier = Modifier.padding(horizontal = 10.dp), style = MaterialTheme.typography.titleSmall)
                }
                BotonContadorMini(Icons.Default.Add, "Agregar", enabled = puedeAgregar) {
                    onCambio(ReglasModificadores.agregar(grupo, opcion, seleccion))
                }
            }
        }
    }
}

@Composable
private fun BotonContadorMini(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(32.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = AppColors.colorLetraRoja,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFE6E6E9),
            disabledContentColor = AppColors.colorLetraGris,
        ),
    ) { Icon(icono, contentDescription = descripcion, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun EtiquetaBloque(texto: String, fondo: Color, letra: Color) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = letra,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Líneas de opciones elegidas (carrito, órdenes, historial). */
@Composable
fun LineasOpciones(opciones: List<String>, modifier: Modifier = Modifier) {
    if (opciones.isEmpty()) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        opciones.forEach { linea ->
            Text(
                text = linea,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.colorGrisv1,
            )
        }
    }
}
