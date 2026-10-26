package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;

import java.time.LocalDate;
import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

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
    private final String numeroSerie;
    private final String compradorId;
    private final LocalDate fechaCompra;
    private final int duracionGarantia;      // en días
    private final LocalDate fechaSolicitud;
    private EstadoSolicitudRMA estado;
    private String motivoRechazo;

    private SolicitudRMA(String id, String componenteId, String compraId, String numeroSerie,
                         String compradorId, LocalDate fechaCompra, int duracionGarantia,
                         LocalDate fechaSolicitud, EstadoSolicitudRMA estado, String motivoRechazo) {
        this.id = id;
        this.componenteId = componenteId;
        this.compraId = compraId;
        this.numeroSerie = numeroSerie == null ? null : numeroSerie.trim();
        this.compradorId = compradorId;
        this.fechaCompra = fechaCompra;
        this.duracionGarantia = duracionGarantia;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado == null ? EstadoSolicitudRMA.PENDIENTE : estado;
        this.motivoRechazo = motivoRechazo;
    }

    // Único punto de entrada: valida ANTES de existir
    public static SolicitudRMA crear(String id, String componenteId, String compraId, String numeroSerie,
                                     String compradorId, LocalDate fechaCompra, int duracionGarantia,
                                     LocalDate fechaSolicitud) {
        exigirTexto(id, "El identificador de la SolicitudRMA es obligatorio");
        exigirTexto(componenteId, "La SolicitudRMA debe indicar el componente");
        exigirTexto(compraId, "La SolicitudRMA debe indicar la compra");
        exigirTexto(numeroSerie, "La SolicitudRMA debe indicar el número de serie de la unidad");
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
        return new SolicitudRMA(id, componenteId, compraId, numeroSerie, compradorId,
                fechaCompra, duracionGarantia, fechaSolicitud, EstadoSolicitudRMA.PENDIENTE, null);
    }

    public static SolicitudRMA reconstruir(String id, String componenteId, String compraId,
                                           String numeroSerie, String compradorId,
                                           LocalDate fechaCompra, int duracionGarantia,
                                           LocalDate fechaSolicitud, EstadoSolicitudRMA estado,
                                           String motivoRechazo) {
        exigirTexto(id, "El identificador de la SolicitudRMA es obligatorio");
        exigirTexto(componenteId, "La SolicitudRMA debe indicar el componente");
        exigirTexto(compraId, "La SolicitudRMA debe indicar la compra");
        exigirTexto(numeroSerie, "La SolicitudRMA debe indicar el número de serie de la unidad");
        exigirTexto(compradorId, "La SolicitudRMA debe indicar el comprador");
        if (fechaCompra == null) {
            throw new ReglaDominioException("La fecha de compra es obligatoria");
        }
        if (fechaSolicitud == null) {
            throw new ReglaDominioException("La fecha de la solicitud es obligatoria");
        }
        if (estado == null) {
            throw new ReglaDominioException("El estado de la SolicitudRMA es obligatorio");
        }
        return new SolicitudRMA(id, componenteId, compraId, numeroSerie, compradorId,
                fechaCompra, duracionGarantia, fechaSolicitud, estado, motivoRechazo);
    }

    // Cada método valida PRIMERO, cambia el estado DESPUÉS.

    /**
     * Acepta la solicitud RMA.
     * Valida que esté en estado PENDIENTE.
     */
    public void aceptar() {
        validarAceptar();
        verificarTransicion(EstadoSolicitudRMA.ACEPTADA);
        this.estado = EstadoSolicitudRMA.ACEPTADA;
    }

    /**
     * Valida que la solicitud pueda ser aceptada.
     */
    public void validarAceptar() {
        exigir(estaPendiente(), "Solo se puede aceptar una SolicitudRMA pendiente");
    }

    /**
     * Rechaza la solicitud RMA.
     * Valida que esté en estado PENDIENTE y que se proporcione un motivo.
     */
    public void rechazar(String motivo) {
        validarRechazar(motivo);
        verificarTransicion(EstadoSolicitudRMA.RECHAZADA);
        this.estado = EstadoSolicitudRMA.RECHAZADA;
        this.motivoRechazo = motivo;
    }

    /**
     * Valida que la solicitud pueda ser rechazada.
     */
    public void validarRechazar(String motivo) {
        exigir(estaPendiente(), "Solo se puede rechazar una SolicitudRMA pendiente");
        exigir(motivo != null && !motivo.isBlank(), "El rechazo de una SolicitudRMA exige indicar un motivo");
    }

    public boolean estaPendiente() {
        return estado == EstadoSolicitudRMA.PENDIENTE;
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