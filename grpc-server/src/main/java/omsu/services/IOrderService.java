package omsu.services;

import omsu.grpc.IdMessage;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;

import java.util.UUID;

public interface IOrderService {
    UUID create(OrderData order);
    boolean update(OrderDataWithId order);
    OrderInfoEntity getOrderInfo(IdMessage request);
}
