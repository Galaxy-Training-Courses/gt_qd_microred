package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
