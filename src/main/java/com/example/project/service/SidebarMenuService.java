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
 * presentational config (no database access) and its labels are Vietnamese; only the role
 * <em>codes</em> (OWNER, CASHIER, ...) stay in English. Each role maps to an ordered list of
 * {@link SidebarMenuGroup}s; the Thymeleaf {@code fragments/sidebar} renders whatever this
 * service returns for the current role.</p>
 *
 * <p>Detail/edit pages (e.g. {@code /owner/branches/123}) are intentionally NOT listed as menu
 * items; they are highlighted under their parent list item via
 * {@link #resolveActiveUrl(List, String)} (longest-prefix match).</p>
 */
@Service
public class SidebarMenuService {

    /** Single-item "home" group; rendered as a plain link, so this label is not shown. */
    private static final String GROUP_MAIN = "Tổng quan";

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
     * Finds the menu item whose URL exactly matches the given request URI, across all roles.
     * Used by placeholder pages to label themselves from the same config.
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
     * Returns the URL of the menu item that should be highlighted for the current URI, or
     * {@code null} if none. An item matches when the URI equals its URL or is a sub-path of it
     * (segment boundary); the longest such URL wins, so detail pages highlight their parent.
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
    // Per-role menu configuration (Vietnamese labels)
    // ------------------------------------------------------------------

    private List<SidebarMenuGroup> ownerMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/owner/dashboard", "ti ti-trending-up")),

                menuGroup("Nhân sự & phân quyền",
                        i("Danh sách người dùng", "/owner/users", "ti ti-users"),
                        i("Bảng phân quyền", "/owner/permissions", "ti ti-shield-lock")),

                menuGroup("Chi nhánh",
                        i("Danh sách chi nhánh", "/owner/branches", "ti ti-building-store"),
                        i("Chọn chi nhánh", "/owner/branches/select", "ti ti-switch-horizontal")),

                linkGroup("Phê duyệt",
                        i("Phê duyệt", "/owner/approvals", "ti ti-clipboard-check")),

                linkGroup("Kho nội bộ",
                        i("Kho nội bộ", "/owner/internal-export", "ti ti-package")),

                linkGroup("Thông báo",
                        i("Thông báo", "/owner/notifications", "ti ti-bell")),

                linkGroup("Hồ sơ cá nhân",
                        i("Hồ sơ cá nhân", "/profile", "ti ti-user"))
        );
    }

    private List<SidebarMenuGroup> chiefPharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/chief-pharmacist/dashboard", "ti ti-chart-bar")),

                menuGroup("Hàng hóa",
                        i("Danh sách hàng hóa", "/chief-pharmacist/products", "ti ti-box"),
                        i("Tạo hàng hóa", "/chief-pharmacist/products/create", "ti ti-circle-plus"),
                        i("Lịch sử tồn kho", "/chief-pharmacist/products/inventory-history", "ti ti-history"),
                        i("In mã vạch", "/chief-pharmacist/products/barcode", "ti ti-barcode")),

                menuGroup("Danh mục",
                        i("Danh sách vị trí", "/chief-pharmacist/positions", "ti ti-map-pin"),
                        i("Danh sách xuất xứ", "/chief-pharmacist/origins", "ti ti-world"),
                        i("Danh sách loại hàng", "/chief-pharmacist/types", "ti ti-category"),
                        i("Danh sách nhà sản xuất", "/chief-pharmacist/producers", "ti ti-building-factory-2")),

                menuGroup("Cung ứng",
                        i("Danh sách nhà cung cấp", "/chief-pharmacist/suppliers", "ti ti-truck"),
                        i("Danh sách dự trù", "/chief-pharmacist/procurements", "ti ti-shopping-cart"),
                        i("Danh sách đề nghị mua hàng", "/chief-pharmacist/purchase-requisitions", "ti ti-clipboard-list"),
                        i("Nhập hàng vào lô", "/chief-pharmacist/purchase-invoice-to-batch", "ti ti-packages")),

                menuGroup("Giao dịch",
                        i("Danh sách hóa đơn", "/chief-pharmacist/invoices", "ti ti-file-invoice"),
                        i("Danh sách khoản chi", "/chief-pharmacist/expenses", "ti ti-cash")),

                linkGroup("Xuất kho",
                        i("Xuất kho", "/chief-pharmacist/export-cancellation", "ti ti-shopping-cart")),

                linkGroup("Thông báo",
                        i("Thông báo", "/chief-pharmacist/notifications", "ti ti-bell")),

                linkGroup("Hồ sơ cá nhân",
                        i("Hồ sơ cá nhân", "/profile", "ti ti-user"))
        );
    }

    private List<SidebarMenuGroup> pharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/pharmacist/dashboard", "ti ti-layout-grid")),

                linkGroup("Hàng hóa",
                        i("Hàng hóa", "/pharmacist/products", "ti ti-box")),

                linkGroup("Hóa đơn",
                        i("Hóa đơn", "/pharmacist/invoices", "ti ti-file-invoice")),

                linkGroup("Thông báo",
                        i("Thông báo", "/pharmacist/notifications", "ti ti-bell")),

                linkGroup("Hồ sơ cá nhân",
                        i("Hồ sơ cá nhân", "/profile", "ti ti-user"))
        );
    }

    private List<SidebarMenuGroup> accountantMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/accountant/dashboard", "ti ti-file-text")),

                menuGroup("Tài chính",
                        i("Danh sách khoản chi", "/accountant/expenses", "ti ti-cash"),
                        i("Danh sách công nợ", "/accountant/debts", "ti ti-credit-card"),
                        i("Thiết lập tài chính", "/accountant/financial-setting", "ti ti-settings")),

                linkGroup("Hóa đơn",
                        i("Hóa đơn", "/accountant/invoices", "ti ti-file-invoice")),

                linkGroup("Sổ quỹ",
                        i("Sổ quỹ", "/accountant/income", "ti ti-cash-banknote")),

                linkGroup("Báo cáo",
                        i("Báo cáo", "/accountant/daily-reports", "ti ti-report")),

                linkGroup("Nhập hàng",
                        i("Nhập hàng", "/accountant/purchase-invoices", "ti ti-package-import")),

                linkGroup("Thông báo",
                        i("Thông báo", "/accountant/notifications", "ti ti-bell")),

                linkGroup("Hồ sơ cá nhân",
                        i("Hồ sơ cá nhân", "/profile", "ti ti-user"))
        );
    }

    private List<SidebarMenuGroup> cashierMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/cashier/dashboard", "ti ti-layout-grid")),

                linkGroup("Hóa đơn",
                        i("Hóa đơn", "/cashier/invoices", "ti ti-file-invoice")),

                linkGroup("Khoản thu",
                        i("Khoản thu", "/cashier/income", "ti ti-cash-banknote")),

                linkGroup("Khách hàng",
                        i("Khách hàng", "/cashier/customers", "ti ti-user")),

                linkGroup("Báo cáo",
                        i("Báo cáo", "/cashier/shift-reports", "ti ti-report")),

                linkGroup("Thông báo",
                        i("Thông báo", "/cashier/notifications", "ti ti-bell")),

                linkGroup("Hồ sơ cá nhân",
                        i("Hồ sơ cá nhân", "/profile", "ti ti-user"))
        );
    }

    // ------------------------------------------------------------------
    // Small builders: an item's "module" is auto-set to its group's label.
    // ------------------------------------------------------------------

    private SidebarMenuGroup linkGroup(String label, ItemSpec... specs) {
        return buildGroup(label, false, specs);
    }

    private SidebarMenuGroup menuGroup(String label, ItemSpec... specs) {
        return buildGroup(label, true, specs);
    }

    private SidebarMenuGroup buildGroup(String label, boolean collapsible, ItemSpec... specs) {
        List<SidebarMenuItem> items = new ArrayList<>(specs.length);
        for (ItemSpec spec : specs) {
            items.add(new SidebarMenuItem(spec.label(), spec.url(), spec.icon(), label));
        }
        return new SidebarMenuGroup(label, List.copyOf(items), collapsible);
    }

    private static ItemSpec i(String label, String url, String icon) {
        return new ItemSpec(label, url, icon);
    }

    private record ItemSpec(String label, String url, String icon) {
    }
}
