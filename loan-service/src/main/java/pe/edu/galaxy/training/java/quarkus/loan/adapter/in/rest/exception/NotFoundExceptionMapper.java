package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.exception;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.NotFoundException;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(NotFoundException exception) {
        ErrorResponse body = ErrorResponse.of(
                404,
                "NOT_FOUND",
                exception.getMessage(),
                requestContext.getUriInfo().getPath()
        );
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}
