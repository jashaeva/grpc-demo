package omsu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public final class ContainerHolder {
    private static volatile PostgreSQLContainer<?> postgres;
    private static volatile KafkaContainer kafka;
    private static final Object lock = new Object();
    private static final Logger containerLogger = LoggerFactory.getLogger("Testcontainers");

    private ContainerHolder() {}

    public static PostgreSQLContainer<?> getPostgres() {
        if (postgres == null) {
            synchronized (lock) {
                if (postgres == null) {
                    postgres = new PostgreSQLContainer<>("postgres:15")
                            .withDatabaseName("testdb")
                            .withUsername("postgres")
                            .withPassword("secret")
                            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Postgres")))
                            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
                            .withReuse(true);
                    postgres.start();
                }
            }
        }
        return postgres;
    }

    public static KafkaContainer getKafka() {
        if (kafka == null) {
            synchronized (lock) {
                if (kafka == null) {
                    kafka = new KafkaContainer(
//                            DockerImageName.parse("confluentinc/cp-kafka:7.3.2")
                            DockerImageName.parse("apache/kafka:4.3.1")
                                    .asCompatibleSubstituteFor("apache/kafka")
                    ).withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Kafka")))
                    .waitingFor(Wait.forLogMessage(".*started.*", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
                    .withReuse(true);
                    kafka.start();
                }
            }
        }
        return kafka;
    }
}
