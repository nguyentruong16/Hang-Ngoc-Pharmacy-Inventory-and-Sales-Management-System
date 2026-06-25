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
 * <em>codes</em> (OWNER, CHIEF_PHARMACIST, ...) stay in English. Each role maps to an ordered list of
 * {@link SidebarMenuGroup}s; the Thymeleaf {@code fragments/sidebar} renders whatever this
 * service returns for the current role.</p>
 *
 * <p>Detail/edit pages (e.g. {@code /owner/products/123}) are intentionally NOT listed as menu
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
        if (uri.startsWith("/owner/branch-list/branch-detail")) {
            uri = "/owner/branch-list";
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
                linkGroup(GROUP_MAIN, "ti ti-chart-line",
                        i("Tổng quan", "/owner/dashboard", "ti ti-chart-line")),

                menuGroup("Quản trị", "ti ti-settings",
                        i("Danh sách người dùng", "/owner/users", "ti ti-users"),
                        i("Bảng phân quyền", "/owner/permissions", "ti ti-shield-lock"),
                        i("Danh sách chi nhánh", "/owner/branch-list", "ti ti-building-store")),

                menuGroup("Hàng hóa", "ti ti-package",
                        i("Danh sách hàng hóa", "/owner/products", "ti ti-package")),

                menuGroup("Danh mục hàng hóa", "ti ti-layout-grid",
                        i("Xuất xứ", "/owner/origins", "ti ti-world"),
                        i("Loại hàng", "/owner/types", "ti ti-category"),
                        i("Nhà sản xuất", "/owner/producers", "ti ti-building-factory-2")),

                menuGroup("Cung ứng", "ti ti-truck",
                        i("Danh sách nhà cung cấp", "/owner/suppliers", "ti ti-truck"),
                        i("Danh sách phiếu nhập", "/owner/purchase-invoices", "ti ti-receipt")),

                menuGroup("Tài chính", "ti ti-cash-banknote",
                        i("Danh sách công nợ", "/owner/debts", "ti ti-credit-card"),
                        i("Danh sách hóa đơn VAT", "/owner/vat-invoices", "ti ti-file-dollar"),
                        i("Thiết lập tài chính", "/owner/financial-setting", "ti ti-settings")),

                menuGroup("Kho", "ti ti-archive",
                        i("Xuất kho", "/owner/stock-outs", "ti ti-archive")),

                linkGroup("Khách hàng", "ti ti-users",
                        i("Khách hàng", "/owner/customers", "ti ti-users")),

                linkGroup("Phê duyệt", "ti ti-clipboard-check",
                        i("Phê duyệt", "/owner/approvals", "ti ti-clipboard-check"))
        );
    }

    private List<SidebarMenuGroup> chiefPharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN, "ti ti-chart-line",
                        i("Tổng quan", "/chief-pharmacist/dashboard", "ti ti-chart-line")),

                linkGroup("Bán hàng", "ti ti-shopping-cart",
                        i("Bán hàng", "/chief-pharmacist/selling", "ti ti-shopping-cart")),

                linkGroup("Hàng hóa", "ti ti-package",
                        i("Hàng hóa", "/chief-pharmacist/products", "ti ti-package")),

                menuGroup("Danh mục", "ti ti-layout-grid",
                        i("Danh sách vị trí", "/chief-pharmacist/positions", "ti ti-map-pin"),
                        i("Danh sách xuất xứ", "/chief-pharmacist/origins", "ti ti-world"),
                        i("Danh sách loại hàng", "/chief-pharmacist/types", "ti ti-category"),
                        i("Danh sách nhà sản xuất", "/chief-pharmacist/producers", "ti ti-building-factory-2")),

                menuGroup("Cung ứng", "ti ti-truck",
                        i("Danh sách nhà cung cấp", "/chief-pharmacist/suppliers", "ti ti-truck"),
                        i("Danh sách dự trù", "/chief-pharmacist/procurements", "ti ti-clipboard-list"),
                        i("Danh sách đề nghị mua hàng", "/chief-pharmacist/purchase-requisitions", "ti ti-shopping-cart-plus"),
                        i("Danh sách phiếu nhập", "/chief-pharmacist/purchase-invoices", "ti ti-receipt")),

                menuGroup("Tài chính", "ti ti-cash-banknote",
                        i("Danh sách công nợ", "/chief-pharmacist/debts", "ti ti-credit-card")),

                menuGroup("Kho", "ti ti-archive",
                        i("Xuất kho", "/chief-pharmacist/stock-outs", "ti ti-archive")),

                menuGroup("Giao dịch", "ti ti-file-invoice",
                        i("Danh sách hóa đơn", "/chief-pharmacist/invoices", "ti ti-file-invoice"),
                        i("Danh sách khoản thu", "/chief-pharmacist/incomes", "ti ti-cash-banknote"),
                        i("Danh sách khoản chi", "/chief-pharmacist/expenses", "ti ti-cash"),
                        i("Danh sách trả hàng", "/chief-pharmacist/returns", "ti ti-rotate")),

                linkGroup("Khách hàng", "ti ti-users",
                        i("Khách hàng", "/chief-pharmacist/customers", "ti ti-users")),

                linkGroup("Báo cáo ca", "ti ti-report",
                        i("Báo cáo ca", "/chief-pharmacist/shift-reports", "ti ti-report"))
        );
    }

    private List<SidebarMenuGroup> pharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN, "ti ti-chart-line",
                        i("Tổng quan", "/pharmacist/dashboard", "ti ti-chart-line")),

                linkGroup("Bán hàng", "ti ti-shopping-cart",
                        i("Bán hàng", "/pharmacist/selling", "ti ti-shopping-cart")),

                linkGroup("Hàng hóa", "ti ti-package",
                        i("Hàng hóa", "/pharmacist/products", "ti ti-package")),

                linkGroup("Hóa đơn", "ti ti-file-invoice",
                        i("Hóa đơn", "/pharmacist/invoices", "ti ti-file-invoice")),

                linkGroup("Khách hàng", "ti ti-users",
                        i("Khách hàng", "/pharmacist/customers", "ti ti-users")),

                linkGroup("Báo cáo ca", "ti ti-report",
                        i("Báo cáo ca", "/pharmacist/shift-reports", "ti ti-report"))
        );
    }

    private List<SidebarMenuGroup> accountantMenu() {
        return List.of(
                linkGroup(GROUP_MAIN, "ti ti-chart-line",
                        i("Tổng quan", "/accountant/dashboard", "ti ti-chart-line")),

                linkGroup("Xuất kho", "ti ti-archive",
                        i("Xuất kho", "/accountant/stock-outs", "ti ti-archive")),

                linkGroup("Công nợ", "ti ti-credit-card",
                        i("Công nợ", "/accountant/debts", "ti ti-credit-card")),

                linkGroup("Phiếu nhập", "ti ti-receipt",
                        i("Phiếu nhập", "/accountant/purchase-invoices", "ti ti-receipt")),

                linkGroup("Khoản chi", "ti ti-cash",
                        i("Khoản chi", "/accountant/expenses", "ti ti-cash")),

                menuGroup("Báo cáo", "ti ti-report",
                        i("Danh sách báo cáo ngày", "/accountant/daily-reports", "ti ti-report"),
                        i("Danh sách báo cáo ca", "/accountant/shift-reports", "ti ti-report-analytics")),

                menuGroup("Hóa đơn", "ti ti-file-invoice",
                        i("Danh sách hóa đơn", "/accountant/invoices", "ti ti-file-invoice"),
                        i("Danh sách hóa đơn VAT", "/accountant/vat-invoices", "ti ti-file-dollar"))
        );
    }

    // ------------------------------------------------------------------
    // Small builders: an item's "module" is auto-set to its group's label.
    // ------------------------------------------------------------------

    private SidebarMenuGroup linkGroup(String label, String icon, ItemSpec... specs) {
        return buildGroup(label, icon, false, specs);
    }

    private SidebarMenuGroup menuGroup(String label, String icon, ItemSpec... specs) {
        return buildGroup(label, icon, true, specs);
    }

    private SidebarMenuGroup buildGroup(String label, String icon, boolean collapsible, ItemSpec... specs) {
        List<SidebarMenuItem> items = new ArrayList<>(specs.length);
        for (ItemSpec spec : specs) {
            items.add(new SidebarMenuItem(spec.label(), spec.url(), spec.icon(), label));
        }
        return new SidebarMenuGroup(label, icon, List.copyOf(items), collapsible);
    }

    private static ItemSpec i(String label, String url, String icon) {
        return new ItemSpec(label, url, icon);
    }

    private record ItemSpec(String label, String url, String icon) {
    }
}
