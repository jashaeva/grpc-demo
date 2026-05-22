package omsu;

import omsu.grpc.InventoryCRUDGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.grpc.client.GrpcChannelFactory;

@SpringBootApplication
@Import(InventoryCRUDGrpc.class)
public class  GrpcClientApplication  {

    @Bean
    public InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryStub(GrpcChannelFactory channels) {
        return InventoryCRUDGrpc.newBlockingStub(channels.createChannel("inventory-service").build());
    }

    public static void main(String[] args) {
        SpringApplication.run( GrpcClientApplication.class, args);
    }
}
