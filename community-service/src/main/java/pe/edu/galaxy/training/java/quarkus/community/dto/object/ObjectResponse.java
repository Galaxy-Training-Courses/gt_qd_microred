package pe.edu.galaxy.training.java.quarkus.community.dto.object;

import java.time.Instant;

public record ObjectResponse(
        Long id,
        Long ownerId,
        String name,
        String description,
        String category,
        String condition,
        String status,
        Instant createdAt
) {}
