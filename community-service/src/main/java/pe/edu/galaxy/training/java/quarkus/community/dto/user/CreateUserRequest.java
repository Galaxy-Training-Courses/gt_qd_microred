package pe.edu.galaxy.training.java.quarkus.community.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * status y createdAt los fija el servidor.
 */
public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 100) String neighborhood
) {}
