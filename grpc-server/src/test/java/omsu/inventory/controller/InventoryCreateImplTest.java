package omsu.inventory.controller;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.envoyproxy.pgv.ValidationException;
import io.grpc.*;
import omsu.grpc.CreateRequest;
import omsu.grpc.CreateResponse;
import omsu.grpc.InventoryByIdRequest;
import omsu.grpc.InventoryCRUDGrpc;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.google.protobuf.util.JsonFormat.printer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryCreateImplTest extends BaseTest {
    private static final String PRODUCT_NAME = "Plain";

    @BeforeEach
    void setUpEach() {
        if (jdbcTemplate != null) {
            jdbcTemplate.update("DELETE FROM inventory WHERE name = ?", PRODUCT_NAME);
        }
    }

    @Test
    void testCreateInventory_pos() throws InvalidProtocolBufferException {
        CreateRequest request = CreateRequest.newBuilder()
                .setCount(100L)
                .setName(PRODUCT_NAME)
                .build();

        System.out.println("request " + jsonPrinter.print(request));

        CreateResponse response = inventoryBlockingStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .createInventory(request);

        String actualId = response.getId();
        assertNotNull(actualId);
        System.out.println("response " + jsonPrinter.print(response));
    }

    @Test
    void testCreateInvalidInventory_neg() {
        CreateRequest createRequest = CreateRequest.newBuilder()
                .setCount(-1L)
                .setName(PRODUCT_NAME)
                .build();

        StatusRuntimeException thrown =
                Assertions.assertThrows(StatusRuntimeException.class, () ->
                        inventoryBlockingStub.createInventory(createRequest));
        System.out.println("**** ");
        System.out.println(thrown.getStatus());
        System.out.println(thrown.getMessage());
        System.out.println("**** ");
        // Проверяем код ошибки
        assertEquals(Status.INVALID_ARGUMENT.getCode(), thrown.getStatus().getCode());

        // Проверяем сообщение об ошибке валидации (не "Failed to create...")
        assertTrue(thrown.getMessage().contains("must be greater than or equal to 1"));
        assertTrue(thrown.getMessage().contains("count:"));
    }
}