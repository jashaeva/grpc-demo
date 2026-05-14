package omsu;

import com.google.protobuf.util.JsonFormat;
import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.OrderGrpc;
import omsu.steps.InventoryGrpcSteps;
import omsu.steps.OrderGrpcSteps;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.google.protobuf.util.JsonFormat.printer;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Execution(ExecutionMode.SAME_THREAD)
public abstract class BaseTestcontainersTest {
    private static final String TEST_SERVER_NAME = "test-server-" + (UUID.randomUUID());
    protected static final JsonFormat.Printer jsonPrinter = printer();

    static {
        try {
            Class.forName("omsu.config.TestContainerConfig");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Существующие настройки БД
        registry.add("spring.datasource.url", () -> System.getProperty("spring.datasource.url"));
        registry.add("spring.datasource.username", () -> System.getProperty("spring.datasource.username"));
        registry.add("spring.datasource.password", () -> System.getProperty("spring.datasource.password"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.testcontainers.enabled", () -> "true");

        // КЛЮЧЕВОЕ: уникальный in-process сервер для каждого тестового класса
        registry.add("grpc.server.in-process-name", () -> TEST_SERVER_NAME);
        registry.add("grpc.client.test-server.address", () -> "in-process:" + TEST_SERVER_NAME);
    }
    @Autowired
    protected JdbcTemplate jdbcTemplate;

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
