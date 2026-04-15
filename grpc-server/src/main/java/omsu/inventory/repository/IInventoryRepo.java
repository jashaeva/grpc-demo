package omsu.inventory.repository;

import omsu.inventory.model.InventoryEntity;

import java.sql.SQLException;
import java.util.UUID;

public interface IInventoryRepo {
    UUID create(InventoryEntity entity) throws SQLException;
    void update(InventoryEntity entity) throws SQLException;
    InventoryEntity getById(UUID id) throws SQLException;
    void deleteById (UUID id) throws SQLException;
}
