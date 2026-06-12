package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.view.PermissionMatrixRow;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides the permission matrix (which modules/actions each role may use) entirely from
 * code/config.
 *
 * <p><b>Read-only by design.</b> The database has no Permission or RolePermission table,
 * so there is nothing to persist to. This service exists to <i>describe</i> the intended
 * access model for the Owner-only Permission Table screen; it never saves anything.</p>
 */
@Service
public class PermissionMatrixService {

    // Action columns, in display order.
    public static final String A_VIEW = "View";
    public static final String A_CREATE = "Create";
    public static final String A_UPDATE = "Update";
    public static final String A_DELETE = "Delete";
    public static final String A_APPROVE = "Approve";
    public static final String A_PRINT = "Print";
    public static final String A_EXPORT = "Export";

    public static final List<String> ACTIONS = List.of(
            A_VIEW, A_CREATE, A_UPDATE, A_DELETE, A_APPROVE, A_PRINT, A_EXPORT);

    // Module rows, in display order.
    public static final String M_DASHBOARD = "Dashboard";
    public static final String M_PRODUCT = "Product";
    public static final String M_PRODUCT_CATEGORY = "Product Category";
    public static final String M_SUPPLIER = "Supplier";
    public static final String M_PURCHASE_REQUISITION = "Purchase Requisition";
    public static final String M_PROCUREMENT = "Procurement";
    public static final String M_PURCHASE_INVOICE = "Purchase Invoice";
    public static final String M_INVOICE = "Invoice";
    public static final String M_CUSTOMER = "Customer";
    public static final String M_INCOME = "Income";
    public static final String M_EXPENSE = "Expense";
    public static final String M_DEBT = "Debt";
    public static final String M_SHIFT_REPORT = "Shift Report";
    public static final String M_DAILY_REPORT = "Daily Report";
    public static final String M_BRANCH = "Branch";
    public static final String M_USER = "User";
    public static final String M_PERMISSION = "Permission";
    public static final String M_APPROVAL = "Approval";
    public static final String M_INTERNAL_EXPORT = "Internal Export";
    public static final String M_FINANCIAL_SETTING = "Financial Setting";
    public static final String M_NOTIFICATION = "Notification";

    public static final List<String> MODULES = List.of(
            M_DASHBOARD, M_PRODUCT, M_PRODUCT_CATEGORY, M_SUPPLIER, M_PURCHASE_REQUISITION,
            M_PROCUREMENT, M_PURCHASE_INVOICE, M_INVOICE, M_CUSTOMER, M_INCOME, M_EXPENSE,
            M_DEBT, M_SHIFT_REPORT, M_DAILY_REPORT, M_BRANCH, M_USER, M_PERMISSION,
            M_APPROVAL, M_INTERNAL_EXPORT, M_FINANCIAL_SETTING, M_NOTIFICATION);

    /** role -> (module -> set of allowed actions). */
    private final Map<String, Map<String, Set<String>>> permissionsByRole = new LinkedHashMap<>();

    public PermissionMatrixService() {
        permissionsByRole.put(RoleConstants.OWNER, ownerPermissions());
        permissionsByRole.put(RoleConstants.CHIEF_PHARMACIST, chiefPharmacistPermissions());
        permissionsByRole.put(RoleConstants.PHARMACIST, pharmacistPermissions());
        permissionsByRole.put(RoleConstants.ACCOUNTANT, accountantPermissions());
        permissionsByRole.put(RoleConstants.CASHIER, cashierPermissions());
    }

    /**
     * Builds the full matrix for a role: one row per module (all modules are shown), each
     * cell flagged true only where the role is granted that action.
     */
    public List<PermissionMatrixRow> buildMatrix(String role) {
        String key = RoleConstants.isValid(role) ? role : RoleConstants.DEFAULT_ROLE;
        Map<String, Set<String>> granted = permissionsByRole.getOrDefault(key, Map.of());

        List<PermissionMatrixRow> rows = new java.util.ArrayList<>(MODULES.size());
        for (String module : MODULES) {
            Set<String> allowed = granted.getOrDefault(module, Set.of());
            Map<String, Boolean> cells = new LinkedHashMap<>();
            for (String action : ACTIONS) {
                cells.put(action, allowed.contains(action));
            }
            rows.add(new PermissionMatrixRow(module, cells));
        }
        return rows;
    }

