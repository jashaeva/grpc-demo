package omsu.steps;

import com.google.protobuf.Timestamp;
import io.qameta.allure.Step;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.grpc.OrderItem;
import omsu.grpc.OrderStatus;

import java.time.Instant;
import java.util.UUID;

import static omsu.utils.DataUtils.randomStatusStart;
import static omsu.utils.DataUtils.randomUsername;
import static omsu.utils.TimestampConverter.instantToProto;

public class OrderTestDataFactory {

    @Step("Create test order with user='{username}', status={status}, createdAt={createdAt}")
    public static OrderData createOrder(final String username, final OrderStatus status, final Timestamp createdAt) {
        return OrderData.newBuilder()
                .setStatus(status)
                .setUser(username)
                .setCreatedAt(createdAt)
                .build();
    }

    @Step("Create test order with user='{username}', status={status}, createdAt={createdAt}")
    public static OrderData createOrder(final String username, final OrderStatus status, final Instant createdAt) {
        return createOrder( username, status, instantToProto(createdAt));
    }

    @Step("Create test order with id={id}, user='{username}', status={status}, createdAt={createdAt}")
    public static OrderDataWithId createOrderWithId(
            final String id,
            final String username,
            final OrderStatus status,
            final Instant createdAt
    ) {
        return OrderDataWithId.newBuilder()
                .setId(id)
                .setStatus(status)
                .setCreatedAt(instantToProto(createdAt))
                .setUser(username)
                .build();
    }

    @Step("Create order with id={id} and random data")
    public static OrderDataWithId createOrderWithId(final String id) {
        return createOrderWithId(id, randomUsername(), randomStatusStart(), Instant.now());
    }


    @Step("Create test order with id={id}, user='{username}', status={status}, createdAt={createdAt}")
    public static OrderDataWithId createOrderWithId(
            final UUID id,
            final String username,
            final OrderStatus status,
            final Instant createdAt
    ) {
        return createOrderWithId(id.toString(), username, status, createdAt);
    }

    @Step("Add inventory {invId} to order {orderId} with quantity {count}")
    public static OrderItem createOrderItem(String orderId, String invId, long count) {
        return OrderItem.newBuilder()
                .setOrderId(orderId)
                .setInventoryId(invId)
                .setQuantity(count)
                .build();
    }
}
