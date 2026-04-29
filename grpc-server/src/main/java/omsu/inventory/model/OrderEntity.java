package omsu.inventory.model;

import omsu.grpc.OrderStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class OrderEntity {
    private final UUID id;
    private final String username;
    private final OrderStatus status;
    private final Instant created_at;

    public OrderEntity(UUID id, String user, OrderStatus status, Instant created_at) {
        this.id = id;
        this.username = user;
        this.status = (status == null) ? OrderStatus.CREATED : status ;
        this.created_at = created_at;
    }

    public OrderEntity(UUID id, String user, OrderStatus status, Timestamp created_at) {
        this.id = id;
        this.username = user;
        this.status = (status == null) ? OrderStatus.CREATED : status ;
        this.created_at = created_at.toInstant();
    }

    public OrderEntity(UUID id, String user, OrderStatus status) {
        this.id = id;
        this.username = user;
        this.status = (status == null) ? OrderStatus.CREATED : status ;
        this.created_at = Instant.now() ;
    }

    public OrderEntity(String user, OrderStatus status) {
        this.id = null;
        this.username = user;
        this.status = (status == null) ? OrderStatus.CREATED : status ;
        this.created_at = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity that = (OrderEntity) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUsername(), that.getUsername()) && getStatus() == that.getStatus() && Objects.equals(getCreated_at(), that.getCreated_at());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getStatus(), getCreated_at());
    }
}
