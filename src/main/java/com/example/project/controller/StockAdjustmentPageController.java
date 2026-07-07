package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.response.StockAdjustmentDetailPageResponse;
import com.example.project.dto.response.StockAdjustmentListItemResponse;
import com.example.project.service.StockadjustmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StockAdjustmentPageController {

    private final StockadjustmentService stockadjustmentService;
    private final CurrentUserContext currentUserContext;

    public StockAdjustmentPageController(StockadjustmentService stockadjustmentService,
                                         CurrentUserContext currentUserContext) {
        this.stockadjustmentService = stockadjustmentService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping({
            "/owner/stock-outs",
            "/chief-pharmacist/stock-outs",
            "/accountant/stock-outs"
    })
    public String listStockOuts(@RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "fromDate", required = false) String fromDate,
                                @RequestParam(name = "toDate", required = false) String toDate,
                                @RequestParam(name = "outType", required = false) String outType,
                                @RequestParam(name = "branchId", required = false) Integer branchId,
                                @RequestParam(name = "status", required = false) String status,
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

        Page<StockAdjustmentListItemResponse> stockOutPage =
                stockadjustmentService.searchStockOuts(
                        keyword,
                        fromDate,
                        toDate,
                        outType,
                        branchId,
                        status,
                        PageRequest.of(page, size)
                );

        model.addAttribute("stockOutPage", stockOutPage);
        model.addAttribute("stockOuts", stockOutPage.getContent());
        model.addAttribute("stats", stockadjustmentService.getStats());

        model.addAttribute("branches", stockadjustmentService.listBranches());
        model.addAttribute("statuses", stockadjustmentService.listStatuses());
        model.addAttribute("outTypeLabels", stockadjustmentService.outTypeLabels());

        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterOutType", outType);
        model.addAttribute("filterBranchId", branchId);
        model.addAttribute("filterStatus", status);

        model.addAttribute("currentPage", stockOutPage.getNumber());
        model.addAttribute("totalPages", stockOutPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", stockOutPage.getTotalElements());

        model.addAttribute("basePath", resolveBasePath(request));

        return "stock-out/list";
    }

    @GetMapping({
            "/owner/stock-outs/{stockOutId}",
            "/chief-pharmacist/stock-outs/{stockOutId}",
            "/accountant/stock-outs/{stockOutId}"
    })
    public String viewStockOutDetail(@PathVariable Integer stockOutId,
                                     HttpServletRequest request,
                                     Model model) {
        StockAdjustmentDetailPageResponse detail = stockadjustmentService.getDetail(stockOutId);

        model.addAttribute("detail", detail);
        model.addAttribute("basePath", resolveBasePath(request));

        return "stock-out/detail";
    }

    @PostMapping({
            "/owner/stock-outs/{stockOutId}/approve",
            "/chief-pharmacist/stock-outs/{stockOutId}/approve",
            "/accountant/stock-outs/{stockOutId}/approve"
    })
    public String approve(@PathVariable Integer stockOutId,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        try {
            stockadjustmentService.approve(stockOutId, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt phiếu xuất kho");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:" + resolveBasePath(request) + "/" + stockOutId;
    }

    @PostMapping({
            "/owner/stock-outs/{stockOutId}/reject",
            "/chief-pharmacist/stock-outs/{stockOutId}/reject",
            "/accountant/stock-outs/{stockOutId}/reject"
    })
    public String reject(@PathVariable Integer stockOutId,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            stockadjustmentService.reject(stockOutId, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối phiếu xuất kho");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:" + resolveBasePath(request) + "/" + stockOutId;
    }

    private String resolveBasePath(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/owner/stock-outs")) {
            return "/owner/stock-outs";
        }

        if (uri.startsWith("/accountant/stock-outs")) {
            return "/accountant/stock-outs";
        }

        return "/chief-pharmacist/stock-outs";
    }
}