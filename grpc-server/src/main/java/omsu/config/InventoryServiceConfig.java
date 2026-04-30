package omsu.config;

import omsu.repository.IInventoryRepository;
import omsu.services.IInventoryService;
import omsu.services.impl.InventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryServiceConfig {
    @Bean
    public IInventoryService inventoryService(IInventoryRepository invRepo) {
        return new InventoryService(invRepo);
    }
}
