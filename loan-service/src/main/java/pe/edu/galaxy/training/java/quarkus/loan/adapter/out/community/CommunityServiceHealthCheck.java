package pe.edu.galaxy.training.java.quarkus.loan.adapter.out.community;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;

/**
 * Readiness propio, "loan-service comprobando alcanzabilidad de community-service"
 */
@Readiness
@ApplicationScoped
public class CommunityServiceHealthCheck implements HealthCheck {

    @RestClient
    CommunityHealthRestClient communityHealthRestClient;

    @Override
    public HealthCheckResponse call() {
        try {
            Response response = communityHealthRestClient
                    .checkLiveness()
                    .await()
                    .atMost(Duration.ofSeconds(2));
            return HealthCheckResponse
                    .named("community-service")
                    .status(response.getStatus() == 200)
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.down("community-service");
        }
    }
}
