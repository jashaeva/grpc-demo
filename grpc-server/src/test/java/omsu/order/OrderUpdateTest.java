package omsu.order;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import omsu.BaseTest;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.grpc.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderUpdateTest extends BaseTest {

    // create - update - get and check
    @Test
    void updateOrder_pos_allFields() throws InvalidProtocolBufferException {
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        OrderData orderData = OrderData.newBuilder()
                .setStatus(OrderStatus.PENDING)
                .setUser(johnFSmith + " other")
                .setCreatedAt(instantToProto(createdAt))
                .build();

        log.info("request " + jsonPrinter.print(orderData));
        IdMessage created = orderBlockingStub.createOrder(orderData);

        String uuid = created.getId();
        Timestamp updateCreatedAt = instantToProto(Instant.now());
        String adamGSmith = "Adam G Smith";

        OrderDataWithId updateData = OrderDataWithId.newBuilder()
                .setId(uuid)
                .setStatus(OrderStatus.ACTUAL)
                .setCreatedAt(updateCreatedAt)
                .setUser(adamGSmith)
                .build();

        Empty empty = orderBlockingStub.updateOrder(updateData);
        OrderDataWithId response = orderBlockingStub.getOrderById(
                IdMessage.newBuilder().setId(uuid).build()
        );
        log.info("response " + jsonPrinter.print(response));

        assertEquals(uuid, response.getId());
        assertEquals(adamGSmith, response.getUser());
        assertEquals(OrderStatus.ACTUAL, response.getStatus());
        assertEquals(updateCreatedAt, response.getCreatedAt());
    }
}