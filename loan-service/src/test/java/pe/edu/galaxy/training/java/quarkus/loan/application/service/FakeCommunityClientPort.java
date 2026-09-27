package pe.edu.galaxy.training.java.quarkus.loan.application.service;

import io.smallrye.mutiny.Uni;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.CommunityClientPort;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.ObjectFact;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FakeCommunityClientPort implements CommunityClientPort {

    private static final Set<Long> EXISTING_USERS = Set.of(1L, 2L);

    private static final Map<Long, Long> OBJECT_OWNERS = Map.of(
            1L, 1L,
            2L, 1L,
            3L, 2L,
            4L, 2L
    );

    private static final Set<Long> UNAVAILABLE_OBJECTS = Set.of(4L);

    final Set<Long> reservedObjectIds = new HashSet<>();
    final Set<Long> releasedObjectIds = new HashSet<>();

    @Override
    public Uni<Boolean> existsUser(Long userId) {
        return Uni.createFrom().item(EXISTING_USERS.contains(userId));
    }

    @Override
    public Uni<List<ObjectFact>> fetchObjectFacts(List<Long> objectIds) {
        List<ObjectFact> facts = objectIds.stream()
                .filter(OBJECT_OWNERS::containsKey)
                .map(objectId -> new ObjectFact(
                        objectId, OBJECT_OWNERS.get(objectId), UNAVAILABLE_OBJECTS.contains(objectId)))
                .toList();
        return Uni.createFrom().item(facts);
    }

    @Override
    public Uni<Void> reserveObject(Long objectId) {
        reservedObjectIds.add(objectId);
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Void> releaseObject(Long objectId) {
        releasedObjectIds.add(objectId);
        return Uni.createFrom().nullItem();
    }
}
