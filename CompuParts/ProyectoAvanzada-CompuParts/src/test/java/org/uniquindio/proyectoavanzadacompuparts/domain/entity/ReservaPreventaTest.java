package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoReservaPreventa;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaPreventaTest {

    @Test
    void unaReservaNaceActiva() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");

        assertEquals(EstadoReservaPreventa.ACTIVA, reserva.getEstado());
        assertTrue(reserva.estaActiva());
        assertTrue(reserva.estaVigente());
        assertNotNull(reserva.getFechaReserva());
    }

    @Test
    void unaReservaActivaPuedeCancelarse() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");

        reserva.cancelar();

        assertEquals(EstadoReservaPreventa.CANCELADA, reserva.getEstado());
        assertFalse(reserva.estaActiva());
        assertFalse(reserva.estaVigente());
    }

    @Test
    void unaReservaCanceladaNoPuedeConvertirseNiCancelarseOtraVez() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");
        reserva.cancelar();

        assertThrows(ReglaDominioException.class, reserva::convertir);
        assertThrows(ReglaDominioException.class, reserva::cancelar);
    }

    @Test
    void unaReservaActivaPuedeIniciarUnaCompra() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");

        reserva.iniciarCompra();

        assertEquals(EstadoReservaPreventa.EN_COMPRA, reserva.getEstado());
        assertTrue(reserva.estaEnCompra());
        assertTrue(reserva.estaVigente());
    }

    @Test
    void unaReservaEnCompraPuedeConvertirse() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");
        reserva.iniciarCompra();

        reserva.convertir();

        assertEquals(EstadoReservaPreventa.CONVERTIDA, reserva.getEstado());
        assertFalse(reserva.estaVigente());
    }

    @Test
    void unaReservaEnCompraNoPuedeCancelarseMientrasLaCompraSigaPendiente() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");
        reserva.iniciarCompra();

        assertThrows(ReglaDominioException.class, reserva::cancelar);
        assertEquals(EstadoReservaPreventa.EN_COMPRA, reserva.getEstado());
    }

    @Test
    void unaReservaEnCompraPuedeVolverAActivaSiSeCancelaLaCompra() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");
        reserva.iniciarCompra();

        reserva.volverAActiva();

        assertEquals(EstadoReservaPreventa.ACTIVA, reserva.getEstado());
        assertTrue(reserva.estaVigente());
    }

    @Test
    void unaReservaActivaNoPuedeConvertirseSinIniciarLaCompra() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");

        assertThrows(ReglaDominioException.class, reserva::convertir);
    }

    @Test
    void unaReservaConvertidaNoPuedeCancelarse() {
        ReservaPreventa reserva = ReservaPreventa.crear("componente-1", UUID.randomUUID(), "comprador-1");
        reserva.iniciarCompra();
        reserva.convertir();

        assertThrows(ReglaDominioException.class, reserva::cancelar);
    }

    @Test
    void unaReservaExigeComponenteUnidadYComprador() {
        assertThrows(ReglaDominioException.class,
                () -> ReservaPreventa.crear(" ", UUID.randomUUID(), "comprador-1"));
        assertThrows(ReglaDominioException.class,
                () -> ReservaPreventa.crear("componente-1", null, "comprador-1"));
        assertThrows(ReglaDominioException.class,
                () -> ReservaPreventa.crear("componente-1", UUID.randomUUID(), " "));
    }
}
