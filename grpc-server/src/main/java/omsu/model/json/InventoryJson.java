package omsu.model.json;


import omsu.model.InventoryEntity;

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
