package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.AuthService;
import com.adabyron.application.persona.LoginDTO;
import com.adabyron.application.persona.PersonaDTO;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.Rol;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    // -------------------------
    // LOGIN OK
    // -------------------------
    @Test
    void deberiaHacerLoginCorrecto() {

        LoginDTO dto = mock(LoginDTO.class);

        UUID personaId = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        when(dto.email()).thenReturn("test@test.com");
        when(dto.password()).thenReturn("1234");

        when(authService.autenticar("test@test.com", "1234"))
                .thenReturn(Optional.of(persona));

        when(persona.getId()).thenReturn(personaId);
        when(persona.getEmail()).thenReturn("test@test.com");
        when(persona.getRoles()).thenReturn(Set.of(Rol.ESTUDIANTE));

        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = authController.login(dto, request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        verify(authService).autenticar("test@test.com", "1234");
    }

    // -------------------------
    // LOGIN FAIL
    // -------------------------
    @Test
    void deberiaFallarLoginSiCredencialesIncorrectas() {

        LoginDTO dto = mock(LoginDTO.class);

        when(dto.email()).thenReturn("bad@test.com");
        when(dto.password()).thenReturn("wrong");

        when(authService.autenticar(any(), any()))
                .thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = authController.login(dto, request);

        assertEquals(401, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Credenciales inválidas"));
    }

    // -------------------------
    // LOGOUT
    // -------------------------
    @Test
    void deberiaCerrarSesion() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();

        request.setSession(session);

        ResponseEntity<Void> response = authController.logout(request);

        assertEquals(204, response.getStatusCodeValue());
    }

    // -------------------------
    // ME OK
    // -------------------------
    @Test
    void deberiaDevolverUsuarioAutenticado() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();

        UUID personaId = UUID.randomUUID();

        session.setAttribute("personaId", personaId.toString());
        session.setAttribute("email", "test@test.com");
        session.setAttribute("roles", Set.of("ROLE_USER"));

        request.setSession(session);

        ResponseEntity<?> response = authController.getCurrentUser(request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    // -------------------------
    // ME FAIL (sin sesión)
    // -------------------------
    @Test
    void deberiaDevolver401SiNoHaySesion() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = authController.getCurrentUser(request);

        assertEquals(401, response.getStatusCodeValue());
    }
}