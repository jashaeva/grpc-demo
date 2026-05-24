package omsu.repository.impl;

import omsu.grpc.OrderStatus;
import omsu.exception.EntityNotFoundException;
import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;
import omsu.repository.IOrderRepository;
import omsu.utils.RowMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.UUID;

import static omsu.utils.RowMappers.getOrderInfoEntityRowMapper;
import static omsu.utils.UuidUtils.getUuid;

public class OrderRepository implements IOrderRepository {
    private Logger log = LoggerFactory.getLogger(OrderRepository.class);
    private final JdbcOperations jdbcTemplate;
    public OrderRepository(JdbcOperations jdbc) {
        this.jdbcTemplate = jdbc;
    }

    @Override
    public UUID create(OrderEntity entity) {
        String sqlInsert = """
        INSERT INTO inventory_schema.orders (username, status, created_at)
        VALUES (?, ?, ?);
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try{
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlInsert, new String[]{"id"});
                ps.setString(1, entity.getUsername());
                ps.setString(2, entity.getStatus().name());
                ps.setTimestamp(3, entity.getCreated_at());
                return ps;
            }, keyHolder);

            return getUuid(keyHolder);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Failed to create order for username "+ entity.getUsername(), e);
        }
    }

    @Override
    public boolean update(OrderEntity entity) {
        String sqlUpdate = """
        UPDATE inventory_schema.orders
        SET username = ?, status = ?, created_at = ?
        WHERE id = ?;
        """;

        try {
            int updated = jdbcTemplate.update(
                    sqlUpdate,
                    entity.getUsername(),
                    entity.getStatus().name(),
                    entity.getCreated_at(),
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
        FROM inventory_schema.orders
        WHERE id = ?;
        """;
        try {
            return jdbcTemplate.queryForObject(
                sqlGet,
                RowMappers.getOrderEntityRowMapper(),
                id
            );
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
             count(B.inventory_id) as inv_count
         FROM inventory_schema.orders as A LEFT JOIN inventory_schema.order_items B
         ON A.id = B.order_id
         WHERE A.id = ?
         GROUP BY 1;
        """;
        try {
            OrderInfoEntity orderInfo = jdbcTemplate.queryForObject(
                sqlGet,
                getOrderInfoEntityRowMapper(),
                id
            );
            log.info("orderInfo {}", orderInfo.getOrder().getUsername() + ", " + orderInfo.getInvQuantity());
            return orderInfo;
        } catch (DataAccessException e) {
            throw new EntityNotFoundException("Order id " + id + " not found OR smth wrong happened", e);
        }
    }


}
