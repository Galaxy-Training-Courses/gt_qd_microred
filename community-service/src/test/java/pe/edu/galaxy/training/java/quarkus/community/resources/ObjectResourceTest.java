package pe.edu.galaxy.training.java.quarkus.community.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ObjectResourceTest {

    private String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private long createOwner() {
        String suffix = unique();
        String body = """
                {"name":"Propietario %s","email":"owner%s@microred.pe","neighborhood":"San Borja"}
                """.formatted(suffix, suffix);
        return given().contentType(ContentType.JSON).body(body)
                .when().post("/api/users")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createObject(long ownerId, String category, String condition) {
        String body = """
                {"ownerId":%d,"name":"Escalera","description":"4m plegable","category":"%s","condition":"%s"}
                """.formatted(ownerId, category, condition);
        return given().contentType(ContentType.JSON).body(body)
                .when().post("/api/objects")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void createObject_success() {
        long ownerId = createOwner();
        String category = "HERRAMIENTAS-" + unique();

        given().contentType(ContentType.JSON).body("""
                {"ownerId":%d,"name":"Taladro","description":"Inalámbrico","category":"%s","condition":"GOOD"}
                """.formatted(ownerId, category))
                .when().post("/api/objects")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("ownerId", equalTo((int) ownerId))
                .body("status", equalTo("AVAILABLE"))
                .body("category", equalTo(category));
    }

    @Test
    void createObject_ownerNotFound_returns404() {
        given().contentType(ContentType.JSON).body("""
                {"ownerId":999999995,"name":"Carpa","description":"6p","category":"CAMPING","condition":"GOOD"}
                """)
                .when().post("/api/objects")
                .then().statusCode(404).body("error", equalTo("NOT_FOUND"));
    }

    @Test
    void createObject_invalidPayload_returns400() {
        given().contentType(ContentType.JSON).body("""
                {"ownerId":1,"name":"","description":"","category":"","condition":""}
                """)
                .when().post("/api/objects")
                .then().statusCode(400);
    }

    @Test
    void findById_success() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "CAMPING-" + unique(), "NEW");

        given().when().get("/api/objects/{id}", id)
                .then().statusCode(200).body("id", equalTo((int) id));
    }

    @Test
    void findById_notFound_returns404() {
        given().when().get("/api/objects/{id}", 999_999_994L).then().statusCode(404);
    }

    @Test
    void replace_success() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "CAMPING-" + unique(), "NEW");

        given().contentType(ContentType.JSON).body("""
                {"name":"Carpa 6 personas","description":"Impermeable","category":"CAMPING","condition":"USED"}
                """)
                .when().put("/api/objects/{id}", id)
                .then().statusCode(200)
                .body("name", equalTo("Carpa 6 personas"))
                .body("condition", equalTo("USED"));
    }

    @Test
    void replace_notFound_returns404() {
        given().contentType(ContentType.JSON).body("""
                {"name":"X","description":"Y","category":"Z","condition":"NEW"}
                """)
                .when().put("/api/objects/{id}", 999_999_993L)
                .then().statusCode(404);
    }

    @Test
    void patchStatus_success() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "ELECTRONICA-" + unique(), "GOOD");

        given().contentType(ContentType.JSON).body("""
                {"status":"RESERVED"}
                """)
                .when().patch("/api/objects/{id}", id)
                .then().statusCode(200).body("status", equalTo("RESERVED"));
    }

    @Test
    void patchStatus_invalidValue_returns400() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "ELECTRONICA-" + unique(), "GOOD");

        given().contentType(ContentType.JSON).body("""
                {"status":"NO_EXISTE"}
                """)
                .when().patch("/api/objects/{id}", id)
                .then().statusCode(400);
    }

    @Test
    void delete_success() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "CAMPING-" + unique(), "NEW");

        given().when().delete("/api/objects/{id}", id).then().statusCode(204);
        given().when().get("/api/objects/{id}", id).then().statusCode(404);
    }

    @Test
    void delete_notFound_returns404() {
        given().when().delete("/api/objects/{id}", 999_999_992L).then().statusCode(404);
    }

    @Test
    void delete_reserved_returns409() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "CAMPING-" + unique(), "NEW");

        given().contentType(ContentType.JSON).body("""
                {"status":"RESERVED"}
                """)
                .when().patch("/api/objects/{id}", id).then().statusCode(200);

        given().when().delete("/api/objects/{id}", id)
                .then().statusCode(409).body("error", equalTo("CONFLICT"));
    }

    @Test
    void owner_success() {
        long ownerId = createOwner();
        long id = createObject(ownerId, "CAMPING-" + unique(), "NEW");

        given().when().get("/api/objects/{id}/owner", id)
                .then().statusCode(200).body("id", equalTo((int) ownerId));
    }

    @Test
    void owner_objectNotFound_returns404() {
        given().when().get("/api/objects/{id}/owner", 999_999_991L).then().statusCode(404);
    }

    @Test
    void list_filterByStatusAndPagination() {
        long ownerId = createOwner();
        String category = "FILTRO-" + unique();
        createObject(ownerId, category, "GOOD");
        createObject(ownerId, category, "GOOD");

        given().when().get("/api/objects?category={category}&status=AVAILABLE&page=0&size=10", category)
                .then().statusCode(200)
                .body("content.size()", equalTo(2))
                .body("totalElements", equalTo(2))
                .body("page", equalTo(0))
                .body("size", equalTo(10));
    }

    @Test
    void list_sizeOver100_returns400() {
        given().when().get("/api/objects?page=0&size=101")
                .then().statusCode(400);
    }

    @Test
    void list_invalidSortField_returns400() {
        given().when().get("/api/objects?sort=noExiste")
                .then().statusCode(400);
    }
}
