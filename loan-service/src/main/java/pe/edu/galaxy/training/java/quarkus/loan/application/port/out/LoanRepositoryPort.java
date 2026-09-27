package pe.edu.galaxy.training.java.quarkus.loan.application.port.out;

import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia. Lo implementa
 * {@code adapter/out/persistence/LoanRepositoryAdapter}
 * sobre Panache; la aplicación y el dominio no saben que existe Hibernate.
 */
public interface LoanRepositoryPort {

    LoanRequest save(LoanRequest loanRequest);

    Optional<LoanRequest> findById(Long id);

    LoanPage search(int page, int size, LoanStatus status, Long requesterId, LocalDate from, LocalDate until,
                    String sort);
}
