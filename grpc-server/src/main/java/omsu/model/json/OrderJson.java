package omsu.model.json;

import omsu.grpc.OrderStatus;
import omsu.model.OrderEntity;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.UUID;

public record OrderJson(
    UUID id,
    String username,
    OrderStatus status,
    Timestamp created_at
){

    public static @Nonnull OrderJson fromEntity(@Nonnull OrderEntity entity) {
        return new OrderJson(
                entity.getId(),
                entity.getUsername(),
                entity.getStatus(),
                entity.getCreated_at()
        );
    }
}
