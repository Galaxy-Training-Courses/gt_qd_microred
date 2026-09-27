package pe.edu.galaxy.training.java.quarkus.loan.application.port;

import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;

import java.util.List;

/**
 * Página de solicitudes en términos de dominio — la usan tanto
 * {@code LoanUseCase.search} (port/in) como {@code LoanRepositoryPort.search}
 * (port/out). Deliberadamente distinta de {@code adapter.in.rest.dto.PageResponse}:
 * esa es la forma HTTP; esta es la forma de dominio. El mapper de
 * adapter/in/rest traduce una a la otra.
 */
public record LoanPage(
        List<LoanRequest> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
