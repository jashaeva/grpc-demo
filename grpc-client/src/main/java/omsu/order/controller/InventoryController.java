package omsu.order.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.IdMessage;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.order.model.Inventory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @GrpcClient("inventory-service")
    private InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryStub;

    @GetMapping("/api/inventory/{id}")
    public Inventory getProduct(@PathVariable String id) {
        var request = IdMessage.newBuilder().setId(id).build();
        var response = inventoryStub.getInventory(request);
        return new Inventory(response.getId(), response.getName(), response.getCount());
    }
}