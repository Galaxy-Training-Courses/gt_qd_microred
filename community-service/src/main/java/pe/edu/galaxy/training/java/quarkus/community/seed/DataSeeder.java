package pe.edu.galaxy.training.java.quarkus.community.seed;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectCondition;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectEntity;
import pe.edu.galaxy.training.java.quarkus.community.entities.object.ObjectStatus;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserEntity;
import pe.edu.galaxy.training.java.quarkus.community.entities.user.UserStatus;
import pe.edu.galaxy.training.java.quarkus.community.repository.ObjectRepository;
import pe.edu.galaxy.training.java.quarkus.community.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Volumen para que la paginación de /api/objects y /api/users se vea en %dev.
 * Solo %dev: en %test el volumen no aporta nada y solo agregaría ruido a las aserciones.
 * Idempotente: db/seed/V2__seed_dev.sql ya deja {@value #DETERMINISTIC_USERS}
 * usuarios fijos en cada arranque (Flyway corre antes que este observer), así
 * que ese conteo no sirve como marca de "ya sembrado" — se compara contra esa
 * línea base para no sembrar el volumen dos veces en un reinicio.
 */
@ApplicationScoped
@RequiredArgsConstructor
@IfBuildProfile("dev")
public class DataSeeder {

    private static final int DETERMINISTIC_USERS = 2;
    private static final int USERS_TO_SEED = 10;
    private static final int OBJECTS_TO_SEED = 20;
    private static final List<String> CATEGORIES = List.of("HERRAMIENTAS", "ELECTRONICA", "CAMPING", "DEPORTES", "COCINA");

    private final UserRepository userRepository;
    private final ObjectRepository objectRepository;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (userRepository.count() > DETERMINISTIC_USERS) {
            return;
        }
        Faker faker = new Faker();
        List<UserEntity> users = seedUsers(faker);
        seedObjects(faker, users);
    }

    private List<UserEntity> seedUsers(Faker faker) {
        List<UserEntity> users = new ArrayList<>();
        for (int i = 0; i < USERS_TO_SEED; i++) {
            UserEntity user = new UserEntity();
            user.name = faker.name().fullName();
            user.email = "%s.%d@example.com".formatted(faker.credentials().username(), i);
            user.neighborhood = faker.address().cityName();
            user.status = UserStatus.ACTIVE;
            user.createdAt = Instant.now();
            userRepository.persist(user);
            users.add(user);
        }
        return users;
    }

    private void seedObjects(Faker faker, List<UserEntity> owners) {
        ObjectCondition[] conditions = ObjectCondition.values();
        for (int i = 0; i < OBJECTS_TO_SEED; i++) {
            ObjectEntity object = new ObjectEntity();
            object.owner = owners.get(i % owners.size());
            object.name = faker.commerce().productName();
            object.description = faker.lorem().sentence();
            object.category = CATEGORIES.get(i % CATEGORIES.size());
            object.condition = conditions[i % conditions.length];
            object.status = ObjectStatus.AVAILABLE;
            object.createdAt = Instant.now();
            objectRepository.persist(object);
        }
    }
}
