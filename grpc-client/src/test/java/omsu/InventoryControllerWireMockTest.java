package omsu;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import omsu.grpc.IdMessage;
import omsu.grpc.InventoryData;
import omsu.model.Inventory;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import omsu.grpc.InventoryCRUDGrpc;
import org.wiremock.grpc.Jetty12GrpcExtensionFactory;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import java.io.File;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.wiremock.grpc.dsl.WireMockGrpc.*;


@Disabled
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "grpc.client.inventory-service.address=static://localhost:${inventory-service.port}"
        }
)
@ActiveProfiles("test")
public class InventoryControllerWireMockTest {
    private static final Logger log = LoggerFactory.getLogger(InventoryControllerWireMockTest.class);
    static File descriptorFile = new File("grpc-common/src/test/resources/wiremock/grpc/services.dsc");

    private static WireMockServer wireMockServer;
    private static WireMockGrpcService mockInventoryService;

    @LocalServerPort
    int serverPort;

    private RestClient client;


    @DynamicPropertySource
    static void registerWireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("inventory-service.port", () -> String.valueOf(wireMockServer.port()));
        // Принудительно обновляем адрес gRPC клиента
        registry.add("grpc.client.inventory-service.address",
                () -> "static://localhost:" + wireMockServer.port());
    }

    @BeforeAll
    static void setupWireMock() {
        wireMockServer =  new WireMockServer(
                WireMockConfiguration.wireMockConfig()
                        .dynamicPort()
                        .withRootDirectory("src/test/resources/wiremock")
                        .extensions(new Jetty12GrpcExtensionFactory())
        );
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        mockInventoryService = new WireMockGrpcService(
                new WireMock(wireMockServer.port()),
                InventoryCRUDGrpc.SERVICE_NAME
        );

        log.info("WireMock started on port: {}", wireMockServer.port());
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            log.info("Wiremock server stopped");
        }
    }

    @BeforeEach
    void init() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + serverPort)
                .build();
    }

    @Test
    void returns_canned_message_from_grpc_service() throws Exception {
        log.info("WireMock port: " + wireMockServer.port());
        log.info("SERVICE_NAME: '" + InventoryCRUDGrpc.SERVICE_NAME + "'");
        log.info("Method name: '" + InventoryCRUDGrpc.getGetInventoryMethod().getFullMethodName() + "'");

        String uuid = String.valueOf(UUID.randomUUID());
        String invName = "Test Product";
        long count = 42;

        IdMessage requestMessage = IdMessage.newBuilder().setId(uuid).build();

        InventoryData responseMessage = InventoryData.newBuilder()
                .setId(uuid)
                .setName(invName)
                .setCount(count)
                .build();

        mockInventoryService.stubFor(
                method( "getInventory" )
                    .withRequestMessage(equalToMessage(requestMessage))
                    .willReturn(message(responseMessage))
        );

        log.info("Registered stubs: " + wireMockServer.getStubMappings());

        String url = "http://localhost:" + serverPort + "/api/inventory/" + uuid;
        log.info("URL " + url);
        ResponseEntity<InventoryData> response = client.get()
                .uri(url)
                .retrieve()
                .toEntity(InventoryData.class);

        Inventory inventory = client.get()
                .uri(url)
                .retrieve()
                .body(Inventory.class);

        assert inventory != null;
        log.info("response " + inventory.id());

//        InventoryData body = response.getBody();
        int statusCode = response.getStatusCode().value(); // если нужно
        assertThat(statusCode, is(200));
        assertThat(inventory, notNullValue());
        assertThat(inventory.name(), is(invName));
        assertThat(inventory.stock(), is(count));
        assertThat(inventory.id(), is(uuid));
    }
}

