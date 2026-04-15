package omsu.inventory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class InventoryServiceApplication {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run( InventoryServiceApplication.class, args);
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            latch.countDown();
        }));

        latch.await(); // Ждем, пока не поступит сигнал завершения
    }
}
