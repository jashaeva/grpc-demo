package omsu.inventory.repository.impl;

import omsu.grpc.InventoryData;
import omsu.grpc.OrderInfo;
import omsu.grpc.OrderStatus;
import omsu.inventory.exception.EntityNotFoundException;
import omsu.inventory.model.OrderEntity;
import omsu.inventory.model.OrderInfoEntity;
import omsu.inventory.repository.IOrderRepository;
import omsu.inventory.utils.ProtoTimestampConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;

public class OrderRepository implements IOrderRepository {

    private JdbcOperations jdbcTemplate;
    public OrderRepository(JdbcOperations jdbc) {
        this.jdbcTemplate = jdbc;
    }

    @Override
    public UUID create(OrderEntity entity) {
        String sqlInsert = """
        INSERT INTO orders (username, status, created_at)
        VALUES (?, CAST(? AS order_status), ?)
        RETURNING id
        """;
        try{
            return jdbcTemplate.queryForObject(
                sqlInsert,
                UUID.class,
                new SqlParameterValue(Types.VARCHAR, entity.getUsername()),
                entity.getStatus().name(),
                new SqlParameterValue(Types.TIMESTAMP, entity.getCreated_at())
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Failed to create order for username "+ entity.getUsername(), e);
        }
    }

    @Override
    public boolean update(OrderEntity entity) {
        String sqlUpdate = "UPDATE orders SET username = ?, status = CAST(? AS order_status) WHERE id = ?";

        try {
            int updated = jdbcTemplate.update(
                    sqlUpdate,
                    entity.getUsername(),
                    entity.getStatus().name(),
                    entity.getId());
            return (updated > 0);
        } catch (DataAccessException e) {
            throw new EntityNotFoundException("Order id " + entity.getId() + " not found OR smth wrong happened", e);
        }
    }

    @Override
    public OrderEntity getById(UUID id) {
        String sqlGet = """
        SELECT
            id,
            username,
            status,
            created_at
        FROM orders
        WHERE id = ?
        """;
        try {
            OrderEntity order = jdbcTemplate.queryForObject(
                sqlGet,
                (rs, rowNum) -> {
                    return new OrderEntity(
                        UUID.fromString(rs.getString("order_id")),
                        rs.getString("username"),
                        OrderStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant()
                    );
                },
                id
            );
            return order;
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Failed to get order by id "+ id, e);
        }
    }

    @Override
    public OrderInfoEntity getOrderInfo(UUID id) {
        String sqlGet = """
        SELECT
            A.id as order_id,
            A.username as username,
            A.status as status,
            A.created_at as created_at,
            count(*) as inv_count
        FROM orders as A INNER JOIN order_items B
        ON A.id = B.order_id
        WHERE A.id = ?
        GROUP BY 1,2,3,4
        """;
        try {
            OrderInfoEntity orderInfo = jdbcTemplate.queryForObject(
                sqlGet,
                (rs, rowNum) ->{
                    Instant createdAt = rs.getTimestamp("created_at").toInstant();
                    String statusStr = rs.getString("status");
                    OrderStatus status = OrderStatus.valueOf(statusStr);
                    return  new OrderInfoEntity(
                        new OrderEntity(
                            UUID.fromString(rs.getString("order_id")),
                            rs.getString("username"),
                            status,
                            createdAt
                        ),
                        rs.getInt("inv_count")
                    );
                },
                id
            );
            return orderInfo;
        } catch (DataAccessException e) {
            throw new EntityNotFoundException("Order id " + id + " not found OR smth wrong happened", e);
        }
    }
}
