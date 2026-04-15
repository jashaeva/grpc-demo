package omsu.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,  // Исключаем БД
        FlywayAutoConfiguration.class       // Исключаем миграции
})
public class  OrderServiceApplication  {
    public static void main(String[] args) {
        System.out.println("Starting minimal test...");
        SpringApplication.run( OrderServiceApplication.class, args);
        System.out.println("Started!");
    }
}

//@SpringBootApplication
//public class OrderServiceApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(OrderServiceApplication.class, args);
//    }
//}
