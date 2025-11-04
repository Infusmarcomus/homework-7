package com.example.notificationservice;

import com.example.notificationservice.config.ConfigClient;  // Импортируем наш ConfigClient
import com.example.common.dto.ServiceInstance;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HeartbeatScheduler {

    // Добавляем зависимость на ConfigClient
    private final ConfigClient configClient;

    // Spring автоматически передаст ConfigClient в конструктор
    public HeartbeatScheduler(ConfigClient configClient) {
        this.configClient = configClient;
    }

    @Scheduled(fixedRate = 15000)  // Выполняется каждые 15 секунд
    public void sendHeartbeat() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // ВАЖНО: Теперь порт берем из конфига, а не хардкодим
            int port = configClient.getIntProperty("server.port");

            ServiceInstance instance = new ServiceInstance(
                    "notification-service",
                    "localhost",
                    port  // Используем порт из конфига
            );

            // ВАЖНО: URL Service Discovery тоже берем из конфига
            String discoveryUrl = configClient.getProperty("service.discovery.url");
            String url = discoveryUrl + "/discovery/heartbeat";

            restTemplate.postForObject(url, instance, String.class);

        } catch (Exception e) {
            System.err.println("❌ Ошибка heartbeat в Notification Service: " + e.getMessage());
        }
    }
}