package pe.edu.galaxy.training.java.quarkus.community.exceptions.mapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import pe.edu.galaxy.training.java.quarkus.community.exceptions.ErrorResponse;

/**
 * Red de seguridad: cualquier excepción no capturada por un mapper más
 * específico termina aquí como 500 con ErrorResponse, nunca como un
 * stacktrace crudo.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Unhandled error in " + requestContext.getUriInfo().getPath(), exception);
        ErrorResponse body = ErrorResponse.of(
                500,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                requestContext.getUriInfo().getPath()
        );
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .build();
    }
}
