package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.dto;

public record LoanItemResponse(
        Long id,
        Long objectId,
        String notes
) {}
