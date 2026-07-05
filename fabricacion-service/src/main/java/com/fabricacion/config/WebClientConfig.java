package com.fabricacion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // Configuramos la URL base apuntando al microservicio de producción
        return builder.baseUrl("http://localhost:8082").build();
    }
}
