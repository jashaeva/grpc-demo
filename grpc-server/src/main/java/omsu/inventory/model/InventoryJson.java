package omsu.inventory.model;


import javax.annotation.Nonnull;
import java.util.UUID;

public record InventoryJson(
    UUID id,
    String name,
    long count
){

    public static @Nonnull InventoryJson fromEntity(@Nonnull InventoryEntity entity) {
        return new InventoryJson(
                entity.getId(),
                entity.getName(),
                entity.getCount()
        );
    }
}
