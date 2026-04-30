package omsu;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import omsu.exception.GrpcExceptionAdvice;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.controller.InventoryCRUDImpl;
import omsu.controller.OrderGrpcImpl;
import omsu.controller.ValidationInterceptor;
import omsu.grpc.OrderGrpc;
import omsu.repository.impl.InventoryRepository;
import omsu.repository.impl.OrderRepository;
import omsu.services.impl.InventoryService;
import omsu.services.impl.OrderService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import java.io.IOException;

import static com.google.protobuf.util.JsonFormat.printer;

public abstract class BaseTest {
    protected static final Logger log = LoggerFactory.getLogger(GrpcExceptionAdvice.class);

    protected static JdbcTemplate jdbcTemplate;
    protected static ManagedChannel channel;
    protected static InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;
    protected static OrderGrpc.OrderBlockingStub orderBlockingStub;
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

        InventoryRepository invRepository = new InventoryRepository(jdbcTemplate);
        OrderRepository orderRepository = new OrderRepository(jdbcTemplate);
        InventoryCRUDImpl inventoryCRUDService = new InventoryCRUDImpl(new InventoryService(invRepository));
        OrderGrpcImpl orderGrpcService = new OrderGrpcImpl(new OrderService(orderRepository, invRepository));
        ValidationInterceptor validationInterceptor = new ValidationInterceptor();

        grpcServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(inventoryCRUDService)
                .addService(orderGrpcService)
                .intercept(validationInterceptor)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
        inventoryBlockingStub = InventoryCRUDGrpc.newBlockingStub(channel);
        orderBlockingStub = OrderGrpc.newBlockingStub(channel);

    }

    @AfterAll
    static void tearDown() {
        if (channel != null) {
            channel.shutdownNow();  // Принудительное немедленное закрытие
        }
        if (grpcServer != null) {
            grpcServer.shutdownNow();
        }
    }
}
