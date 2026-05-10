package omsu.utils;

import omsu.grpc.OrderStatus;
import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;

import java.util.UUID;

public class RowMappers {
    @NotNull
    public static RowMapper<OrderEntity> getOrderEntityRowMapper() {
        return (rs, rowNum) -> new OrderEntity(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at")
        );
    }

    @NotNull
    public static RowMapper<OrderInfoEntity> getOrderInfoEntityRowMapper() {
        return (rs, rowNum) -> {
            OrderInfoEntity orderInfoEntity = new OrderInfoEntity(
                    new OrderEntity(
                            UUID.fromString(rs.getString("order_id")),
                            rs.getString("username"),
                            OrderStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at")
                    ),
                    rs.getInt("inv_count")
            );
            System.out.println(orderInfoEntity);
            return orderInfoEntity;
        };
    }
}
