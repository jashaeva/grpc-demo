package omsu.controller;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import omsu.grpc.*;

import omsu.kafka.KafkaLogProducer;
import omsu.model.InventoryEntity;
import omsu.dto.LogEvent;
import omsu.services.IInventoryService;

import java.util.UUID;

@GrpcService
public class InventoryCRUDImpl extends InventoryCRUDGrpc.InventoryCRUDImplBase {

    private final IInventoryService service;
    private final KafkaLogProducer kafkaLogProducer;

    public InventoryCRUDImpl(IInventoryService service, KafkaLogProducer kafkaProducer) {
        this.kafkaLogProducer = kafkaProducer;
        this.service = service;
    }

    @Override
    public void createInventory(InventoryMessage request, StreamObserver<IdMessage> responseObserver) {

        InventoryEntity entity = new InventoryEntity(request.getName(), request.getCount());
        UUID uuid = service.create(entity);

        IdMessage response = IdMessage.newBuilder()
                .setId(uuid.toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        kafkaLogProducer.sendLog(new LogEvent("createInventory", request.toString(), response.toString()));

    }

    @Override
    public void editInventory (InventoryData request,
                               StreamObserver<Empty> responseObserver) {

        InventoryEntity entity = new InventoryEntity(
                UUID.fromString(request.getId()),
                request.getName(),
                request.getCount());
        boolean response = service.update(entity);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
        kafkaLogProducer.sendLog(new LogEvent("editInventory", request.toString(),
                (response)? "TRUE": "FALSE"));
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
        kafkaLogProducer.sendLog(new LogEvent("getInventory", request.toString(), response.toString()));
    }

    @Override
    public void deleteInventory (IdMessage request,
                                 StreamObserver<BoolMessage> responseObserver) {
            boolean res = service.deleteById(UUID.fromString(request.getId()));

            BoolMessage response = BoolMessage.newBuilder().setResult(res).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            kafkaLogProducer.sendLog(new LogEvent("deleteInventory", request.toString(),
                (response.getResult())? "TRUE": "FALSE"));
    }
}
