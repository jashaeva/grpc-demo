package omsu.inventory.repository;

import omsu.inventory.model.OrderEntity;
import omsu.inventory.model.OrderInfoEntity;

import java.util.UUID;

public interface IOrderRepository {
    UUID create (OrderEntity entity);
    boolean update (OrderEntity entity);
    OrderEntity getById(UUID id);
    OrderInfoEntity getOrderInfo(UUID id);
}
