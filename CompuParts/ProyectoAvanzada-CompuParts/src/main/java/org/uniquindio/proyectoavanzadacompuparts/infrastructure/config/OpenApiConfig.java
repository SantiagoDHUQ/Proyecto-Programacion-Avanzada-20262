package org.uniquindio.proyectoavanzadacompuparts.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "Bearer Auth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CompuParts Marketplace API")
                        .version("1.0.0")
                        .description("API REST para el Marketplace de Hardware CompuParts con arquitectura DDD y seguridad JWT."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido del endpoint /api/auth/login")));
    }
}

