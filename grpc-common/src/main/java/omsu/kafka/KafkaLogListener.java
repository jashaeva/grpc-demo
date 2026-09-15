package omsu.kafka;

import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import omsu.dto.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class KafkaLogListener {
    private final Logger log = LoggerFactory.getLogger(KafkaLogListener.class);
    private static final Map<String, LogEvent> storedValues = new ConcurrentHashMap<>();

    @KafkaListener(
            id = "rest-listener",
            topics = "grpc-logs",
            clientIdPrefix = "grpc-log-consumer"
    )
    public void listen(LogEvent logEvent, ConsumerRecordMetadata metadata) {
        log.debug("Received: offset={}, value={}", metadata.offset(), logEvent);

        if (logEvent != null) {
            log.info("RECEIVED LogEvent {}", logEvent);
            storedValues.put(logEvent.request(), logEvent);
        } else {
            log.info("LOGEVENT IS NULL");
        }

    }

    public LogEvent getMessage(@Nonnull  String request) {
        return storedValues.get(request);
    }
}
