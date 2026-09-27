package pe.edu.galaxy.training.java.quarkus.loan.application.port.in;

import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.LocalDate;

/**
 * Puerto de entrada: las operaciones de negocio de loan-service, en términos
 * de dominio. {@code adapter/in/rest/LoanResource} depende solo de esta interfaz,
 * nunca de la implementación.
 */
public interface LoanUseCase {

    LoanRequest create(CreateLoanCommand command);

    LoanRequest findById(Long id);

    LoanPage search(int page, int size, LoanStatus status, Long requesterId, LocalDate from, LocalDate until,
                    String sort);

    LoanRequest approve(Long id);

    LoanRequest reject(Long id);

    LoanRequest cancel(Long id);

    LoanRequest returnLoan(Long id);
}
