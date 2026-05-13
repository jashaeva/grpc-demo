package omsu;

import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.OrderGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.google.protobuf.util.JsonFormat.printer;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
public abstract class BaseTestcontainersTest {

    protected static final JsonFormat.Printer jsonPrinter = printer();

    static {
        try {
            Class.forName("omsu.config.TestContainerConfig");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void overrideDatasourceProperties(DynamicPropertyRegistry registry) {
        // Переопределяем только то, что относится к БД
        registry.add("spring.datasource.url",()-> System.getProperty("spring.datasource.url"));
        registry.add("spring.datasource.username",()-> System.getProperty("getUsername"));
        registry.add("spring.datasource.password", ()->System.getProperty("getPassword"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Меняем путь к миграциям на PostgreSQL-версию
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        // Отключаем H2-специфичные настройки
        registry.add("spring.testcontainers.enabled", () -> "true");
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @GrpcClient("test-server")
    protected InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;

    @GrpcClient("test-server")
    protected OrderGrpc.OrderBlockingStub orderBlockingStub;
}
