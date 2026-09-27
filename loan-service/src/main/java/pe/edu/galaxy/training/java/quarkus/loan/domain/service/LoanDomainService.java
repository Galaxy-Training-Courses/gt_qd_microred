package pe.edu.galaxy.training.java.quarkus.loan.domain.service;

import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ConflictException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.NotFoundException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.RequesterIsOwnerException;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequest;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanRequestItem;
import pe.edu.galaxy.training.java.quarkus.loan.domain.model.LoanStatus;

import java.util.Map;
import java.util.Set;

/**
 * Reglas de negocio del enunciado, puras: sin JAX-RS, sin JPA, sin Quarkus,
 * sin puertos. Quien reúne los hechos que necesitan estas reglas (existe el
 * usuario, quién es el dueño de cada objeto, qué objetos están UNAVAILABLE)
 * es {@code LoanApplicationService}, consultando los puertos de salida —
 * aquí solo se decide con esos hechos ya resueltos.
 */
public class LoanDomainService {

    /**
     * Las 5 reglas de creación, mismo orden que tenía LoanService (para que,
     * ante varias reglas violadas a la vez, el código de error sea determinista):
     * fechas, existencia del solicitante, existencia y propietario de cada ítem,
     * disponibilidad de cada ítem.
     */
    public void validateCreation(LoanRequest candidate, boolean requesterExists, Map<Long, Long> objectOwners,
                                 Set<Long> unavailableObjectIds) {
        validateDateRange(candidate);
        validateRequesterExistence(candidate, requesterExists);
        validateObjects(candidate, objectOwners, unavailableObjectIds);
    }

    private static void validateDateRange(LoanRequest candidate) {
        if (!candidate.requestedFrom().isBefore(candidate.requestedUntil())) {
            throw InvalidFieldValueException.invalidDateRange(
                    candidate.requestedFrom().toString(), candidate.requestedUntil().toString());
        }
    }

    private static void validateRequesterExistence(LoanRequest candidate, boolean requesterExists) {
        if (!requesterExists) {
            throw NotFoundException.requesterNotFound(candidate.requesterId());
        }
    }

    private static void validateObjects(LoanRequest candidate, Map<Long, Long> objectOwners, Set<Long> unavailableObjectIds) {
        for (LoanRequestItem item : candidate.items()) {
            Long ownerId = objectOwners.get(item.objectId());
            if (ownerId == null) {
                throw NotFoundException.objectNotFound(item.objectId());
            }
            if (ownerId.equals(candidate.requesterId())) {
                throw new RequesterIsOwnerException(candidate.requesterId(), item.objectId());
            }
        }
        for (LoanRequestItem item : candidate.items()) {
            if (unavailableObjectIds.contains(item.objectId())) {
                throw ConflictException.objectUnavailable(item.objectId());
            }
        }
    }

    /** Las 4 transiciones. */
    public void validateTransition(LoanStatus current, LoanStatus target) {
        if (!current.canTransitionTo(target)) {
            throw ConflictException.invalidTransition(current, target);
        }
    }
}
