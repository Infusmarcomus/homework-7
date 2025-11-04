package com.example.apigateway.discovery;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ServiceDiscoveryClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String discoveryServiceUrl = "http://localhost:8761";

    public ServiceDiscoveryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String getServiceUrl(String serviceName) {
        try {
            String url = discoveryServiceUrl + "/discovery/services";
            String jsonResponse = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode serviceArray = root.get(serviceName);

            if (serviceArray != null && serviceArray.isArray() && serviceArray.size() > 0) {
                JsonNode instance = serviceArray.get(0);
                String host = instance.get("host").asText();
                int port = instance.get("port").asInt(); // Гарантированно int!

                String serviceUrl = "http://" + host + ":" + port;
                System.out.println("✅ Resolved " + serviceName + " -> " + serviceUrl);
                return serviceUrl;
            }

            throw new RuntimeException("Service not found: " + serviceName);

        } catch (Exception e) {
            System.err.println("❌ ServiceDiscovery ошибка для сервиса" + serviceName + ": " + e.getMessage());
            throw new RuntimeException("Не получилось получить URL из сервиса: " + serviceName, e);
        }
    }

    public String getRegisteredServices() {
        try {
            String url = discoveryServiceUrl + "/discovery/services";
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.fieldNames().toString();
        } catch (Exception e) {
            return "Не получилось извлечь сервис: " + e.getMessage();
        }
    }
}