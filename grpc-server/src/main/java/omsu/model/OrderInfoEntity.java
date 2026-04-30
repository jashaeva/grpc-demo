package omsu.model;

import java.util.Objects;

public class OrderInfoEntity {
    private OrderEntity order;
    private int invQuantity;

    public OrderInfoEntity(OrderEntity order, int invQuantity) {
        this.order = new OrderEntity(order.getId(), order.getUsername(), order.getStatus(), order.getCreated_at());
        this.invQuantity = invQuantity;
    }

    public int getInvQuantity() {
        return invQuantity;
    }

    public OrderEntity getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderInfoEntity that = (OrderInfoEntity) o;
        return getInvQuantity() == that.getInvQuantity() && Objects.equals(getOrder(), that.getOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrder(), getInvQuantity());
    }
}
