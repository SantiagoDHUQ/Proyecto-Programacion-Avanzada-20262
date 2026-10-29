package org.uniquindio.proyectoavanzadacompuparts.domain.service;

import org.uniquindio.proyectoavanzadacompuparts.domain.entity.ReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.domain.exception.ReglaDominioException;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;

import java.util.UUID;

/**
 * Servicio de dominio para validar reglas de negocio que cruzan agregados
 * relacionados con reservas de preventa.
 * <p>
 * No depende de Spring: es una clase POJO del dominio. Se configura como bean
 * en DomainServiceConfig.
 */
public class VerificadorDeReservaPreventa {

    private final ReservaPreventaRepository reservaPreventaRepository;

    public VerificadorDeReservaPreventa(ReservaPreventaRepository reservaPreventaRepository) {
        this.reservaPreventaRepository = reservaPreventaRepository;
    }

    /**
     * Valida que el comprador indicado sea el dueño de la reserva de preventa.
     * Lanza ReglaDominioException si no lo es.
     */
    public void validarDueno(UUID reservaId, String compradorId) {
        boolean esDueno = reservaPreventaRepository.esDueno(reservaId, compradorId);
        if (!esDueno) {
            throw new ReglaDominioException("Solo el comprador que reservó puede operar esta reserva de preventa");
        }
    }
}
