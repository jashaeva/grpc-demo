package omsu.model;

import java.util.Objects;
import java.util.UUID;

public class OrderItemEntity {
    private UUID orderId;
    private UUID inventoryId;
    private int quantity;

    public OrderItemEntity(UUID orderId, UUID inventoryId, int quantity) {
        this.orderId = orderId;
        this.inventoryId = inventoryId;
        this.quantity = quantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getInventoryId() {
        return inventoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemEntity that = (OrderItemEntity) o;
        return getQuantity() == that.getQuantity() && Objects.equals(getOrderId(), that.getOrderId()) && Objects.equals(getInventoryId(), that.getInventoryId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), getInventoryId(), getQuantity());
    }
}
