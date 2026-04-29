package omsu.inventory.controller;

import com.google.protobuf.util.JsonFormat;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.inventory.repository.impl.InventoryRepository;
import omsu.inventory.services.impl.InventoryService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import java.io.IOException;

import static com.google.protobuf.util.JsonFormat.printer;

public class BaseTest {
    protected static JdbcTemplate jdbcTemplate;
    protected static ManagedChannel channel;
    protected static InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;
    protected static Server grpcServer;
    protected static final String PRODUCT_NAME = "Plain";


    protected final JsonFormat.Printer jsonPrinter = printer();

    private static void initWithPostgresDb() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5437/inventorydb");
        dataSource.setUsername("postgres");
        dataSource.setPassword("secret");

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static void initWithH2Embedded(){
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("init.sql")
//                .addScript("V2__schema.sql")    // опционально: тестовые данные
                .build();

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeAll
    static void setUp() throws IOException {
        initWithPostgresDb();
        String serverName = InProcessServerBuilder.generateName();

        InventoryCRUDImpl inventoryCRUDService =
                new InventoryCRUDImpl(new InventoryService(new InventoryRepository(jdbcTemplate)));
        ValidationInterceptor validationInterceptor = new ValidationInterceptor();

        grpcServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(inventoryCRUDService)
                .intercept(validationInterceptor)
                .build()
                .start();

        // СОЗДАЕМ КЛИЕНТ ДЛЯ IN-MEMORY СЕРВЕРА
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

//        // Подключаемся к реальному серверу Spring Boot
//        channel = ManagedChannelBuilder
//                .forAddress("localhost", 9090)
//                .usePlaintext()
//                .build();

//        jdbcTemplate.update("DELETE FROM inventory WHERE name = ?", PRODUCT_NAME);
        inventoryBlockingStub = InventoryCRUDGrpc.newBlockingStub(channel);

    }

    @AfterAll
    static void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }

        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }
}
