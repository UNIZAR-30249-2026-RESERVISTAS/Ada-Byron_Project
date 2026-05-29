package com.adabyron.infraestructure.web;

import com.adabyron.application.edificio.CambiarPorcentajeOcupacionDTO;
import com.adabyron.application.edificio.EdificioOcupacionDTO;
import com.adabyron.application.edificio.EdificioService;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdificioControllerTest {

    @Mock
    private EdificioService edificioService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EdificioController controller;

    // -----------------------------
    // helper session mock
    // -----------------------------
    private HttpServletRequest mockRequest(UUID personaId, boolean gerente) {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(false)).thenReturn(session);

        when(session.getAttribute("personaId"))
                .thenReturn(personaId.toString());

        if (gerente) {
            when(session.getAttribute("roles"))
                    .thenReturn(List.of("GERENTE"));
        } else {
            when(session.getAttribute("roles"))
                    .thenReturn(List.of("USUARIO"));
        }

        return request;
    }

    // -----------------------------
    // GET OCUPACION OK
    // -----------------------------
    @Test
    void deberiaObtenerPorcentajeOcupacion() throws Exception {

        EdificioOcupacionDTO dto = mock(EdificioOcupacionDTO.class);

        when(rabbitTemplate.convertSendAndReceive(
                eq("edificio.porcentajeOcupacion"),
                eq("GET")
        )).thenReturn(dto);

        EdificioOcupacionDTO result = controller.obtenerPorcentajeOcupacion();

        assertEquals(dto, result);
        verify(rabbitTemplate).convertSendAndReceive(anyString(), any(Object.class));
    }

    // -----------------------------
    // GET OCUPACION TIMEOUT
    // -----------------------------
    @Test
    void deberiaLanzarTimeoutSiNoHayRespuesta() {

        when(rabbitTemplate.convertSendAndReceive(anyString(), any(Object.class)))
                .thenReturn(null);

        assertThrows(
                TimeoutException.class,
                () -> controller.obtenerPorcentajeOcupacion()
        );
    }

    // -----------------------------
    // PUT OCUPACION OK (GERENTE)
    // -----------------------------
    @Test
    void deberiaCambiarPorcentajeSiEsGerente() throws Exception {

        CambiarPorcentajeOcupacionDTO dto =
                mock(CambiarPorcentajeOcupacionDTO.class);

        EdificioOcupacionDTO respuesta =
                mock(EdificioOcupacionDTO.class);

        HttpServletRequest request =
                mockRequest(UUID.randomUUID(), true);

        when(rabbitTemplate.convertSendAndReceive(
                eq("edificio.cambiarPorcentajeOcupacion"),
                eq(dto)
        )).thenReturn(respuesta);

        EdificioOcupacionDTO result =
                controller.cambiarPorcentajeOcupacion(dto, request);

        assertEquals(respuesta, result);
    }

    // -----------------------------
    // PUT OCUPACION FAIL (NO GERENTE)
    // -----------------------------
    @Test
    void deberiaFallarSiNoEsGerente() {

        CambiarPorcentajeOcupacionDTO dto =
                mock(CambiarPorcentajeOcupacionDTO.class);

        HttpServletRequest request =
                mockRequest(UUID.randomUUID(), false);

        assertThrows(
                OperacionNoAutorizadaException.class,
                () -> controller.cambiarPorcentajeOcupacion(dto, request)
        );
    }

    // -----------------------------
    // PUT OCUPACION TIMEOUT
    // -----------------------------
    @Test
    void deberiaLanzarTimeoutEnCambioOcupacion() {

        CambiarPorcentajeOcupacionDTO dto =
                mock(CambiarPorcentajeOcupacionDTO.class);

        HttpServletRequest request =
                mockRequest(UUID.randomUUID(), true);

        when(rabbitTemplate.convertSendAndReceive(anyString(), any(Object.class)))
                .thenReturn(null);

        assertThrows(
                TimeoutException.class,
                () -> controller.cambiarPorcentajeOcupacion(dto, request)
        );
    }
}