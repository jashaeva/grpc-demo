package omsu.inventory;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import omsu.grpc.CreateRequest;
import omsu.grpc.IdMessage;
import omsu.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class InventoryDeleteImplTest extends BaseTest {

    private static final String PRODUCT_NAME = "Plain";

    @BeforeEach
    void setUpEach() {
        if (jdbcTemplate != null) {
            jdbcTemplate.update("DELETE FROM inventory_schema.inventory WHERE name = ?;", PRODUCT_NAME);
        }
    }

    @Test
    void testDeleteInventory() throws InvalidProtocolBufferException {
        // Сначала создаем
        CreateRequest createRequest = CreateRequest.newBuilder()
                .setCount(100L)
                .setName(PRODUCT_NAME)
                .build();
        IdMessage createResponse = inventoryBlockingStub.createInventory(createRequest);
        UUID uuid = UUID.fromString(createResponse.getId());
        System.out.println("uuid " + uuid);

        IdMessage request = IdMessage.newBuilder()
                .setId(uuid.toString())
                .build();
        System.out.println("request to delete" + jsonPrinter.print(request));
        Empty response = inventoryBlockingStub.deleteInventory(request);
        System.out.println("response " + jsonPrinter.print(response));
    }
}