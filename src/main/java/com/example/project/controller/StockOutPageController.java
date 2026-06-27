package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.response.StockOutDetailPageResponse;
import com.example.project.dto.response.StockOutListItemResponse;
import com.example.project.service.StockoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StockOutPageController {

    private final StockoutService stockoutService;
    private final CurrentUserContext currentUserContext;

    public StockOutPageController(StockoutService stockoutService,
                                  CurrentUserContext currentUserContext) {
        this.stockoutService = stockoutService;
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
                                @RequestParam(name = "statusId", required = false) Integer statusId,
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

        Page<StockOutListItemResponse> stockOutPage =
                stockoutService.searchStockOuts(
                        keyword,
                        fromDate,
                        toDate,
                        outType,
                        branchId,
                        statusId,
                        PageRequest.of(page, size)
                );

        model.addAttribute("stockOutPage", stockOutPage);
        model.addAttribute("stockOuts", stockOutPage.getContent());
        model.addAttribute("stats", stockoutService.getStats());

        model.addAttribute("branches", stockoutService.listBranches());
        model.addAttribute("statuses", stockoutService.listStatuses());
        model.addAttribute("outTypeLabels", stockoutService.outTypeLabels());

        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterOutType", outType);
        model.addAttribute("filterBranchId", branchId);
        model.addAttribute("filterStatusId", statusId);

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
        StockOutDetailPageResponse detail = stockoutService.getDetail(stockOutId);

        model.addAttribute("detail", detail);
        model.addAttribute("basePath", resolveBasePath(request));

        return "stock-out/detail";
    }

    @PostMapping({
            "/owner/stock-outs/{stockOutId}/reconcile",
            "/chief-pharmacist/stock-outs/{stockOutId}/reconcile",
            "/accountant/stock-outs/{stockOutId}/reconcile"
    })
    public String markAsReconciled(@PathVariable Integer stockOutId,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        try {
            stockoutService.markAsReconciled(stockOutId, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu phiếu xuất kho là đã đối chiếu");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:" + resolveBasePath(request) + "/" + stockOutId;
    }

    @PostMapping({
            "/owner/stock-outs/{stockOutId}/request-check",
            "/chief-pharmacist/stock-outs/{stockOutId}/request-check",
            "/accountant/stock-outs/{stockOutId}/request-check"
    })
    public String requestCheck(@PathVariable Integer stockOutId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            stockoutService.requestCheck(stockOutId, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu kiểm tra phiếu xuất kho");
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