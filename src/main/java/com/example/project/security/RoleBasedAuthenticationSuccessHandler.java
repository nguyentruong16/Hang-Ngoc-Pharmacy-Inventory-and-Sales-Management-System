package com.example.project.security;

import com.example.project.constant.RoleConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * On successful login, sends the user to their own role's dashboard (e.g. OWNER →
 * {@code /owner/dashboard}) instead of a single shared landing page.
 *
 * <p>The destination is derived from the authenticated {@link AccountPrincipal}'s
 * {@link AccountPrincipal#getPrimaryRole() primaryRole}. Any previously saved request is
 * intentionally ignored so every login lands on the role dashboard.</p>
 */
public class RoleBasedAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            return;
        }
        getRedirectStrategy().sendRedirect(request, response, determineTargetUrl(request, response, authentication));
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof AccountPrincipal accountPrincipal
                && RoleConstants.isValid(accountPrincipal.getPrimaryRole())) {
            return RoleConstants.dashboardPath(accountPrincipal.getPrimaryRole());
        }
        // Should not happen (login requires a valid role); fall back to the /dashboard bridge.
        return "/dashboard";
    }
}
