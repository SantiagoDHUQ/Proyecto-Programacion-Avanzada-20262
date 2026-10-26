package org.uniquindio.proyectoavanzadacompuparts.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RegistrarBuildRequest(

        //debería validar que Build no esté vacío (Build dentro contiene una lista de componentes, un UUID, generado dentro del dominio y un estado
        @NotBlank
        List<Object> componenetes
){
}
