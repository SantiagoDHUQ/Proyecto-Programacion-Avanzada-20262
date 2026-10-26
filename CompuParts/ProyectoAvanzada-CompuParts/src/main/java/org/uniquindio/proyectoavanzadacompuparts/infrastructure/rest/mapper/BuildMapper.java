package org.uniquindio.proyectoavanzadacompuparts.infrastructure.rest.mapper;

import org.springframework.stereotype.Component;
import org.uniquindio.proyectoavanzadacompuparts.application.dto.response.BuildResponse;
import org.uniquindio.proyectoavanzadacompuparts.domain.entity.Build;

@Component
public class BuildMapper {

    public BuildResponse toResponse(Build build) {
        return BuildResponse.from(build);
    }
}
