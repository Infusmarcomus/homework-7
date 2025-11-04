package com.example.apigateway.controller;

import com.example.apigateway.circuitbreaker.CircuitBreaker;
import com.example.apigateway.discovery.ServiceDiscoveryClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class    GatewayController {

    private final ServiceDiscoveryClient serviceDiscovery;
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;

    public GatewayController(ServiceDiscoveryClient serviceDiscovery,
                             RestTemplate restTemplate,
                             CircuitBreaker circuitBreaker) {
        this.serviceDiscovery = serviceDiscovery;
        this.restTemplate = restTemplate;
        this.circuitBreaker = circuitBreaker;
    }

    // Маршрутизация для User Service с Circuit Breaker
    @RequestMapping("/users/**")
    public ResponseEntity<?> routeToUserService(HttpServletRequest request) {
        try {
            return circuitBreaker.execute("user-service", () -> {
                return routeRequest(request, "user-service");
            });
        } catch (Exception e) {
            // Обрабатываем ошибку после Circuit Breaker
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Сервис недоступен: user-service. Ошибка: " + e.getMessage());
        }
    }

    // Маршрутизация для Notification Service с Circuit Breaker
    @RequestMapping("/notifications/**")
    public ResponseEntity<?> routeToNotificationService(HttpServletRequest request) {
        try {
            return circuitBreaker.execute("notification-service", () -> {
                return routeRequest(request, "notification-service");
            });
        } catch (Exception e) {
            // Обрабатываем ошибку после Circuit Breaker
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Сервис недоступен: notification-service. Ошибка: " + e.getMessage());
        }
    }

    private ResponseEntity<?> routeRequest(HttpServletRequest request, String serviceName) {
        String targetUrl = buildTargetUrl(request, serviceName);
        targetUrl = targetUrl.replace(":8081s", ":8081")
                .replace(":8082s", ":8082");

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<String> entity = createHttpEntity(request);

        // Позволяем исключению пройти через Circuit Breaker
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl, method, entity, String.class);

        System.out.println("✅ Ворота: " + request.getMethod() + " " +
                request.getRequestURI() + " → " + targetUrl);

        return response;
    }

    // Методы buildTargetUrl, extractPath, createHttpEntity остаются без изменений
    private String buildTargetUrl(HttpServletRequest request, String serviceName) {
        String baseUrl = serviceDiscovery.getServiceUrl(serviceName);
        String path = extractPath(request, serviceName);

        // ФИКС: Принудительно убираем 's' из порта если есть
        baseUrl = baseUrl.replace(":8081s", ":8081")
                .replace(":8082s", ":8082");

        System.out.println("🔍 ДЕБАГ: '" + baseUrl + "'");

        return baseUrl + path;
    }
    private String extractPath(HttpServletRequest request, String serviceName) {
        String requestUri = request.getRequestURI();
        return requestUri.replace("/api/" + serviceName.split("-")[0], "");
    }

    private HttpEntity<String> createHttpEntity(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.add(headerName, request.getHeader(headerName));
        }

        String body = "";
        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {
            try {
                body = request.getReader().lines().collect(Collectors.joining());
            } catch (Exception e) {
            }
        }

        return new HttpEntity<>(body, headers);
    }

    // Health check для самого Gateway
    @GetMapping("/health")
    public String health() {
        return "API Gateway работает. Сервис работает: " +
                serviceDiscovery.getRegisteredServices();
    }

    // Эндпоинт для проверки состояния Circuit Breaker
    @GetMapping("/circuit-breaker/status")
    public Map<String, String> getCircuitBreakerStatus() {
        return circuitBreaker.getAllStates().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getState().toString() +
                                " (failures: " + entry.getValue().getFailureCount() + ")"
                ));
    }


}