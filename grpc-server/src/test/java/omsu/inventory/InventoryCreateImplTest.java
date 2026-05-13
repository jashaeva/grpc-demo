package omsu.inventory;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.*;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import omsu.BaseSpringTest;
import omsu.BaseTestcontainersTest;
import omsu.grpc.IdMessage;
import omsu.grpc.InventoryMessage;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.InventoryTestDataFactory.createInventoryMessage;
import static omsu.utils.DataUtils.randomInventory;
import static omsu.utils.DataUtils.randomQuantity;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Inventory Management")
@Feature("Create Inventory Operations")
class InventoryCreateImplTest extends BaseTestcontainersTest {
    private static final Logger log = LoggerFactory.getLogger(InventoryCreateImplTest.class);
    private final String inventory = randomInventory();
    private final long count = randomQuantity();

    @Test
    @DisplayName("TC-IMCr01: Create inventory item with valid data")
    @Description("Positive test: Create inventory item with valid count and name")
    void testCreateInventory_pos() throws InvalidProtocolBufferException {
        InventoryMessage request = createInventoryMessage(inventory, count);
        attachText("InventoryData request ", jsonPrinter.print(request));
        step("Execute create inventory call",
            () -> {
                IdMessage result = inventoryBlockingStub.createInventory(request);
                attachText("Created inventory ID ", result.getId());
                step("Verify response contains ID", () -> {
                    assertNotNull(result.getId(), "ID should not be null");
                    assertFalse(result.getId().isEmpty(), "ID should not be empty");
                });
            });
    }

    @Test
    @DisplayName("TC-IMCr02: Create inventory with negative count - should fail")
    @Description("Negative test: Create inventory with count = -1, expected INVALID_ARGUMENT")
    void testCreateInvalidInventory_neg() {
        InventoryMessage invalidRequest = createInventoryMessage(inventory, -1L);

        step("Execute create inventory with invalid data", () -> {
            StatusRuntimeException thrown = assertThrows(
                    StatusRuntimeException.class,
                    () -> inventoryBlockingStub.createInventory(invalidRequest)
            );

            step("Verify error status code", () ->
                    assertEquals(Status.INVALID_ARGUMENT.getCode(), thrown.getStatus().getCode())
            );

            step("Verify error message contains validation details", () -> {
                String errorMessage = thrown.getMessage();
                assertTrue(errorMessage.contains("must be greater than or equal to 1"));
                assertTrue(errorMessage.contains("count:"));
                log.info("Validation error received: {}", errorMessage);
            });
        });
    }

    @Test
    @DisplayName("TC-IMCr03: Create inventory with empty name - should fail")
    @Description("Negative test: Create inventory with empty name")
    void testCreateInventoryWithEmptyName_neg() {
        InventoryMessage requestWithEmptyName = createInventoryMessage("", count);

        step("Attempt to create inventory with empty name", () -> {
            StatusRuntimeException thrown = assertThrows(
                    StatusRuntimeException.class,
                    () -> inventoryBlockingStub.createInventory(requestWithEmptyName)
            );

            step("Verify validation error", () -> {
                assertEquals(Status.INVALID_ARGUMENT.getCode(), thrown.getStatus().getCode());
                assertTrue(thrown.getMessage().contains("length must be at least 1"));
            });
        });
    }
}