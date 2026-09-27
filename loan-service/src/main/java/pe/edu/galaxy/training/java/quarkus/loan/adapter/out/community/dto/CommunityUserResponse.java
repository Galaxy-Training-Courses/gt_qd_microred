package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto;

import java.time.Instant;

/**
 * Espejo local de `community-service`'s `UserResponse`.
 * loan-service no importa clases de otro módulo — se
 * declara el JSON completo, no solo `id`, para no depender
 * de que Jackson ignore campos desconocidos.
 */
public record CommunityUserResponse(
        Long id,
        String name,
        String email,
        String neighborhood,
        String status,
        Instant createdAt
) {}
