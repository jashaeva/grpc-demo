package omsu;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.controller.InventoryCRUDImpl;
import omsu.controller.OrderGrpcImpl;
import omsu.controller.ValidationInterceptor;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.OrderGrpc;
import omsu.repository.impl.InventoryRepository;
import omsu.repository.impl.OrderRepository;
import omsu.services.impl.InventoryService;
import omsu.services.impl.OrderService;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.google.protobuf.util.JsonFormat.printer;
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "grpc.server.in-process-name=test-server",
        "grpc.server.port=-1",
        "grpc.client.test-server.address=in-process:test-server",
        "grpc.client.test-server.negotiation-type=PLAINTEXT",
        "grpc.client.test-server.security.enabled=false",
        "spring.flyway.enabled=true"
})
public abstract class BaseSpringTest {

    protected static final JsonFormat.Printer jsonPrinter = printer();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5437/inventorydb");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "secret");
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @GrpcClient("test-server")
    protected InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;

    @GrpcClient("test-server")
    protected OrderGrpc.OrderBlockingStub orderBlockingStub;

//    @BeforeEach
//    void setUp() {
//        cleanDB();
//    }
//
//    @AfterEach
//    void tearDown() {
//        cleanDB();
//    }

    private void cleanDB() {
        jdbcTemplate.update("DELETE FROM inventory_schema.inventory;");
        jdbcTemplate.update("DELETE FROM inventory_schema.orders;");
    }

}