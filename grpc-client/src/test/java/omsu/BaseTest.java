package omsu;

import omsu.api.RestToGrpcClientApi;
import omsu.kafka.KafkaLogListener;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseTest {
    private final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected RestToGrpcClientApi api;

    @LocalServerPort
    private int port;

    @Autowired
    protected KafkaLogListener kafkaLogListener;

    @BeforeEach
    void setUpApi() {
        api = new RestToGrpcClientApi(port);
    }

    @DynamicPropertySource
    static void configureGrpcServerProperties(DynamicPropertyRegistry registry) {
        var grpc = ContainerHolder.getGrpcServer();
        registry.add("grpc.client.inventory-service.address", () ->
                "localhost:" + grpc.getMappedPort(9090));
    }

    @DynamicPropertySource
    static void configurePostgreSQLProperties(DynamicPropertyRegistry registry) {
        var POSTGRES_CONTAINER = ContainerHolder.getPostgres();
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name",POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.schemas", () -> "inventory_schema");
        registry.add("spring.testcontainers.enabled", () -> "true");
    }

    @DynamicPropertySource
    static void configureKafkaProperties(DynamicPropertyRegistry registry) {
        var KAFKA_CONTAINER = ContainerHolder.getKafka();
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }
}
