package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.StockAdjustmentDestroyCreateRequest;
import com.example.project.service.StockAdjustmentDestroyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StockAdjustmentDestroyController {

    private final StockAdjustmentDestroyService stockAdjustmentDestroyService;
    private final CurrentUserContext currentUserContext;

    public StockAdjustmentDestroyController(StockAdjustmentDestroyService stockAdjustmentDestroyService,
                                            CurrentUserContext currentUserContext) {
        this.stockAdjustmentDestroyService = stockAdjustmentDestroyService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/owner/stock-outs/destroy/create")
    public String createPage(@RequestParam(name = "keyword", required = false) String keyword,
                             HttpServletRequest request,
                             Model model) {
        StockAdjustmentDestroyCreateRequest form = new StockAdjustmentDestroyCreateRequest();

        model.addAttribute("form", form);
        model.addAttribute("candidates", stockAdjustmentDestroyService.listAvailableBatches(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("basePath", resolveStockOutsBasePath(request));

        return "stock-out/destroy-create";
    }

    @PostMapping("/owner/stock-outs/destroy/create")
    public String createDestroyStockOut(@ModelAttribute("form") StockAdjustmentDestroyCreateRequest form,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        String basePath = resolveStockOutsBasePath(request);
        try {
            Integer stockOutId = stockAdjustmentDestroyService.createDestroyStockOut(
                    form,
                    currentUserContext.getCurrentAccountId()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu hủy xuất kho thành công");
            return "redirect:" + basePath + "/" + stockOutId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("candidates", stockAdjustmentDestroyService.listAvailableBatches(null));
            model.addAttribute("keyword", null);
            model.addAttribute("basePath", basePath);

            return "stock-out/destroy-create";
        }
    }

    private String resolveStockOutsBasePath(HttpServletRequest request) {
        return "/owner/stock-outs";
    }
}