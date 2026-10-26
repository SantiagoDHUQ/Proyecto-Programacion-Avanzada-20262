package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.request.SolicitarRMARequest;
import org.uniquindio.proyectoavanzadacompuparts.application.exception.RecursoNoEncontradoException;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Compra;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.SolicitudRMA;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.UnidadInventario;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.ItemCompra;

import java.time.LocalDate;
import java.util.Objects;

import static org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException.exigir;

/**
 * Caso de uso, del lado del comprador, que solicita una RMA para una unidad
 * concreta de una compra completada y dentro de garantía.
 *
 * <p>La solicitud se identifica por el número de serie de la unidad. Al crearse,
 * la unidad pasa a {@code EN_RMA} para que no vuelva a venderse mientras el
 * vendedor dueño resuelve la solicitud.</p>
 */
@Service
@RequiredArgsConstructor
public class SolicitarRMAUseCase {

    private final CompraRepository compraRepository;
    private final ComponenteRepository componenteRepository;
    private final SolicitudRMARepository solicitudRMARepository;

    @Transactional
    public SolicitudRMA ejecutar(SolicitarRMARequest request) {
        exigir(request != null, "La solicitud RMA es obligatoria");
        exigir(request.id() != null && !request.id().isBlank(),
                "El identificador de la SolicitudRMA es obligatorio");
        exigir(request.compraId() != null && !request.compraId().isBlank(),
                "La SolicitudRMA debe indicar la compra");
        exigir(request.numeroSerie() != null && !request.numeroSerie().isBlank(),
                "La SolicitudRMA debe indicar el número de serie de la unidad");
        exigir(request.compradorId() != null && !request.compradorId().isBlank(),
                "La SolicitudRMA debe indicar el comprador");

        if (solicitudRMARepository.buscarPorId(request.id()).isPresent()) {
            throw new ReglaDominioException("Ya existe una SolicitudRMA con ese identificador");
        }

        Compra compra = compraRepository.buscarPorId(request.compraId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una Compra con ese identificador"));

        if (compra.getEstado() != EstadoCompra.COMPLETADA) {
            throw new ReglaDominioException("Solo se puede solicitar RMA de una compra completada");
        }
        if (!compra.getCompradorId().equals(request.compradorId())) {
            throw new ReglaDominioException("Solo el comprador de la compra puede solicitar el RMA");
        }

        String numeroSerie = request.numeroSerie().trim();
        ItemCompra item = compra.getItems().stream()
                .filter(i -> i.numeroSerie().equalsIgnoreCase(numeroSerie))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException(
                        "El número de serie no pertenece a la compra"));

        Componente componente = componenteRepository.buscarPorId(item.componenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un Componente con ese identificador"));

        if (solicitudRMARepository.existePendientePorNumeroSerie(numeroSerie)) {
            throw new ReglaDominioException("Ya existe una SolicitudRMA pendiente para esa unidad");
        }

        // El dominio valida el estado de la unidad
        UnidadInventario unidad = componente.unidadPorId(item.unidadId());
        if (!unidad.estaVendida()) {
            throw new ReglaDominioException("La unidad no está vendida y no puede entrar en RMA");
        }

        LocalDate fechaCompra = compra.getFechaCompra().toLocalDate();
        LocalDate fechaSolicitud = request.fechaSolicitud() == null
                ? LocalDate.now()
                : request.fechaSolicitud();

        if (!componente.admiteRMA(fechaCompra, fechaSolicitud)) {
            throw new ReglaDominioException("El componente no tiene garantía vigente para RMA");
        }

        int duracionGarantia = componente.getGarantia()
                .orElseThrow(() -> new ReglaDominioException("El componente no tiene garantía"))
                .dias();

        SolicitudRMA solicitud = SolicitudRMA.crear(
                request.id(),
                componente.getId(),
                compra.getId(),
                item.numeroSerie(),
                compra.getCompradorId(),
                fechaCompra,
                duracionGarantia,
                fechaSolicitud);

        componente.entrarEnRMA(unidad.getId());
        componenteRepository.guardar(componente);
        return solicitudRMARepository.guardar(solicitud);
    }
}
