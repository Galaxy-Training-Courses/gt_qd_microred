package pe.edu.galaxy.training.java.quarkus.community.exceptions.mapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ErrorResponse;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ObjectNotFoundException;

@Provider
public class ObjectNotFoundExceptionMapper implements ExceptionMapper<ObjectNotFoundException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ObjectNotFoundException exception) {
        ErrorResponse body = ErrorResponse.of(
                404,
                "NOT_FOUND",
                exception.getMessage(),
                requestContext.getUriInfo().getPath()
        );
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(body)
                .build();
    }
}
