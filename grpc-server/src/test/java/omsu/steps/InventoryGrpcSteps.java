package omsu.steps;

import com.google.protobuf.Empty;
import io.qameta.allure.Step;
import omsu.grpc.*;

import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.InventoryTestDataFactory.createInventoryMessage;
import static omsu.utils.DataUtils.randomInventory;
import static omsu.utils.DataUtils.randomQuantity;

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

    @Step("gRPC: Create inventory item {name} count={count}")
    public InventoryData createInventory(final String name, long count) {
        InventoryMessage inventory = createInventoryMessage(name, count);
        IdMessage created = stub.createInventory(inventory);
        return InventoryData.newBuilder()
                .setId(created.getId())
                .setName(name)
                .setCount(count)
                .build();
    }

    @Step("gRPC: Create inventory with random data")
    public InventoryData createInventory() {
        return createInventory(randomInventory(), randomQuantity());
    }
}
