package omsu.inventory.model.json;

import omsu.grpc.OrderStatus;
import omsu.inventory.model.OrderEntity;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.UUID;

public record OrderJson(
    UUID id,
    String username,
    OrderStatus status,
    Instant created_at
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
