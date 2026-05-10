package omsu.repository.impl;

import omsu.repository.IOrderInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.UUID;

import static omsu.utils.UuidUtils.getUuid;

public class OrderInventoryRepository implements IOrderInventoryRepository {
    private Logger log = LoggerFactory.getLogger(OrderInventoryRepository.class);
    private final JdbcOperations jdbcTemplate;
    public OrderInventoryRepository(JdbcOperations jdbc) {
        this.jdbcTemplate = jdbc;
    }

    @Override
    public boolean create(UUID order_id, UUID inventory_id, long quantity) {
        String sqlInsert = """
        INSERT INTO inventory_schema.order_items (order_id, inventory_id, quantity)
        VALUES (?, ?, ?);
        """;
        try{
            int rows = jdbcTemplate.update(sqlInsert,
                    order_id,
                    inventory_id,
                    quantity
            );
            return (rows == 1);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Failed to add inventory "
                + inventory_id + " to order " + order_id,
            e);
        }
    }

    @Override
    public boolean updateCount(UUID order_id, UUID inventory_id, long newQuantity) {
        return false;
    }

    @Override
    public boolean delete(UUID order_id, UUID inventory_id) {
        return false;
    }
}
