package pe.edu.galaxy.training.java.quarkus.community.dto.user;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        String neighborhood,
        String status,
        Instant createdAt
) {}
