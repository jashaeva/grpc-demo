package omsu.rest;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import omsu.BaseTest;
import omsu.api.RestToGrpcClientApi;
import omsu.model.IdDTO;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static omsu.utils.DataUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("REST client tests")
@Feature("Check CRUD methods for inventory")
@Tag("Rest-assured")
public class RestControllerTest extends BaseTest {
    private final Logger log = LoggerFactory.getLogger(RestControllerTest.class);

    @Test
    @DisplayName("[REST][Pos]Check that get api is working correctly")
    @Description("Просто проверка того, что запрос написан правильно и работает")
    void get_inventory_pos(){
        String name = randomInventory();
        long count = randomQuantity();
        Response inventory = api.createInventory(name, count);
        inventory.then()
                .log().all()
                .statusCode(200)
                .body("id", notNullValue());

        String id = inventory.as(IdDTO.class).id();
        Response response = api.getInventory(id);
        response.then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("name", equalTo(name));

        Number actualCount = response.then()
                .extract()
                .path("count");

        assertEquals(count, actualCount.longValue());
    }

    @Test
    @DisplayName("[REST][Pos]Check that post api is working correctly")
    @Description("Просто проверка того, что запрос написан правильно и работает")
    void create_inventory_pos(){
        Response inventory = api.createInventory(randomName(), randomQuantity());
        inventory.then()
                .statusCode(200)
                .body("id", notNullValue())
                .log().all();
    }
}
