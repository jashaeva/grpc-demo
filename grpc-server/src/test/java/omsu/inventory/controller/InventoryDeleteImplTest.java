package omsu.inventory.controller;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import omsu.grpc.CreateRequest;
import omsu.grpc.CreateResponse;
import omsu.grpc.InventoryByIdRequest;
import omsu.grpc.InventoryCRUDGrpc;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.google.protobuf.util.JsonFormat.printer;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class InventoryDeleteImplTest extends BaseTest {

    private static final String PRODUCT_NAME = "Plain";

    @BeforeEach
    void setUpEach() {
        if (jdbcTemplate != null) {
            jdbcTemplate.update("DELETE FROM inventory WHERE name = ?", PRODUCT_NAME);
        }
    }

    @Test
    void testDeleteInventory() throws InvalidProtocolBufferException {
        // Сначала создаем
        CreateRequest createRequest = CreateRequest.newBuilder()
                .setCount(100L)
                .setName(PRODUCT_NAME)
                .build();
        CreateResponse createResponse = inventoryBlockingStub.createInventory(createRequest);
        UUID uuid = UUID.fromString(createResponse.getId());
        System.out.println("uuid " + uuid);

        InventoryByIdRequest request = InventoryByIdRequest.newBuilder()
                .setId(uuid.toString())
                .build();
        System.out.println("request to delete" + jsonPrinter.print(request));
        Empty response = inventoryBlockingStub.deleteInventory(request);
        System.out.println("response " + jsonPrinter.print(response));
    }
}