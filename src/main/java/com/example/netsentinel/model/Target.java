package com.example.netsentinel.model;

import java.time.LocalDateTime;

public class Target {

    private String id;
    private String name;
    private TargetType type; // PING, TCP, HTTP
    private String host;
    private Integer port;
    private String endpointPath;
    private Integer expectedStatusCode;
    private Integer intervalSeconds;
    private Integer timeoutMs;
    private boolean enabled;
    private LocalDateTime createdAt;

    public Target() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
    }

    public Target(String id, String name, TargetType type, String host, Integer port,
                  String endpointPath, Integer expectedStatusCode, Integer intervalSeconds, Integer timeoutMs) {
        this();
        this.id = id;
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.endpointPath = endpointPath;
        this.expectedStatusCode = expectedStatusCode;
        this.intervalSeconds = intervalSeconds;
        this.timeoutMs = timeoutMs;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TargetType getType() { return type; }
    public void setType(TargetType type) { this.type = type; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getEndpointPath() { return endpointPath; }
    public void setEndpointPath(String endpointPath) { this.endpointPath = endpointPath; }

    public Integer getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(Integer expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }

    public Integer getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(Integer intervalSeconds) { this.intervalSeconds = intervalSeconds; }

    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
