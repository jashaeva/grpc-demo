package omsu;

import com.google.protobuf.util.JsonFormat;
import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.OrderGrpc;
import omsu.steps.InventoryGrpcSteps;
import omsu.steps.OrderGrpcSteps;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.google.protobuf.util.JsonFormat.printer;

/*
 * Локально, используется БД Н2
 * Базовый класс для проверки модульных тестов (юнит)
 * Чтобы пользоваться, эти юнит тесты надо написать
 */
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
public abstract class BaseGrpcTest {
    private static final String TEST_SERVER_NAME = "test-server-" + UUID.randomUUID();
    protected static final JsonFormat.Printer jsonPrinter = printer();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("grpc.server.in-process-name", () -> TEST_SERVER_NAME);
        registry.add("grpc.client.test-server.address",
                () -> "in-process:" + TEST_SERVER_NAME);
    }

    @GrpcClient("test-server")
    protected InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;

    @GrpcClient("test-server")
    protected OrderGrpc.OrderBlockingStub orderBlockingStub;

    protected OrderGrpcSteps orderGrpcSteps;
    protected InventoryGrpcSteps inventoryGrpcSteps;

    @PostConstruct
    void initSteps() {
        this.orderGrpcSteps = new OrderGrpcSteps(orderBlockingStub);
        this.inventoryGrpcSteps = new InventoryGrpcSteps(inventoryBlockingStub);
    }
}
