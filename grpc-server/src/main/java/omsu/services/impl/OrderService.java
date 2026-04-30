package omsu.services.impl;

import omsu.grpc.IdMessage;
import omsu.exception.EntityNotFoundException;
import omsu.grpc.OrderData;
import omsu.grpc.OrderDataWithId;
import omsu.model.InventoryEntity;
import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;
import omsu.repository.IInventoryRepository;
import omsu.repository.IOrderRepository;
import omsu.services.IOrderService;

import java.util.UUID;

import static omsu.utils.TimestampConverter.protoToTimestamp;


public class OrderService implements IOrderService {

    private final IInventoryRepository inventoryRepository;
    private final IOrderRepository orderRepository;

    public OrderService(IOrderRepository orderRepo, IInventoryRepository inventoryRepo) {
        this.orderRepository = orderRepo;
        this.inventoryRepository = inventoryRepo;
    }

    private void checkInventoryIdExists (UUID uuid) {
        InventoryEntity invEntity;
        try {
            invEntity = inventoryRepository.getById(uuid);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("No such inventory with id = "+ uuid,e);
        }
    }

    @Override
    public UUID create(OrderData request) {
        OrderEntity order = new OrderEntity(
                request.getUser(),
                request.getStatus(),
                protoToTimestamp(request.getCreatedAt())
        );
        return orderRepository.create(order);
    }

    @Override
    public boolean update(OrderDataWithId request) {
        OrderEntity order = new OrderEntity(
                UUID.fromString(request.getId()),
                request.getUser(),
                request.getStatus(),
                protoToTimestamp(request.getCreatedAt())
        );
        return orderRepository.update(order);
    }

    @Override
    public OrderInfoEntity getOrderInfo(IdMessage request) {
        return orderRepository.getOrderInfo(UUID.fromString(request.getId()));
    }
}
