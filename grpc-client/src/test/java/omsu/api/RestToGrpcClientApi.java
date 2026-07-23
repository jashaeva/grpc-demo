package omsu.api;

import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import omsu.grpc.InventoryData;
import omsu.grpc.InventoryMessage;
import omsu.model.Inventory;

import static io.restassured.RestAssured.*;
import static io.restassured.config.JsonConfig.jsonConfig;

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

    public  Response createInventory (String name, long count) {
        return given()
//                .config(RestAssured.config().jsonConfig(jsonConfig()
//                        .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL)))
                .port(this.port)
                .contentType("application/json")
                .body(new Inventory(null, name, count))
                .when()
                .post("/api/inventory");
    }
}
