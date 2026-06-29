package com.example.project.controller;

import com.example.project.dto.request.PurchaseInvoiceToBatchRequest;
import com.example.project.service.PurchaseInvoiceBatchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chief-pharmacist/purchase-invoices")
public class PurchaseInvoiceBatchController {

    private final PurchaseInvoiceBatchService purchaseInvoiceBatchService;

    public PurchaseInvoiceBatchController(PurchaseInvoiceBatchService purchaseInvoiceBatchService) {
        this.purchaseInvoiceBatchService = purchaseInvoiceBatchService;
    }

    @GetMapping("/{purchaseId}/to-batch")
    public String toBatchPage(@PathVariable Integer purchaseId, Model model) {
        model.addAttribute("pageData", purchaseInvoiceBatchService.getPage(purchaseId));
        model.addAttribute("form", purchaseInvoiceBatchService.buildForm(purchaseId));
        model.addAttribute("basePath", "/chief-pharmacist/purchase-invoices");

        return "purchase-invoice/to-batch";
    }

    @PostMapping("/{purchaseId}/to-batch/draft")
    public String saveDraft(@PathVariable Integer purchaseId,
                            @ModelAttribute("form") PurchaseInvoiceToBatchRequest form,
                            RedirectAttributes redirectAttributes) {
        try {
            purchaseInvoiceBatchService.saveDraft(purchaseId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu nháp thông tin lô hàng");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/chief-pharmacist/purchase-invoices/" + purchaseId + "/to-batch";
    }

    @PostMapping("/{purchaseId}/to-batch/create")
    public String createBatches(@PathVariable Integer purchaseId,
                                @ModelAttribute("form") PurchaseInvoiceToBatchRequest form,
                                RedirectAttributes redirectAttributes) {
        try {
            purchaseInvoiceBatchService.createBatches(purchaseId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo lô hàng từ phiếu nhập thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/chief-pharmacist/purchase-invoices/" + purchaseId + "/to-batch";
    }
}