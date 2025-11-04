package com.example.apigateway.circuitbreaker;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class CircuitBreaker {

    private final Map<String, CircuitBreakerState> breakers = new ConcurrentHashMap<>();

    public <T> T execute(String serviceName, Supplier<T> supplier) {
        CircuitBreakerState state = breakers.computeIfAbsent(
                serviceName, k -> new CircuitBreakerState());

        System.out.println("🔧 Circuit Breaker: " + serviceName + " состояние=" + state.getState() + ", ошибки=" + state.getFailureCount());

        if (!state.allowRequest()) {
            System.out.println("🚫 Circuit Breaker: Заблокирован запрос для " + serviceName);
            throw new CircuitBreakerOpenException("Circuit breaker ОТКРЫТ для: " + serviceName);
        }

        try {
            T result = supplier.get();
            state.recordSuccess();
            System.out.println("✅ Circuit Breaker: Доступен для " + serviceName);
            return result;
        } catch (Exception e) {
            state.recordFailure();
            System.out.println("❌ Circuit Breaker: Ошибка " + serviceName + " - " + e.getMessage());
            throw e;
        }
    }
    public Map<String, CircuitBreakerState> getAllStates() {
        return new ConcurrentHashMap<>(breakers);
    }
}