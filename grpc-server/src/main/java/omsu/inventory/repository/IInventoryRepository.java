package omsu.inventory.repository;

import omsu.inventory.exception.EntityNotFoundException;
import omsu.inventory.model.InventoryEntity;
import org.springframework.dao.DataAccessException;

import java.util.UUID;

public interface IInventoryRepository {
    UUID create(InventoryEntity entity) ;
    boolean update(InventoryEntity entity) ;
    InventoryEntity getById(UUID id) throws EntityNotFoundException;
    boolean deleteById (UUID id) throws DataAccessException;
}
