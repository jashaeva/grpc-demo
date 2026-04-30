package omsu.repository;

import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;

import java.util.UUID;

public interface IOrderRepository {
    UUID create (OrderEntity entity);
    boolean update (OrderEntity entity);
    OrderEntity getById(UUID id);
    OrderInfoEntity getOrderInfo(UUID id);
}
