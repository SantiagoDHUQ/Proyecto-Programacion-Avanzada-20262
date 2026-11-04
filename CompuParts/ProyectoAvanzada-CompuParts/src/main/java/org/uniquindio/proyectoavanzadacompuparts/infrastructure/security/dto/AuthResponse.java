package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security.dto;

import java.util.Date;

public record AuthResponse(
        String token,
        String correo,
        String rol,
        Date fechaVencimiento
) {}

