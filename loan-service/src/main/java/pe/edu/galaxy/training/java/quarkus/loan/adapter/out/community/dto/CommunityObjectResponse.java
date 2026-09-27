package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto;

import java.time.Instant;

/**
 * Espejo local de `community-service`'s `ObjectResponse`.
 * `ownerId` y `status` (AVAILABLE/RESERVED/UNAVAILABLE) llegan en la misma
 * respuesta — de aquí sale un {@code ObjectFact} completo con una sola
 * llamada.
 */
public record CommunityObjectResponse(
        Long id,
        Long ownerId,
        String name,
        String description,
        String category,
        String condition,
        String status,
        Instant createdAt
) {}
