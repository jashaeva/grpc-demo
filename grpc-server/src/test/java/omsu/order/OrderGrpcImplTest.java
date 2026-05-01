package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderStatus;
import omsu.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static omsu.utils.TimestampConverter.instantToProto;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderGrpcImplTest extends BaseTest {

    @Test
    void createOrder_pos_allFields() throws InvalidProtocolBufferException {
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        OrderData request = OrderData.newBuilder()
                .setStatus(OrderStatus.PENDING)
                .setUser(johnFSmith)
                .setCreatedAt(instantToProto(createdAt))
                .build();

        log.info("request " + jsonPrinter.print(request));
        IdMessage response = orderBlockingStub.createOrder(request);
        UUID actualId = UUID.fromString(response.getId());
        assertNotNull(actualId);
        assertInstanceOf(UUID.class, actualId);
        log.info("response " + jsonPrinter.print(response));
    }

}