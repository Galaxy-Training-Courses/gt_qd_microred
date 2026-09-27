package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class LoanResourceTest {

    private static final String AUTH_HEADER = "Bearer " + TestJwt.validToken();

    private long createLoan(long requesterId, long... objectIds) {
        StringBuilder items = new StringBuilder();
        for (long objectId : objectIds) {
            if (!items.isEmpty()) {
                items.append(",");
            }
            items.append("{\"objectId\":").append(objectId).append(",\"notes\":null}");
        }
        String body = """
                {"requesterId":%d,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"Prueba","items":[%s]}
                """.formatted(requesterId, items);
        return given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void create_success_persistsMasterAndItems() {
        long id = createLoan(2L, 1L, 2L);

        given().when().get("/api/loans/{id}", id)
                .then().statusCode(200)
                .body("id", equalTo((int) id))
                .body("requesterId", equalTo(2))
                .body("status", equalTo("PENDING"))
                .body("items.size()", equalTo(2))
                .body("items[0].objectId", notNullValue())
                .body("items[1].objectId", notNullValue());
    }

    @Test
    void create_itemsEmpty_returns400() {
        String body = """
                {"requesterId":2,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"Prueba","items":[]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(400);
    }

    @Test
    void create_blankReason_returns400() {
        String body = """
                {"requesterId":2,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"","items":[{"objectId":1,"notes":null}]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(400);
    }

    @Test
    void create_invalidDateRange_returns400() {
        String body = """
                {"requesterId":2,"requestedFrom":"2026-09-25","requestedUntil":"2026-09-20",
                 "reason":"Prueba","items":[{"objectId":1,"notes":null}]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(400);
    }

    @Test
    void create_requesterNotFound_returns404() {
        String body = """
                {"requesterId":999999999,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"Prueba","items":[{"objectId":1,"notes":null}]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(404).body("error", equalTo("NOT_FOUND"));
    }

    @Test
    void create_objectNotFound_returns404() {
        String body = """
                {"requesterId":2,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"Prueba","items":[{"objectId":999999998,"notes":null}]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(404).body("error", equalTo("NOT_FOUND"));
    }

    @Test
    void create_requesterIsOwner_returns422() {
        // El objeto 1 pertenece al usuario 1 (CommunityStubClient).
        String body = """
                {"requesterId":1,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"Prueba","items":[{"objectId":1,"notes":null}]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(422);
    }

    @Test
    void create_objectUnavailable_returns409() {
        // El objeto 4 está UNAVAILABLE en el stub; su owner es 2, así que
        // requesterId=1 no dispara la regla de propietario primero.
        String body = """
                {"requesterId":1,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
                 "reason":"Prueba","items":[{"objectId":4,"notes":null}]}
                """;
        given().contentType(ContentType.JSON).header("Authorization", AUTH_HEADER).body(body)
                .when().post("/api/loans")
                .then().statusCode(409).body("error", equalTo("CONFLICT"));
    }

    @Test
    void findById_notFound_returns404() {
        given().when().get("/api/loans/{id}", 999_999_999L)
                .then().statusCode(404).body("error", equalTo("NOT_FOUND"));
    }

    @Test
    void approve_success() {
        long id = createLoan(2L, 1L);

        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", id)
                .then().statusCode(200).body("status", equalTo("APPROVED"));
    }

    @Test
    void approve_alreadyApproved_returns409() {
        long id = createLoan(2L, 1L);
        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", id).then().statusCode(200);

        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", id)
                .then().statusCode(409).body("error", equalTo("CONFLICT"));
    }

    @Test
    void approve_notFound_returns404() {
        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", 999_999_997L).then().statusCode(404);
    }

    @Test
    void reject_success() {
        long id = createLoan(2L, 1L);

        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/reject", id)
                .then().statusCode(200).body("status", equalTo("REJECTED"));
    }

    @Test
    void reject_afterApprove_returns409() {
        long id = createLoan(2L, 1L);
        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", id).then().statusCode(200);

        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/reject", id).then().statusCode(409);
    }

    @Test
    void cancel_success() {
        long id = createLoan(2L, 1L);

        given().when().patch("/api/loans/{id}/cancel", id)
                .then().statusCode(200).body("status", equalTo("CANCELLED"));
    }

    @Test
    void cancel_afterApprove_returns409() {
        long id = createLoan(2L, 1L);
        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", id).then().statusCode(200);

        given().when().patch("/api/loans/{id}/cancel", id).then().statusCode(409);
    }

    @Test
    void returnLoan_success() {
        long id = createLoan(2L, 1L);
        given().header("Authorization", AUTH_HEADER).when().patch("/api/loans/{id}/approve", id).then().statusCode(200);

        given().when().patch("/api/loans/{id}/return", id)
                .then().statusCode(200).body("status", equalTo("RETURNED"));
    }

    @Test
    void returnLoan_stillPending_returns409() {
        long id = createLoan(2L, 1L);

        given().when().patch("/api/loans/{id}/return", id)
                .then().statusCode(409).body("error", equalTo("CONFLICT"));
    }

    @Test
    void list_isPaginated() {
        given().when().get("/api/loans?page=0&size=5")
                .then().statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(5))
                .body("content.size()", org.hamcrest.Matchers.lessThanOrEqualTo(5))
                .body("totalElements", notNullValue())
                .body("totalPages", notNullValue());
    }

    @Test
    void list_filterByStatusAndRequester() {
        createLoan(1L, 3L);

        given().when().get("/api/loans?status=PENDING&requesterId=1")
                .then().statusCode(200)
                .body("totalElements", greaterThanOrEqualTo(1))
                .body("content.status", everyItem(equalTo("PENDING")))
                .body("content.requesterId", everyItem(equalTo(1)));
    }

    @Test
    void list_sizeOver100_returns400() {
        given().when().get("/api/loans?page=0&size=101").then().statusCode(400);
    }
}
