package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolicitudRMATest {

    //Arrange general
    private static final LocalDate COMPRA = LocalDate.of(2026, 1, 1);
    private static final int GARANTIA = 30;// vence el 2026-01-31

    private SolicitudRMA solicitud(String id, LocalDate fechaSolicitud) {
        return SolicitudRMA.crear(id, "comp-1", "compra-1", COMPRA, GARANTIA, "comprador-1", fechaSolicitud);
    }

    @Test
    void solicitudDentroDeGarantiaNaceEnPendiente() {

        //Arrange
        SolicitudRMA rma = solicitud("rma-1", COMPRA.plusDays(10));
        //Act & Assert
        assertEquals(EstadoSolicitudRMA.PENDIENTE, rma.getEstado());
        assertEquals("comp-1", rma.getComponenteId());
        assertEquals("compra-1", rma.getCompraId());
    }

    @Test
    void solicitudElUltimoDiaVigenteEsValida() {
        //Arrange, Act & Assert
        assertDoesNotThrow(() -> solicitud("rma-1", COMPRA.plusDays(GARANTIA - 1)));
    }
}
