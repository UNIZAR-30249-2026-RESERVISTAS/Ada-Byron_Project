package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.AuthService;
import com.adabyron.application.persona.LoginDTO;
import com.adabyron.application.persona.PersonaDTO;
import com.adabyron.domain.persona.Persona;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Gestión de sesiones: login, logout y usuario actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica al usuario con email y contraseña. Si las credenciales son válidas, " +
                      "crea una sesión HTTP (cookie `JSESSIONID`) con duración de 24 horas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login correcto. Se devuelve el perfil del usuario autenticado.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    { "error": "Credenciales inválidas" }
                """)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto, HttpServletRequest request) {
        Optional<Persona> personaOpt = authService.autenticar(dto.email(), dto.password());

        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        Persona persona = personaOpt.get();

        HttpSession sessionAnterior = request.getSession(false);
        if (sessionAnterior != null) {
            sessionAnterior.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("personaId", persona.getId().toString());
        session.setAttribute("email", persona.getEmail());
        session.setAttribute("roles", persona.getRoles());
        session.setMaxInactiveInterval(24 * 60 * 60);

        return ResponseEntity.ok(PersonaDTO.fromEntity(persona));
    }

    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida la sesión activa del usuario y limpia el contexto de seguridad. " +
                      "La cookie `JSESSIONID` queda eliminada automáticamente por Spring Security."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente. Sin cuerpo en la respuesta."),
        @ApiResponse(responseCode = "204", description = "El usuario no tenía sesión activa. Se responde igualmente con 204.")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Obtener usuario autenticado",
        description = "Devuelve los datos básicos del usuario cuya sesión esté activa en la petición. " +
                      "Requiere que la cookie `JSESSIONID` sea enviada por el cliente."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Sesión válida. Se devuelven los datos de la sesión activa.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "authenticated": true,
                      "personaId": "a1b2c3d4-...",
                      "email": "usuario@ejemplo.com",
                      "roles": ["ROLE_USER"]
                    }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No existe sesión activa o la sesión ha expirado.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    { "error": "No autenticado" }
                """)
            )
        )
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

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
     * No expuesto como endpoint, por lo que no requiere anotaciones Swagger.
     */
    public static Optional<String> getPersonaIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        return Optional.ofNullable((String) session.getAttribute("personaId"));
    }
}