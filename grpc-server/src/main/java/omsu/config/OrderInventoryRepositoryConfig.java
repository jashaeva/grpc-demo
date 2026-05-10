package omsu.config;

import omsu.repository.IOrderInventoryRepository;
import omsu.repository.IOrderRepository;
import omsu.repository.impl.OrderInventoryRepository;
import omsu.repository.impl.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

@Configuration
public class OrderInventoryRepositoryConfig {
    @Bean
    public IOrderInventoryRepository orderInventoryRepository(JdbcOperations jdbc) {
        return new OrderInventoryRepository(jdbc);
    }

}
