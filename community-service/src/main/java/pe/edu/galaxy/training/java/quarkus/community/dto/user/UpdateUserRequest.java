package pe.edu.galaxy.training.java.quarkus.community.dto.user;

import jakarta.validation.constraints.Size;

/**
 * Actualización parcial: solo los campos no nulos se aplican.
 * `status` se valida contra el enum en el servicio (400 si no es un valor válido)
 */
public record UpdateUserRequest(
        @Size(max = 100) String neighborhood,
        String status
) {}
