package pe.edu.galaxy.training.java.quarkus.community.exceptions.mapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ErrorResponse;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.conflict.ConflictException;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ConflictException exception) {
        ErrorResponse body = ErrorResponse.of(
                409,
                "CONFLICT",
                exception.getMessage(),
                requestContext.getUriInfo().getPath()
        );
        return Response
                .status(Response.Status.CONFLICT)
                .entity(body)
                .build();
    }
}
