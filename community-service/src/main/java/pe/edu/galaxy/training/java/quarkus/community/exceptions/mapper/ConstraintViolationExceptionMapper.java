package pe.edu.galaxy.training.java.quarkus.community.exceptions.mapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ErrorResponse;

import java.util.List;

/**
 * Bean Validation sobre los @NotBlank/@Email/@Size de los DTOs Request:
 * el 400 de validación rellena `violations`).
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> violations = exception.getConstraintViolations().stream()
                .map(this::describe)
                .toList();
        ErrorResponse body = ErrorResponse.ofViolations(
                400,
                "BAD_REQUEST",
                "Invalid petition",
                requestContext.getUriInfo().getPath(),
                violations
        );
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(body)
                .build();
    }

    private String describe(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
