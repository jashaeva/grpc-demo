package omsu.config;

import omsu.repository.IInventoryRepository;
import omsu.repository.impl.InventoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

@Configuration
public class InventoryRepositoryConfig {
    @Bean
    public IInventoryRepository inventoryRepository(JdbcOperations jdbc) {
        return new InventoryRepository(jdbc);
    }
}
