package org.uniquindio.proyectoavanzadacompuparts.infrastructure.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.security.dto.AuthResponse;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.security.dto.LoginRequest;
import org.uniquindio.proyectoavanzadacompuparts.infrastructure.security.dto.RegistroRequest;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioJpaDataRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "mensaje", "Ya existe un usuario registrado con el correo: " + request.correo()
            ));
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        UsuarioEntity usuario = new UsuarioEntity(request.correo(), hashedPassword, request.rol());
        usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generarToken(usuario.getCorreo(), usuario.getRol());
        Date fechaVencimiento = jwtTokenProvider.obtenerFechaVencimiento(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                usuario.getCorreo(),
                usuario.getRol().name(),
                fechaVencimiento
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(request.correo())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", HttpStatus.UNAUTHORIZED.value(),
                    "mensaje", "Credenciales inválidas"
            ));
        }

        String token = jwtTokenProvider.generarToken(usuario.getCorreo(), usuario.getRol());
        Date fechaVencimiento = jwtTokenProvider.obtenerFechaVencimiento(token);

        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getCorreo(),
                usuario.getRol().name(),
                fechaVencimiento
        ));
    }
}

