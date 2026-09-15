package omsu.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.IdMessage;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.InventoryData;
import omsu.grpc.InventoryMessage;
import omsu.model.IdDTO;
import omsu.model.Inventory;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
    @GrpcClient("inventory-service")
    private InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryStub;

    @GetMapping("/api/inventory/{id}")
    public Inventory getProduct(@PathVariable String id) {
        var request = IdMessage.newBuilder().setId(id).build();
        InventoryData response = inventoryStub.getInventory(request);
        return new Inventory(response.getId(), response.getName(), response.getCount());
    }

    @PostMapping("/api/inventory")
    public IdDTO createProduct(@RequestBody Inventory inventory) {
        InventoryMessage data = InventoryMessage.newBuilder()
                .setName(inventory.name())
                .setCount(inventory.count())
                .build();
        IdMessage response = inventoryStub.createInventory(data);
        return new IdDTO(response.getId());
    }
}