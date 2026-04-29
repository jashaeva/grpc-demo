package omsu.inventory.services;

import omsu.grpc.IdMessage;
import omsu.inventory.model.OrderEntity;
import omsu.inventory.model.OrderInfoEntity;

import java.util.UUID;

public interface IOrderService {
    UUID create(OrderEntity order);
    boolean update(OrderEntity order);
    OrderInfoEntity getOrderInfo(IdMessage request);
}
