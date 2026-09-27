package pe.edu.galaxy.training.java.quarkus.loan.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanStatusTest {

    @Test
    void pending_canTransitionToApprovedRejectedOrCancelled() {
        assertTrue(LoanStatus.PENDING.canTransitionTo(LoanStatus.APPROVED));
        assertTrue(LoanStatus.PENDING.canTransitionTo(LoanStatus.REJECTED));
        assertTrue(LoanStatus.PENDING.canTransitionTo(LoanStatus.CANCELLED));
    }

    @Test
    void pending_cannotTransitionToReturned() {
        assertFalse(LoanStatus.PENDING.canTransitionTo(LoanStatus.RETURNED));
    }

    @Test
    void approved_canTransitionToReturned() {
        assertTrue(LoanStatus.APPROVED.canTransitionTo(LoanStatus.RETURNED));
    }

    @Test
    void approved_cannotTransitionToApprovedRejectedOrCancelled() {
        assertFalse(LoanStatus.APPROVED.canTransitionTo(LoanStatus.APPROVED));
        assertFalse(LoanStatus.APPROVED.canTransitionTo(LoanStatus.REJECTED));
        assertFalse(LoanStatus.APPROVED.canTransitionTo(LoanStatus.CANCELLED));
    }

    @Test
    void terminalStates_haveNoValidTransitions() {
        for (LoanStatus target : LoanStatus.values()) {
            assertFalse(LoanStatus.REJECTED.canTransitionTo(target));
            assertFalse(LoanStatus.CANCELLED.canTransitionTo(target));
            assertFalse(LoanStatus.RETURNED.canTransitionTo(target));
        }
    }

    @Test
    void parseStatus_invalidValue_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException.class,
                () -> LoanStatus.parseStatus("NO_EXISTE"));
    }
}
