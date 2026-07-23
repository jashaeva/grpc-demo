package omsu.kafka;

import io.qameta.allure.Description;
import io.restassured.response.Response;
import omsu.BaseTest;
import omsu.dto.LogEvent;
import omsu.grpc.IdMessage;
import omsu.grpc.InventoryMessage;
import omsu.model.IdDTO;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.utils.DataUtils.randomInventory;
import static omsu.utils.DataUtils.randomQuantity;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

//todo

/*
1. добавить тесты на исключения все
2. на исключения от валидации

проверить что сообщение правильное
 */

class KafkaLogConsumerTest extends BaseTest {
    private final Logger log = LoggerFactory.getLogger(KafkaLogConsumerTest.class);

    @Test
    @Tag("Kafka")
    @DisplayName("[Kafka][Neg] Сообщение о неуспехе не читается из топика")
    @Description("Если запрос на стороне grpc не выполнился, в топике сообщения не будет.")
    void testConsumerReceivesMessage_neg() {
        String id = UUID.randomUUID().toString();
        String request = IdMessage.newBuilder().setId(id).build().toString();
        Response inventory = api.getInventory(id);

        attachText("rest api returned: ", inventory.body().asPrettyString());

        LogEvent messageFromKafka = await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> kafkaLogListener.getMessage(request), nullValue());

        step("Check that message from kafka exist", () ->{
                assertNull(messageFromKafka);
                inventory.then()
                        .statusCode(500);
        });
    }

    @Test
    @Tag("Kafka")
    @DisplayName("[Kafka][Pos] Проверяем, что сообщение читается из топика")
    @Description("Плохой тест в плане структуры, так как использует реальное содержимое БД вместо того, чтобы подготовить их")
    //fixme поменять метод на create
    void testConsumerReceivesMessage() {
        String name = randomInventory();
        long count = randomQuantity();
        Response idResponse = api.createInventory(name, count);
        idResponse.then()
                .log().all()
                .statusCode(200)
                .body("id", notNullValue());

        String id = idResponse.as(IdDTO.class).id();

        log.info("*   inventory body {}", idResponse.as(IdDTO.class).toString());

        String request = InventoryMessage.newBuilder()
                .setName(name)
                .setCount(count)
                .build().toString();

        LogEvent messageFromKafka = await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> kafkaLogListener.getMessage(request), notNullValue());

        step("Check that message from kafka exist", () ->{
            assertNotNull(messageFromKafka);
            assertEquals("createInventory", messageFromKafka.method());
            assertEquals(request, messageFromKafka.request());
            assertThat(messageFromKafka.response(), notNullValue());
        });
    }
}
