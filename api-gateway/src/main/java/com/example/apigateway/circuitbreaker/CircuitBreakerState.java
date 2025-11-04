package com.example.apigateway.circuitbreaker;

public class CircuitBreakerState {
    private State state = State.CLOSED;
    private int failureCount = 0;
    private long lastFailureTime = 0;

    private static final int MAX_FAILURES = 3;
    private static final long TIMEOUT_MS = 10000;

    public enum State { CLOSED, OPEN, HALF_OPEN }

    public boolean allowRequest() {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > TIMEOUT_MS) {
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        return true;
    }

    public void recordSuccess() {
        failureCount = 0;
        state = State.CLOSED;
    }

    public void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        if (failureCount >= MAX_FAILURES) {
            state = State.OPEN;
        }
    }

    public State getState() { return state; }
    public int getFailureCount() { return failureCount; }
}