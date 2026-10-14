package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SolicitudRMATest {

    //Arrange general
    private static final LocalDate COMPRA = LocalDate.of(2026, 1, 1);
    private static final int GARANTIA = 30;

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

    @Test
    void solicitudElDiaDelVencimientoDEbeLanzarReglaDominioException() {
        //Arrange
        LocalDate diaDelVencimiento = COMPRA.plusDays(GARANTIA);

        //Act && Assert
        assertThrows(ReglaDominioException.class, () -> solicitud("rma-1", diaDelVencimiento));
    }

    @Test
    void solicitudConGarantiaVencida() {
        //Arrange
        LocalDate vencida = COMPRA.plusDays(GARANTIA + 90);

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> solicitud("rma-1", vencida));
    }

    @Test
    void solicitudAnteriorALaCompra() {
        //Arrange
        LocalDate previa = COMPRA.minusDays(GARANTIA-1);

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> solicitud("RMA-1", previa));
    }

    @Test
    void solicitudYaAceptadaNoPuedeAceptarseNiRechazarse() {
        //Arrange
        SolicitudRMA rma = solicitud("rma-1", COMPRA.plusDays(10));
        rma.aceptar();
        EstadoSolicitudRMA anterior = rma.getEstado();

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> rma.rechazar("Si"));
        assertEquals(EstadoSolicitudRMA.ACEPTADA, rma.getEstado());
    }

    @Test
    void rechazarSinMotivoYConservarElEstadoPendiente() {
        //Arrange
        SolicitudRMA rma = solicitud("rma-1", COMPRA.plusDays(5));
        EstadoSolicitudRMA pendiente = rma.getEstado();

        //Act & Assert
        assertThrows(ReglaDominioException.class, () -> rma.rechazar(""));
        assertEquals(pendiente, rma.getEstado());
    }

    @Test
    void dosSolicitudesMismoIdSonIguales() {
        //Arrange
        SolicitudRMA rma1 = solicitud("rma-1", COMPRA.plusDays(10));
        SolicitudRMA rma2 = solicitud("rma-1", COMPRA.plusDays(15));

        //Act & Assert
        assertEquals(rma1, rma2);
    }
}
