package org.uniquindio.proyectoavanzadacompuparts.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AceptarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AgregarComponenteABuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.AgregarUnidadUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CancelarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ComprarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConfirmarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarReservaPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarUnidadUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ConsultarVendedorUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.CrearBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.PublicarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.PublicarPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.EliminarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarComponentesUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ListarVendedoresUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.MarcarBuildListoParaCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.MarcarComponenteAgotadoUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RealizarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RechazarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RecibirPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RegistrarComponenteUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.RegistrarVendedorUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ReembolsarCompraUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ReservarPreventaUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.SolicitarRMAUseCase;
import org.uniquindio.proyectoavanzadacompuparts.application.usecase.ValidarCompatibilidadBuildUseCase;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.BuildRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.CompraRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ReservaPreventaRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.SolicitudRMARepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.VendedorRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.BuildFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.ComponenteFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.factory.VendedorFactory;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CatalogoService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.CompraService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.RmaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.VerificadorDeCompra;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.VerificadorDeReembolso;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.PreventaService;
import org.uniquindio.proyectoavanzadacompuparts.domain.service.VerificadorDeReservaPreventa;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.BuildRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.CompraRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ComponenteRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ReservaPreventaRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.SolicitudRMARepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.VendedorRepositoryEnMemoria;

import java.time.Clock;

/**
 * Configuración de beans de infraestructura.
 * <p>
 * Mantiene las capas de dominio y aplicación libres de anotaciones de Spring: los casos de uso
 * se siguen construyendo con sus puertos y es aquí donde se conectan las implementaciones.
 */
@Configuration
public class BeanConfiguration {

    // ---------- Adaptadores de persistencia ----------

    @Bean
    public BuildRepository buildRepository() {
        return new BuildRepositoryEnMemoria();
    }

    @Bean
    public ComponenteRepository componenteRepository() {
        return new ComponenteRepositoryEnMemoria();
    }

    @Bean
    public CompraRepository compraRepository() {
        return new CompraRepositoryEnMemoria();
    }

    @Bean
    public ReservaPreventaRepository reservaPreventaRepository() {
        return new ReservaPreventaRepositoryEnMemoria();
    }

    @Bean
    public SolicitudRMARepository solicitudRMARepository() {
        return new SolicitudRMARepositoryEnMemoria();
    }

    @Bean
    public VendedorRepository vendedorRepository() {
        return new VendedorRepositoryEnMemoria();
    }

    // ---------- Fábricas de dominio ----------

    @Bean
    public VendedorFactory vendedorFactory() {
        return new VendedorFactory();
    }

    @Bean
    public ComponenteFactory componenteFactory() {
        return new ComponenteFactory();
    }

    @Bean
    public BuildFactory buildFactory() {
        return new BuildFactory();
    }

    // ---------- Casos de uso ----------

    @Bean
    public CrearBuildUseCase crearBuildUseCase(BuildRepository buildRepository, BuildFactory buildFactory) {
        return new CrearBuildUseCase(buildRepository, buildFactory);
    }

    @Bean
    public ConsultarBuildUseCase consultarBuildUseCase(BuildRepository buildRepository) {
        return new ConsultarBuildUseCase(buildRepository);
    }

    @Bean
    public AgregarComponenteABuildUseCase agregarComponenteABuildUseCase(
            BuildRepository buildRepository,
            ComponenteRepository componenteRepository) {
        return new AgregarComponenteABuildUseCase(buildRepository, componenteRepository);
    }

    @Bean
    public ValidarCompatibilidadBuildUseCase validarCompatibilidadBuildUseCase(BuildRepository buildRepository) {
        return new ValidarCompatibilidadBuildUseCase(buildRepository);
    }

    @Bean
    public MarcarBuildListoParaCompraUseCase marcarBuildListoParaCompraUseCase(BuildRepository buildRepository) {
        return new MarcarBuildListoParaCompraUseCase(buildRepository);
    }

    @Bean
    public PublicarPreventaUseCase publicarPreventaUseCase(ComponenteRepository componenteRepository, Clock clock) {
        return new PublicarPreventaUseCase(componenteRepository, clock);
    }

    @Bean
    public AgregarUnidadUseCase agregarUnidadUseCase(ComponenteRepository componenteRepository) {
        return new AgregarUnidadUseCase(componenteRepository);
    }

    @Bean
    public ConsultarComponenteUseCase consultarComponenteUseCase(ComponenteRepository componenteRepository) {
        return new ConsultarComponenteUseCase(componenteRepository);
    }

    @Bean
    public ConsultarUnidadUseCase consultarUnidadUseCase(ComponenteRepository componenteRepository) {
        return new ConsultarUnidadUseCase(componenteRepository);
    }

    @Bean
    public ListarComponentesUseCase listarComponentesUseCase(ComponenteRepository componenteRepository) {
        return new ListarComponentesUseCase(componenteRepository);
    }

    @Bean
    public ReservarPreventaUseCase reservarPreventaUseCase(
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository,
            PreventaService preventaService) {
        return new ReservarPreventaUseCase(componenteRepository, reservaPreventaRepository, preventaService);
    }

    @Bean
    public CancelarReservaPreventaUseCase cancelarReservaPreventaUseCase(
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository,
            PreventaService preventaService) {
        return new CancelarReservaPreventaUseCase(componenteRepository, reservaPreventaRepository, preventaService);
    }

