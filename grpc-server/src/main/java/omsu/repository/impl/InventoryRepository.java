package omsu.repository.impl;

import omsu.exception.EntityNotFoundException;
import omsu.model.InventoryEntity;
import omsu.repository.IInventoryRepository;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.UUID;

import static omsu.utils.UuidUtils.getUuid;

public class InventoryRepository implements IInventoryRepository {
    private static final Logger log = LoggerFactory.getLogger(InventoryRepository.class);
    private final JdbcOperations jdbcTemplate;

    public InventoryRepository(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UUID create(InventoryEntity entity) {
        String sqlInsert = """
        INSERT INTO inventory_schema.inventory (name, stock_quantity)
        VALUES (?, ?);
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try{
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlInsert, new String[]{"id"});
                ps.setString(1, entity.getName());
                ps.setLong(2, entity.getCount());
                return ps;
            }, keyHolder);
            return getUuid(keyHolder);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Failed to create inventory item with name "+ entity.getName(), e);
        }
    }


    @Override
    public boolean update(InventoryEntity entity) {
        String sqlUpdate = "UPDATE inventory_schema.inventory SET name = ?, stock_quantity = ? WHERE id = ?;";

        try {
            int rowsAffected = jdbcTemplate.update(sqlUpdate, entity.getName(), entity.getCount(), entity.getId());
            log.info("DB Inventory: updated {}", rowsAffected);
            return (rowsAffected > 0);
        } catch (DataAccessException e) {
            throw new EntityNotFoundException("Failed to update inventory with id " + entity.getId(), e);
        }
    }

    @Override
    public InventoryEntity getById(UUID id) throws EntityNotFoundException {
        String sqlGet = "SELECT id, name, stock_quantity FROM inventory_schema.inventory WHERE id = ?;";
        try {
            InventoryEntity inventoryEntity = jdbcTemplate.queryForObject(
                sqlGet,
                (rs, rowNum) ->
                    new InventoryEntity(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getLong("stock_quantity")
                    ),
                id
            );
            log.info("DB Inventory: got {}, name = {}", id, inventoryEntity.getName());
            return inventoryEntity;
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Failed to get inventory item by id "+ id, e);
        }
    }

    @Override
    public boolean deleteById(UUID id) throws DataAccessException {
        String sqlDelete = "DELETE FROM inventory_schema.inventory WHERE id = ?;";
        try {
            int rowsAffected = jdbcTemplate.update(sqlDelete, id);
            log.info("DB Inventory: deleted rows {}", rowsAffected);
            return (rowsAffected > 0);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to delete inventory item with id " + id, e) {};
        }
    }
}
