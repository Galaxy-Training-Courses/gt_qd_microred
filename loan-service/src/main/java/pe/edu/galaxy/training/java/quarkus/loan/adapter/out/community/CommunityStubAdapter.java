package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.CommunityClientPort;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.ObjectFact;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementación alternativa de {@link CommunityClientPort} con
 * fixtures fijos
 * Razón de negocio: las reglas de creación (solicitante existente,
 * solicitante distinto del propietario, objeto `UNAVAILABLE`) se deciden con
 * hechos que pertenecen a otro contexto, community-service. Para verificar
 * esas reglas de forma determinista hace falta poder sustituir esa fuente de
 * hechos por otra que devuelva siempre los mismos; ese es exactamente el caso
 * de dos implementaciones del mismo tipo CDI ({@link CommunityClientAdapter}
 * es la real, esta es la alternativa). `@Alternative` lo expresa
 * como una sustitución explícita, con selección declarada en
 * configuración (`%test.quarkus.arc.selected-alternatives`), sin
 * `@Priority` — así nunca queda activa por accidente en `%dev`/`%prod`.
 * Los fixtures son deterministas y coherentes con
 * community-service/src/main/resources/db/seed/V2__seed_dev.sql:
 * usuarios 1 y 2 existen; objetos 1 y 2 pertenecen al usuario 1, el objeto 3
 * al usuario 2, los tres AVAILABLE. El objeto 4 está marcado UNAVAILABLE,
 * necesario para poder probar esa regla de creación — no muta estado:
 * `reserveObject`/`releaseObject` no escriben nada de verdad, la fuente de
 * verdad del objeto vive en community_db, no aquí.
 */
@ApplicationScoped
@Alternative
public class CommunityStubAdapter implements CommunityClientPort {

    private static final Set<Long> EXISTING_USERS = Set.of(1L, 2L);

    private static final Map<Long, Long> OBJECT_OWNERS = Map.of(
            1L, 1L,
            2L, 1L,
            3L, 2L,
            4L, 2L
    );

    private static final Set<Long> UNAVAILABLE_OBJECTS = Set.of(4L);

    @Override
    public Uni<Boolean> existsUser(Long userId) {
        return Uni.createFrom().item(EXISTING_USERS.contains(userId));
    }

    @Override
    public Uni<List<ObjectFact>> fetchObjectFacts(List<Long> objectIds) {
        List<ObjectFact> facts = objectIds.stream()
                .filter(OBJECT_OWNERS::containsKey)
                .map(objectId -> new ObjectFact(
                        objectId,
                        OBJECT_OWNERS.get(objectId),
                        UNAVAILABLE_OBJECTS.contains(objectId))
                )
                .toList();
        return Uni.createFrom().item(facts);
    }

    @Override
    public Uni<Void> reserveObject(Long objectId) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Void> releaseObject(Long objectId) {
        return Uni.createFrom().nullItem();
    }
}
