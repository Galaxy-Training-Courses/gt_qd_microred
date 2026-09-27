package pe.edu.galaxy.training.java.quarkus.community.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class UserResourceTest {

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private long createUser(String neighborhood) {
        String suffix = unique();
        String body = """
                {"name":"Ana Quispe %s","email":"ana%s@microred.pe","neighborhood":"%s"}
                """.formatted(suffix, suffix, neighborhood);
        return given().contentType(ContentType.JSON).body(body)
                .when().post("/api/users")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void createUser_success() {
        String suffix = unique();
        String body = """
                {"name":"Ana Quispe","email":"ana%s@microred.pe","neighborhood":"San Borja"}
                """.formatted(suffix);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/users")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("id", notNullValue())
                .body("name", equalTo("Ana Quispe"))
                .body("status", equalTo("ACTIVE"))
                .body("createdAt", notNullValue());
    }

    @Test
    void createUser_invalidPayload_returns400() {
        String body = """
                {"name":"","email":"no-es-un-email","neighborhood":""}
                """;

        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/users")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("violations.size()", org.hamcrest.Matchers.greaterThan(0));
    }

    @Test
    void createUser_duplicateEmail_returns409() {
        String suffix = unique();
        String email = "dup" + suffix + "@microred.pe";
        String body = """
                {"name":"Primero","email":"%s","neighborhood":"Surco"}
                """.formatted(email);

        given().contentType(ContentType.JSON).body(body).when().post("/api/users").then().statusCode(201);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/users")
                .then()
                .statusCode(409)
                .body("error", equalTo("CONFLICT"));
    }

    @Test
    void findById_success() {
        long id = createUser("Miraflores-" + unique());

        given().when().get("/api/users/{id}", id)
                .then().statusCode(200).body("id", equalTo((int) id));
    }

    @Test
    void findById_notFound_returns404() {
        given().when().get("/api/users/{id}", 999_999_999L)
                .then().statusCode(404).body("error", equalTo("NOT_FOUND"));
    }

    @Test
    void patch_success() {
        long id = createUser("Barranco-" + unique());

        given().contentType(ContentType.JSON).body("""
                {"neighborhood":"La Molina"}
                """)
                .when().patch("/api/users/{id}", id)
                .then().statusCode(200).body("neighborhood", equalTo("La Molina"));
    }

    @Test
    void patch_invalidStatus_returns400() {
        long id = createUser("Chorrillos-" + unique());

        given().contentType(ContentType.JSON).body("""
                {"status":"NO_EXISTE"}
                """)
                .when().patch("/api/users/{id}", id)
                .then().statusCode(400);
    }

    @Test
    void patch_notFound_returns404() {
        given().contentType(ContentType.JSON).body("""
                {"neighborhood":"X"}
                """)
                .when().patch("/api/users/{id}", 999_999_998L)
                .then().statusCode(404);
    }

    @Test
    void delete_success() {
        long id = createUser("SanIsidro-" + unique());

        given().when().delete("/api/users/{id}", id).then().statusCode(204);
        given().when().get("/api/users/{id}", id).then().statusCode(404);
    }

    @Test
    void delete_notFound_returns404() {
        given().when().delete("/api/users/{id}", 999_999_997L).then().statusCode(404);
    }

    @Test
    void delete_withReservedObject_returns409() {
        long ownerId = createUser("Callao-" + unique());
        String category = "TEST-" + unique();
        String objectBody = """
                {"ownerId":%d,"name":"Carpa","description":"6 personas","category":"%s","condition":"GOOD"}
                """.formatted(ownerId, category);

        long objectId = given().contentType(ContentType.JSON).body(objectBody)
                .when().post("/api/objects")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        given().contentType(ContentType.JSON).body("""
                {"status":"RESERVED"}
                """)
                .when().patch("/api/objects/{id}", objectId)
                .then().statusCode(200);

        given().when().delete("/api/users/{id}", ownerId)
                .then().statusCode(409).body("error", equalTo("CONFLICT"));
    }

    @Test
    void list_isPaginated() {
        given().when().get("/api/users?page=0&size=5")
                .then().statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(5))
                .body("content.size()", org.hamcrest.Matchers.lessThanOrEqualTo(5))
                .body("totalElements", notNullValue())
                .body("totalPages", notNullValue());
    }

    @Test
    void objectsByOwner_hierarchicalEndpoint() {
        String suffix = unique();
        long ownerId = createUser("Lince-" + suffix);
        String category = "TEST-" + suffix;

        given().contentType(ContentType.JSON).body("""
                {"ownerId":%d,"name":"Proyector","description":"HDMI","category":"%s","condition":"GOOD"}
                """.formatted(ownerId, category))
                .when().post("/api/objects").then().statusCode(201);

        given().when().get("/api/users/{id}/objects?category={category}", ownerId, category)
                .then().statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].ownerId", equalTo((int) ownerId));
    }

    @Test
    void objectsByOwner_ownerNotFound_returns404() {
        given().when().get("/api/users/{id}/objects", 999_999_996L)
                .then().statusCode(404);
    }
}
