package omsu;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import omsu.exception.GrpcExceptionAdvice;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.controller.InventoryCRUDImpl;
import omsu.controller.OrderGrpcImpl;
import omsu.controller.ValidationInterceptor;
import omsu.grpc.OrderGrpc;
import omsu.repository.impl.InventoryRepository;
import omsu.repository.impl.OrderInventoryRepository;
import omsu.repository.impl.OrderRepository;
import omsu.services.impl.InventoryService;
import omsu.services.impl.OrderService;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

import static com.google.protobuf.util.JsonFormat.printer;
import static io.qameta.allure.Allure.step;

/**
 * Попытка написать базовый класс без использования спринга и его бинов.
 * Не получилось - не перехватываются нормально бизнес-исключения.
 */
public abstract class BaseTest {
    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static final String PRODUCT_NAME = "Plain";
    protected static final String johnFSmith = "John F Smith";


    protected final JsonFormat.Printer jsonPrinter = printer();
    protected final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    protected static JdbcTemplate jdbcTemplate;
    protected InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;
    protected OrderGrpc.OrderBlockingStub orderBlockingStub;

    private Server server;
    private ManagedChannel channel;

    private static void initWithPostgresDb() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5437/inventorydb");
        dataSource.setUsername("postgres");
        dataSource.setPassword("secret");
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected void cleanDB(){
        jdbcTemplate.update("DELETE FROM inventory_schema.inventory;");
        jdbcTemplate.update("DELETE FROM inventory_schema.orders;");
    }
    @BeforeAll
    static void setUpDB(){
        log.info("BaseTest: BeforeAll init DB");
        initWithPostgresDb();
    }

    @BeforeEach
    void setUpEach() throws Exception {
        log.info("BeforeEach");
        cleanDB();
        log.info("Clean DB");

        String serverName = InProcessServerBuilder.generateName();
        server = grpcCleanup.register(
                InProcessServerBuilder.forName(serverName)
                        .addService(new InventoryCRUDImpl(new InventoryService(
                                new InventoryRepository(jdbcTemplate))))
                        .addService(new OrderGrpcImpl(
                                new OrderService(
                                    new OrderRepository(jdbcTemplate),
                                    new InventoryRepository(jdbcTemplate),
                                    new OrderInventoryRepository(jdbcTemplate)
                                )
                        ))
                        .intercept(new ValidationInterceptor())
                        .build()
                        .start()
        );
        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName)
                        .build()
        );

        inventoryBlockingStub = InventoryCRUDGrpc.newBlockingStub(channel);
        orderBlockingStub = OrderGrpc.newBlockingStub(channel);
    }
//
//    @BeforeEach
//    void setUpEach() {
//        log.info("BaseTest: BeforeEach");
//        step("Clean database before test", this::cleanDB);
//    }
//    @AfterEach
//    void tearDownEach() {
//        log.info("BaseTest: AfterEach");
//        step("Clean database after test", this::cleanDB);
//    }
}
