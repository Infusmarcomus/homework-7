package com.example.springdemo;

import com.example.springdemo.config.ConfigClient;  // ← ДОБАВЬ импорт
import com.example.common.dto.ServiceInstance;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HeartbeatScheduler {


    private final ConfigClient configClient;

    public HeartbeatScheduler(ConfigClient configClient) {
        this.configClient = configClient;
    }

    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // - получаем порт из конфига
            int port = configClient.getIntProperty("server.port");

            ServiceInstance instance = new ServiceInstance(
                    "user-service",
                    "localhost",
                    port
            );

            // - получаем URL из конфига
            String discoveryUrl = configClient.getProperty("service.discovery.url");
            String url = discoveryUrl + "/discovery/heartbeat";

            restTemplate.postForObject(url, instance, String.class);

        } catch (Exception e) {
            System.err.println("❌ User Service heartbeat ошибка: " + e.getMessage());
        }
    }
}