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
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.BuildRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.CompraRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ComponenteRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.ReservaPreventaRepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.SolicitudRMARepositoryEnMemoria;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.persistence.VendedorRepositoryEnMemoria;

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

    // ---------- Casos de uso ----------

    @Bean
    public CrearBuildUseCase crearBuildUseCase(BuildRepository buildRepository) {
        return new CrearBuildUseCase(buildRepository);
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
    public PublicarPreventaUseCase publicarPreventaUseCase(ComponenteRepository componenteRepository) {
        return new PublicarPreventaUseCase(componenteRepository);
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
            ReservaPreventaRepository reservaPreventaRepository) {
        return new ReservarPreventaUseCase(componenteRepository, reservaPreventaRepository);
    }

    @Bean
    public CancelarReservaPreventaUseCase cancelarReservaPreventaUseCase(
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository) {
        return new CancelarReservaPreventaUseCase(componenteRepository, reservaPreventaRepository);
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
            VendedorRepository vendedorRepository) {
        return new RegistrarComponenteUseCase(componenteRepository, vendedorRepository);
    }

    @Bean
    public RegistrarVendedorUseCase registrarVendedorUseCase(VendedorRepository vendedorRepository) {
        return new RegistrarVendedorUseCase(vendedorRepository);
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
            BuildRepository buildRepository) {
        return new EliminarComponenteUseCase(componenteRepository, buildRepository);
    }

    @Bean
    public SolicitarRMAUseCase solicitarRMAUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            SolicitudRMARepository solicitudRMARepository) {
        return new SolicitarRMAUseCase(compraRepository, componenteRepository, solicitudRMARepository);
    }

    @Bean
    public AceptarRMAUseCase aceptarRMAUseCase(
            SolicitudRMARepository solicitudRMARepository,
            ComponenteRepository componenteRepository) {
        return new AceptarRMAUseCase(solicitudRMARepository, componenteRepository);
    }

    @Bean
    public RechazarRMAUseCase rechazarRMAUseCase(
            SolicitudRMARepository solicitudRMARepository,
            ComponenteRepository componenteRepository) {
        return new RechazarRMAUseCase(solicitudRMARepository, componenteRepository);
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
            CompraRepository compraRepository) {
        return new RealizarCompraUseCase(buildRepository, componenteRepository, compraRepository);
    }

    @Bean
    public ConfirmarCompraUseCase confirmarCompraUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            BuildRepository buildRepository,
            ReservaPreventaRepository reservaPreventaRepository) {
        return new ConfirmarCompraUseCase(compraRepository, componenteRepository, buildRepository, reservaPreventaRepository);
    }

    @Bean
    public CancelarCompraUseCase cancelarCompraUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository) {
        return new CancelarCompraUseCase(compraRepository, componenteRepository, reservaPreventaRepository);
    }

    @Bean
    public ConsultarCompraUseCase consultarCompraUseCase(CompraRepository compraRepository) {
        return new ConsultarCompraUseCase(compraRepository);
    }

    @Bean
    public ReembolsarCompraUseCase reembolsarCompraUseCase(
            CompraRepository compraRepository,
            ComponenteRepository componenteRepository,
            SolicitudRMARepository solicitudRMARepository) {
        return new ReembolsarCompraUseCase(compraRepository, componenteRepository, solicitudRMARepository);
    }

    @Bean
    public ComprarReservaPreventaUseCase comprarReservaPreventaUseCase(
            ComponenteRepository componenteRepository,
            ReservaPreventaRepository reservaPreventaRepository,
            CompraRepository compraRepository) {
        return new ComprarReservaPreventaUseCase(componenteRepository, reservaPreventaRepository, compraRepository);
    }
}
