package pe.edu.galaxy.training.java.quarkus.loan.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class LoanResourceSecurityTest {

    private static final String BODY = """
            {"requesterId":2,"requestedFrom":"2026-09-20","requestedUntil":"2026-09-25",
             "reason":"Prueba P4","items":[{"objectId":1,"notes":null}]}
            """;

    @Test
    void create_withoutToken_returns401() {
        given().contentType(ContentType.JSON).body(BODY)
                .when().post("/api/loans")
                .then().statusCode(401);
    }

    @Test
    void create_withValidToken_returns201() {
        given().contentType(ContentType.JSON).body(BODY)
                .header("Authorization", "Bearer " + TestJwt.validToken())
                .when().post("/api/loans")
                .then().statusCode(201);
    }

    @Test
    void approve_withoutToken_returns401() {
        long id = createLoanWithToken();

        given().when().patch("/api/loans/{id}/approve", id)
                .then().statusCode(401);
    }

    @Test
    void reject_withoutToken_returns401() {
        long id = createLoanWithToken();

        given().when().patch("/api/loans/{id}/reject", id)
                .then().statusCode(401);
    }

    @Test
    void list_withoutToken_staysPublic() {
        given().when().get("/api/loans")
                .then().statusCode(200);
    }

    @Test
    void cancel_withoutToken_staysPublic() {
        long id = createLoanWithToken();

        given().when().patch("/api/loans/{id}/cancel", id)
                .then().statusCode(200);
    }

    private long createLoanWithToken() {
        return given().contentType(ContentType.JSON).body(BODY)
                .header("Authorization", "Bearer " + TestJwt.validToken())
                .when().post("/api/loans")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
