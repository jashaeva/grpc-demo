package omsu.inventory.controller;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.service.GrpcService;
import omsu.grpc.*;

import omsu.inventory.model.InventoryEntity;
import omsu.inventory.repository.IInventoryRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.UUID;

@GrpcService
public class InventoryCRUDImpl extends InventoryCRUDGrpc.InventoryCRUDImplBase {

    @Autowired
    IInventoryRepo repo;

    @Override
    public void test(Empty request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void createInventory(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {
        try {
            InventoryEntity entity = new InventoryEntity(request.getName(), request.getCount());
            UUID uuid = repo.create(entity);

            CreateResponse response = CreateResponse.newBuilder()
                    .setId(uuid.toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void editInventory (InventoryData request, StreamObserver<Empty> responseObserver) {
        try {
            InventoryEntity entity = new InventoryEntity(
                    UUID.fromString(request.getId()),
                    request.getName(),
                    request.getCount());
            repo.update(entity);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }

    }

    @Override
    public void getInventory (InventoryByIdRequest request, StreamObserver<InventoryData> responseObserver) {
        try {
            InventoryEntity entity = repo.getById(UUID.fromString(request.getId()));

            InventoryData response = InventoryData.newBuilder()
                    .setId(String.valueOf(entity.getId()))
                    .setName(entity.getName())
                    .setCount(entity.getCount())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }

    }

    @Override
    public void deleteInventory (InventoryByIdRequest request, StreamObserver<Empty> responseObserver) {
        try {
            repo.deleteById(UUID.fromString(request.getId()));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }

    }
}
