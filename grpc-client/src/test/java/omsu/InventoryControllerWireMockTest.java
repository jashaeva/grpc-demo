package omsu;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import omsu.grpc.IdMessage;
import omsu.grpc.InventoryData;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import omsu.grpc.InventoryCRUDGrpc;
import org.wiremock.grpc.Jetty12GrpcExtensionFactory;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.wiremock.grpc.dsl.WireMockGrpc.*;


@Disabled
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {GrpcClientApplication.class}
)
@Import(InventoryCRUDGrpc.class)
@ActiveProfiles("test")
public class InventoryControllerWireMockTest {
    private static final Logger log = LoggerFactory.getLogger(InventoryControllerWireMockTest.class);


    private static WireMockServer wireMockServer;
    private WireMockGrpcService mockInventoryService;

    @LocalServerPort
    int serverPort;

    RestClient client;

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
        System.setProperty("inventory-service.port", String.valueOf(wireMockServer.port()));
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
        mockInventoryService = new WireMockGrpcService(
                new WireMock(wireMockServer),
                InventoryCRUDGrpc.SERVICE_NAME
        );
        client = RestClient.create();
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
        String url = "http://localhost:" + serverPort + "/api/inventory/" + uuid;
        log.info("URL " + url);
        ResponseEntity<InventoryData> response = client.get()
                .uri(url)
                .retrieve()
                .toEntity(InventoryData.class);

        InventoryData body = client.get()
                .uri(url)
                .retrieve()
                .body(InventoryData.class);

        assert body != null;
        log.info("response " + body.getId());

//        InventoryData body = response.getBody();
        int statusCode = response.getStatusCode().value(); // если нужно
        assertThat(statusCode, is(200));
        assertThat(body, notNullValue());
        assertThat(body.getName(), is(invName));
        assertThat(body.getCount(), is(count));
        assertThat(body.getId(), is(uuid));
    }
}

