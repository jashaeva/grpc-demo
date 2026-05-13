package omsu.order;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import kotlin.random.URandomKt;
import omsu.BaseSpringTest;
import omsu.BaseTest;
import omsu.BaseTestcontainersTest;
import omsu.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachTable;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.OrderTestDataFactory.createOrder;
import static omsu.steps.OrderTestDataFactory.createOrderWithId;
import static omsu.utils.DataUtils.randomUsername;
import static omsu.utils.TimestampAssertions.assertEqualsWithDefaultTolerance;
import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Order Management")
@Feature("Update order entity")
class OrderUpdateTest extends BaseTestcontainersTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderUpdateTest.class);
    private String testOrderId;
    private String username = randomUsername();

    @BeforeEach
    void createTestOrder(){
        step("Setup: Create test order item for updating",
                () -> {
                    Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
                    OrderData order = createOrder(username, OrderStatus.PENDING, createdAt);
                    step("Creating...", ()->{
                        IdMessage created = orderBlockingStub.createOrder(order);
                        testOrderId = created.getId();
                        step("UUID created: "+ testOrderId, ()->{});
                    });
                });
    }

    @Test
    @DisplayName("TC-OMUp01: update existing item")
    @Description("Positive case with changes in all fields")
    void updateOrder_pos_allFields() throws InvalidProtocolBufferException {
        Instant updateCreatedAt = Instant.now();
        String adamGSmith = randomUsername();
        final OrderDataWithId[] updateData = new OrderDataWithId[1];
        step("Get all orders BEFORE", ()->{
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM inventory_schema.orders;");
            attachTable(rows, "orders updating" );
        });
        step("Build new OrderData object",()-> {
                updateData[0] = createOrderWithId(
                    testOrderId, adamGSmith, OrderStatus.ACTUAL, updateCreatedAt
                );
                attachText("New object data to save", updateData[0].toString());
        });
        step("Updating...", ()->{
            Empty empty = orderBlockingStub.updateOrder(updateData[0]);
            step("Updated "+ jsonPrinter.print(empty), ()->{});
            step("Get all orders AFTER", ()->{
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM inventory_schema.orders;");
                attachTable(rows, "orders updating" );
            });
            step("Get order and check it was updated", ()->{
                OrderDataWithId response = orderBlockingStub.getOrderById(
                    IdMessage.newBuilder().setId(testOrderId).build()
                );
                attachText("object ", jsonPrinter.print(response));
                step("Assertions",()->{
                    assertEquals(testOrderId, response.getId());
                    assertEquals(adamGSmith, response.getUser());
                    assertEquals(OrderStatus.ACTUAL, response.getStatus());
                    assertEqualsWithDefaultTolerance(instantToProto(updateCreatedAt), response.getCreatedAt());
                });
            });
        });
    }
}