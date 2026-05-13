package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import omsu.BaseSpringTest;
import omsu.BaseTestcontainersTest;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderStatus;
import omsu.BaseTest;
import org.junit.jupiter.api.Disabled;
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
    private String username = randomUsername();

    @Test
    @DisplayName("TC-OMCr01: create order item")
    @Description("Positive case of creation order item")
    void createOrder_pos_allFields() throws InvalidProtocolBufferException {
        final OrderData[] request = new OrderData[1];
        step("Build OrderData request", ()->{
            Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
            request[0] =  createOrder(username, OrderStatus.PENDING, createdAt);
            attachText("request created: ",  jsonPrinter.print(request[0]));
        });

        step("Creating and verify the order", ()->{
            step("Get all orders BEFORE", ()->{
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM inventory_schema.orders;");
                attachTable(rows, "orders" );
            });

            IdMessage response = step("Creating...", ()-> orderBlockingStub.createOrder(request[0]));
            UUID actualId = UUID.fromString(response.getId());
            attachText("response ", jsonPrinter.print(response));

            step("Get all orders AFTER", ()->{
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM inventory_schema.orders;");
                attachTable(rows, "orders" );
            });

            step("Check that it was created", ()-> {
                assertNotNull(actualId);
                assertInstanceOf(UUID.class, actualId);
            });
        });
    }
}
