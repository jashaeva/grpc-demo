package omsu.repository.impl;

import omsu.exception.EntityNotFoundException;
import omsu.model.InventoryEntity;
import omsu.repository.IInventoryRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.UUID;

public class InventoryRepository implements IInventoryRepository {

    private final JdbcOperations jdbcTemplate;

    public InventoryRepository(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UUID create(InventoryEntity entity) {
        String sqlInsert = """
        INSERT INTO inventory_schema.inventory (name, stock_quantity)
        VALUES (?, ?)
        RETURNING id;
        """;
        try{
            return jdbcTemplate.queryForObject(
                    sqlInsert,
                    UUID.class,
                    entity.getName(),
                    entity.getCount()
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Failed to create inventory item with name "+ entity.getName(), e);
        }
    }

    @Override
    public boolean update(InventoryEntity entity) {
        String sqlUpdate = "UPDATE inventory_schema.inventory SET name = ?, stock_quantity = ? WHERE id = ?;";

        try {
            int rowsAffected = jdbcTemplate.update(sqlUpdate, entity.getName(), entity.getCount(), entity.getId());

            return (rowsAffected > 0);
        } catch (DataAccessException e) {
            throw new EntityNotFoundException("Inventory item with id " + entity.getId() + " not found", e);
        }
    }

    @Override
    public InventoryEntity getById(UUID id) throws EntityNotFoundException {
        String sqlGet = "SELECT id, name, stock_quantity FROM inventory_schema.inventory WHERE id = ?;";
        try {
            InventoryEntity entity = jdbcTemplate.queryForObject(
                    sqlGet,
                    (rs, rowNum) -> {
                        return new InventoryEntity(
                                UUID.fromString(rs.getString("id")),
                                rs.getString("name"),
                                rs.getLong("stock_quantity")
                        );
                    },
                    id);
            return entity;
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Failed to get inventory item by id "+ id, e);
        }
    }

    @Override
    public boolean deleteById(UUID id) throws DataAccessException {
        String sqlDelete = "DELETE FROM inventory_schema.inventory WHERE id = ?;";
        try {
            int rowsAffected = jdbcTemplate.update(sqlDelete, id);
            return (rowsAffected > 0);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to delete inventory item with id " + id, e) {};
        }
    }
}
