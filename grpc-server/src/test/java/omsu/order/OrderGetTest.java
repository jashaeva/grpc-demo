package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import io.qameta.allure.Description;
import omsu.BaseSpringTest;
import omsu.BaseTest;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.grpc.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.OrderTestDataFactory.createOrder;
import static omsu.utils.DataUtils.randomUsername;
import static omsu.utils.TimestampAssertions.assertEqualsWithDefaultTolerance;
import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderGetTest extends BaseSpringTest {
    private String testOrderId;
    private Instant testCreatedAt;
    private String username = randomUsername();

    @BeforeEach
    void createTestOrder(){
        step("Setup: Create test order item for updating",
                () -> {
                    testCreatedAt = Instant.now().minus(1, ChronoUnit.DAYS);
                    OrderData order = createOrder(username, OrderStatus.PENDING, testCreatedAt);
                    IdMessage created = orderBlockingStub.createOrder(order);
                    testOrderId = created.getId();
                    attachText("UUID created: ", testOrderId);
                });
    }

    @Test
    @DisplayName("TC-OMGe01: get order item by id")
    @Description("Positive case")
    void getOrderById() throws InvalidProtocolBufferException {
        step("Get order by is...", ()->{
            OrderDataWithId response = orderBlockingStub.getOrderById(
                    IdMessage.newBuilder().setId(testOrderId).build()
            );
            attachText("Got response: ", jsonPrinter.print(response));
            step("Assertions...", ()->{
                assertEquals(testOrderId, response.getId());
                assertEquals(username, response.getUser());
                assertEquals(OrderStatus.PENDING, response.getStatus());
                assertEqualsWithDefaultTolerance(instantToProto(testCreatedAt), response.getCreatedAt());
            });
        });
    }

    @Disabled
    @Test
    @DisplayName("TC-OMGe02: get order with additional info")
    @Description("Positive case of getting order with additional info")
    void getOrderInfo() {
    }
}