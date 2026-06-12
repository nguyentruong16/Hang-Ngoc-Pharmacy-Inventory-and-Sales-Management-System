package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.view.SidebarMenuGroup;
import com.example.project.view.SidebarMenuItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the sidebar menu for a given role from a fixed, code-defined configuration.
 *
 * <p>This is the single source of truth for role-based navigation. The menu is purely
 * presentational config (no database access). Each role maps to an ordered list of
 * {@link SidebarMenuGroup}s; the Thymeleaf {@code fragments/sidebar} renders whatever
 * this service returns for the current role.</p>
 *
 * <p>Detail/edit pages (e.g. {@code /owner/branches/123}) are intentionally NOT listed
 * as menu items; they are highlighted under their parent list item via
 * {@link #resolveActiveUrl(List, String)} (longest-prefix match).</p>
 */
@Service
public class SidebarMenuService {

    private static final String GROUP_MAIN = "Main";

    private final Map<String, List<SidebarMenuGroup>> menusByRole = new LinkedHashMap<>();

    public SidebarMenuService() {
        menusByRole.put(RoleConstants.OWNER, ownerMenu());
        menusByRole.put(RoleConstants.CHIEF_PHARMACIST, chiefPharmacistMenu());
        menusByRole.put(RoleConstants.PHARMACIST, pharmacistMenu());
        menusByRole.put(RoleConstants.ACCOUNTANT, accountantMenu());
        menusByRole.put(RoleConstants.CASHIER, cashierMenu());
    }

    /** Menu groups for the given role (falls back to the default role if unknown). */
    public List<SidebarMenuGroup> getMenu(String role) {
        String key = RoleConstants.isValid(role) ? role : RoleConstants.DEFAULT_ROLE;
        return menusByRole.getOrDefault(key, menusByRole.get(RoleConstants.DEFAULT_ROLE));
    }

    /**
     * Finds the menu item whose URL exactly matches the given request URI, across all
     * roles. Used by placeholder pages to label themselves from the same config.
     */
    public SidebarMenuItem findByUri(String uri) {
        if (uri == null) {
            return null;
        }
        for (List<SidebarMenuGroup> groups : menusByRole.values()) {
            for (SidebarMenuGroup group : groups) {
                for (SidebarMenuItem item : group.getItems()) {
                    if (item.getUrl().equals(uri)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the URL of the menu item that should be highlighted for the current URI,
     * or {@code null} if none. An item matches when the URI equals its URL or is a
     * sub-path of it (segment boundary); the longest such URL wins, so detail pages
     * highlight their parent list item.
     */
    public String resolveActiveUrl(List<SidebarMenuGroup> menu, String uri) {
        if (menu == null || uri == null) {
            return null;
        }
        String best = null;
        for (SidebarMenuGroup group : menu) {
            for (SidebarMenuItem item : group.getItems()) {
                String url = item.getUrl();
                boolean matches = uri.equals(url) || uri.startsWith(url + "/");
                if (matches && (best == null || url.length() > best.length())) {
                    best = url;
                }
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // Per-role menu configuration
    // ------------------------------------------------------------------

    private List<SidebarMenuGroup> ownerMenu() {
        return List.of(
                group(GROUP_MAIN,
                        i("Dashboard", "/owner/dashboard", "ti ti-home")),
                group("Branch Management",
                        i("Branch Selection", "/owner/branches/select", "ti ti-switch-horizontal"),
                        i("Branch List", "/owner/branches", "ti ti-building-store"),
                        i("Create New Branch", "/owner/branches/create", "ti ti-circle-plus")),
                group("User Management",
                        i("User List", "/owner/users", "ti ti-users"),
                        i("Create New User", "/owner/users/create", "ti ti-user-plus")),
                group("Permission Management",
                        i("Permission Table", "/owner/permissions", "ti ti-shield-lock")),
                group("Approval",
                        i("Approve List", "/owner/approvals", "ti ti-checklist")),
                group("Internal Export",
                        i("Internal Export", "/owner/internal-export", "ti ti-file-export")),
                group("Notifications",
                        i("Notifications", "/owner/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> chiefPharmacistMenu() {
        return List.of(
                group(GROUP_MAIN,
                        i("Dashboard", "/chief-pharmacist/dashboard", "ti ti-home")),
                group("Products",
                        i("Product List", "/chief-pharmacist/products", "ti ti-box"),
                        i("Add New Product", "/chief-pharmacist/products/create", "ti ti-circle-plus"),
                        i("Print Barcode", "/chief-pharmacist/products/barcode", "ti ti-barcode"),
                        i("Product Inventory History", "/chief-pharmacist/products/inventory-history", "ti ti-history")),
                group("Product Categories",
                        i("Origin List", "/chief-pharmacist/origins", "ti ti-world"),
                        i("Type List", "/chief-pharmacist/types", "ti ti-category"),
                        i("Producer List", "/chief-pharmacist/producers", "ti ti-building-factory-2"),
                        i("Position List", "/chief-pharmacist/positions", "ti ti-map-pin")),
                group("Suppliers",
                        i("Supplier List", "/chief-pharmacist/suppliers", "ti ti-truck"),
                        i("Create New Supplier", "/chief-pharmacist/suppliers/create", "ti ti-circle-plus")),
                group("Purchase Requisitions",
                        i("Purchase Requisition List", "/chief-pharmacist/purchase-requisitions", "ti ti-clipboard-list"),
                        i("Create New Purchase Requisition", "/chief-pharmacist/purchase-requisitions/create", "ti ti-circle-plus")),
                group("Procurements",
                        i("Procurement List", "/chief-pharmacist/procurements", "ti ti-shopping-cart"),
                        i("Create New Procurement", "/chief-pharmacist/procurements/create", "ti ti-circle-plus")),
                group("Expenses",
                        i("Expense List", "/chief-pharmacist/expenses", "ti ti-cash"),
                        i("Create New Expense", "/chief-pharmacist/expenses/create", "ti ti-circle-plus")),
                group("Invoices",
                        i("Invoice List", "/chief-pharmacist/invoices", "ti ti-file-invoice"),
                        i("Purchase Invoice to Batch", "/chief-pharmacist/purchase-invoice-to-batch", "ti ti-packages")),
                group("Inventory Operations",
                        i("Export Cancellation", "/chief-pharmacist/export-cancellation", "ti ti-file-x")),
                group("Notifications",
                        i("Notifications", "/chief-pharmacist/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> pharmacistMenu() {
        return List.of(
                group(GROUP_MAIN,
                        i("Dashboard", "/pharmacist/dashboard", "ti ti-home")),
                group("Products",
                        i("Product List", "/pharmacist/products", "ti ti-box")),
                group("Sales Invoices",
                        i("Invoice List", "/pharmacist/invoices", "ti ti-file-invoice")),
                group("Notifications",
                        i("Notifications", "/pharmacist/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> accountantMenu() {
        return List.of(
                group(GROUP_MAIN,
                        i("Dashboard", "/accountant/dashboard", "ti ti-home")),
                group("Purchase Invoices",
                        i("Purchase Invoice List", "/accountant/purchase-invoices", "ti ti-file-invoice"),
                        i("Create New Purchase Invoice", "/accountant/purchase-invoices/create", "ti ti-circle-plus")),
                group("Daily Reports",
                        i("Daily Report List", "/accountant/daily-reports", "ti ti-report"),
                        i("Create New Daily Report", "/accountant/daily-reports/create", "ti ti-circle-plus")),
                group("Debts",
                        i("Debt List", "/accountant/debts", "ti ti-credit-card"),
                        i("Create New Debt", "/accountant/debts/create", "ti ti-circle-plus")),
                group("Expenses",
                        i("Expense List", "/accountant/expenses", "ti ti-cash"),
                        i("Create New Expense", "/accountant/expenses/create", "ti ti-circle-plus")),
                group("Sales Invoices",
                        i("Invoice List", "/accountant/invoices", "ti ti-file-invoice")),
                group("Shift Reports",
                        i("Shift Report List", "/accountant/shift-reports", "ti ti-report-money")),
                group("Financial Setting",
                        i("Financial Setting", "/accountant/financial-setting", "ti ti-settings")),
                group("Notifications",
                        i("Notifications", "/accountant/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> cashierMenu() {
        return List.of(
                group(GROUP_MAIN,
                        i("Dashboard", "/cashier/dashboard", "ti ti-home")),
                group("Customers",
                        i("Customer List", "/cashier/customers", "ti ti-users"),
                        i("Create New Customer", "/cashier/customers/create", "ti ti-user-plus")),
                group("Sales Invoices",
                        i("Invoice List", "/cashier/invoices", "ti ti-file-invoice")),
                group("Income",
                        i("Income List", "/cashier/income", "ti ti-cash"),
                        i("Create New Income", "/cashier/income/create", "ti ti-circle-plus")),
                group("Receipts",
                        i("Other Receipt", "/cashier/receipts/other", "ti ti-receipt"),
                        i("Debt", "/cashier/debts", "ti ti-credit-card")),
                group("Shift Reports",
                        i("Shift Report List", "/cashier/shift-reports", "ti ti-report-money"),
                        i("Create New Shift Report", "/cashier/shift-reports/create", "ti ti-circle-plus")),
                group("Notifications",
                        i("Notifications", "/cashier/notifications", "ti ti-bell")));
    }

    // ------------------------------------------------------------------
    // Small builders: an item's "module" is auto-set to its group's label.
    // ------------------------------------------------------------------

    private SidebarMenuGroup group(String label, ItemSpec... specs) {
        List<SidebarMenuItem> items = new ArrayList<>(specs.length);
        for (ItemSpec spec : specs) {
            items.add(new SidebarMenuItem(spec.label(), spec.url(), spec.icon(), label));
        }
        return new SidebarMenuGroup(label, List.copyOf(items));
    }

    private static ItemSpec i(String label, String url, String icon) {
        return new ItemSpec(label, url, icon);
    }

    private record ItemSpec(String label, String url, String icon) {
    }
}
