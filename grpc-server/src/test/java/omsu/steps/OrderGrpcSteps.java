package omsu.steps;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.qameta.allure.Step;
import net.datafaker.Faker;
import omsu.grpc.*;
import omsu.utils.TimestampConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.google.protobuf.util.JsonFormat.printer;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.OrderTestDataFactory.createOrder;
import static omsu.steps.OrderTestDataFactory.createOrderItem;
import static omsu.utils.DataUtils.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OrderGrpcSteps {
    private final OrderGrpc.OrderBlockingStub orderBlockingStub;
    private final Logger log = LoggerFactory.getLogger(OrderGrpcSteps.class);
    private static final JsonFormat.Printer jsonPrinter = printer();

    public OrderGrpcSteps(OrderGrpc.OrderBlockingStub orderBlockingStub) {
        this.orderBlockingStub = orderBlockingStub;
    }

    @Step("Order: create entity with random name etc")
    public OrderDataWithId createOrderEntity() throws InvalidProtocolBufferException {
        String username = randomUsername();
        OrderStatus status = randomStatusStart();
        Instant testCreatedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        OrderData order = createOrder(username, status, testCreatedAt);
        log.info("Order to create {}", jsonPrinter.print(order));

        IdMessage created = orderBlockingStub.createOrder(order);
        String testOrderId = created.getId();
        attachText("UUID created: ", testOrderId);
        return OrderDataWithId.newBuilder()
                .setId(testOrderId)
                .setUser(username)
                .setCreatedAt(TimestampConverter.instantToProto(testCreatedAt))
                .setStatus(status)
                .build();
    }

    @Step("Add relation order {orderId} to inventory {inventoryId}")
    public BoolMessage addInventoryToOrder(final String orderId, final String inventoryId, long count){
        OrderItem orderItem = createOrderItem(orderId, inventoryId, count);
        return  orderBlockingStub.addInventory(orderItem);
    }
}
