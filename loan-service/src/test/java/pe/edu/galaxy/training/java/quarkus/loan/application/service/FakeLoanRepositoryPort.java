package pe.edu.galaxy.training.java.quarkus.loan.application.service;

import pe.edu.galaxy.training.java.quarkus.loan.application.port.LoanPage;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.LoanRepositoryPort;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FakeLoanRepositoryPort implements LoanRepositoryPort {

    private final Map<Long, LoanRequest> store = new HashMap<>();
    private long nextId = 1;

    @Override
    public LoanRequest save(LoanRequest loanRequest) {
        LoanRequest toSave = loanRequest.id() == null
                ? new LoanRequest(nextId++, loanRequest.requesterId(), loanRequest.requestedFrom(),
                        loanRequest.requestedUntil(), loanRequest.status(), loanRequest.reason(),
                        loanRequest.createdAt(), loanRequest.items())
                : loanRequest;
        store.put(toSave.id(), toSave);
        return toSave;
    }

    @Override
    public Optional<LoanRequest> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public LoanPage search(int page, int size, LoanStatus status, Long requesterId, LocalDate from,
                           LocalDate until, String sort) {
        throw new UnsupportedOperationException(
                "search() no se ejercita en LoanApplicationServiceTest; ya lo cubre LoanResourceTest");
    }
}
