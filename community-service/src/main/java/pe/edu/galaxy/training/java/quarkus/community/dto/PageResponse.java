package pe.edu.galaxy.training.java.quarkus.community.dto;

import java.util.List;

/**
 * Sobre de paginación exigido por el enunciado §12. El formato es exacto:
 * content, page, size, totalElements, totalPages — no agregar campos.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
