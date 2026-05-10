package omsu.steps;

import com.google.protobuf.Empty;
import io.qameta.allure.Step;
import omsu.grpc.*;

public class InventoryGrpcSteps {
    private final InventoryCRUDGrpc.InventoryCRUDBlockingStub stub;

    public InventoryGrpcSteps(InventoryCRUDGrpc.InventoryCRUDBlockingStub stub) {
        this.stub = stub;
    }

    @Step("gRPC: Create inventory item")
    public IdMessage createInventory(InventoryMessage request) {
        return stub.createInventory(request);
    }

    @Step("gRPC: Delete inventory item")
    public BoolMessage deleteInventory(IdMessage request) {
        return stub.deleteInventory(request);
    }

    @Step("gRPC: Get inventory item with ID {id}")
    public InventoryData getInventory(String id) {
        IdMessage request = IdMessage.newBuilder().setId(id).build();
        return stub.getInventory(request);
    }
}
