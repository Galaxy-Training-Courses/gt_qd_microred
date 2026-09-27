package pe.edu.galaxy.training.java.quarkus.community.exceptions.mapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ErrorResponse;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.InvalidFieldValueException;

@Provider
public class InvalidFieldValueExceptionMapper implements ExceptionMapper<InvalidFieldValueException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(InvalidFieldValueException exception) {
        ErrorResponse body = ErrorResponse.of(
                400,
                "BAD_REQUEST",
                exception.getMessage(),
                requestContext.getUriInfo().getPath()
        );
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(body)
                .build();
    }
}
