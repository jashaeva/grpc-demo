package omsu.order;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import omsu.BaseTestcontainersTest;
import omsu.grpc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.OrderTestDataFactory.createOrderWithId;
import static omsu.utils.TimestampAssertions.assertEqualsWithDefaultTolerance;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Order Management")
@Feature("Update order entity")
class OrderUpdateTest extends BaseTestcontainersTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderUpdateTest.class);
    private String testOrderId;
    private OrderDataWithId order;


    @BeforeEach
    void createTestOrder(TestInfo testInfo) throws InvalidProtocolBufferException {
        order = orderGrpcSteps.createOrderEntity();
        testOrderId = order.getId();
        logger.info("(Before {}) Order created {}", testInfo.getDisplayName(), jsonPrinter.print(order));
    }

    @Test
    @DisplayName("TC-OMUp01: update existing item")
    @Description("Positive case with changes in all fields")
    void updateOrder_pos_allFields() throws InvalidProtocolBufferException {
        OrderDataWithId updateData = createOrderWithId( testOrderId);
        attachText("New object data to save", jsonPrinter.print(updateData));

        String username = updateData.getUser();
        OrderStatus status = updateData.getStatus();
        Timestamp updateCreatedAt = updateData.getCreatedAt();
        step("Updating", ()->{
            Empty empty = orderBlockingStub.updateOrder(updateData);
            step("Verify the order was updated", ()->{
                OrderDataWithId response = orderBlockingStub.getOrderById(
                    IdMessage.newBuilder().setId(testOrderId).build()
                );
                attachText("Got the object {}", jsonPrinter.print(response));
                assertEquals(testOrderId, response.getId());
                assertEquals(username, response.getUser());
                assertEquals(status, response.getStatus());
                assertEqualsWithDefaultTolerance(updateCreatedAt, response.getCreatedAt());
            });
        });
    }
}