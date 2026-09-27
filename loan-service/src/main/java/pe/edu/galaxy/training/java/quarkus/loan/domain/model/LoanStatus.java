package pe.edu.galaxy.training.java.quarkus.loan.domain.model;

import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;

import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados de la solicitud de préstamo. PENDING es el único estado de
 * entrada; APPROVED/REJECTED/CANCELLED/RETURNED son alcanzables solo por las
 * transiciones válidas de {@link #canTransitionTo}.
 */
public enum LoanStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    RETURNED;

    private static final Map<LoanStatus, Set<LoanStatus>> VALID_TRANSITIONS = Map.of(
            PENDING, Set.of(APPROVED, REJECTED, CANCELLED),
            APPROVED, Set.of(RETURNED)
    );

    public boolean canTransitionTo(LoanStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public static LoanStatus parseStatus(String status) {
        try {
            return LoanStatus.valueOf(status);
        } catch (IllegalArgumentException _) {
            throw InvalidFieldValueException.invalidEnum("status", status);
        }
    }
}
