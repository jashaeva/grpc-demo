package omsu.config;

import omsu.repository.IOrderRepository;
import omsu.repository.impl.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

@Configuration
public class OrderRepositoryConfig {
    @Bean
    public IOrderRepository orderRepository(JdbcOperations jdbc) {
        return new OrderRepository(jdbc);
    }

}
