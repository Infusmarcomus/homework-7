package com.example.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ServiceInstance {
    private String serviceName;
    private String host;
    private int port;
    private String healthCheckUrl;
    private LocalDateTime registrationTime;
    private LocalDateTime lastHeartbeat;

    // Конструктор для Jackson
    @JsonCreator
    public ServiceInstance(
            @JsonProperty("serviceName") String serviceName,
            @JsonProperty("host") String host,
            @JsonProperty("port") int port,  // Jackson будет конвертировать в int
            @JsonProperty("healthCheckUrl") String healthCheckUrl,
            @JsonProperty("registrationTime") LocalDateTime registrationTime,
            @JsonProperty("lastHeartbeat") LocalDateTime lastHeartbeat) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.healthCheckUrl = healthCheckUrl;
        this.registrationTime = registrationTime;
        this.lastHeartbeat = lastHeartbeat;
    }

    // Дефолтный конструктор
    public ServiceInstance() {}

    // Твой конструктор
    public ServiceInstance(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.registrationTime = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
        this.healthCheckUrl = "http://" + host + ":" + port + "/health";
    }

    // остальные геттеры/сеттеры без изменений
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getHealthCheckUrl() { return healthCheckUrl; }
    public void setHealthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; }

    public LocalDateTime getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(LocalDateTime registrationTime) { this.registrationTime = registrationTime; }

    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public String getUrl() {
        return "http://" + host + ":" + port;
    }

    public boolean isHealthy() {
        return lastHeartbeat.isAfter(LocalDateTime.now().minusSeconds(30));
    }
}