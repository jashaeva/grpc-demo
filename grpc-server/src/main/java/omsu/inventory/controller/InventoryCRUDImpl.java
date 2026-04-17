package omsu.inventory.controller;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import omsu.grpc.*;

import omsu.inventory.exception.EntityNotFoundException;
import omsu.inventory.model.InventoryEntity;
import omsu.inventory.repository.IInventoryRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@GrpcService
public class InventoryCRUDImpl extends InventoryCRUDGrpc.InventoryCRUDImplBase {

    @Autowired
    IInventoryRepo repo;

    @Override
    public void createInventory(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {

            InventoryEntity entity = new InventoryEntity(request.getName(), request.getCount());
            UUID uuid = repo.create(entity);

            CreateResponse response = CreateResponse.newBuilder()
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
            boolean result = repo.update(entity);
            responseObserver.onCompleted();
    }

    @Override
    public void getInventory (InventoryByIdRequest request,
                              StreamObserver<InventoryData> responseObserver) {

            InventoryEntity entity = repo.getById(UUID.fromString(request.getId()));

            InventoryData response = InventoryData.newBuilder()
                    .setId(String.valueOf(entity.getId()))
                    .setName(entity.getName())
                    .setCount(entity.getCount())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
    }

    @Override
    public void deleteInventory (InventoryByIdRequest request,
                                 StreamObserver<Empty> responseObserver) {
            repo.deleteById(UUID.fromString(request.getId()));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
    }
}
