package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.StockOutDestroyCreateRequest;
import com.example.project.service.StockOutDestroyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chief-pharmacist/stock-outs/destroy")
public class StockOutDestroyController {

    private final StockOutDestroyService stockOutDestroyService;
    private final CurrentUserContext currentUserContext;

    public StockOutDestroyController(StockOutDestroyService stockOutDestroyService,
                                     CurrentUserContext currentUserContext) {
        this.stockOutDestroyService = stockOutDestroyService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/create")
    public String createPage(@RequestParam(name = "branchId", required = false) Integer branchId,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {
        StockOutDestroyCreateRequest form = new StockOutDestroyCreateRequest();
        form.setBranchId(branchId);

        model.addAttribute("form", form);
        model.addAttribute("branches", stockOutDestroyService.listBranches());
        model.addAttribute("candidates", stockOutDestroyService.listAvailableBatches(branchId, keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("basePath", "/chief-pharmacist/stock-outs");

        return "stock-out/destroy-create";
    }

    @PostMapping("/create")
    public String createDestroyStockOut(@ModelAttribute("form") StockOutDestroyCreateRequest form,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        try {
            Integer stockOutId = stockOutDestroyService.createDestroyStockOut(
                    form,
                    currentUserContext.getCurrentAccountId()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu hủy xuất kho thành công");
            return "redirect:/chief-pharmacist/stock-outs/" + stockOutId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("branches", stockOutDestroyService.listBranches());
            model.addAttribute("candidates", stockOutDestroyService.listAvailableBatches(form.getBranchId(), null));
            model.addAttribute("keyword", null);
            model.addAttribute("basePath", "/chief-pharmacist/stock-outs");

            return "stock-out/destroy-create";
        }
    }
}