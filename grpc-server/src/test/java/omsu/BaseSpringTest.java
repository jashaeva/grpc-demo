package omsu;

import com.google.protobuf.util.JsonFormat;
import net.devh.boot.grpc.client.inject.GrpcClient;
import omsu.grpc.InventoryCRUDGrpc;
import omsu.grpc.OrderGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

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

//    @DynamicPropertySource
//    static void properties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5437/inventorydb");
//        registry.add("spring.datasource.username", () -> "postgres");
//        registry.add("spring.datasource.password", () -> "secret");
//    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @GrpcClient("test-server")
    protected InventoryCRUDGrpc.InventoryCRUDBlockingStub inventoryBlockingStub;

    @GrpcClient("test-server")
    protected OrderGrpc.OrderBlockingStub orderBlockingStub;
}
