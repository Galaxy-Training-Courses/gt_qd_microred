package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.exception;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.RequesterIsOwnerException;

@Provider
public class RequesterIsOwnerExceptionMapper implements ExceptionMapper<RequesterIsOwnerException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(RequesterIsOwnerException exception) {
        ErrorResponse body = ErrorResponse.of(
                422,
                "UNPROCESSABLE_ENTITY",
                exception.getMessage(),
                requestContext.getUriInfo().getPath()
        );
        return Response.status(422, "Unprocessable Entity").entity(body).build();
    }
}
