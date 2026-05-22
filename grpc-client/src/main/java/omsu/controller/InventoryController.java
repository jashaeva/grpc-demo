package omsu.controller;

import omsu.grpc.IdMessage;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.InventoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class InventoryController {
    @Autowired
    private InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryStub;

    @GetMapping("/api/inventory/{id}")
    public InventoryData getProduct(@PathVariable String id) {
        var request = IdMessage.newBuilder().setId(id).build();
        return inventoryStub.getInventory(request);
    }
}