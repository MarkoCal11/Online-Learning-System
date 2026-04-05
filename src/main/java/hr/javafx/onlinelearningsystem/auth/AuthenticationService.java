package hr.javafx.onlinelearningsystem.auth;

import hr.javafx.onlinelearningsystem.exception.InvalidLoginCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("hr.javafx.onlinelearningsystem.audit");

    private final UserCredentialsService repository = new UserCredentialsService();

    public AuthResult authenticate(String username, String plainPassword) throws IOException {

        Map<String, UserCredential> credentials = repository.loadCredentials();

        UserCredential user = credentials.get(username);

        if (user == null) {
            logAuthenticationFailure(username);
            throw new InvalidLoginCredentialsException("Incorrect username or password");
        }

        boolean passwordMatches = PasswordHashing.verifyPassword(plainPassword, user.hashedPassword());

        if(!passwordMatches) {
            logAuthenticationFailure(username);
            throw new InvalidLoginCredentialsException("Incorrect username or password");
        }

        return new AuthResult(user.username(), user.role());
    }

    private void logAuthenticationFailure(String username) {
        LOGGER.warn("Authentication failed for username '{}': invalid credentials", username);

        if (AUDIT_LOGGER.isInfoEnabled()) {
            AUDIT_LOGGER.info("AUTH_FAILURE|username={}|reason=INVALID_CREDENTIALS", sanitizeForAudit(username));
        }
    }

    private String sanitizeForAudit(String value) {
        if (value == null) {
            return "unknown";
        }

        String sanitizedValue = value
                .replace("|", "_")
                .replace("\n", "_")
                .replace("\r", "_")
                .trim();

        return sanitizedValue.isBlank() ? "unknown" : sanitizedValue;
    }
}