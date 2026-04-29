package omsu.inventory.config;

import omsu.inventory.repository.IInventoryRepository;
import omsu.inventory.repository.IOrderRepository;
import omsu.inventory.services.IOrderService;
import omsu.inventory.services.impl.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderServiceConfig {
    @Bean
    public IOrderService orderService(IOrderRepository orderRepo, IInventoryRepository invRepo) {
        return new OrderService(orderRepo, invRepo);
    }
}
