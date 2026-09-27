package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community.dto.UpdateObjectStatusRequest;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.CommunityClientPort;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.ObjectFact;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ServiceUnavailableException;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Implementación real de {@link CommunityClientPort} sobre {@link CommunityRestClient}.
 * Activa en por defecto en todos los perfiles; en `%test` la sustituye la alternativa
 * CDI {@code CommunityStubAdapter} (fixtures fijos, sin red) para que la suite REST no
 * dependa de un `community-service` real corriendo.
 * Tolerancia a fallos aplicada aquí, nunca en el dominio: un 404 ("no existe") es un
 * resultado de negocio normal y no dispara `@Fallback` — solo una caída real (timeout,
 * conexión rechazada, 5xx) lo hace, tras agotar los reintentos, traduciéndose siempre a
 * {@link ServiceUnavailableException}, nunca a la excepción técnica cruda.
 * Cada fallback recibe la causa como último parámetro (extensión de
 * SmallRye Fault Tolerance) y la registra como WARN — el 503 que ve el
 * cliente es genérico, la causa real (timeout, conexión rechazada, 5xx)
 * queda en el log con el mismo `requestId`.
 */
@ApplicationScoped
public class CommunityClientAdapter implements CommunityClientPort {

    private static final Logger LOG = Logger.getLogger(CommunityClientAdapter.class);

    private final CommunityRestClient communityRestClient;

    @Inject
    public CommunityClientAdapter(@RestClient CommunityRestClient communityRestClient) {
        this.communityRestClient = communityRestClient;
    }

    @Override
    @Timeout(2000)
    @Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Fallback(fallbackMethod = "existsUserFallback")
    public Uni<Boolean> existsUser(Long userId) {
        return communityRestClient.getUser(userId)
                .onItem().transform(_ -> true)
                .onFailure(NotFoundException.class).recoverWithItem(false);
    }

    Uni<Boolean> existsUserFallback(Long userId, Throwable cause) {
        logUnreachable("consultar el usuario " + userId, cause);
        return Uni.createFrom().failure(ServiceUnavailableException.communityUnreachable());
    }

    @Override
    @Timeout(2000)
    @Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Fallback(fallbackMethod = "fetchObjectFactsFallback")
    public Uni<List<ObjectFact>> fetchObjectFacts(List<Long> objectIds) {
        List<Uni<ObjectFact>> calls = objectIds.stream().map(this::fetchOneObjectFact).toList();
        return Uni.join().all(calls).andCollectFailures()
                .onItem().transform(facts -> facts.stream().filter(Objects::nonNull).toList());
    }

    Uni<List<ObjectFact>> fetchObjectFactsFallback(List<Long> objectIds, Throwable cause) {
        logUnreachable("consultar los objetos " + objectIds, cause);
        return Uni.createFrom().failure(ServiceUnavailableException.communityUnreachable());
    }

    private Uni<ObjectFact> fetchOneObjectFact(Long objectId) {
        return communityRestClient.getObject(objectId)
                .onItem().transform(response -> new ObjectFact(
                        response.id(), response.ownerId(), !"AVAILABLE".equals(response.status())))
                .onFailure(NotFoundException.class).recoverWithItem((ObjectFact) null);
    }

    @Override
    @Timeout(2000)
    @Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Fallback(fallbackMethod = "reserveObjectFallback")
    public Uni<Void> reserveObject(Long objectId) {
        return communityRestClient.patchObjectStatus(objectId, new UpdateObjectStatusRequest("RESERVED"))
                .onItem().transform(_ -> null);
    }

    Uni<Void> reserveObjectFallback(Long objectId, Throwable cause) {
        logUnreachable("reservar el objeto " + objectId, cause);
        return Uni.createFrom().failure(ServiceUnavailableException.communityUnreachable());
    }

    @Override
    @Timeout(2000)
    @Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Fallback(fallbackMethod = "releaseObjectFallback")
    public Uni<Void> releaseObject(Long objectId) {
        return communityRestClient.patchObjectStatus(objectId, new UpdateObjectStatusRequest("AVAILABLE"))
                .onItem().transform(_ -> null);
    }

    Uni<Void> releaseObjectFallback(Long objectId, Throwable cause) {
        logUnreachable("liberar el objeto " + objectId, cause);
        return Uni.createFrom().failure(ServiceUnavailableException.communityUnreachable());
    }

    private void logUnreachable(String action, Throwable cause) {
        LOG.warnf("community-service no disponible al %s tras agotar reintentos: %s", action, cause.toString());
    }
}
