package com.example.configservice.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigController {

    // Хранилище конфигураций (в памяти)
    private final Map<String, Map<String, Object>> configs = new HashMap<>();

    public ConfigController() {
        // Инициализируем дефолтные конфиги
        initializeDefaultConfigs();
    }

    private void initializeDefaultConfigs() {
        // Конфиг для User Service
        Map<String, Object> userServiceConfig = new HashMap<>();
        userServiceConfig.put("server.port", 8081);
        userServiceConfig.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/users");
        userServiceConfig.put("spring.kafka.bootstrap-servers", "localhost:9092");
        userServiceConfig.put("service.discovery.url", "http://localhost:8761");
        configs.put("user-service", userServiceConfig);

        // Конфиг для Notification Service
        Map<String, Object> notificationServiceConfig = new HashMap<>();
        notificationServiceConfig.put("server.port", 8082);
        notificationServiceConfig.put("spring.mail.host", "smtp.gmail.com");
        notificationServiceConfig.put("spring.mail.port", 587);
        notificationServiceConfig.put("service.discovery.url", "http://localhost:8761");
        configs.put("notification-service", notificationServiceConfig);

        // Конфиг для API Gateway
        Map<String, Object> apiGatewayConfig = new HashMap<>();
        apiGatewayConfig.put("server.port", 8080);
        apiGatewayConfig.put("service.discovery.url", "http://localhost:8761");
        apiGatewayConfig.put("circuit-breaker.user-service.max-failures", 3);
        apiGatewayConfig.put("circuit-breaker.user-service.timeout-ms", 10000);
        configs.put("api-gateway", apiGatewayConfig);

        // Конфиг для Service Discovery
        Map<String, Object> serviceDiscoveryConfig = new HashMap<>();
        serviceDiscoveryConfig.put("server.port", 8761);
        serviceDiscoveryConfig.put("service-discovery.heartbeat-timeout", 30);
        serviceDiscoveryConfig.put("service-discovery.cleanup-interval", 30000);
        configs.put("service-discovery", serviceDiscoveryConfig);
    }

    // Получить конфиг для сервиса
    @GetMapping("/{serviceName}")
    public Map<String, Object> getConfig(@PathVariable String serviceName) {
        Map<String, Object> config = configs.get(serviceName);
        if (config == null) {
            throw new RuntimeException("Config не найден для сервиса: " + serviceName);
        }
        System.out.println("⚙️ Настраиваем config для: " + serviceName);
        return config;
    }

    // Обновить конфиг для сервиса
    @PutMapping("/{serviceName}")
    public String updateConfig(@PathVariable String serviceName,
                               @RequestBody Map<String, Object> newConfig) {
        configs.put(serviceName, newConfig);
        System.out.println("🔄 Обновление config для: " + serviceName);
        return "Config успешна обновлена для: " + serviceName;
    }

    // Получить все конфиги
    @GetMapping
    public Map<String, Map<String, Object>> getAllConfigs() {
        return configs;
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "✅ Config Service работает";
    }
}