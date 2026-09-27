package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.exception;

import java.time.Instant;
import java.util.List;

/**
 * Formato uniforme de error
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> violations
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }

    public static ErrorResponse ofViolations(int status, String error, String message, String path,
                                             List<String> violations) {
        return new ErrorResponse(Instant.now(), status, error, message, path, violations);
    }
}
