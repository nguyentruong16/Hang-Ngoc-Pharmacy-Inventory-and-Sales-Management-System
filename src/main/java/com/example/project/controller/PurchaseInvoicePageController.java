package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.PurchaseInvoiceCreateRequest;
import com.example.project.dto.request.PurchaseInvoiceDetailCreateRequest;
import com.example.project.dto.response.PurchaseInvoiceDetailPageResponse;
import com.example.project.dto.response.PurchaseInvoiceListItemResponse;
import com.example.project.dto.response.PurchaseInvoicePrintPageResponse;
import com.example.project.service.PurchaseinvoiceService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PurchaseInvoicePageController {

    private final PurchaseinvoiceService purchaseinvoiceService;
    private final CurrentUserContext currentUserContext;

    public PurchaseInvoicePageController(PurchaseinvoiceService purchaseinvoiceService,
                                         CurrentUserContext currentUserContext) {
        this.purchaseinvoiceService = purchaseinvoiceService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping({
            "/owner/purchase-invoices",
            "/accountant/purchase-invoices",
            "/pharmacist/purchase-invoices"
    })
    public String listPurchaseInvoices(@RequestParam(name = "keyword", required = false) String keyword,
                                       @RequestParam(name = "fromDate", required = false) String fromDate,
                                       @RequestParam(name = "toDate", required = false) String toDate,
                                       @RequestParam(name = "supplierId", required = false) Integer supplierId,
                                       @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                       @RequestParam(name = "size", defaultValue = "5") int size,
                                       HttpServletRequest request,
                                       Model model) {
        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 5;
        }

        Page<PurchaseInvoiceListItemResponse> invoicePage =
                purchaseinvoiceService.searchPurchaseInvoices(
                        keyword,
                        fromDate,
                        toDate,
                        supplierId,
                        paymentStatus,
                        PageRequest.of(page, size)
                );

        model.addAttribute("invoicePage", invoicePage);
        model.addAttribute("purchaseInvoices", invoicePage.getContent());
        model.addAttribute("stats", purchaseinvoiceService.getStats());

        model.addAttribute("suppliers", purchaseinvoiceService.listSuppliers());
        model.addAttribute("paymentStatuses", purchaseinvoiceService.listPaymentStatuses());

        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterSupplierId", supplierId);
        model.addAttribute("filterPaymentStatus", paymentStatus);

        model.addAttribute("currentPage", invoicePage.getNumber());
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", invoicePage.getTotalElements());

        model.addAttribute("basePath", resolveBasePath(request));

        return "purchase-invoice/list";
    }

    @GetMapping({
            "/owner/purchase-invoices/create",
            "/pharmacist/purchase-invoices/create"
    })
    public String createPage(HttpServletRequest request, Model model) {
        PurchaseInvoiceCreateRequest form = new PurchaseInvoiceCreateRequest();

        PurchaseInvoiceDetailCreateRequest firstItem = new PurchaseInvoiceDetailCreateRequest();
        form.getDetails().add(firstItem);

        model.addAttribute("form", form);
        addCreatePageData(request, model);

        return "purchase-invoice/create";
    }

    @PostMapping({
            "/owner/purchase-invoices/create",
            "/pharmacist/purchase-invoices/create"
    })
    public String createPurchaseInvoice(@Valid @ModelAttribute("form") PurchaseInvoiceCreateRequest form,
                                        BindingResult bindingResult,
                                        HttpServletRequest request,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCreatePageData(request, model);
            return "purchase-invoice/create";
        }

        try {
            Integer purchaseId = purchaseinvoiceService.createPurchaseInvoice(
                    form,
                    currentUserContext.getCurrentAccountId()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu nhập thành công");
            return "redirect:" + resolveBasePath(request) + "/" + purchaseId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            addCreatePageData(request, model);
            return "purchase-invoice/create";
        }
    }

    @GetMapping({
            "/owner/purchase-invoices/{purchaseId}",
            "/accountant/purchase-invoices/{purchaseId}",
            "/pharmacist/purchase-invoices/{purchaseId}"
    })
    public String detailPage(@PathVariable Integer purchaseId,
                             HttpServletRequest request,
                             Model model) {
        PurchaseInvoiceDetailPageResponse detail = purchaseinvoiceService.getDetail(purchaseId);

        model.addAttribute("detail", detail);
        model.addAttribute("basePath", resolveBasePath(request));

        return "purchase-invoice/detail";
    }

    @GetMapping({
            "/owner/purchase-invoices/{purchaseId}/print",
            "/accountant/purchase-invoices/{purchaseId}/print",
            "/pharmacist/purchase-invoices/{purchaseId}/print"
    })
    public String printPage(@PathVariable Integer purchaseId,
                            HttpServletRequest request,
                            Model model) {
        PurchaseInvoicePrintPageResponse printData = purchaseinvoiceService.getPrintPage(purchaseId);

        model.addAttribute("printData", printData);
        model.addAttribute("basePath", resolveBasePath(request));

        return "purchase-invoice/print";
    }

    private void addCreatePageData(HttpServletRequest request, Model model) {
        model.addAttribute("suppliers", purchaseinvoiceService.listSuppliers());
        model.addAttribute("products", purchaseinvoiceService.listProducts());
        model.addAttribute("existingLotsByProduct", purchaseinvoiceService.buildExistingLotsByProduct());
        model.addAttribute("costPriceBySupplierAndProduct", purchaseinvoiceService.buildCostPriceBySupplierAndProduct());
        model.addAttribute("vatRateByProduct", purchaseinvoiceService.getVatRateByProduct());
        model.addAttribute("basePath", resolveBasePath(request));
    }

    private String resolveBasePath(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/owner/purchase-invoices")) {
            return "/owner/purchase-invoices";
        }
        if (uri.startsWith("/pharmacist/purchase-invoices")) {
            return "/pharmacist/purchase-invoices";
        }

        return "/accountant/purchase-invoices";
    }
}