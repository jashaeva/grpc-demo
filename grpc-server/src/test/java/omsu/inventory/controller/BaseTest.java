package omsu.inventory.controller;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import omsu.grpc.InventoryCRUDGrpc;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.google.protobuf.util.JsonFormat.printer;

public class BaseTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    protected static ManagedChannel channel;
    protected static InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;
    protected final JsonFormat.Printer jsonPrinter = printer();

    @BeforeAll
    static void setUp() {
        // Подключаемся к реальному серверу Spring Boot
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        inventoryBlockingStub = InventoryCRUDGrpc.newBlockingStub(channel);
    }
    @AfterAll
    static void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
