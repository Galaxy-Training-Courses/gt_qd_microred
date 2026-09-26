package pe.edu.galaxy.training.java.quarkus.community.dto.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * reemplazo completo, no permite cambiar ownerId ni status
 * (ese cambio de estado es exclusivo de PATCH, porque es lo que usa loan-service).
 */
public record UpdateObjectRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank String description,
        @NotBlank @Size(max = 50) String category,
        @NotBlank String condition
) {}
