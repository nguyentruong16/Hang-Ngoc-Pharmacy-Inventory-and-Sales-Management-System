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
                        i("Tổng quan", "/owner/dashboard", "ti ti-home")),
                menuGroup("Quản lý chi nhánh",
                        i("Chọn chi nhánh", "/owner/branches/select", "ti ti-switch-horizontal"),
                        i("Danh sách chi nhánh", "/owner/branches", "ti ti-building-store"),
                        i("Tạo chi nhánh mới", "/owner/branches/create", "ti ti-circle-plus")),
                menuGroup("Quản lý người dùng",
                        i("Danh sách người dùng", "/owner/users", "ti ti-users"),
                        i("Tạo người dùng mới", "/owner/users/create", "ti ti-user-plus")),
                menuGroup("Phân quyền",
                        i("Phân quyền theo chi nhánh", "/owner/permissions", "ti ti-shield-lock")),
                menuGroup("Phê duyệt",
                        i("Danh sách phê duyệt", "/owner/approvals", "ti ti-checklist")),
                menuGroup("Xuất nội bộ",
                        i("Xuất nội bộ", "/owner/internal-export", "ti ti-file-export")),
                menuGroup("Thông báo",
                        i("Thông báo", "/owner/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> chiefPharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/chief-pharmacist/dashboard", "ti ti-home")),
                menuGroup("Sản phẩm",
                        i("Danh sách sản phẩm", "/chief-pharmacist/products", "ti ti-box"),
                        i("Thêm sản phẩm mới", "/chief-pharmacist/products/create", "ti ti-circle-plus"),
                        i("In mã vạch", "/chief-pharmacist/products/barcode", "ti ti-barcode"),
                        i("Lịch sử tồn kho", "/chief-pharmacist/products/inventory-history", "ti ti-history")),
                menuGroup("Danh mục sản phẩm",
                        i("Xuất xứ", "/chief-pharmacist/origins", "ti ti-world"),
                        i("Loại sản phẩm", "/chief-pharmacist/types", "ti ti-category"),
                        i("Nhà sản xuất", "/chief-pharmacist/producers", "ti ti-building-factory-2"),
                        i("Vị trí", "/chief-pharmacist/positions", "ti ti-map-pin")),
                menuGroup("Nhà cung cấp",
                        i("Danh sách nhà cung cấp", "/chief-pharmacist/suppliers", "ti ti-truck"),
                        i("Thêm nhà cung cấp", "/chief-pharmacist/suppliers/create", "ti ti-circle-plus")),
                menuGroup("Yêu cầu mua hàng",
                        i("Danh sách yêu cầu mua hàng", "/chief-pharmacist/purchase-requisitions", "ti ti-clipboard-list"),
                        i("Tạo yêu cầu mua hàng", "/chief-pharmacist/purchase-requisitions/create", "ti ti-circle-plus")),
                menuGroup("Kế hoạch mua hàng",
                        i("Danh sách kế hoạch mua hàng", "/chief-pharmacist/procurements", "ti ti-shopping-cart"),
                        i("Tạo kế hoạch mua hàng", "/chief-pharmacist/procurements/create", "ti ti-circle-plus")),
                menuGroup("Chi phí",
                        i("Danh sách chi phí", "/chief-pharmacist/expenses", "ti ti-cash"),
                        i("Tạo chi phí", "/chief-pharmacist/expenses/create", "ti ti-circle-plus")),
                menuGroup("Hóa đơn",
                        i("Danh sách hóa đơn", "/chief-pharmacist/invoices", "ti ti-file-invoice"),
                        i("Nhập lô từ hóa đơn mua", "/chief-pharmacist/purchase-invoice-to-batch", "ti ti-packages")),
                menuGroup("Thao tác kho",
                        i("Hủy xuất kho", "/chief-pharmacist/export-cancellation", "ti ti-file-x")),
                menuGroup("Thông báo",
                        i("Thông báo", "/chief-pharmacist/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> pharmacistMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/pharmacist/dashboard", "ti ti-home")),
                menuGroup("Sản phẩm",
                        i("Danh sách sản phẩm", "/pharmacist/products", "ti ti-box")),
                menuGroup("Hóa đơn bán hàng",
                        i("Danh sách hóa đơn", "/pharmacist/invoices", "ti ti-file-invoice")),
                menuGroup("Thông báo",
                        i("Thông báo", "/pharmacist/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> accountantMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/accountant/dashboard", "ti ti-home")),
                menuGroup("Hóa đơn nhập hàng",
                        i("Danh sách hóa đơn nhập", "/accountant/purchase-invoices", "ti ti-file-invoice"),
                        i("Tạo hóa đơn nhập", "/accountant/purchase-invoices/create", "ti ti-circle-plus")),
                menuGroup("Báo cáo ngày",
                        i("Danh sách báo cáo ngày", "/accountant/daily-reports", "ti ti-report"),
                        i("Tạo báo cáo ngày", "/accountant/daily-reports/create", "ti ti-circle-plus")),
                menuGroup("Công nợ",
                        i("Danh sách công nợ", "/accountant/debts", "ti ti-credit-card"),
                        i("Tạo công nợ", "/accountant/debts/create", "ti ti-circle-plus")),
                menuGroup("Chi phí",
                        i("Danh sách chi phí", "/accountant/expenses", "ti ti-cash"),
                        i("Tạo chi phí", "/accountant/expenses/create", "ti ti-circle-plus")),
                menuGroup("Hóa đơn bán hàng",
                        i("Danh sách hóa đơn", "/accountant/invoices", "ti ti-file-invoice")),
                menuGroup("Báo cáo ca",
                        i("Danh sách báo cáo ca", "/accountant/shift-reports", "ti ti-report-money")),
                menuGroup("Cấu hình tài chính",
                        i("Cấu hình tài chính", "/accountant/financial-setting", "ti ti-settings")),
                menuGroup("Thông báo",
                        i("Thông báo", "/accountant/notifications", "ti ti-bell")));
    }

    private List<SidebarMenuGroup> cashierMenu() {
        return List.of(
                linkGroup(GROUP_MAIN,
                        i("Tổng quan", "/cashier/dashboard", "ti ti-home")),
                menuGroup("Khách hàng",
                        i("Danh sách khách hàng", "/cashier/customers", "ti ti-users"),
                        i("Thêm khách hàng", "/cashier/customers/create", "ti ti-user-plus")),
                menuGroup("Hóa đơn bán hàng",
                        i("Danh sách hóa đơn", "/cashier/invoices", "ti ti-file-invoice")),
                menuGroup("Phiếu thu",
                        i("Danh sách phiếu thu", "/cashier/income", "ti ti-cash"),
                        i("Tạo phiếu thu", "/cashier/income/create", "ti ti-circle-plus")),
                menuGroup("Thu tiền",
                        i("Phiếu thu khác", "/cashier/receipts/other", "ti ti-receipt"),
                        i("Thu công nợ", "/cashier/debts", "ti ti-credit-card")),
                menuGroup("Báo cáo ca",
                        i("Danh sách báo cáo ca", "/cashier/shift-reports", "ti ti-report-money"),
                        i("Tạo báo cáo ca", "/cashier/shift-reports/create", "ti ti-circle-plus")),
                menuGroup("Thông báo",
                        i("Thông báo", "/cashier/notifications", "ti ti-bell")));
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
