package omsu.inventory.repository;

import omsu.inventory.model.InventoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.UUID;

@Repository
public class InventoryRepository implements IInventoryRepo{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UUID create(InventoryEntity entity) throws SQLException {
        String sqlInsert = "INSERT INTO product (name, stock_quantity) VALUES (?, ?)";
        String sqlGet = "SELECT id FROM product WHERE name = ?";

        try {
            jdbcTemplate.update(sqlInsert, entity.getName(), entity.getCount());
            String idStr = jdbcTemplate.queryForObject(sqlGet, (rs, rowNum) -> rs.getString("id"), entity.getName());
            return UUID.fromString(idStr);
        } catch (Exception e) {
            throw new SQLException("Failed to create inventory item", e);
        }
    }

    @Override
    public void update(InventoryEntity entity) throws SQLException {
        String sqlUpdate = "UPDATE product SET name = ?, stock_quantity = ? WHERE id = ?";

        try {
            jdbcTemplate.update(sqlUpdate, entity.getName(), entity.getCount(), entity.getId());
        } catch (Exception e) {
            throw new SQLException("Failed to update inventory item", e);
        }
    }

    @Override
    public InventoryEntity getById(UUID id) throws SQLException{
        String sqlGet = "SELECT id, name, stock_quantity FROM product WHERE id = ?";
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
        } catch (Exception e) {
            throw new SQLException("Failed to get inventory item by id "+ id, e);
        }
    }

    @Override
    public void deleteById(UUID id) throws SQLException {
        String sqlGet = "DELETE FROM product WHERE id = ?";
        try {
            jdbcTemplate.update(sqlGet, id);
        } catch (Exception e) {
            throw new SQLException("Failed to delete inventory item", e);
        }
    }
}
