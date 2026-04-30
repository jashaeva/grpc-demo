package omsu.repository;

import omsu.exception.EntityNotFoundException;
import omsu.model.InventoryEntity;
import org.springframework.dao.DataAccessException;

import java.util.UUID;

public interface IInventoryRepository {
    UUID create(InventoryEntity entity) ;
    boolean update(InventoryEntity entity) ;
    InventoryEntity getById(UUID id) throws EntityNotFoundException;
    boolean deleteById (UUID id) throws DataAccessException;
}
