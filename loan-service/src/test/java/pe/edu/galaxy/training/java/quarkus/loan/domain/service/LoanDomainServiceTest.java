package pe.edu.galaxy.training.java.quarkus.loan.domain.service;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ConflictException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.NotFoundException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.RequesterIsOwnerException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanDomainServiceTest {

    private final LoanDomainService loanDomainService = new LoanDomainService();

    private LoanRequest candidate(Long requesterId, Long... objectIds) {
        List<LoanRequestItem> items = List.of(objectIds).stream()
                .map(objectId -> new LoanRequestItem(null, objectId, null))
                .toList();
        return new LoanRequest(null, requesterId, LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 25),
                LoanStatus.PENDING, "Prueba", Instant.now(), items);
    }

    @Test
    void validateCreation_allRulesSatisfied_doesNotThrow() {
        LoanRequest candidate = candidate(2L, 1L);
        assertDoesNotThrow(() -> loanDomainService.validateCreation(
                candidate, true, Map.of(1L, 1L), Set.of()));
    }

    @Test
    void validateCreation_invalidDateRange_throws() {
        LoanRequest invalid = new LoanRequest(null, 2L, LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 20),
                LoanStatus.PENDING, "Prueba", Instant.now(), List.of(new LoanRequestItem(null, 1L, null)));

        assertThrows(InvalidFieldValueException.class, () -> loanDomainService.validateCreation(
                invalid, true, Map.of(1L, 1L), Set.of()));
    }

    @Test
    void validateCreation_requesterNotFound_throws() {
        LoanRequest candidate = candidate(999L, 1L);
        assertThrows(NotFoundException.class, () -> loanDomainService.validateCreation(
                candidate, false, Map.of(1L, 1L), Set.of()));
    }

    @Test
    void validateCreation_objectNotFound_throws() {
        LoanRequest candidate = candidate(2L, 999L);
        assertThrows(NotFoundException.class, () -> loanDomainService.validateCreation(
                candidate, true, Map.of(), Set.of()));
    }

    @Test
    void validateCreation_requesterIsOwner_throws() {
        LoanRequest candidate = candidate(1L, 1L);
        assertThrows(RequesterIsOwnerException.class, () -> loanDomainService.validateCreation(
                candidate, true, Map.of(1L, 1L), Set.of()));
    }

    @Test
    void validateCreation_objectUnavailable_throws() {
        LoanRequest candidate = candidate(2L, 4L);
        assertThrows(ConflictException.class, () -> loanDomainService.validateCreation(
                candidate, true, Map.of(4L, 1L), Set.of(4L)));
    }

    @Test
    void validateTransition_validTransition_doesNotThrow() {
        assertDoesNotThrow(() -> loanDomainService.validateTransition(LoanStatus.PENDING, LoanStatus.APPROVED));
    }

    @Test
    void validateTransition_invalidTransition_throws() {
        assertThrows(ConflictException.class,
                () -> loanDomainService.validateTransition(LoanStatus.APPROVED, LoanStatus.APPROVED));
    }
}
