package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.StockAdjustmentDestroyCreateRequest;
import com.example.project.service.StockAdjustmentDestroyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chief-pharmacist/stock-outs/destroy")
public class StockAdjustmentDestroyController {

    private final StockAdjustmentDestroyService stockAdjustmentDestroyService;
    private final CurrentUserContext currentUserContext;

    public StockAdjustmentDestroyController(StockAdjustmentDestroyService stockAdjustmentDestroyService,
                                            CurrentUserContext currentUserContext) {
        this.stockAdjustmentDestroyService = stockAdjustmentDestroyService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/create")
    public String createPage(@RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {
        StockAdjustmentDestroyCreateRequest form = new StockAdjustmentDestroyCreateRequest();

        model.addAttribute("form", form);
        model.addAttribute("candidates", stockAdjustmentDestroyService.listAvailableBatches(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("basePath", "/chief-pharmacist/stock-outs");

        return "stock-out/destroy-create";
    }

    @PostMapping("/create")
    public String createDestroyStockOut(@ModelAttribute("form") StockAdjustmentDestroyCreateRequest form,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        try {
            Integer stockOutId = stockAdjustmentDestroyService.createDestroyStockOut(
                    form,
                    currentUserContext.getCurrentAccountId()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu hủy xuất kho thành công");
            return "redirect:/chief-pharmacist/stock-outs/" + stockOutId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("candidates", stockAdjustmentDestroyService.listAvailableBatches(null));
            model.addAttribute("keyword", null);
            model.addAttribute("basePath", "/chief-pharmacist/stock-outs");

            return "stock-out/destroy-create";
        }
    }
}