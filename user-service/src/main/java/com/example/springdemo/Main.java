package com.example.springdemo;

import com.example.common.dto.ServiceInstance;
import com.example.springdemo.config.ConfigClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling  // Добавляем для heartbeat
public class Main {


    private final ConfigClient configClient;


    public Main(ConfigClient configClient) {
        this.configClient = configClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerInServiceDiscovery() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            int port = configClient.getIntProperty("server.port");

            ServiceInstance instance = new ServiceInstance(
                    "user-service",
                    "localhost",
                    port
            );

            String discoveryUrl = configClient.getProperty("service.discovery.url");
            String url = discoveryUrl + "/discovery/register";

            String response = restTemplate.postForObject(url, instance, String.class);

            System.out.println("✅ User Service зарегистрирован на порту: " + port +  ": " + response);

        } catch (Exception e) {
            System.err.println("❌ ошибка регистрации User Service: " + e.getMessage());
        }
    }
}