    @Bean
    public ConsultarReservaPreventaUseCase consultarReservaPreventaUseCase(
            ReservaPreventaRepository reservaPreventaRepository) {
        return new ConsultarReservaPreventaUseCase(reservaPreventaRepository);
    }

    @Bean
    public RecibirPreventaUseCase recibirPreventaUseCase(ComponenteRepository componenteRepository) {
        return new RecibirPreventaUseCase(componenteRepository);
    }

    @Bean
    public RegistrarComponenteUseCase registrarComponenteUseCase(
            ComponenteRepository componenteRepository,
            VendedorRepository vendedorRepository,
            ComponenteFactory componenteFactory) {
        return new RegistrarComponenteUseCase(componenteRepository, vendedorRepository, componenteFactory);
    }

    @Bean
    public RegistrarVendedorUseCase registrarVendedorUseCase(VendedorRepository vendedorRepository, VendedorFactory vendedorFactory) {
        return new RegistrarVendedorUseCase(vendedorRepository, vendedorFactory);
    }

    @Bean
    public ConsultarVendedorUseCase consultarVendedorUseCase(VendedorRepository vendedorRepository) {
        return new ConsultarVendedorUseCase(vendedorRepository);
    }

    @Bean
    public ListarVendedoresUseCase listarVendedoresUseCase(VendedorRepository vendedorRepository) {
        return new ListarVendedoresUseCase(vendedorRepository);
    }

    @Bean
    public MarcarComponenteAgotadoUseCase marcarComponenteAgotadoUseCase(ComponenteRepository componenteRepository) {
        return new MarcarComponenteAgotadoUseCase(componenteRepository);
    }

    @Bean
    public PublicarComponenteUseCase publicarComponenteUseCase(ComponenteRepository componenteRepository) {
        return new PublicarComponenteUseCase(componenteRepository);
    }

    @Bean
    public EliminarComponenteUseCase eliminarComponenteUseCase(
            ComponenteRepository componenteRepository,
            BuildRepository buildRepository,
            CatalogoService catalogoService) {
        return new EliminarComponenteUseCase(componenteRepository, buildRepository, catalogoService);
    }

    @Bean
    public SolicitarRMAUseCase solicitarRMAUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            SolicitudRMARepository solicitudRMARepository,
            RmaService rmaService) {
        return new SolicitarRMAUseCase(compraRepository, componenteRepository, solicitudRMARepository, rmaService);
    }

    @Bean
    public AceptarRMAUseCase aceptarRMAUseCase(
            SolicitudRMARepository solicitudRMARepository,
            ComponenteRepository componenteRepository,
            RmaService rmaService) {
        return new AceptarRMAUseCase(solicitudRMARepository, componenteRepository, rmaService);
    }

    @Bean
    public RechazarRMAUseCase rechazarRMAUseCase(
            SolicitudRMARepository solicitudRMARepository,
            ComponenteRepository componenteRepository,
            RmaService rmaService) {
        return new RechazarRMAUseCase(solicitudRMARepository, componenteRepository, rmaService);
    }

    @Bean
    public ConsultarRMAUseCase consultarRMAUseCase(SolicitudRMARepository solicitudRMARepository) {
        return new ConsultarRMAUseCase(solicitudRMARepository);
    }

    @Bean
    public ListarRMAUseCase listarRMAUseCase(SolicitudRMARepository solicitudRMARepository) {
        return new ListarRMAUseCase(solicitudRMARepository);
    }

    @Bean
    public RealizarCompraUseCase realizarCompraUseCase(
            BuildRepository buildRepository,
            ComponenteRepository componenteRepository,
            CompraRepository compraRepository,
            CompraService compraService) {
        return new RealizarCompraUseCase(buildRepository, componenteRepository, compraRepository, compraService);
    }

    @Bean
    public ConfirmarCompraUseCase confirmarCompraUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            BuildRepository buildRepository,
            ReservaPreventaRepository reservaPreventaRepository,
            CompraService compraService) {
        return new ConfirmarCompraUseCase(compraRepository, componenteRepository, buildRepository, reservaPreventaRepository, compraService);
    }

    @Bean
    public CancelarCompraUseCase cancelarCompraUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository,
            CompraService compraService) {
        return new CancelarCompraUseCase(compraRepository, componenteRepository, reservaPreventaRepository, compraService);
    }

    @Bean
    public ConsultarCompraUseCase consultarCompraUseCase(CompraRepository compraRepository) {
        return new ConsultarCompraUseCase(compraRepository);
    }

    @Bean
    public ReembolsarCompraUseCase reembolsarCompraUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            SolicitudRMARepository solicitudRMARepository,
            CompraService compraService) {
        return new ReembolsarCompraUseCase(compraRepository, componenteRepository, solicitudRMARepository, compraService);
    }

    @Bean
    public ComprarReservaPreventaUseCase comprarReservaPreventaUseCase(
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository,
            CompraRepository compraRepository,
            PreventaService preventaService,
            CompraService compraService) {
        return new ComprarReservaPreventaUseCase(componenteRepository, reservaPreventaRepository, compraRepository, preventaService, compraService);
    }
}
