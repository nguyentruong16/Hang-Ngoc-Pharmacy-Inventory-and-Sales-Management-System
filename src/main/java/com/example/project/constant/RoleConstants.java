package com.example.project.constant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central definition of the valid role values used across the application.
 *
 * <p>The conceptual source of truth for a user's role is
 * {@code Accountpermission.role} (a {@code varchar(50)} in the database). There is
 * intentionally no Role / Permission / RolePermission table — roles are a fixed,
 * code-defined set. These constants exist so role names are written in exactly one
 * place and never drift between the sidebar, the permission matrix and (later) the
 * login module.</p>
 */
public final class RoleConstants {

    public static final String OWNER = "OWNER";
    public static final String CHIEF_PHARMACIST = "CHIEF_PHARMACIST";
    public static final String PHARMACIST = "PHARMACIST";
    public static final String ACCOUNTANT = "ACCOUNTANT";
    public static final String CASHIER = "CASHIER";

    /** All valid roles, in display order. */
    public static final List<String> ALL = List.of(
            OWNER, CHIEF_PHARMACIST, PHARMACIST, ACCOUNTANT, CASHIER);

    public static final List<String> NON_OWNER_ROLES = List.of(
            CHIEF_PHARMACIST, PHARMACIST, ACCOUNTANT, CASHIER
    );

    /**
     * Safe fallback used only when an <em>invalid</em> role string is supplied to a lookup
     * (e.g. an unknown {@code ?role=} value on the Permission Table, or a menu/matrix lookup
     * for an unrecognised role). It is NOT used to resolve the signed-in user's role — that
     * always comes from the authenticated {@code AccountPrincipal}; an unauthenticated request
     * gets no role and no sidebar.
     */
    public static final String DEFAULT_ROLE = OWNER;

    private RoleConstants() {
    }

    public static boolean isValid(String role) {
        return role != null && ALL.contains(role);
    }

    /** Human-friendly label, e.g. {@code CHIEF_PHARMACIST -> "Chief Pharmacist"}. */
    public static String displayName(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }
        String[] parts = role.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /** Vietnamese display label for a role, e.g. {@code CASHIER -> "Thu ngân"}. */
    public static String vietnameseName(String role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case OWNER -> "Chủ sở hữu";
            case CHIEF_PHARMACIST -> "Dược sĩ trưởng";
            case PHARMACIST -> "Dược sĩ";
            case ACCOUNTANT -> "Kế toán";
            case CASHIER -> "Thu ngân";
            default -> role;
        };
    }

    /** Ordered map of role code -> Vietnamese label, for building role dropdowns. */
    public static Map<String, String> vietnameseLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String role : ALL) {
            labels.put(role, vietnameseName(role));
        }
        return labels;
    }

    public static Map<String, String> nonOwnerVietnameseLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (String role : NON_OWNER_ROLES) {
            labels.put(role, vietnameseName(role));
        }
        return labels;
    }

    /** URL slug for a role, e.g. {@code CHIEF_PHARMACIST -> "chief-pharmacist"}. */
    public static String urlPrefix(String role) {
        if (role == null) {
            return "";
        }
        return role.toLowerCase().replace('_', '-');
    }

    /** Landing page for a role, e.g. {@code OWNER -> "/owner/dashboard"}. */
    public static String dashboardPath(String role) {
        String safe = isValid(role) ? role : DEFAULT_ROLE;
        return "/" + urlPrefix(safe) + "/dashboard";
    }
}
