package com.example.project.controller;

import com.example.project.service.SidebarMenuService;
import com.example.project.view.SidebarMenuItem;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves a single generic placeholder page for every role-specific route in the sidebar
 * that does not yet have a full implementation.
 *
 * <p>This exists purely so the role-based navigation is fully clickable for the demo. The
 * page title / module shown are looked up from {@link SidebarMenuService} by request URI,
 * so labels live in exactly one place (the menu config). Real module pages can later
 * replace these by adding more specific mappings — Spring will prefer the exact path over
 * these placeholder mappings. {@code /owner/permissions} is intentionally excluded (it has
 * its own {@link PermissionController}).</p>
 *
 * <p>Routes are enumerated explicitly (no greedy wildcard) so unknown URLs still 404.</p>
 */
@Controller
public class PlaceholderController {

    private final SidebarMenuService sidebarMenuService;

    public PlaceholderController(SidebarMenuService sidebarMenuService) {
        this.sidebarMenuService = sidebarMenuService;
    }

    @GetMapping({
            "/owner/dashboard",
            "/owner/branches/select",
            "/owner/branches",
            "/owner/branches/create",
            "/owner/users",
            "/owner/users/create",
            "/owner/approvals",
            "/owner/internal-export",
            "/owner/notifications"
    })
    public String owner(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/chief-pharmacist/dashboard",
            "/chief-pharmacist/products",
            "/chief-pharmacist/products/create",
            "/chief-pharmacist/products/barcode",
            "/chief-pharmacist/products/inventory-history",
            "/chief-pharmacist/origins",
            "/chief-pharmacist/types",
            "/chief-pharmacist/producers",
            "/chief-pharmacist/positions",
            "/chief-pharmacist/suppliers",
            "/chief-pharmacist/suppliers/create",
            "/chief-pharmacist/purchase-requisitions",
            "/chief-pharmacist/purchase-requisitions/create",
            "/chief-pharmacist/procurements",
            "/chief-pharmacist/procurements/create",
            "/chief-pharmacist/expenses",
            "/chief-pharmacist/expenses/create",
            "/chief-pharmacist/invoices",
            "/chief-pharmacist/purchase-invoice-to-batch",
            "/chief-pharmacist/export-cancellation",
            "/chief-pharmacist/notifications"
    })
    public String chiefPharmacist(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/pharmacist/dashboard",
            "/pharmacist/products",
            "/pharmacist/invoices",
            "/pharmacist/notifications"
    })
    public String pharmacist(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/accountant/dashboard",
            "/accountant/purchase-invoices",
            "/accountant/purchase-invoices/create",
            "/accountant/daily-reports",
            "/accountant/daily-reports/create",
            "/accountant/debts",
            "/accountant/debts/create",
            "/accountant/expenses",
            "/accountant/expenses/create",
            "/accountant/invoices",
            "/accountant/shift-reports",
            "/accountant/financial-setting",
            "/accountant/notifications"
    })
    public String accountant(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    @GetMapping({
            "/cashier/dashboard",
            "/cashier/customers",
            "/cashier/customers/create",
            "/cashier/invoices",
            "/cashier/income",
            "/cashier/income/create",
            "/cashier/receipts/other",
            "/cashier/debts",
            "/cashier/shift-reports",
            "/cashier/shift-reports/create",
            "/cashier/notifications"
    })
    public String cashier(HttpServletRequest request, Model model) {
        return render(request, model);
    }

    /** User dropdown destinations (kept out of the sidebar, per the topbar requirement). */
    @GetMapping("/profile")
    public String profile(Model model) {
        return account(model, "User Profile");
    }

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        return account(model, "Change Password");
    }

    private String render(HttpServletRequest request, Model model) {
        SidebarMenuItem item = sidebarMenuService.findByUri(request.getRequestURI());
        model.addAttribute("pageTitle", item != null ? item.getLabel() : "Page");
        model.addAttribute("moduleName", item != null ? item.getModule() : "-");
        return "placeholder";
    }

    private String account(Model model, String title) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("moduleName", "Account");
        return "placeholder";
    }
}
