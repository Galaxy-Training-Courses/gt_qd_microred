package pe.edu.galaxy.training.java.quarkus.loan.domain.exception;

/**
 * `community-service` no respondió tras agotar reintentos. La lanza el método
 * `@Fallback` del adaptador de salida ({@code adapter/out/community/CommunityClientAdapter})
 * cuando el {@code Uni} falla por timeout/conexión rechazada/5xx — nunca por
 * un 404 (eso es {@link NotFoundException}, un resultado de negocio normal,
 * no una caída). Misma forma que las demás: clase simple, sin anotaciones de
 * framework, factory estática.
 */
public class ServiceUnavailableException extends RuntimeException {
    private ServiceUnavailableException(String message) {
        super(message);
    }

    public static ServiceUnavailableException communityUnreachable() {
        return new ServiceUnavailableException(
                "community-service is not responding; try again in a few seconds."
        );
    }
}
