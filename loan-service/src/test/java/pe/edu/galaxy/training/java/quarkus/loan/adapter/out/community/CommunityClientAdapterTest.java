package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.quarkus.loan.application.port.out.CommunityClientPort;
import pe.edu.galaxy.training.java.quarkus.loan.domain.exception.ServiceUnavailableException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(CommunityClientAdapterTest.CommunityDownProfile.class)
class CommunityClientAdapterTest {

    @Inject
    CommunityClientPort communityClientPort;

    @Test
    void existsUser_communityUnreachable_failsControlledUnderFiveSeconds() {
        Instant start = Instant.now();

        assertThrows(ServiceUnavailableException.class,
                () -> communityClientPort.existsUser(1L).await().indefinitely());

        Duration elapsed = Duration.between(start, Instant.now());
        assertTrue(elapsed.toSeconds() < 5, "Debia responder en menos de 5s, tardo " + elapsed);
    }

    public static class CommunityDownProfile implements QuarkusTestProfile {
        @Override
        public String getConfigProfile() {
            return "community-down-test";
        }

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.rest-client.community-api.url", "http://localhost:1");
        }
    }
}
