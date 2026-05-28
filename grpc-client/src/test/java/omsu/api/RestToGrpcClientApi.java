package omsu.api;

import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

public class RestToGrpcClientApi {
    private int port;

    public RestToGrpcClientApi(final int port) {
        this.port = port;
    }

    public  Response getInventory (String id) {
         return given()
             .port(this.port)
             .contentType("application/json")
             .pathParam("id", id)
        .when()
            .get("/api/inventory/{id}");
    }
}
