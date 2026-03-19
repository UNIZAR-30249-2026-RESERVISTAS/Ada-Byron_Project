package com.adabyron.application.persona;

import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de autenticación.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder; 
    
    public AuthService(PersonaRepository personaRepository, PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica a un usuario por email y contraseña.
     * @return Optional con la persona si las credenciales son válidas, empty si no.
     */
    public Optional<Persona> autenticar(String email, String password) {
        return personaRepository.findByEmail(email)
                .filter(persona -> passwordEncoder.matches(password, persona.getPasswordHash()));
    }
}
