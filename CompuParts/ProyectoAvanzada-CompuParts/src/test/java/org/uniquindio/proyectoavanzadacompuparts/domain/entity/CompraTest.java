package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CompraTest {

    private final Precio precio = new Precio(new BigDecimal("2500000"), "COP");

    private ItemCompra item(String componenteId, String serial) {
        return item(componenteId, serial, precio);
    }

    private ItemCompra item(String componenteId, String serial, Precio precioItem) {
        return new ItemCompra(UUID.randomUUID(), componenteId, serial, precioItem);
    }

    private Compra compra() {
        return Compra.realizar("compra-1", UUID.randomUUID(), "comprador-1",
                List.of(item("componente-1", "SER-1")));
    }

    @Test
    void todaCompraNacePendiente() {
        Compra compra = compra();

        assertEquals(EstadoCompra.PENDIENTE, compra.getEstado());
        assertEquals(precio, compra.getPrecioCongelado());
        assertEquals(1, compra.getItems().size());
    }

    @Test
    void elPrecioCongeladoEsLaSumaDeLosItems() {
        Precio otroPrecio = new Precio(new BigDecimal("1000000"), "COP");
        Compra compra = Compra.realizar("compra-1", UUID.randomUUID(), "comprador-1",
                List.of(item("componente-1", "SER-1", precio),
                        item("componente-2", "SER-2", otroPrecio)));

        assertEquals(new Precio(new BigDecimal("3500000"), "COP"), compra.getPrecioCongelado());
    }

    @Test
    void unaCompraPuedeSerDirectaSinBuild() {
        Compra compra = Compra.realizar("compra-1", null, "comprador-1",
                List.of(item("componente-1", "SER-1")));

        assertNull(compra.getBuildId());
    }

    @Test
    void unaCompraPendientePuedeConfirmarse() {
        Compra compra = compra();

        compra.confirmar();

        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void unaCompraPendientePuedeCancelarse() {
        Compra compra = compra();

        compra.cancelar();

        assertEquals(EstadoCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void unaCompraCanceladaEsFinalYNoPuedeConfirmarse() {
        Compra compra = compra();
        compra.cancelar();

        assertThrows(ReglaDominioException.class, compra::confirmar);
        assertEquals(EstadoCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void unaCompraCanceladaNoPuedeReembolsarse() {
        Compra compra = compra();
        compra.cancelar();

        assertThrows(ReglaDominioException.class, () -> compra.solicitarReembolso("motivo"));
        assertEquals(EstadoCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void unaCompraCompletadaNoPuedeCancelarse() {
        Compra compra = compra();
        compra.confirmar();

        assertThrows(ReglaDominioException.class, compra::cancelar);
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void unaCompraCompletadaPuedeReembolsarseConMotivo() {
        Compra compra = compra();
        compra.confirmar();

        compra.solicitarReembolso("Falla de fábrica");

        assertEquals(EstadoCompra.REEMBOLSADA, compra.getEstado());
    }

    @Test
    void elReembolsoExigeMotivo() {
        Compra compra = compra();
        compra.confirmar();

        assertThrows(ReglaDominioException.class, () -> compra.solicitarReembolso(" "));
        assertEquals(EstadoCompra.COMPLETADA, compra.getEstado());
    }

    @Test
    void unaCompraReembolsadaEsFinal() {
        Compra compra = compra();
        compra.confirmar();
        compra.solicitarReembolso("Falla");

        assertThrows(ReglaDominioException.class, compra::confirmar);
        assertThrows(ReglaDominioException.class, compra::cancelar);
        assertEquals(EstadoCompra.REEMBOLSADA, compra.getEstado());
    }

    @Test
    void unaCompraExigeAlMenosUnItem() {
        assertThrows(ReglaDominioException.class,
                () -> Compra.realizar("compra-1", UUID.randomUUID(), "comprador-1", List.of()));
    }

    @Test
    void unaCompraNoPuedeRepetirLaMismaUnidad() {
        UUID unidadId = UUID.randomUUID();
        ItemCompra item = new ItemCompra(unidadId, "componente-1", "SER-1", precio);

        assertThrows(ReglaDominioException.class,
                () -> Compra.realizar("compra-1", UUID.randomUUID(), "comprador-1", List.of(item, item)));
    }

    @Test
    void unaCompraExigeComprador() {
        assertThrows(ReglaDominioException.class,
                () -> Compra.realizar("compra-1", UUID.randomUUID(), null,
                        List.of(item("componente-1", "SER-1"))));
    }

    @Test
    void losItemsConDistintaMonedaNoSePuedenSumar() {
        ItemCompra enCop = item("componente-1", "SER-1", new Precio(BigDecimal.TEN, "COP"));
        ItemCompra enUsd = item("componente-2", "SER-2", new Precio(BigDecimal.ONE, "USD"));

        assertThrows(ReglaDominioException.class,
                () -> Compra.realizar("compra-1", null, "comprador-1", List.of(enCop, enUsd)));
    }
}
