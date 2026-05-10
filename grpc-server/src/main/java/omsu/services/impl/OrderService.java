package omsu.services.impl;

import omsu.exception.NotEnoughInventoryException;
import omsu.grpc.*;
import omsu.exception.EntityNotFoundException;
import omsu.model.InventoryEntity;
import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;
import omsu.repository.IInventoryRepository;
import omsu.repository.IOrderInventoryRepository;
import omsu.repository.IOrderRepository;
import omsu.services.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static omsu.utils.TimestampConverter.protoToTimestamp;
import static omsu.utils.TimestampConverter.timestampToProto;


public class OrderService implements IOrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final IInventoryRepository inventoryRepository;
    private final IOrderRepository orderRepository;
    private final IOrderInventoryRepository orderInvRepository;

    public OrderService(IOrderRepository orderRepo,
                        IInventoryRepository inventoryRepo,
                        IOrderInventoryRepository orderInvRepo) {
        this.orderRepository = orderRepo;
        this.orderInvRepository = orderInvRepo;
        this.inventoryRepository = inventoryRepo;
    }

    private void checkInventoryIdExists (UUID uuid) {
        InventoryEntity invEntity;
        try {
            invEntity = inventoryRepository.getById(uuid);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Inventory with id = "+ uuid + " not found",e);
        }
    }


    private void checkInventoryId(UUID uuid, long count) {
        InventoryEntity invEntity;
        try {
            invEntity = inventoryRepository.getById(uuid);
            if (invEntity.getCount() < count) {
                throw new NotEnoughInventoryException(
                        "Inventory not enough quantity " + invEntity.getCount() + "/" + count);
            }
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Inventory with id = "+ uuid + " not found",e);
        } catch (NotEnoughInventoryException e) {
            throw new NotEnoughInventoryException(e);
        }
    }

    private void checkOrderExists(UUID uuid) {
        OrderEntity entity;
        try {
            entity = orderRepository.getById(uuid);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Order with id = "+ uuid + " not found",e);
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
    public OrderInfo getOrderInfo(IdMessage request) {
        OrderInfoEntity orderInfo = orderRepository.getOrderInfo(UUID.fromString(request.getId()));
        log.info("returned orderInfo {}", orderInfo.toString());
        OrderInfo result = OrderInfo.newBuilder()
                .setId(orderInfo.getOrder().getId().toString())
                .setUser(orderInfo.getOrder().getUsername())
                .setStatus(orderInfo.getOrder().getStatus())
                .setCreatedAt(timestampToProto(orderInfo.getOrder().getCreated_at()))
                .setInvCount(orderInfo.getInvQuantity())
                .build();
        return result;
    }

    @Override
    public OrderDataWithId getOrderById(IdMessage request) {
        UUID uuid = UUID.fromString(request.getId());

        OrderEntity entity = orderRepository.getById(uuid);
        log.info("entity by id {}", entity);
        OrderDataWithId result = OrderDataWithId.newBuilder()
                .setId(entity.getId().toString())
                .setUser(entity.getUsername())
                .setStatus(entity.getStatus())
                .setCreatedAt(timestampToProto(entity.getCreated_at()))
                .build();
        return result;
    }

    @Override
    public BoolMessage addInventory(OrderItem request) {

        UUID orderId = UUID.fromString(request.getOrderId());
        UUID inventoryId = UUID.fromString(request.getInventoryId());
        long quantity = request.getQuantity();

        checkInventoryId(inventoryId, quantity);
        checkOrderExists(orderId);

        boolean result = orderInvRepository.create(
                orderId,
                inventoryId,
                quantity
        );
        log.info("Inventory {} added to order {}: {}", inventoryId, orderId, result);
        return BoolMessage.newBuilder().setResult(result).build();
    }
}
