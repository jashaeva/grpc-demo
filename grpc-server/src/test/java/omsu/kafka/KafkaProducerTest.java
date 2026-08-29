package omsu.kafka;

import io.qameta.allure.Description;
import omsu.BaseTestContainersTest;
import omsu.ContainerHolder;
import omsu.dto.LogEvent;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.kafka.KafkaContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KafkaProducerTest extends BaseTestContainersTest {

    @Autowired
    KafkaConsumer<String, String> consumer;

    @Autowired
    KafkaLogProducer producer;

    @Test
    @DisplayName("TC-KP01: Отправка валидного сообщения")
    @Description("Positive test: the valid message should be sent")
    void sendRecord_ShouldAddToHistory() {
        LogEvent event = new LogEvent("test-message", "test-key", "test-value");
        producer.sendLog(event);

        //producer.
    }

}
