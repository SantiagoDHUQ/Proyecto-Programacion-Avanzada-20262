package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private JwtTokenProvider jwtTokenProvider;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // Consola H2
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("""
                                    {"status":401,"error":"Unauthorized","mensaje":"Acceso no autorizado: Token no proporcionado o invalido"}
                                    """);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("""
                                    {"status":403,"error":"Forbidden","mensaje":"Acceso denegado: No cuenta con el rol requerido para este recurso"}
                                    """);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos de autenticación, documentación y base de datos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Rutas restringidas al rol VENDEDOR
                        .requestMatchers(HttpMethod.POST, "/api/componentes/*/preventas").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/componentes/*/publicar").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/componentes/*/agotado").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/componentes/*/unidades/*/recibir").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/componentes").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/componentes/**").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/rma/*/aceptar").hasRole(RolSeguridad.VENDEDOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/rma/*/rechazar").hasRole(RolSeguridad.VENDEDOR.name())

                        // Rutas restringidas al rol COMPRADOR
                        .requestMatchers(HttpMethod.POST, "/api/compras/*/reembolsar").hasRole(RolSeguridad.COMPRADOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/compras/*/confirmar").hasRole(RolSeguridad.COMPRADOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/compras/*/cancelar").hasRole(RolSeguridad.COMPRADOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/compras").hasRole(RolSeguridad.COMPRADOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/rma").hasRole(RolSeguridad.COMPRADOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/componentes/*/unidades/*/reservas-preventa").hasRole(RolSeguridad.COMPRADOR.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/reservas-preventa/**").hasRole(RolSeguridad.COMPRADOR.name())

                        // El resto de peticiones (consultas GET de catálogo, etc.) permitidas
                        .anyRequest().permitAll()
                );

        if (jwtTokenProvider != null) {
            http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}

