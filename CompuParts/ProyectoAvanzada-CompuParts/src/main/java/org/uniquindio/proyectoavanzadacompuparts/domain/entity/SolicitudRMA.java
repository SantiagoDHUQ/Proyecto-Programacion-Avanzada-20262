package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import lombok.Getter;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EstadoSolicitudRMA;

import java.time.LocalDate;
import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Raíz del agregado SolicitudRMA.
 *
 * Invariantes que garantiza:
 *  - solo se crea si la fecha de solicitud es ANTERIOR a fechaCompra + duracionGarantia (regla 3);
 *  - la solicitud no puede ser anterior a la fecha de compra;
 *  - toda solicitud nace PENDIENTE;
 *  - solo se transita PENDIENTE -> ACEPTADA | RECHAZADA; los estados finales no admiten cambios.
 *
 * Reglas que NO puede validar por sí sola (cruzan agregados) y que debe verificar el caso de uso
 * antes de llamar a crear(): vendedor autorizado y componente nuevo (reglas 5 y 8), compra
 * completada, el componente pertenece a la compra, y no existe otra solicitud PENDIENTE para
 * el mismo componente.
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
    public static SolicitudRMA crear(String id, String componenteId, String compraId, LocalDate fechaCompra, int duracionGarantia, String compradorId, LocalDate fechaSolicitud) {
        exigir(fechaCompra != null, "La fecha de compra es obligatoria");
        exigir(fechaSolicitud != null, "La fecha de la solicitud es obligatoria");
        exigir(duracionGarantia <= 0, "La duración de la garantía debe ser positiva");
        exigir(fechaSolicitud.isBefore(fechaCompra), "La solicitud no puede ser anterior a la fecha de compra");

        // Regla 3: la fecha actual debe ser anterior a fechaCompra + duracionGarantia
        exigir(fechaSolicitud.isBefore(fechaCompra), "La SolicitudRMA no puede crearse después del vencimiento de la garantía");

        return new SolicitudRMA(id, componenteId, compraId, compradorId, fechaCompra, duracionGarantia, fechaSolicitud);
    }

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

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof SolicitudRMA solicitudRMA)) return false;
        return id.equals(solicitudRMA.id);
    }

    @Override
    public int hashCode(){return Objects.hash(id);}
}