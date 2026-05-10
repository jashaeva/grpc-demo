package omsu.services;

import omsu.grpc.*;

import java.util.UUID;

public interface IOrderService {
    UUID create(OrderData order);
    boolean update(OrderDataWithId order);
    OrderInfo getOrderInfo(IdMessage request);
    OrderDataWithId getOrderById(IdMessage request);

    BoolMessage addInventory(OrderItem request);
}
