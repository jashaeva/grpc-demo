package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import io.qameta.allure.Description;
import omsu.BaseSpringTest;
import omsu.BaseTest;
import omsu.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.InventoryTestDataFactory.createInventoryMessage;
import static omsu.steps.OrderTestDataFactory.createOrder;
import static omsu.steps.OrderTestDataFactory.createOrderItem;
import static omsu.utils.DataUtils.*;
import static omsu.utils.TimestampAssertions.assertEqualsWithDefaultTolerance;
import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderGetTest extends BaseSpringTest {
    private String testOrderId;
    private String testInvId;
    private Instant testCreatedAt;
    private String username = randomUsername();

    @BeforeEach
    void createTestOrder(){
        step("Setup: Create test order item to get info",
                () -> {
                    testCreatedAt = Instant.now().minus(1, ChronoUnit.DAYS);
                    OrderData order = createOrder(username, OrderStatus.PENDING, testCreatedAt);
                    IdMessage created = orderBlockingStub.createOrder(order);
                    testOrderId = created.getId();
                    attachText("UUID created: ", testOrderId);
                });
        step("Setup: Create test inventory item to get order info",
                () -> {
                    InventoryMessage inventory = createInventoryMessage(
                            randomInventory(), 10L);
                    IdMessage created = inventoryBlockingStub.createInventory(inventory);
                    testInvId = created.getId();
                    attachText("UUID created: ", testInvId);
                });
        step("Add relation order-inventory", ()->{
            OrderItem orderItem = createOrderItem(testOrderId, testInvId, 1);
            BoolMessage result = orderBlockingStub.addInventory(orderItem);
            assertTrue(result.getResult());
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

    @Test
    @DisplayName("TC-OMGe02: get order with additional info")
    @Description("Positive case of getting order with additional info")
    void getOrderInfo_pos() {
        step("Get order with info", () -> {
            IdMessage idMessage = IdMessage.newBuilder().setId(testOrderId).build();
            OrderInfo orderInfo = orderBlockingStub.getOrderInfo(idMessage);
            attachText("orderInfo", jsonPrinter.print(orderInfo));

            step("Assertions", ()->{
                assertEquals(testOrderId, orderInfo.getId());
                assertEquals(username, orderInfo.getUser());
                assertEqualsWithDefaultTolerance(instantToProto(testCreatedAt), orderInfo.getCreatedAt());
                assertEquals(1, orderInfo.getInvCount());
            });
        });
    }
}