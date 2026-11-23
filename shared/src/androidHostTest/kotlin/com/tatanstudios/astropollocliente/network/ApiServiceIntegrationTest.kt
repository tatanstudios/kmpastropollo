package com.tatanstudios.astropollocliente.network

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pruebas contra la API real (solo endpoints de LECTURA, no modifican datos).
 * Requiere el servidor local: php artisan serve --host=192.168.1.4 --port=8080
 *
 * Ejecutar:  ./gradlew :shared:testAndroidHostTest
 * Si el servidor no responde, las pruebas se omiten.
 */
class ApiServiceIntegrationTest {

    private val api = ApiService()

    // IDs de ejemplo de la base local (cámbialos si tu base es distinta)
    private val idCliente = System.getenv("ASTRO_CLIENTE") ?: "3"
    private val idCategoria = 11
    private val idProducto = 21
    private val idOrden = 1805

    private fun servidorDisponible(): Boolean = runCatching {
        java.net.URL(ApiConfig.BASE_URL.removeSuffix("api/") + "up").openConnection().apply {
            connectTimeout = 2000; readTimeout = 2000
        }.getInputStream().close()
    }.isSuccess

    private fun conServidor(bloque: suspend () -> Unit) {
        if (!servidorDisponible()) {
            println("Servidor ${ApiConfig.BASE_URL} no disponible: prueba omitida")
            return
        }
        runBlocking { bloque() }
    }

    @Test fun poligonos() = conServidor {
        val r = api.listadoPoligonos()
        assertEquals(1, r.success)
        assertTrue(r.lista.isNotEmpty(), "debe haber zonas")
        println("zonas: ${r.lista.size}, puntos zona 1: ${r.lista.first().listado.size}")
    }

    @Test fun menuPrincipal() = conServidor {
        val r = api.listadoMenuPrincipal(idCliente)
        println("menu success=${r.success} categorias=${r.arrayCategorias.size} populares=${r.arrayPopulares.size} slider=${r.arraySlider.size}")
        assertTrue(r.success > 0)
    }

    @Test fun productosDeCategoria() = conServidor {
        val r = api.listadoProductos(idCategoria)
        println("productos success=${r.success} subcategorias=${r.listaProductoCategoria.size}")
        assertTrue(r.success > 0)
    }

    @Test fun informacionProducto() = conServidor {
        val r = api.informacionProducto(idProducto)
        println("producto success=${r.success} -> ${r.informacionProducto.firstOrNull()?.nombre}")
        assertTrue(r.success > 0)
    }

    @Test fun horario() = conServidor {
        val r = api.listadoHorario(idCliente)
        println("horario success=${r.success} restaurante=${r.restaurante} dias=${r.lista.size}")
        assertTrue(r.success > 0)
    }

    @Test fun direcciones() = conServidor {
        val r = api.listadoDirecciones(idCliente)
        println("direcciones success=${r.success} total=${r.lista.size}")
        assertTrue(r.success > 0)
    }

    @Test fun carrito() = conServidor {
        val r = api.listadoCarritoCompras(idCliente)
        println("carrito success=${r.success} productos=${r.listadoCarritoTemporal.size} subtotal=${r.subTotal}")
        assertTrue(r.success > 0)
    }

    @Test fun ordenesActivas() = conServidor {
        val r = api.listadoOrdenes(idCliente)
        println("ordenes success=${r.success} total=${r.ordenes.size}")
        assertTrue(r.success > 0)
    }

    @Test fun informacionUsuario() = conServidor {
        val r = api.informacionUsuario(idCliente)
        println("usuario success=${r.success} usuario=${r.usuario}")
        assertTrue(r.success > 0)
    }

    @Test fun historial() = conServidor {
        val r = api.listadoHistorialOrdenes(idCliente, "2020-01-01", "2030-01-01")
        println("historial success=${r.success} hay=${r.hayordenes} ordenes=${r.lista.size}")
        assertTrue(r.success > 0)
    }

    @Test fun estadoYProductosDeOrden() = conServidor {
        val estado = api.informacionOrdenIndividual(idOrden)
        val productos = api.listadoProductosOrden(idOrden)
        println("orden $idOrden estado success=${estado.success} productos=${productos.productos.size}")
        assertTrue(estado.success > 0 && productos.success > 0)
    }
}
