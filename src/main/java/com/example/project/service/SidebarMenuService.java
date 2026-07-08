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
 * <em>codes</em> (OWNER, PHARMACIST, ...) stay in English. Each role maps to an ordered list of
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
        if (uri.startsWith("/owner/producers/create-producer")
                || uri.startsWith("/owner/producers/update-producer")) {
            uri = "/owner/producers";
        }
        if (uri.startsWith("/owner/types/create-type")
                || uri.startsWith("/owner/types/update-type")) {
            uri = "/owner/types";
        }
        if (uri.startsWith("/owner/positions/create-position")
                || uri.startsWith("/owner/positions/update-position")) {
            uri = "/owner/positions";
        }
        if (uri.startsWith("/owner/procurements/create-procurementplan")
                || uri.startsWith("/owner/procurements/update-procurementplan")
                || uri.startsWith("/owner/procurements/view-detail")) {
            uri = "/owner/procurements";
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
                        i("Bảng phân quyền", "/owner/permissions", "ti ti-shield-lock")),

                linkGroup("Bán hàng", "ti ti-shopping-cart",
                        i("Bán hàng", "/owner/selling", "ti ti-shopping-cart")),

                menuGroup("Hàng hóa", "ti ti-package",
                        i("Danh sách hàng hóa", "/owner/products", "ti ti-package")),

                menuGroup("Danh mục hàng hóa", "ti ti-layout-grid",
                        i("Danh sách vị trí", "/owner/positions", "ti ti-map-pin"),
                        i("Loại hàng", "/owner/types", "ti ti-category"),
                        i("Nhà sản xuất", "/owner/producers", "ti ti-building-factory-2")),

                menuGroup("Cung ứng", "ti ti-truck",
                        i("Danh sách nhà cung cấp", "/supplier", "ti ti-truck"),
                        i("Danh sách dự trù mua hàng", "/owner/procurements", "ti ti-clipboard-list"),
                        i("Danh sách phiếu nhập", "/owner/purchase-invoices", "ti ti-receipt")),

                menuGroup("Tài chính", "ti ti-cash-banknote",
                        i("Danh sách công nợ", "/owner/debts", "ti ti-credit-card"),
                        i("Danh sách hóa đơn VAT", "/owner/vat-invoices", "ti ti-file-dollar"),
                        i("Thiết lập tài chính", "/owner/financial-setting", "ti ti-settings")),

                menuGroup("Kho", "ti ti-archive",
                        i("Xuất kho", "/owner/stock-outs", "ti ti-archive"),
                        i("Tạo phiếu hủy xuất kho", "/owner/stock-outs/destroy/create", "ti ti-trash")),

                menuGroup("Giao dịch", "ti ti-file-invoice",
                        i("Danh sách hóa đơn", "/owner/invoices", "ti ti-file-invoice"),
                        i("Danh sách khoản thu", "/owner/incomes", "ti ti-cash-banknote"),
                        i("Danh sách khoản chi", "/owner/expenses", "ti ti-cash"),
                        i("Danh sách trả hàng", "/owner/returns", "ti ti-rotate")),

                linkGroup("Khách hàng", "ti ti-users",
                        i("Khách hàng", "/customer", "ti ti-users")),

                linkGroup("Phê duyệt", "ti ti-clipboard-check",
                        i("Phê duyệt", "/owner/approvals", "ti ti-clipboard-check")),

                menuGroup("Báo cáo", "ti ti-report",
                        i("Báo cáo ca", "/owner/shift-reports", "ti ti-report"),
                        i("Báo cáo tổng hợp theo ngày", "/owner/daily-reports", "ti ti-report-analytics"))
        );
    }

    private List<SidebarMenuGroup> pharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN, "ti ti-chart-line",
                        i("Tổng quan", "/pharmacist/dashboard", "ti ti-chart-line")),

                linkGroup("Bán hàng", "ti ti-shopping-cart",
                        i("Bán hàng", "/pharmacist/selling", "ti ti-shopping-cart")),

                menuGroup("Hàng hóa", "ti ti-package",
                        i("Danh sách hàng hóa", "/pharmacist/products", "ti ti-package"),
                        i("Loại hàng", "/pharmacist/types", "ti ti-category"),
                        i("Nhà sản xuất", "/pharmacist/producers", "ti ti-building-factory-2")),

                linkGroup("Danh sách vị trí", "ti ti-map-pin",
                        i("Danh sách vị trí", "/pharmacist/positions", "ti ti-map-pin")),

                menuGroup("Cung ứng", "ti ti-truck",
                        i("Danh sách nhà cung cấp", "/supplier", "ti ti-truck"),
                        i("Danh sách phiếu nhập", "/pharmacist/purchase-invoices", "ti ti-receipt")),

                linkGroup("Kho", "ti ti-archive",
                        i("Tạo phiếu điều chỉnh kho", "/pharmacist/stock-outs/create", "ti ti-archive")),

                menuGroup("Giao dịch", "ti ti-file-invoice",
                        i("Danh sách hóa đơn", "/pharmacist/invoices", "ti ti-file-invoice"),
                        i("Danh sách khoản thu", "/pharmacist/incomes", "ti ti-cash-banknote"),
                        i("Danh sách trả hàng", "/pharmacist/returns", "ti ti-rotate")),

                linkGroup("Khách hàng", "ti ti-users",
                        i("Khách hàng", "/customer", "ti ti-users")),

                linkGroup("Báo cáo ca", "ti ti-report",
                        i("Báo cáo ca", "/pharmacist/shift-reports", "ti ti-report"))
        );
    }

    private List<SidebarMenuGroup> accountantMenu() {
        return List.of(
                linkGroup(GROUP_MAIN, "ti ti-chart-line",
                        i("Tổng quan", "/accountant/dashboard", "ti ti-chart-line")),

                menuGroup("Hàng hóa", "ti ti-package",
                        i("Danh sách hàng hóa", "/accountant/products", "ti ti-package"),
                        i("Loại hàng", "/accountant/types", "ti ti-category"),
                        i("Nhà sản xuất", "/accountant/producers", "ti ti-building-factory-2")),

                linkGroup("Nhà cung cấp", "ti ti-truck",
                        i("Nhà cung cấp", "/supplier", "ti ti-truck")),

                linkGroup("Khách hàng", "ti ti-users",
                        i("Khách hàng", "/customer", "ti ti-users")),

                linkGroup("Công nợ", "ti ti-credit-card",
                        i("Công nợ", "/accountant/debts", "ti ti-credit-card")),

                linkGroup("Phiếu nhập", "ti ti-receipt",
                        i("Phiếu nhập", "/accountant/purchase-invoices", "ti ti-receipt")),

                linkGroup("Khoản chi", "ti ti-cash",
                        i("Khoản chi", "/accountant/expenses", "ti ti-cash")),

                linkGroup("Thiết lập tài chính", "ti ti-settings",
                        i("Thiết lập tài chính", "/accountant/financial-setting", "ti ti-settings")),

                linkGroup("Báo cáo", "ti ti-report",
                        i("Danh sách báo cáo ngày", "/accountant/daily-reports", "ti ti-report")),

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
