package hr.javafx.onlinelearningsystem.service;

public record AuditLogEvent(String timestamp, String username, String reason) {
}

