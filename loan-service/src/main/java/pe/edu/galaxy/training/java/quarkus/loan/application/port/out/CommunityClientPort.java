package pe.edu.galaxy.training.java.quarkus.loan.application.port.out;

import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Puerto de salida hacia community-service.
 * Se implementaba con {@code CommunityStubAdapter} (fixtures fijos, síncrono);
 * Luego se agregó {@code CommunityClientAdapter} (REST Client Reactive real) como
 * implementación de `%dev`/`%prod` — el stub se mantiene como `@Alternative` de
 * CDI, seleccionado solo en `%test`, para que la suite REST siga sin
 * depender de un `community-service` real corriendo.
 * Un 404 de community ("no existe") es un resultado de negocio normal y
 * nunca hace fallar el {@code Uni}: `existsUser` devuelve {@code false},
 * `fetchObjectFacts` simplemente omite ese id de la lista.
 * `LoanDomainService` ya sabe interpretar ambos casos. Solo una caída real
 * (timeout, conexión rechazada, 5xx) hace fallar el {@code Uni}, con
 * `ServiceUnavailableException` como resultado final tras agotar reintentos
 * (`@Fallback` en el adaptador).
 */
public interface CommunityClientPort {

    Uni<Boolean> existsUser(Long userId);

    Uni<List<ObjectFact>> fetchObjectFacts(List<Long> objectIds);

    Uni<Void> reserveObject(Long objectId);

    Uni<Void> releaseObject(Long objectId);
}
