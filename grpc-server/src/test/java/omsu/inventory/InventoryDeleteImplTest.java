package omsu.inventory;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import omsu.BaseSpringTest;
import omsu.grpc.BoolMessage;
import omsu.grpc.CreateRequest;
import omsu.grpc.IdMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static omsu.allure.AllureAttachments.attachTable;
import static omsu.allure.AllureAttachments.attachText;
import static omsu.steps.InventoryTestDataFactory.createIdMessage;
import static omsu.steps.InventoryTestDataFactory.createRequest;
import static omsu.utils.DataUtils.randomInventory;
import static omsu.utils.DataUtils.randomQuantity;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Inventory Management")
@Feature("Delete Inventory Operations")
class InventoryDeleteImplTest extends BaseSpringTest {
    private static final Logger log = LoggerFactory.getLogger(InventoryDeleteImplTest.class);
    private String testInventoryId;
    private String inventory = randomInventory();
    private long count = randomQuantity();

    @Test
    @DisplayName("TC-04: Delete existing inventory item")
    @Description("Positive test: Delete inventory item that exists in system with double-check")
    void testDeleteInventory() {
        step("Create test inventory item for deletion",
                () -> {
                    CreateRequest createRequest = createRequest(inventory, count);
                    IdMessage createResponse = inventoryBlockingStub.createInventory(createRequest);
                    testInventoryId = createResponse.getId();
                    attachText("Request to delete created: ", jsonPrinter.print(createResponse));
                });
        step("Delete inventory item", () -> {
            IdMessage deleteRequest = createIdMessage(testInventoryId);
            step("Delete and verify", () ->{
                BoolMessage response = inventoryBlockingStub.deleteInventory(deleteRequest);
                assertTrue(response.getResult());
            });

            step("Verify item no longer exists", () -> {
                StatusRuntimeException thrown = assertThrows(
                        StatusRuntimeException.class,
                        () -> inventoryBlockingStub.getInventory(deleteRequest)
                );
                attachText("thrown ", thrown.getMessage());
                assertEquals(Status.NOT_FOUND.getCode(), thrown.getStatus().getCode());
            });
        });
    }

    @Test
    @DisplayName("TC-05: Delete non-existent inventory item")
    @Description("Negative test: Delete inventory with UUID that doesn't exist")
    void testDeleteNonExistentInventory_neg() {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        step("Attempt to delete non-existent inventory", () -> {
            IdMessage request = createIdMessage(nonExistentId);
            attachText("request", jsonPrinter.print(request));
            step("Get all orders BEFORE", ()->{
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT * FROM inventory_schema.inventory;");
                attachTable(rows, "inventory" );
            });
            BoolMessage response = inventoryBlockingStub.deleteInventory(request);
            assertFalse(response.getResult());
        });
    }
}