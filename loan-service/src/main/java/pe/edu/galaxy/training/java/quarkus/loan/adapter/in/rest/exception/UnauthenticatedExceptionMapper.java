package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest.exception;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Un {@code @Authenticated} fallido en create/approve/reject  lanza
 * {@link AuthenticationFailedException}, que
 * Quarkus REST ya mapea a 401 por defecto pero sin el sobre {@link ErrorResponse}
 * que ya es consistente en el resto de códigos de error (400/404/409/422/503).
 * Este mapper (un @Provider de la aplicación) reemplaza al mapper por defecto de
 * la extensión.
 */
@Provider
public class UnauthenticatedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        ErrorResponse body = ErrorResponse.of(
                401,
                "UNAUTHORIZED",
                "Se requiere un token JWT valido para esta operacion.",
                requestContext.getUriInfo().getPath()
        );
        return Response.status(Response.Status.UNAUTHORIZED).entity(body).build();
    }
}
