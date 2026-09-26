package pe.edu.galaxy.training.java.quarkus.community.dto.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * status lo fija el servidor en AVAILABLE.
 * `condition` se valida contra el enum ObjectCondition en el servicio.
 */
public record CreateObjectRequest(
        @NotNull Long ownerId,
        @NotBlank @Size(max = 150) String name,
        @NotBlank String description,
        @NotBlank @Size(max = 50) String category,
        @NotBlank String condition
) {}
