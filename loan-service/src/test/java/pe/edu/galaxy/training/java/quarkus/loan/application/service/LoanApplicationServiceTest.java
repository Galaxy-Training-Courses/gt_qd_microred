package pe.edu.galaxy.training.java.quarkus.loan.application.service;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.in.CreateLoanCommand;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ConflictException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.NotFoundException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.RequesterIsOwnerException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanApplicationServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-20T10:00:00Z");

    private final FakeLoanRepositoryPort repository = new FakeLoanRepositoryPort();
    private final FakeCommunityClientPort community = new FakeCommunityClientPort();
    private final FakeLoanEventPublisherPort events = new FakeLoanEventPublisherPort();
    private final Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    private final LoanApplicationService service =
            new LoanApplicationService(repository, community, events, clock);

    private LoanRequest createLoan(long requesterId, long objectId) {
        CreateLoanCommand command = new CreateLoanCommand(requesterId, LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5), "Prueba", List.of(new LoanRequestItem(null, objectId, null)));
        return service.create(command);
    }

    @Test
    void create_success_usesInjectedClockAndPublishesEvent() {
        LoanRequest created = createLoan(2L, 1L);

        assertEquals(LoanStatus.PENDING, created.status());
        assertEquals(FIXED_INSTANT, created.createdAt());
        assertEquals(1, events.published.size());
        assertEquals("LOAN_CREATED", events.published.get(0).eventType());
    }

    @Test
    void approve_success_reservesObjectAndPublishesEvent() {
        LoanRequest created = createLoan(2L, 1L);

        LoanRequest approved = service.approve(created.id());

        assertEquals(LoanStatus.APPROVED, approved.status());
        assertTrue(community.reservedObjectIds.contains(1L));
        assertEquals("LOAN_APPROVED", events.published.get(events.published.size() - 1).eventType());
    }

    @Test
    void reject_success_publishesEvent() {
        LoanRequest created = createLoan(2L, 1L);

        LoanRequest rejected = service.reject(created.id());

        assertEquals(LoanStatus.REJECTED, rejected.status());
        assertEquals("LOAN_REJECTED", events.published.get(events.published.size() - 1).eventType());
    }

    @Test
    void cancel_success_publishesEvent() {
        LoanRequest created = createLoan(2L, 1L);

        LoanRequest cancelled = service.cancel(created.id());

        assertEquals(LoanStatus.CANCELLED, cancelled.status());
        assertEquals("LOAN_CANCELLED", events.published.get(events.published.size() - 1).eventType());
    }

    @Test
    void returnLoan_success_releasesObjectAndPublishesEvent() {
        LoanRequest created = createLoan(2L, 1L);
        service.approve(created.id());

        LoanRequest returned = service.returnLoan(created.id());

        assertEquals(LoanStatus.RETURNED, returned.status());
        assertTrue(community.releasedObjectIds.contains(1L));
        assertEquals("LOAN_RETURNED", events.published.get(events.published.size() - 1).eventType());
    }

    @Test
    void create_requesterNotFound_throwsNotFoundException() {
        CreateLoanCommand command = new CreateLoanCommand(999_999_999L, LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5), "Prueba", List.of(new LoanRequestItem(null, 1L, null)));

        assertThrows(NotFoundException.class, () -> service.create(command));
    }

    @Test
    void create_requesterIsOwner_throwsRequesterIsOwnerException() {
        // El objeto 1 pertenece al usuario 1 (FakeCommunityClientPort).
        assertThrows(RequesterIsOwnerException.class, () -> createLoan(1L, 1L));
    }

    @Test
    void create_invalidDateRange_throwsInvalidFieldValueException() {
        CreateLoanCommand command = new CreateLoanCommand(2L, LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 1), "Prueba", List.of(new LoanRequestItem(null, 1L, null)));

        assertThrows(InvalidFieldValueException.class, () -> service.create(command));
    }

    @Test
    void approve_alreadyApproved_throwsConflictException() {
        LoanRequest created = createLoan(2L, 1L);
        service.approve(created.id());

        assertThrows(ConflictException.class, () -> service.approve(created.id()));
    }
}
