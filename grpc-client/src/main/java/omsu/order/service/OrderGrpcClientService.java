package omsu.order.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import omsu.grpc.CreateRequest;
import omsu.grpc.IdMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.stereotype.Component;
import omsu.grpc.InventoryCRUDGrpc;
import org.apache.commons.lang3.RandomStringUtils;

@Component
@ImportAutoConfiguration(GrpcClientAutoConfiguration.class)
public class OrderGrpcClientService implements CommandLineRunner {

    @GrpcClient("inventory-service")
    private InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryStub;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Testing gRPC connection...");

            CreateRequest request = CreateRequest.newBuilder()
                    .setName("test-"+ RandomStringUtils.randomAlphabetic(10))
                    .setCount(0)
                    .build();

            IdMessage response = inventoryStub.createInventory(request);

            System.out.println("gRPC test successful: " + response);
        } catch (Exception e) {
            System.err.println("gRPC test failed: " + e.getMessage());
        }
    }
}