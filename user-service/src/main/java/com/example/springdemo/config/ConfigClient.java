package com.example.springdemo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class ConfigClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String configServiceUrl = "http://localhost:8888/config";
    private Map<String, Object> config;

    public Map<String, Object> getConfig() {
        if (config == null) {
            try {
                config = restTemplate.getForObject(configServiceUrl + "/user-service", Map.class);
                System.out.println("⚙️ Загрузил config из Config Service");
            } catch (Exception e) {
                System.err.println("❌ Ошибка загрузки config из Config Service использую базовые настройки");
                // Возвращаю к базовым настройкам
                config = Map.of(
                        "server.port", 8081,
                        "service.discovery.url", "http://localhost:8761"
                );
            }
        }
        return config;
    }

    public String getProperty(String key) {
        return getConfig().get(key).toString();
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }
}