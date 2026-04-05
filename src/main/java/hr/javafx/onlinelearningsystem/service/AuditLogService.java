package hr.javafx.onlinelearningsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);
    private static final Path AUDIT_LOG_PATH = Path.of("logs", "audit.log");

    public List<AuditLogEvent> getRecentAuthenticationFailures(int limit) {
        if (limit <= 0 || !Files.exists(AUDIT_LOG_PATH)) {
            return List.of();
        }

        try (Stream<String> lines = Files.lines(AUDIT_LOG_PATH, StandardCharsets.UTF_8)) {
            return lines
                    .map(this::parseEvent)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(AuditLogEvent::timestamp).reversed())
                    .limit(limit)
                    .toList();
        } catch (IOException exception) {
            LOGGER.warn("Could not read audit log file '{}': {}", AUDIT_LOG_PATH, exception.getMessage());
            return List.of();
        }
    }

    private Optional<AuditLogEvent> parseEvent(String line) {
        String[] sections = line.split("\\|", 4);
        if (sections.length != 4 || !"AUTH_FAILURE".equals(sections[1])) {
            return Optional.empty();
        }

        String username = extractValue(sections[2], "username=");
        String reason = extractValue(sections[3], "reason=");

        return Optional.of(new AuditLogEvent(sections[0], usernameNullCheck(username), reason));
    }

    private String extractValue(String section, String prefix) {
        return section.startsWith(prefix) ? section.substring(prefix.length()) : "unknown";
    }

    private String usernameNullCheck(String username) {
        if (username == null || username.isBlank()) {
            return "unknown";
        }
        return username;
    }
}


