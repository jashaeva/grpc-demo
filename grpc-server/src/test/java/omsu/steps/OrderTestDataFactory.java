package omsu.steps;

import com.google.protobuf.Timestamp;
import io.qameta.allure.Step;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.grpc.OrderItem;
import omsu.grpc.OrderStatus;

import java.time.Instant;
import java.util.UUID;

import static omsu.utils.TimestampConverter.instantToProto;

public class OrderTestDataFactory {

    @Step("Create test order with user='{username}', status={status}, createdAt={createdAt}")
    public static OrderData createOrder(final String username, final OrderStatus status, final Instant createdAt) {
        return OrderData.newBuilder()
                .setStatus(status)
                .setUser(username)
                .setCreatedAt(instantToProto(createdAt))
                .build();
    }

    @Step("Add inventory {invId} to order {orderId} with quantity {count}")
    public static OrderItem createOrderItem(String orderId, String invId, long count) {
        return OrderItem.newBuilder()
                .setOrderId(orderId)
                .setInventoryId(invId)
                .setQuantity(count)
                .build();
    }

    @Step("Create test order with user='{username}', status={status}, createdAt={createdAt}")
    public static OrderData createOrder(final String username, final OrderStatus status, final Timestamp createdAt) {
        return OrderData.newBuilder()
                .setStatus(status)
                .setUser(username)
                .setCreatedAt(createdAt)
                .build();
    }

    @Step("Create test order with id={id}, user='{username}', status={status}, createdAt={createdAt}")
    public static OrderDataWithId createOrderWithId(
            final UUID id,
            final String username,
            final OrderStatus status,
            final Instant createdAt
    ) {
         return OrderDataWithId.newBuilder()
                .setId(id.toString())
                .setStatus(status)
                .setCreatedAt(instantToProto(createdAt))
                .setUser(username)
                .build();
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
}
