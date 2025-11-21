package com.example.LMS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path baseDir = Paths.get(System.getProperty("user.dir") + "/upload");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // URL frontend gọi: http://localhost:8080/uploads/student/image/filename.png
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + baseDir.toAbsolutePath().toString() + "/");
    }
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173") // frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
