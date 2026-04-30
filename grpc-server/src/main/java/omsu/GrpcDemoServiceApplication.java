package omsu;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcDemoServiceApplication {

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run( GrpcDemoServiceApplication.class, args);

    }
}
