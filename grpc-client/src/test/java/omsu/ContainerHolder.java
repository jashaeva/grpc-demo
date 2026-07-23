package omsu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.Network;

import java.time.Duration;

public final class ContainerHolder {
    private static volatile PostgreSQLContainer<?> postgres;
    private static volatile KafkaContainer kafka;
    private static volatile GenericContainer<?> grpcServer;
    private static final Object lock = new Object();
    private static Network network = Network.newNetwork();

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
                            .withNetwork(network)
                            .withNetworkAliases("postgres")
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
                    .waitingFor(Wait
                            .forLogMessage(".*started.*", 1)
                            .withStartupTimeout(Duration.ofMinutes(2))
                    )
                    .withNetwork(network)
                    .withNetworkAliases("kafka")
                    .withReuse(true);
                    kafka.start();
                }
            }
        }
        return kafka;
    }

    public static GenericContainer<?> getGrpcServer() {
        if (grpcServer == null) {
            synchronized (lock) {
                if (grpcServer == null) {
                    var postgres = getPostgres();
                    var kafka = getKafka();

                    grpcServer = new GenericContainer<>(
                            DockerImageName.parse("grpc-server:latest")
                    )
                            .withExposedPorts(9090)
                            .withNetwork(network)
                            .withNetworkAliases("grpc-server")
                            .withEnv("SPRING_PROFILES_ACTIVE", "docker")
                            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/testdb")
                            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
                            .withEnv("SPRING_DATASOURCE_PASSWORD", "secret")
                            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
                            .withEnv("GRPC_SERVER_PORT", "9090")
                            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("GrpcServer")))
                            .waitingFor(Wait.forLogMessage(".*Started.*", 1)
                                    .withStartupTimeout(Duration.ofMinutes(3)))
                            .withReuse(true);
                    grpcServer.start();
                }
            }
        }
        return grpcServer;
    }
}
