package omsu;


import omsu.model.Inventory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "grpc.client.inventory-service.address=static://localhost:9090"
        }
)
@ActiveProfiles("test")
public class StandaloneWiremockTest {

    @LocalServerPort
    int serverPort;

    private RestClient client;

    @BeforeEach
    void init() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + serverPort)
                .build();
    }

    @Test
    void returns_canned_message_from_grpc_service() {
        String uuid = UUID.randomUUID().toString();

        // Тест вызывает REST API, который внутри делает gRPC вызов
        // Теперь gRPC вызов пойдет в Docker на localhost:9090
        Inventory inventory = client.get()
                .uri("/api/inventory/" + uuid)
                .retrieve()
                .body(Inventory.class);

        assertThat(inventory).isNotNull();
//        assertThat(inventory, is( ));
//        assertThat(inventory.stock(), is(count));
//        assertThat(inventory.id(), is(uuid));
    }
}