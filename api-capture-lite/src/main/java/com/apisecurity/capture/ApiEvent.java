package com.apisecurity.capture;

public class ApiEvent {
    private String timestamp;
    private String method;
    private String endpoint;
    private String ipAddress;
    private int statusCode;
    private long responseTime;

    // Getters and Setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
}
