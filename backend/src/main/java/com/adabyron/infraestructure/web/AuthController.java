package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.AuthService;
import com.adabyron.application.persona.LoginDTO;
import com.adabyron.application.persona.PersonaDTO;
import com.adabyron.domain.persona.Persona;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto, HttpServletRequest request) {
        Optional<Persona> personaOpt = authService.autenticar(dto.email(), dto.password());

        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        Persona persona = personaOpt.get();

        // Invalidar sesión anterior si existía 
        HttpSession sessionAnterior = request.getSession(false);
        if (sessionAnterior != null) {
            sessionAnterior.invalidate();
        }

        // Spring Security crea una sesión nueva con ID regenerado
        HttpSession session = request.getSession(true);
        session.setAttribute("personaId", persona.getId().toString());
        session.setAttribute("email", persona.getEmail());
        session.setAttribute("roles", persona.getRoles());
        session.setMaxInactiveInterval(24 * 60 * 60); // 24 horas

        return ResponseEntity.ok(PersonaDTO.fromEntity(persona));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Spring Security limpia la cookie automáticamente
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // false = no crear sesión nueva

        if (session == null || session.getAttribute("personaId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "personaId",     session.getAttribute("personaId"),
            "email",         session.getAttribute("email"),
            "roles",         session.getAttribute("roles")
        ));
    }

    /**
     * Método auxiliar para verificar sesión desde otros controladores.
     */
    public static Optional<String> getPersonaIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        return Optional.ofNullable((String) session.getAttribute("personaId"));
    }
}