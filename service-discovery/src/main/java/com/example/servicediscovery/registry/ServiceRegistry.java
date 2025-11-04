package com.example.servicediscovery.registry;

import com.example.common.dto.ServiceInstance;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "service-discovery")
public class ServiceRegistry {

    private final Map<String, List<ServiceInstance>> services = new ConcurrentHashMap<>();

    // Настройки из application.yml
    private long cleanupInterval = 30000;
    private long heartbeatTimeout = 30000;
    private int maxInstancesPerService = 5;

    // Геттеры и сеттеры для настроек
    public void setCleanupInterval(long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public void setMaxInstancesPerService(int maxInstancesPerService) {
        this.maxInstancesPerService = maxInstancesPerService;
    }

    // Регистрация сервиса
    public void register(ServiceInstance instance) {
        String serviceName = instance.getServiceName();

        services.compute(serviceName, (key, existingInstances) -> {
            if (existingInstances == null) {
                existingInstances = new ArrayList<>();
            }

            // Проверяем лимит инстансов
            if (existingInstances.size() >= maxInstancesPerService) {
                System.out.println("⚠️ Maximum instances reached for: " + serviceName);
                return existingInstances;
            }

            // Проверяем, не зарегистрирован ли уже этот инстанс
            Optional<ServiceInstance> existing = existingInstances.stream()
                    .filter(inst -> inst.getHost().equals(instance.getHost()) &&
                            inst.getPort() == instance.getPort())
                    .findFirst();

            if (existing.isPresent()) {
                // Обновляем heartbeat существующего инстанса
                existing.get().setLastHeartbeat(LocalDateTime.now());
                System.out.println("🔄 Updated heartbeat for: " + instance.getUrl());
            } else {
                // Добавляем новый инстанс
                existingInstances.add(instance);
                System.out.println("✅ Registered new service: " + serviceName + " at " + instance.getUrl());
            }

            return existingInstances;
        });
    }

    // Получение всех здоровых инстансов сервиса
    public List<ServiceInstance> getHealthyInstances(String serviceName) {
        List<ServiceInstance> instances = services.getOrDefault(serviceName, new ArrayList<>());
        return instances.stream()
                .filter(instance -> instance.getLastHeartbeat()
                        .isAfter(LocalDateTime.now().minusSeconds(heartbeatTimeout / 1000)))
                .collect(Collectors.toList());
    }

    // Удаление мертвых сервисов
    @Scheduled(fixedRateString = "${service-discovery.cleanup-interval:30000}")
    public void cleanupDeadServices() {
        // Конвертируем миллисекунды в секунды
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(heartbeatTimeout / 1000);

        services.forEach((serviceName, instances) -> {
            List<ServiceInstance> healthyInstances = instances.stream()
                    .filter(instance -> instance.getLastHeartbeat().isAfter(cutoff))
                    .collect(Collectors.toList());

            if (healthyInstances.size() != instances.size()) {
                services.put(serviceName, healthyInstances);
                System.out.println("🧹 Cleaned up dead instances for: " + serviceName +
                        ", remaining: " + healthyInstances.size());
            }
        });
    }
    // Получение любого здорового инстанса (для простоты)
    public Optional<ServiceInstance> getInstance(String serviceName) {
        List<ServiceInstance> healthyInstances = getHealthyInstances(serviceName);
        if (healthyInstances.isEmpty()) {
            return Optional.empty();
        }
        // Простая реализация - берем первый здоровый инстанс
        return Optional.of(healthyInstances.get(0));
    }

    // Получение всех зарегистрированных сервисов
    public Map<String, List<ServiceInstance>> getAllServices() {
        return new HashMap<>(services);
    }

    // Удаление сервиса
    public void deregister(String serviceName, String host, int port) {
        services.computeIfPresent(serviceName, (key, instances) -> {
            List<ServiceInstance> updatedInstances = instances.stream()
                    .filter(instance -> !(instance.getHost().equals(host) && instance.getPort() == port))
                    .collect(Collectors.toList());

            System.out.println("🗑️ Deregistered service: " + serviceName + " at " + host + ":" + port);
            return updatedInstances.isEmpty() ? null : updatedInstances;
        });
    }
}