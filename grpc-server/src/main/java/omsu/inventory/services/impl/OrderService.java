package omsu.inventory.services.impl;

import omsu.grpc.IdMessage;
import omsu.inventory.exception.EntityNotFoundException;
import omsu.inventory.model.InventoryEntity;
import omsu.inventory.model.OrderEntity;
import omsu.inventory.model.OrderInfoEntity;
import omsu.inventory.repository.IInventoryRepository;
import omsu.inventory.repository.IOrderRepository;
import omsu.inventory.services.IOrderService;

import java.util.UUID;


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
    public UUID create(OrderEntity order) {
        return orderRepository.create(order);
    }

    @Override
    public boolean update(OrderEntity order) {
     //   checkInventoryIdExists(order.getInventory().getId());
        return orderRepository.update(order);
    }

    @Override
    public OrderInfoEntity getOrderInfo(IdMessage request) {
        return orderRepository.getOrderInfo(UUID.fromString(request.getId()));
    }
}
