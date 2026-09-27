package pe.edu.galaxy.training.java.quarkus.loan.application.port.out;

import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;

/**
 * Puerto de salida hacia la mensajería. Se implementa con un adaptador
 * *no-op* ({@code adapter/out/messaging/NoOpLoanEventPublisherAdapter})
 * {@code eventType} usa los nombres literales (LOAN_CREATED, LOAN_APPROVED, ...)
 */
public interface LoanEventPublisherPort {
    void publish(String eventType, LoanRequest loanRequest);
}
