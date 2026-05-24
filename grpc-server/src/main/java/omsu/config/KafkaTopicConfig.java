package omsu.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {
    @Bean
    public NewTopic grpcLogsTopic() {
        return TopicBuilder.name("grpc-logs")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
