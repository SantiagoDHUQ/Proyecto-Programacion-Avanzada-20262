package org.uniquindio.proyectoavanzadacompuparts.application.usecase;

import org.uniquindio.proyectoavanzadacompuparts.application.dto.RegistrarComponenteRequestDTO;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Componente;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Vendedor;
import org.uniquindio.proyectoavanzadacompuparts.domain.repository.ComponenteRepository;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.util.Objects;

/**
 * Caso de uso que registra un nuevo Componente en el catálogo.
 * Requiere ComponenteRepository para persistir la entidad.
 */
public class RegistrarComponenteUseCase {

    private final ComponenteRepository componenteRepository;

    public RegistrarComponenteUseCase(ComponenteRepository componenteRepository) {
        this.componenteRepository = Objects.requireNonNull(componenteRepository, "El repositorio de componentes no puede ser nulo");
    }

    public Componente ejecutar(RegistrarComponenteRequestDTO request) {
        Vendedor vendedor = Vendedor.crear("Vendedor", request.vendedorTipo());
        Componente componente = Componente.crear(
                request.nombre(),
                CategoriaComponente.valueOf(request.categoria()),
                new EspecificacionTecnica(request.socket(), request.wattajeRequerido(), request.tipoMemoria()),
                new Precio(request.precio(), request.moneda()),
                Disponibilidad.DISPONIBLE,
                vendedor,
                request.numeroSerie()
        );
        return componenteRepository.guardar(componente);
    }
}
