package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.security.RolSeguridad;

public record RegistroRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato de correo no es válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        @NotNull(message = "El rol es obligatorio")
        RolSeguridad rol
) {}

