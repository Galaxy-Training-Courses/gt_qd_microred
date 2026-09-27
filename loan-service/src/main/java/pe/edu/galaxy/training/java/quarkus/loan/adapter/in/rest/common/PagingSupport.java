package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.common;

import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.InvalidFieldValueException;

/**
 * Validación de `page`/`size` para GET /api/loans (size > 100 -> 400).
 */
public final class PagingSupport {

    public static final int MAX_SIZE = 100;
    public static final int DEFAULT_SIZE = 10;

    private PagingSupport() {
    }

    public static void validate(int page, int size) {
        if (page < 0) {
            throw new InvalidFieldValueException("'page' can not be negative: " + page);
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new InvalidFieldValueException("'size' must be between 1 and " + MAX_SIZE + ": " + size);
        }
    }
}
