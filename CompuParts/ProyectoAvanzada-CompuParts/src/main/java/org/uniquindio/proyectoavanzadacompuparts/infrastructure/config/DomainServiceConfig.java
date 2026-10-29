package org.uniquindio.proyectoavanzadacompuparts.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CatalogoService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.PreventaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.RmaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.VerificadorDeCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.VerificadorDeReembolso;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.VerificadorDeReservaPreventa;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuración de servicios de dominio (POJOs del dominio) como beans de Spring.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public VerificadorDeReservaPreventa verificadorDeReservaPreventa(ReservaPreventaRepository reservaPreventaRepository) {
        return new VerificadorDeReservaPreventa(reservaPreventaRepository);
    }

    @Bean
    public VerificadorDeCompra verificadorDeCompra(BuildRepository buildRepository, ComponenteRepository componenteRepository) {
        return new VerificadorDeCompra(buildRepository, componenteRepository);
    }

    @Bean
    public VerificadorDeReembolso verificadorDeReembolso(CompraRepository compraRepository, SolicitudRMARepository solicitudRMARepository) {
        return new VerificadorDeReembolso(compraRepository, solicitudRMARepository);
    }

    @Bean
    public RmaService rmaService() {
        return new RmaService();
    }

    @Bean
    public CompraService compraService(Clock clock) {
        return new CompraService(clock);
    }

    @Bean
    public PreventaService preventaService(ReservaPreventaRepository reservaPreventaRepository, Clock clock) {
        return new PreventaService(reservaPreventaRepository, clock);
    }

    @Bean
    public CatalogoService catalogoService() {
        return new CatalogoService();
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
