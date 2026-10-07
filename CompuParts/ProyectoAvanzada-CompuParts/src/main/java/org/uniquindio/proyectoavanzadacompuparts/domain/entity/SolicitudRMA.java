package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Solicitud de garantía asociada a un Componente con falla de fábrica y en vigencia.
 * 
 * Invariantes que garantiza:
 *  - Toda solicitud nace en estado PENDIENTE.
 *  - La fecha de solicitud no puede ser anterior a la fecha de compra.
 *  - La solicitud debe crearse dentro del periodo de garantía (fechaSolicitud < fechaCompra + duracionGarantia).
 *  - El rechazo de una solicitud exige obligatoriamente indicar un motivo.
 */
@Getter
public class SolicitudRMA {

    private final String id;
    private final String componenteId;
    private final String compraId;
    private final String compradorId;
    private final LocalDate fechaCompra;
    private final int duracionGarantia;      // en días
    private final LocalDate fechaSolicitud;
    private EstadoSolicitudRMA estado;
    private String motivoRechazo;

    private SolicitudRMA(String id, String componenteId, String compraId, String compradorId,
                         LocalDate fechaCompra, int duracionGarantia, LocalDate fechaSolicitud) {
        this.id = id;
        this.componenteId = componenteId;
        this.compraId = compraId;
        this.compradorId = compradorId;
        this.fechaCompra = fechaCompra;
        this.duracionGarantia = duracionGarantia;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = EstadoSolicitudRMA.PENDIENTE; // toda solicitud nace Pendiente
    }

    // Único punto de entrada: valida ANTES de existir
    public static SolicitudRMA crear(String id, String componenteId, String compraId,
                                     LocalDate fechaCompra, int duracionGarantia,
                                     String compradorId, LocalDate fechaSolicitud) {
        exigirTexto(id, "El identificador de la SolicitudRMA es obligatorio");
        exigirTexto(componenteId, "La SolicitudRMA debe indicar el componente");
        exigirTexto(compraId, "La SolicitudRMA debe indicar la compra");
        exigirTexto(compradorId, "La SolicitudRMA debe indicar el comprador");
        if (fechaCompra == null) {
            throw new ReglaDominioException("La fecha de compra es obligatoria");
        }
        if (fechaSolicitud == null) {
            throw new ReglaDominioException("La fecha de la solicitud es obligatoria");
        }
        if (duracionGarantia <= 0) {
            throw new ReglaDominioException("La duración de la garantía debe ser positiva");
        }
        if (fechaSolicitud.isBefore(fechaCompra)) {
            throw new ReglaDominioException("La solicitud no puede ser anterior a la fecha de compra");
        }
        // Regla 3: la fecha actual debe ser anterior a fechaCompra + duracionGarantia
        if (!fechaSolicitud.isBefore(fechaCompra.plusDays(duracionGarantia))) {
            throw new ReglaDominioException("La SolicitudRMA no puede crearse después del vencimiento de la garantía");
        }
        return new SolicitudRMA(id, componenteId, compraId, compradorId, fechaCompra, duracionGarantia, fechaSolicitud);
    }

    // Cada método valida PRIMERO, cambia el estado DESPUÉS.

    public void aceptar() {
        verificarTransicion(EstadoSolicitudRMA.ACEPTADA);
        this.estado = EstadoSolicitudRMA.ACEPTADA;
    }

    public void rechazar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El rechazo de una SolicitudRMA exige indicar un motivo");
        }
        verificarTransicion(EstadoSolicitudRMA.RECHAZADA);
        this.estado = EstadoSolicitudRMA.RECHAZADA;
        this.motivoRechazo = motivo;
    }

    private void verificarTransicion(EstadoSolicitudRMA siguiente) {
        if (!estado.puedeTransicionarA(siguiente)) {
            throw new ReglaDominioException("No se puede pasar de " + estado + " a " + siguiente);
        }
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException(mensaje);
        }
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof SolicitudRMA solicitudRMA)) return false;
        return id. equals(solicitudRMA.id);
    }

    @Override
    public int hashCode(){return Objects.hash(id);}
}