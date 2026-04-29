package omsu.inventory.controller;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import omsu.grpc.*;

import omsu.inventory.model.InventoryEntity;
import omsu.inventory.services.IInventoryService;

import java.util.UUID;

@GrpcService
public class InventoryCRUDImpl extends InventoryCRUDGrpc.InventoryCRUDImplBase {

    private final IInventoryService service;

    public InventoryCRUDImpl(IInventoryService service) {
        this.service = service;
    }

    @Override
    public void createInventory(CreateRequest request, StreamObserver<IdMessage> responseObserver) {

        InventoryEntity entity = new InventoryEntity(request.getName(), request.getCount());
        UUID uuid = service.create(entity);

        IdMessage response = IdMessage.newBuilder()
                .setId(uuid.toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void editInventory (InventoryData request,
                               StreamObserver<Empty> responseObserver) {

        InventoryEntity entity = new InventoryEntity(
                UUID.fromString(request.getId()),
                request.getName(),
                request.getCount());
        boolean result = service.update(entity);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getInventory (IdMessage request,
                              StreamObserver<InventoryData> responseObserver) {

        InventoryEntity entity = service.getById(UUID.fromString(request.getId()));

        InventoryData response = InventoryData.newBuilder()
                .setId(String.valueOf(entity.getId()))
                .setName(entity.getName())
                .setCount(entity.getCount())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteInventory (IdMessage request,
                                 StreamObserver<Empty> responseObserver) {
            service.deleteById(UUID.fromString(request.getId()));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
    }
}
