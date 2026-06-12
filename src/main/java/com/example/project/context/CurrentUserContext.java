package com.example.project.context;

import com.example.project.constant.RoleConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Single, isolated point that reads "who is the current user" from the HTTP session.
 *
 * <p><b>Integration contract with the login task (owned by another team member):</b>
 * after a successful login, that module is expected to put the following attributes on
 * the {@link HttpSession}:</p>
 * <ul>
 *   <li>{@code currentAccountId}   (Integer)  — Account.accountID</li>
 *   <li>{@code currentAccountName} (String)   — Account.name</li>
 *   <li>{@code currentRole}        (String)   — one of {@link RoleConstants} (Accountpermission.role)</li>
 *   <li>{@code currentBranchID}    (Integer)  — selected Branch.branchID</li>
 * </ul>
 *
 * <p>This class only <i>reads</i> those values. It does NOT perform authentication and
 * does NOT write the session on login. Until login is wired up, {@link #getCurrentRole}
 * falls back to {@link RoleConstants#DEFAULT_ROLE} so the sidebar and Permission Table
 * are still demonstrable. The temporary {@code /dev/act-as} endpoint can set
 * {@code currentRole} for manual testing.</p>
 */
@Component
public class CurrentUserContext {

    public static final String CURRENT_ACCOUNT_ID = "currentAccountId";
    public static final String CURRENT_ACCOUNT_NAME = "currentAccountName";
    public static final String CURRENT_ROLE = "currentRole";
    public static final String CURRENT_BRANCH_ID = "currentBranchID";

    public String getCurrentRole(HttpServletRequest request) {
        Object value = sessionAttribute(request, CURRENT_ROLE);
        if (value != null && RoleConstants.isValid(value.toString())) {
            return value.toString();
        }
        return RoleConstants.DEFAULT_ROLE;
    }

    /** True only when a valid role has actually been set on the session (i.e. "logged in"). */
    public boolean hasAuthenticatedRole(HttpServletRequest request) {
        Object value = sessionAttribute(request, CURRENT_ROLE);
        return value != null && RoleConstants.isValid(value.toString());
    }

    public String getCurrentAccountName(HttpServletRequest request) {
        Object value = sessionAttribute(request, CURRENT_ACCOUNT_NAME);
        return value != null ? value.toString() : "Guest";
    }

    public Integer getCurrentAccountId(HttpServletRequest request) {
        return asInteger(sessionAttribute(request, CURRENT_ACCOUNT_ID));
    }

    public Integer getCurrentBranchId(HttpServletRequest request) {
        return asInteger(sessionAttribute(request, CURRENT_BRANCH_ID));
    }

    private Object sessionAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getAttribute(name);
    }

    private Integer asInteger(Object value) {
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Integer.valueOf(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
