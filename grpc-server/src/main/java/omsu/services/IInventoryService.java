package omsu.services;

import omsu.model.InventoryEntity;

import java.util.UUID;

public interface IInventoryService {
    UUID create(InventoryEntity entity);
    boolean update(InventoryEntity entity);
    InventoryEntity getById(UUID uuid);
    void deleteById(UUID uuid);
}
