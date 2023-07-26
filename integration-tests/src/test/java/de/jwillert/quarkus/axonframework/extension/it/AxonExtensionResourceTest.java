package de.jwillert.quarkus.axonframework.extension.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class AxonExtensionResourceTest {

    @Test
    public void sendCommand() throws InterruptedException {
        Thread.sleep(300);
        String id = given()
                .when().post("/axon-extension")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        Thread.sleep(300);
        given()
                .when().delete("/axon-extension/" + id)
                .then()
                .statusCode(204);

    }

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/axon-extension")
                .then()
                .statusCode(200)
                .body(is("Hello axon-extension"));
    }
}