    // ------------------------------------------------------------------
    // Per-role permission configuration (mirrors the assigned spec exactly)
    // ------------------------------------------------------------------

    private Map<String, Set<String>> ownerPermissions() {
        Map<String, Set<String>> p = new LinkedHashMap<>();
        p.put(M_DASHBOARD, Set.of(A_VIEW));
        p.put(M_BRANCH, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_USER, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_PERMISSION, Set.of(A_VIEW));
        p.put(M_APPROVAL, Set.of(A_VIEW, A_APPROVE));
        p.put(M_INTERNAL_EXPORT, Set.of(A_VIEW, A_CREATE, A_APPROVE, A_EXPORT));
        p.put(M_NOTIFICATION, Set.of(A_VIEW));
        return p;
    }

    private Map<String, Set<String>> chiefPharmacistPermissions() {
        Map<String, Set<String>> p = new LinkedHashMap<>();
        p.put(M_DASHBOARD, Set.of(A_VIEW));
        p.put(M_PRODUCT, Set.of(A_VIEW, A_CREATE, A_UPDATE, A_PRINT, A_EXPORT));
        p.put(M_PRODUCT_CATEGORY, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_SUPPLIER, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_PURCHASE_REQUISITION, Set.of(A_VIEW, A_CREATE, A_UPDATE, A_APPROVE));
        p.put(M_PROCUREMENT, Set.of(A_VIEW, A_CREATE, A_UPDATE, A_APPROVE));
        p.put(M_EXPENSE, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_INVOICE, Set.of(A_VIEW));
        p.put(M_PURCHASE_INVOICE, Set.of(A_VIEW));
        p.put(M_INTERNAL_EXPORT, Set.of(A_VIEW, A_CREATE));
        p.put(M_NOTIFICATION, Set.of(A_VIEW));
        return p;
    }

    private Map<String, Set<String>> pharmacistPermissions() {
        Map<String, Set<String>> p = new LinkedHashMap<>();
        p.put(M_DASHBOARD, Set.of(A_VIEW));
        p.put(M_PRODUCT, Set.of(A_VIEW));
        p.put(M_INVOICE, Set.of(A_VIEW));
        p.put(M_NOTIFICATION, Set.of(A_VIEW));
        return p;
    }

    private Map<String, Set<String>> accountantPermissions() {
        Map<String, Set<String>> p = new LinkedHashMap<>();
        p.put(M_DASHBOARD, Set.of(A_VIEW));
        p.put(M_PURCHASE_INVOICE, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_DAILY_REPORT, Set.of(A_VIEW, A_CREATE, A_EXPORT));
        p.put(M_DEBT, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_EXPENSE, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_INVOICE, Set.of(A_VIEW));
        p.put(M_SHIFT_REPORT, Set.of(A_VIEW));
        p.put(M_FINANCIAL_SETTING, Set.of(A_VIEW, A_UPDATE));
        p.put(M_NOTIFICATION, Set.of(A_VIEW));
        return p;
    }

    private Map<String, Set<String>> cashierPermissions() {
        Map<String, Set<String>> p = new LinkedHashMap<>();
        p.put(M_DASHBOARD, Set.of(A_VIEW));
        p.put(M_CUSTOMER, Set.of(A_VIEW, A_CREATE, A_UPDATE));
        p.put(M_INVOICE, Set.of(A_VIEW, A_CREATE, A_PRINT));
        p.put(M_INCOME, Set.of(A_VIEW, A_CREATE));
        p.put(M_DEBT, Set.of(A_VIEW));
        p.put(M_SHIFT_REPORT, Set.of(A_VIEW, A_CREATE));
        p.put(M_NOTIFICATION, Set.of(A_VIEW));
        return p;
    }
}
