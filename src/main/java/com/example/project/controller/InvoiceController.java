package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.InvoiceCreateRequest;
import com.example.project.dto.response.InvoiceLineResponse;
import com.example.project.dto.response.InvoiceListItemResponse;
import com.example.project.dto.response.InvoiceResponse;
import com.example.project.service.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final CurrentUserContext currentUserContext;

    public InvoiceController(InvoiceService invoiceService, CurrentUserContext currentUserContext) {
        this.invoiceService = invoiceService;
        this.currentUserContext = currentUserContext;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    @GetMapping("/invoices")
    @ResponseBody
    public List<InvoiceResponse> getAll() {
        return invoiceService.getAll();
    }

    @GetMapping({"/owner/invoices", "/pharmacist/invoices", "/accountant/invoices"})
    public String invoiceList(@RequestParam(name = "search", required = false) String search,
                              @RequestParam(name = "fromDate", required = false) String fromDate,
                              @RequestParam(name = "toDate", required = false) String toDate,
                              @RequestParam(name = "paymentType", required = false) String paymentType,
                              @RequestParam(name = "status", required = false) String status,
                              @RequestParam(name = "sellerId", required = false) Integer sellerId,
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

        Page<InvoiceListItemResponse> invoicePage = invoiceService.list(
                search, fromDate, toDate, paymentType, status, sellerId, PageRequest.of(page, size));
        String basePath = resolveBasePath(request);

        model.addAttribute("invoices", invoicePage.getContent());
        model.addAttribute("totalInvoices", invoiceService.countAll());
        model.addAttribute("todayInvoices", invoiceService.countToday());
        model.addAttribute("todayRevenue", invoiceService.sumTodayRevenue());
        model.addAttribute("debtInvoices", invoiceService.countDebt());
        model.addAttribute("debtTotal", invoiceService.sumDebtTotal());
        model.addAttribute("returnedInvoices", invoiceService.countReturned());
        model.addAttribute("statuses", invoiceService.listStatuses());
        model.addAttribute("sellers", invoiceService.listSellers());
        model.addAttribute("paymentTypeLabels", invoiceService.paymentTypeLabels());
        model.addAttribute("search", search);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterPaymentType", paymentType);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterSellerId", sellerId);
        model.addAttribute("currentPage", invoicePage.getNumber());
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", invoicePage.getTotalElements());
        model.addAttribute("pageTitle", "Danh sách hóa đơn");
        model.addAttribute("basePath", basePath);
        return "invoice-list";
    }

    @GetMapping(value = {"/owner/invoices/{invoiceId}/lines",
            "/pharmacist/invoices/{invoiceId}/lines",
            "/accountant/invoices/{invoiceId}/lines"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<InvoiceLineResponse> invoiceLines(@PathVariable Integer invoiceId) {
        return invoiceService.loadLines(invoiceId);
    }

    @GetMapping({"/owner/selling", "/pharmacist/selling"})
    public String sellingPage(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new InvoiceCreateRequest());
        }
        addSellingPageData(request, model);
        return "create-invoice";
    }

    @PostMapping({"/owner/selling", "/pharmacist/selling"})
    public String createSale(@ModelAttribute("form") InvoiceCreateRequest form,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            invoiceService.createSaleInvoice(form, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("success", "Tạo hóa đơn bán hàng thành công");
            return "redirect:" + invoiceListBasePath(request);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            addSellingPageData(request, model);
            return "create-invoice";
        }
    }

    private void addSellingPageData(HttpServletRequest request, Model model) {
        model.addAttribute("products", invoiceService.listSellableProducts());
        model.addAttribute("customers", invoiceService.listCustomers());
        model.addAttribute("sellerName", currentUserContext.getCurrentAccountName());
        model.addAttribute("basePath", resolveSellingBasePath(request));
        model.addAttribute("invoicesPath", invoiceListBasePath(request));
    }

    private String resolveSellingBasePath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/pharmacist/selling")
                ? "/pharmacist/selling" : "/owner/selling";
    }

    private String invoiceListBasePath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/pharmacist")
                ? "/pharmacist/invoices" : "/owner/invoices";
    }

    private String resolveBasePath(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/pharmacist/invoices")) {
            return "/pharmacist/invoices";
        }
        if (uri.startsWith("/accountant/invoices")) {
            return "/accountant/invoices";
        }

        return "/owner/invoices";
    }
}
