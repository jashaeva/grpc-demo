package omsu.config;

import omsu.repository.IInventoryRepository;
import omsu.repository.IOrderRepository;
import omsu.services.IOrderService;
import omsu.services.impl.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderServiceConfig {
    @Bean
    public IOrderService orderService(IOrderRepository orderRepo, IInventoryRepository invRepo) {
        return new OrderService(orderRepo, invRepo);
    }
}
