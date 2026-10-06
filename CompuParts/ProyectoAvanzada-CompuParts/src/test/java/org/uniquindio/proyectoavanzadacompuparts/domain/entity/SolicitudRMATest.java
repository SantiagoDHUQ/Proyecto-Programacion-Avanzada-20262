package org.uniquindio.proyectoavanzadacompuparts.domain.entity;

import org.junit.jupiter.api.Test;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.CategoriaComponente;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Disponibilidad;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.EspecificacionTecnica;
import org.uniquindio.proyectoavanzadacompuparts.domain.valueobject.Precio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SolicitudRMATest {
    @Test
    void dosSolicitudesRMAConMismoComponentesNoSonIguales(){
        //Arrange
        Vendedor vendedor = Vendedor.crear(
                "1",
                "Distribuidor Oficial",
                "AUTORIZADO"
        );

        EspecificacionTecnica especificacion =
                new EspecificacionTecnica("AM5", 100, "DDR5");

        Precio precio =
                new Precio(new BigDecimal("1500000"), "COP");

        Componente componente = Componente.crear(
                "1",
                "RTX 4070",
                CategoriaComponente.GPU,
                especificacion,
                precio,
                Disponibilidad.DISPONIBLE,
                vendedor,
                "GPU-001",
                LocalDate.now(),
                30
        );
        SolicitudRMA uno = SolicitudRMA.crear("3" ,componente,LocalDate.of(2024,12, 30));
        SolicitudRMA dos = SolicitudRMA.crear( "",componente, LocalDate.of(2024, 12, 29));

        assertNotEquals(uno, dos);
    }
}
