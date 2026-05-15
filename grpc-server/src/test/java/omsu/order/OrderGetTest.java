package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import io.qameta.allure.Description;
import omsu.BaseTestcontainersTest;
import omsu.grpc.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.utils.DataUtils.*;
import static omsu.utils.TimestampAssertions.assertEqualsWithDefaultTolerance;
import static org.junit.jupiter.api.Assertions.*;

class OrderGetTest extends BaseTestcontainersTest {
    private final String username = randomUsername();
    private final Logger log = LoggerFactory.getLogger(OrderGetTest.class);

    @Test
    @DisplayName("TC-OMGe01: get order item by id")
    @Description("Positive case")
    void getOrderById() throws InvalidProtocolBufferException {
        OrderDataWithId order = orderGrpcSteps.createOrderEntity();
        String testOrderId = order.getId();

        step("Get order by id " + testOrderId, ()->{
            OrderDataWithId response = orderBlockingStub.getOrderById(
                    IdMessage.newBuilder().setId(testOrderId).build()
            );
            attachText("Got response: ", jsonPrinter.print(response));
            step("Assertions", ()->{
                assertEquals(testOrderId, response.getId());
                assertEquals(order.getUser(), response.getUser());
                assertEquals(order.getStatus(), response.getStatus());
                assertEqualsWithDefaultTolerance(order.getCreatedAt(), response.getCreatedAt());
            });
        });
    }

    @Test
    @DisplayName("TC-OMGe02: get order with additional info")
    @Description("Positive case of getting order with additional info")
    void getOrderInfo_pos() throws InvalidProtocolBufferException {
        OrderDataWithId order = orderGrpcSteps.createOrderEntity();
        String testOrderId = order.getId();
        InventoryData inventory = inventoryGrpcSteps.createInventory(randomName(), randomQuantity());
        log.info("inventory {}", jsonPrinter.print(inventory));
        BoolMessage boolMessage = orderGrpcSteps.addInventoryToOrder(
                testOrderId,
                inventory.getId(),
                randomQuantity(inventory.getCount())
        );
        assertTrue(boolMessage.getResult());
        step("Get order with info", () -> {
            IdMessage idMessage = IdMessage.newBuilder().setId(testOrderId).build();
            OrderInfo orderInfo = orderBlockingStub.getOrderInfo(idMessage);
            attachText("orderInfo", jsonPrinter.print(orderInfo));

            step("Assertions", ()->{
                assertEquals(testOrderId, orderInfo.getId());
                assertEquals(order.getUser(), orderInfo.getUser());
                assertEqualsWithDefaultTolerance(order.getCreatedAt(), orderInfo.getCreatedAt());
                assertEquals(1, orderInfo.getInvCount());
            });
        });
    }
}