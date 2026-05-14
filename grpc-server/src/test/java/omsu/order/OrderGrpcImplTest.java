package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import omsu.BaseTestcontainersTest;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.grpc.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachTable;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.OrderTestDataFactory.createOrder;
import static omsu.utils.DataUtils.randomUsername;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Order Management")
@Feature("Create order operations")
class OrderGrpcImplTest extends BaseTestcontainersTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderGrpcImplTest.class);

    @Test
    @DisplayName("TC-OMCr01: create order item")
    @Description("Positive case of creation order item")
    void createOrder_pos_allFields() throws InvalidProtocolBufferException {
        step("Get all orders BEFORE", ()->{
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM inventory_schema.orders;");
            attachTable(rows, "orders" );
        });
        OrderDataWithId order = orderGrpcSteps.createOrderEntity();
        UUID actualId = UUID.fromString(order.getId());
        attachText("Created order: {}", jsonPrinter.print(order));
        step("Get all orders AFTER", ()->{
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM inventory_schema.orders;");
            attachTable(rows, "orders" );
        });

        step("Check that it was created", ()-> {
            assertNotNull(actualId);
            assertInstanceOf(UUID.class, actualId);
        });
    }
}
