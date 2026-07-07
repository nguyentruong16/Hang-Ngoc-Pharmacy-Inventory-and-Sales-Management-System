package com.example.project.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records WHY a session was force-expired so the login screen can show a reason-specific message
 * instead of one shared message.
 *
 * <p>Spring Security's {@code SessionInformation.expireNow()} only flips an "expired" flag with no
 * reason attached. So any code that force-expires a session should first tag it here; then
 * {@link ReasonAwareSessionExpiredStrategy} reads the tag and redirects to {@code /signin?<reason>}.
 *
 * <p>Current taggers:
 * <ul>
 *   <li>{@code PasswordResetService} → {@link #PASSWORD_CHANGED}</li>
 *   <li>{@code OwnerPermissionService} (role change) → does NOT tag; falls back to the default
 *       {@link #ROLE_CHANGED}. If a new reason is ever added, tag it explicitly here to avoid it
 *       being mislabelled as a role change.</li>
 * </ul>
 *
 * <p>NOTE: sessions that simply disappear (idle timeout, app restart, logout) never reach this
 * strategy — they are not in the registry — so they land on a plain {@code /signin} with no message.
 */
@Component
public class SessionExpiryReasonRegistry {

    /** Reason codes double as the query param understood by signin.html. */
    public static final String ROLE_CHANGED = "roleChanged";
    public static final String PASSWORD_CHANGED = "passwordChanged";

    private final Map<String, String> reasonsBySessionId = new ConcurrentHashMap<>();

    /** Tags a session id with the reason it is about to be force-expired for. */
    public void mark(String sessionId, String reason) {
        if (sessionId != null && reason != null) {
            reasonsBySessionId.put(sessionId, reason);
        }
    }

    /**
     * Returns and clears the recorded reason for a session id.
     * Defaults to {@link #ROLE_CHANGED} when the session was force-expired without an explicit tag
     * (the only untagged force-expire path today is the owner role/permission change).
     */
    public String consume(String sessionId) {
        String reason = sessionId == null ? null : reasonsBySessionId.remove(sessionId);
        return reason != null ? reason : ROLE_CHANGED;
    }
}
