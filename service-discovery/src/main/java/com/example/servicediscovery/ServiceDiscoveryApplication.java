package com.example.servicediscovery;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
        System.out.println("Сервис Discovery запщуен на порту 8761");
        System.out.println("Эндпоинты:");
        System.out.println("  POST   /discovery/register    - Регистрация сервиса");
        System.out.println("  GET    /discovery/services/{name} - Конкретный сервис получен");
        System.out.println("  POST   /discovery/heartbeat   - Отправить heartbeat");
        System.out.println("  GET    /discovery/services    - Получены все сервисы");
    }
}