package com.example.notificationservice;

import com.example.notificationservice.config.ConfigClient;  // Импортируем ConfigClient
import com.example.common.dto.ServiceInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class NotificationServiceApplication {

    // Добавляем ConfigClient как зависимость
    private final ConfigClient configClient;

    // Конструктор - Spring автоматически передаст ConfigClient
    public NotificationServiceApplication(ConfigClient configClient) {
        this.configClient = configClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)  // Выполнится после запуска приложения
    public void registerInServiceDiscovery() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // ВАЖНО: Порт теперь берем из Config Service, а не хардкодим
            int port = configClient.getIntProperty("server.port");

            ServiceInstance instance = new ServiceInstance(
                    "notification-service",
                    "localhost",
                    port  // Используем порт из конфига
            );

            // ВАЖНО: URL Service Discovery тоже берем из конфига
            String discoveryUrl = configClient.getProperty("service.discovery.url");
            String url = discoveryUrl + "/discovery/register";

            String response = restTemplate.postForObject(url, instance, String.class);

            System.out.println("✅ Notification Service зарегистрирован в Service Discovery на порту " + port + ": " + response);

        } catch (Exception e) {
            System.err.println("❌ Ошибка регистрации Notification Service: " + e.getMessage());
        }
    }
}