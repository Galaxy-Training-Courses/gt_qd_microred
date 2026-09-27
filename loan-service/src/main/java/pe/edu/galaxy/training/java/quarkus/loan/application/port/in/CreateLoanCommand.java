package pe.edu.galaxy.training.java.quarkus.loan.application.port.in;

import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Comando de entrada de {@link LoanUseCase#create}. Deliberadamente sin
 * anotaciones de Bean Validation: la validación sintáctica del payload HTTP
 * es responsabilidad de {@code adapter/in/rest}, no cruza el puerto.
 */
public record CreateLoanCommand(
        Long requesterId,
        LocalDate requestedFrom,
        LocalDate requestedUntil,
        String reason,
        List<LoanRequestItem> items
) {}
