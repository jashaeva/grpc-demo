package omsu.inventory.config;

import omsu.inventory.repository.IInventoryRepository;
import omsu.inventory.repository.IOrderRepository;
import omsu.inventory.services.IInventoryService;
import omsu.inventory.services.IOrderService;
import omsu.inventory.services.impl.InventoryService;
import omsu.inventory.services.impl.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryServiceConfig {
    @Bean
    public IInventoryService inventoryService(IInventoryRepository invRepo) {
        return new InventoryService(invRepo);
    }
}
