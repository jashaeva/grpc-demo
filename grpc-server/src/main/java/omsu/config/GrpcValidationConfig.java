package omsu.config;

import omsu.controller.ValidationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@Configuration
public class GrpcValidationConfig {

    @Bean
    @GrpcGlobalServerInterceptor
    public ValidationInterceptor validationInterceptor() {
        return new ValidationInterceptor();
    }
}