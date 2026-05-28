package omsu;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import omsu.api.RestToGrpcClientApi;
import omsu.dto.LogEvent;
import omsu.grpc.IdMessage;
import omsu.kafka.KafkaLogListener;
import omsu.model.Inventory;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class KafkaLogConsumerTest {
    private final Logger log = LoggerFactory.getLogger(KafkaLogConsumerTest.class);
    RestToGrpcClientApi api;

    @Autowired
    KafkaLogListener kafkaLogListener;

    @LocalServerPort
    private int port;


    @BeforeEach
    void setUpApi() {
        api = new RestToGrpcClientApi(port);
    }

    @Test
    @Tag("Kafka")
    @DisplayName("[Kafka][Neg] Проверяем, что сообщение о неуспехе не читается из топика")
    @Description("Если запрос на стороне grpc не выполнился, в топике сообщения не будет.")
    void testConsumerReceivesMessage_neg() throws JsonProcessingException {
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
    void testConsumerReceivesMessage() throws JsonProcessingException, InterruptedException {
        String id ="2e692e34-5793-11f1-aa04-7ae9421208de";
        Response inventory = api.getInventory(id);

        log.info("*   inventory body {}", inventory.as(Inventory.class).toString());
        log.info("*** inventory body {}", inventory.body().as(Inventory.class).toString());

        String name = inventory.body().as(Inventory.class).name();
        String request = IdMessage.newBuilder().setId(id).build().toString();

        LogEvent messageFromKafka = await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> kafkaLogListener.getMessage(request), notNullValue());

        step("Check that message from kafka exist", () ->{
            assertNotNull(messageFromKafka);
            assertEquals("getInventory", messageFromKafka.method());
            assertEquals(request, messageFromKafka.request());
            assertThat(messageFromKafka.response(), containsString(name));
        });
    }

    @Disabled
    @Test
    @Tag("Rest-assured")
    @DisplayName("Check that rest-api is working correctly")
    @Description("Просто проверка того, что запрос написан правильно и работает")
    void testRestAssured(){
        String id ="2e692e34-5793-11f1-aa04-7ae9421208de";
        Response inventory = api.getInventory(id);
        inventory.then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("count", greaterThan(10))
                .body("name", equalTo("Pizza"))
                .log().all();
    }
}
