package com.fabricacion.config;

import com.fabricacion.controllers.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/ordenes/**");
    }
//permitir CORS puerto 3000
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas las rutas del microservicio
                .allowedOrigins("http://localhost:3000") // Permite el origen de React
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") //
                .allowedHeaders("*"); // Permite cualquier cabecera (como tu 'Authorization')
    }
}