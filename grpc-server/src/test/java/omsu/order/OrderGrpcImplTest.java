package omsu.order;

import com.google.protobuf.InvalidProtocolBufferException;
import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderStatus;
import omsu.BaseTest;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static omsu.utils.TimestampConverter.toProto;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderGrpcImplTest extends BaseTest {
    @BeforeEach
    void setUpEach() {
//        jdbcTemplate.update("DELETE FROM inventory_schema.inventory WHERE name = ?;", PRODUCT_NAME);
    }

    @Test
    void createOrder_pos_allFields() throws InvalidProtocolBufferException {
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        OrderData request = OrderData.newBuilder()
                .setStatus(OrderStatus.PENDING)
                .setUser("John F Smith")
                .setCreatedAt(toProto(createdAt))
                .build();

        log.info("request " + jsonPrinter.print(request));
        IdMessage response = orderBlockingStub.createOrder(request);
        String actualId = response.getId();
        assertNotNull(actualId);
        log.info("response " + jsonPrinter.print(response));
    }

@Ignore
    @Test
    void updateOrder() {
    }

@Ignore
    @Test
    void getOrderInfo() {
    }
}