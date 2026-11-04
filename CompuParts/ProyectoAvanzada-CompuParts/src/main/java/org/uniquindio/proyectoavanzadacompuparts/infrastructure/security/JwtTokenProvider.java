package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(String correo, RolSeguridad rol) {
        Date ahora = new Date();
        Date fechaVencimiento = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(correo)
                .claim("correo", correo)
                .claim("rol", rol.name())
                .issuedAt(ahora)
                .expiration(fechaVencimiento)
                .signWith(key)
                .compact();
    }

    public Date obtenerFechaVencimiento(String token) {
        return obtenerClaims(token).getExpiration();
    }

    public String obtenerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String obtenerRol(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    public boolean validarToken(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

