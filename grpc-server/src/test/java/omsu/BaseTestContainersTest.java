package omsu;

import com.google.protobuf.util.JsonFormat;
import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.OrderGrpc;
import omsu.steps.InventoryGrpcSteps;
import omsu.steps.OrderGrpcSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static com.google.protobuf.util.JsonFormat.printer;

@ActiveProfiles("test-containers")
@SpringBootTest
public abstract class BaseTestContainersTest {
    private static final String TEST_SERVER_NAME = "test-server-" + (UUID.randomUUID());
    protected static final JsonFormat.Printer jsonPrinter = printer();

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurePostgreSQLProperties(DynamicPropertyRegistry registry) {
            var POSTGRES_CONTAINER = ContainerHolder.getPostgres();
            registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
            registry.add("spring.datasource.driver-class-name",POSTGRES_CONTAINER::getDriverClassName);
            registry.add("spring.flyway.locations", () -> "classpath:db/migration/postgres");
            registry.add("spring.testcontainers.enabled", () -> "true");
    }

    @DynamicPropertySource
    static void configureKafkaProperties(DynamicPropertyRegistry registry) {
            var KAFKA_CONTAINER = ContainerHolder.getKafka();
            registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    @DynamicPropertySource
    static void configureGrpcProperties(DynamicPropertyRegistry registry) {
            registry.add("grpc.server.in-process-name", () -> TEST_SERVER_NAME);
            registry.add("grpc.client.test-server.address", () -> "in-process:" + TEST_SERVER_NAME);
    }

    @GrpcClient("test-server")
    protected InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;

    @GrpcClient("test-server")
    protected OrderGrpc.OrderBlockingStub orderBlockingStub;

    protected OrderGrpcSteps orderGrpcSteps;
    protected InventoryGrpcSteps inventoryGrpcSteps;

    @PostConstruct
    void initSteps() {
        kafkaTemplate.send("grpc-logs","");
        this.orderGrpcSteps = new OrderGrpcSteps(orderBlockingStub);
        this.inventoryGrpcSteps = new InventoryGrpcSteps(inventoryBlockingStub);
    }
}
