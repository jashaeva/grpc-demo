package omsu.order;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import omsu.BaseTest;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.grpc.OrderStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderGetTest extends BaseTest {

    @DisplayName("Get Order by id")
    @Test
    void getOrderById() throws InvalidProtocolBufferException {
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        OrderData orderData = OrderData.newBuilder()
                .setStatus(OrderStatus.PENDING)
                .setUser(johnFSmith)
                .setCreatedAt(instantToProto(createdAt))
                .build();
        String uuid = orderBlockingStub.createOrder(orderData).getId();

        log.info("request " + jsonPrinter.print(orderData));
        OrderDataWithId response = orderBlockingStub.getOrderById(
                IdMessage.newBuilder().setId(String.valueOf(uuid)).build()
        );
        log.info("response " + jsonPrinter.print(response));

        assertEquals(uuid, response.getId());
        assertEquals(johnFSmith, response.getUser());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(instantToProto(createdAt), response.getCreatedAt());
    }

    @Disabled
    @Test
    void getOrderInfo() {
    }
}