package com.example.notificationservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class ConfigClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String configServiceUrl = "http://localhost:8888/config";
    private Map<String, Object> config;

    // Этот метод получает все настройки для notification-service из Config Service
    public Map<String, Object> getConfig() {
        if (config == null) {
            try {
                // Пытаемся получить конфиг из Config Service
                config = restTemplate.getForObject(configServiceUrl + "/notification-service", Map.class);
                System.out.println("⚙️ Загрузил конфиг из Config Service");
            } catch (Exception e) {
                // Если Config Service недоступен - используем настройки по умолчанию
                System.err.println("❌ Не удалось загрузить конфиг из Config Service, использую настройки по умолчанию");
                config = Map.of(
                        "server.port", 8082,  // Порт по умолчанию
                        "service.discovery.url", "http://localhost:8761"  // URL Service Discovery по умолчанию
                );
            }
        }
        return config;
    }

    // Получить конкретную настройку как строку
    public String getProperty(String key) {
        return getConfig().get(key).toString();
    }

    // Получить конкретную настройку как число
    public int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }
}