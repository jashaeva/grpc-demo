package omsu.inventory.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.*;
import omsu.grpc.CreateRequest;
import omsu.grpc.IdMessage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


class InventoryCreateImplTest extends BaseTest {

    @BeforeEach
    void setUpEach() {
        jdbcTemplate.update("DELETE FROM inventory WHERE name = ?", PRODUCT_NAME);
    }

    @Test
    void testCreateInventory_pos() throws InvalidProtocolBufferException {
        CreateRequest request = CreateRequest.newBuilder()
                .setCount(100L)
                .setName(PRODUCT_NAME)
                .build();

        System.out.println("request " + jsonPrinter.print(request));

        IdMessage response = inventoryBlockingStub
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