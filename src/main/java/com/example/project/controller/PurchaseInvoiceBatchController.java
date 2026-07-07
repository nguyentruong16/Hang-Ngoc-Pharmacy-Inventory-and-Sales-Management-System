package com.example.project.controller;

import com.example.project.dto.request.PurchaseInvoiceToBatchRequest;
import com.example.project.service.PurchaseInvoiceBatchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PurchaseInvoiceBatchController {

    private final PurchaseInvoiceBatchService purchaseInvoiceBatchService;

    public PurchaseInvoiceBatchController(PurchaseInvoiceBatchService purchaseInvoiceBatchService) {
        this.purchaseInvoiceBatchService = purchaseInvoiceBatchService;
    }

    @GetMapping("/owner/purchase-invoices/{purchaseId}/to-batch")
    public String toBatchPage(@PathVariable Integer purchaseId, HttpServletRequest request, Model model) {
        model.addAttribute("pageData", purchaseInvoiceBatchService.getPage(purchaseId));
        model.addAttribute("form", purchaseInvoiceBatchService.buildForm(purchaseId));
        model.addAttribute("basePath", resolvePurchaseInvoicesBasePath(request));

        return "purchase-invoice/to-batch";
    }

    @PostMapping("/owner/purchase-invoices/{purchaseId}/to-batch/draft")
    public String saveDraft(@PathVariable Integer purchaseId,
                            @ModelAttribute("form") PurchaseInvoiceToBatchRequest form,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            purchaseInvoiceBatchService.saveDraft(purchaseId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu nháp thông tin lô hàng");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:" + resolvePurchaseInvoicesBasePath(request) + "/" + purchaseId + "/to-batch";
    }

    @PostMapping("/owner/purchase-invoices/{purchaseId}/to-batch/create")
    public String createBatches(@PathVariable Integer purchaseId,
                                @ModelAttribute("form") PurchaseInvoiceToBatchRequest form,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            purchaseInvoiceBatchService.createBatches(purchaseId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo lô hàng từ phiếu nhập thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:" + resolvePurchaseInvoicesBasePath(request) + "/" + purchaseId + "/to-batch";
    }

    private String resolvePurchaseInvoicesBasePath(HttpServletRequest request) {
        return "/owner/purchase-invoices";
    }
}