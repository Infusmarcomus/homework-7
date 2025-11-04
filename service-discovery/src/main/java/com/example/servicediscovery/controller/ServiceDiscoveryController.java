package com.example.servicediscovery.controller;

import com.example.common.dto.ServiceInstance;
import com.example.servicediscovery.registry.ServiceRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discovery")
public class ServiceDiscoveryController {

    private final ServiceRegistry serviceRegistry;

    public ServiceDiscoveryController(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    // Регистрация сервиса
    @PostMapping("/register")
    public ResponseEntity<String> registerService(@RequestBody ServiceInstance instance) {
        serviceRegistry.register(instance);
        return ResponseEntity.ok("Сервис запущен успешно");
    }

    // Получение инстанса для сервиса
    @GetMapping("/services/{serviceName}")
    public ResponseEntity<ServiceInstance> getServiceInstance(@PathVariable String serviceName) {
        return serviceRegistry.getInstance(serviceName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Получение всех здоровых инстансов сервиса
    @GetMapping("/services/{serviceName}/instances")
    public ResponseEntity<List<ServiceInstance>> getServiceInstances(@PathVariable String serviceName) {
        List<ServiceInstance> instances = serviceRegistry.getHealthyInstances(serviceName);
        return ResponseEntity.ok(instances);
    }

    // Получение всех сервисов
    @GetMapping("/services")
    public ResponseEntity<Map<String, List<ServiceInstance>>> getAllServices() {
        return ResponseEntity.ok(serviceRegistry.getAllServices());
    }

    // Отправка heartbeat
    @PostMapping("/heartbeat")
    public ResponseEntity<String> sendHeartbeat(@RequestBody ServiceInstance heartbeat) {
        serviceRegistry.register(heartbeat); // Регистрация обновит heartbeat
        return ResponseEntity.ok("Heartbeat получен");
    }

    // Удаление сервиса
    @PostMapping("/deregister")
    public ResponseEntity<String> deregisterService(@RequestBody ServiceInstance instance) {
        serviceRegistry.deregister(instance.getServiceName(), instance.getHost(), instance.getPort());
        return ResponseEntity.ok("Сервис удален");
    }
}