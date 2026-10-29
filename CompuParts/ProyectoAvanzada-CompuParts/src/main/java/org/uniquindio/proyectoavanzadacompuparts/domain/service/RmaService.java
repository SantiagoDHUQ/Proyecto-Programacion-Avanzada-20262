package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.time.LocalDate;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Servicio de dominio para procesos de RMA que cruzan agregados.
 * <p>
 * Recibe los agregados ya cargados y los hechos técnicos; no recibe repositorios.
 * Implementa "verificar todo, luego aplicar todo" para garantizar atomicidad.
 */
public final class RmaService {

    /**
     * Solicita una RMA para una unidad de una compra.
     * <p>
     * Verifica todo antes de mutar: compra completada y del comprador, serie pertenece a la compra,
     * unidad VENDIDA, garantía vigente, no hay RMA pendiente. Solo entonces crea la solicitud
     * y pone la unidad EN_RMA.
     */
    public SolicitudRMA solicitar(Compra compra, Componente componente, String numeroSerie,
                                  String compradorId, LocalDate hoy, boolean hayPendiente,
                                  String idSolicitud, boolean idYaExiste) {
        // --- VERIFICAR TODO ---
        exigir(idSolicitud != null && !idSolicitud.isBlank(), "El identificador de la SolicitudRMA es obligatorio");
        exigir(!idYaExiste, "Ya existe una SolicitudRMA con ese identificador");
        exigir(compra != null, "La compra es obligatoria");
        exigir(componente != null, "El componente es obligatorio");
        exigir(numeroSerie != null && !numeroSerie.isBlank(), "La SolicitudRMA debe indicar el número de serie de la unidad");
        exigir(compradorId != null && !compradorId.isBlank(), "La SolicitudRMA debe indicar el comprador");
        compra.validarParaSolicitarRMA(compradorId);
        compra.validarNumeroSeriePertenece(numeroSerie);
        exigir(!hayPendiente, "Ya existe una SolicitudRMA pendiente para esa unidad");

        ItemCompra item = compra.getItems().stream()
                .filter(i -> i.numeroSerie() != null && numeroSerie != null && i.numeroSerie().equalsIgnoreCase(numeroSerie.trim()))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("El número de serie no pertenece a la compra"));

        UnidadInventario unidad = componente.unidadPorId(item.unidadId());
        unidad.validarEstaVendida();

        LocalDate fechaCompra = compra.getFechaCompra().toLocalDate();
        LocalDate fechaSolicitud = hoy == null ? LocalDate.now() : hoy;
        if (!componente.admiteRMA(fechaCompra, fechaSolicitud)) {
            throw new ReglaDominioException("El componente no tiene garantía vigente para RMA");
        }

        int duracionGarantia = componente.getGarantia()
                .orElseThrow(() -> new ReglaDominioException("El componente no tiene garantía"))
                .dias();

        // --- APLICAR TODO ---
        SolicitudRMA solicitud = SolicitudRMA.crear(
                idSolicitud,
                componente.getId(),
                compra.getId(),
                item.numeroSerie(),
                compra.getCompradorId(),
                fechaCompra,
                duracionGarantia,
                fechaSolicitud);

        componente.entrarEnRMA(item.unidadId());
        return solicitud;
    }

    public SolicitudRMA solicitar(Compra compra, Componente componente, String numeroSerie,
                                  String compradorId, LocalDate hoy, boolean hayPendiente,
                                  String idSolicitud) {
        return solicitar(compra, componente, numeroSerie, compradorId, hoy, hayPendiente, idSolicitud, false);
    }

    /**
     * Acepta una solicitud RMA.
     */
    public void aceptar(SolicitudRMA solicitud, Componente componente, String vendedorId) {
        componente.validarPropietario(vendedorId);
        solicitud.validarAceptar();

        UnidadInventario unidad = componente.unidadPorNumeroSerie(solicitud.getNumeroSerie());
        unidad.validarEstaEnRMA();

        solicitud.aceptar();
    }

    /**
     * Rechaza una solicitud RMA.
     */
    public void rechazar(SolicitudRMA solicitud, Componente componente, String vendedorId, String motivo) {
        componente.validarPropietario(vendedorId);
        solicitud.validarRechazar(motivo);

        UnidadInventario unidad = componente.unidadPorNumeroSerie(solicitud.getNumeroSerie());
        unidad.validarEstaEnRMA();

        solicitud.rechazar(motivo);
        unidad.rechazarRMA();
    }
}