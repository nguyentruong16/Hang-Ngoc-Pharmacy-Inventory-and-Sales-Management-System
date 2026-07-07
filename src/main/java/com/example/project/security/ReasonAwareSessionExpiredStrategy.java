package com.example.project.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redirects a force-expired session to a reason-specific login URL
 * (e.g. {@code /signin?roleChanged}, {@code /signin?passwordChanged}) using the reason recorded in
 * {@link SessionExpiryReasonRegistry}, instead of one shared {@code expiredUrl}.
 *
 * <p>This keeps each expiry cause on its own message so future debugging does not require guessing
 * which situation triggered the redirect.
 */
@Component
public class ReasonAwareSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    private final SessionExpiryReasonRegistry reasonRegistry;

    public ReasonAwareSessionExpiredStrategy(SessionExpiryReasonRegistry reasonRegistry) {
        this.reasonRegistry = reasonRegistry;
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();
        String sessionId = event.getSessionInformation().getSessionId();
        String reason = reasonRegistry.consume(sessionId);
        response.sendRedirect(request.getContextPath() + "/signin?" + reason);
    }
}
