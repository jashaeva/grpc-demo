package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Description;
import omsu.BaseSpringTest;
import omsu.BaseTestcontainersTest;
import omsu.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.InventoryTestDataFactory.createInventoryMessage;
import static omsu.steps.OrderTestDataFactory.createOrder;
import static omsu.steps.OrderTestDataFactory.createOrderItem;
import static omsu.utils.DataUtils.randomInventory;
import static omsu.utils.DataUtils.randomUsername;
import static omsu.utils.TimestampAssertions.assertEqualsWithDefaultTolerance;
import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.*;

class OrderInventoryAddTest extends BaseTestcontainersTest {
    private String testOrderId;
    private String testInvId;
    private Instant testCreatedAt;
    private final String username = randomUsername();

    @BeforeEach
    void createTestOrder(){
        step("Setup: Create test order item to add inventory",
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
    }

    @Test
    @DisplayName("TC-OMAdInv01: Add inventory to order")
    @Description("Positive case")
    void testAddInventoryToOrder_pos() {
        step("Add relation order-inventory", ()->{
            OrderItem orderItem = createOrderItem(testOrderId, testInvId, 1L);
            BoolMessage result = orderBlockingStub.addInventory(orderItem);
            assertTrue(result.getResult());
        });
    }

    @Test
    @DisplayName("TC-OMAdInv02: Add inventory to order - not enough quantity")
    @Description("Negative case: not enough quantity for adding")
    void testAddInventoryToOrder_neg() {
        step("Add relation order-inventory", ()->{
            OrderItem orderItem = createOrderItem(testOrderId, testInvId, 100L);
            StatusRuntimeException thrown = assertThrows(
                    StatusRuntimeException.class,
                    () -> orderBlockingStub.addInventory(orderItem)
            );
            attachText("thrown ", thrown.getMessage());
            assertEquals(Status.RESOURCE_EXHAUSTED.getCode(), thrown.getStatus().getCode());
        });
    }
}