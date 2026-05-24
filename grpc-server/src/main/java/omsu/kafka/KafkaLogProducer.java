package omsu.kafka;

import omsu.model.dto.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Logger log = LoggerFactory.getLogger(KafkaLogProducer.class);

    public KafkaLogProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLog(LogEvent logEvent) {
        kafkaTemplate.send("grpc-logs", logEvent);
        log.info("Kafka has sent the message: method {}, request {}, response {}",
                logEvent.method(), logEvent.request(), logEvent.response()
        );
    }
}
