package com.example.project.controller;

import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.StockAdjustmentCreateRequest;
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

/**
 * Stock Adjustment screens (list / detail / create / approve / reject).
 *
 * <p>Reachable by the Owner (approver) and the Pharmacist (creator). Both share the same
 * templates; the active base path (<code>/owner/stock-adjustments</code> or
 * <code>/pharmacist/stock-adjustments</code>) is resolved per request so links stay within the
 * caller's role prefix. Approve/reject are Owner-only (enforced by SecurityConfig on
 * <code>/owner/**</code>).</p>
 */
@Controller
public class StockadjustmentController {

    private static final String OWNER_BASE = "/owner/stock-adjustments";
    private static final String PHARMACIST_BASE = "/pharmacist/stock-adjustments";

    private final StockadjustmentService stockadjustmentService;
    private final CurrentUserContext currentUserContext;

    public StockadjustmentController(StockadjustmentService stockadjustmentService,
                                     CurrentUserContext currentUserContext) {
        this.stockadjustmentService = stockadjustmentService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping({OWNER_BASE, PHARMACIST_BASE})
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "fromDate", required = false) String fromDate,
                       @RequestParam(name = "toDate", required = false) String toDate,
                       @RequestParam(name = "adjustmentType", required = false) String adjustmentType,
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

        Page<StockAdjustmentListItemResponse> adjustmentPage =
                stockadjustmentService.search(keyword, fromDate, toDate, adjustmentType, status,
                        PageRequest.of(page, size));

        model.addAttribute("adjustmentPage", adjustmentPage);
        model.addAttribute("adjustments", adjustmentPage.getContent());
        model.addAttribute("stats", stockadjustmentService.getStats());

        model.addAttribute("statuses", stockadjustmentService.listStatuses());
        model.addAttribute("adjustmentTypeLabels", stockadjustmentService.adjustmentTypeLabels());

        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterAdjustmentType", adjustmentType);
        model.addAttribute("filterStatus", status);

        model.addAttribute("currentPage", adjustmentPage.getNumber());
        model.addAttribute("totalPages", adjustmentPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", adjustmentPage.getTotalElements());

        model.addAttribute("basePath", resolveBasePath(request));

        return "stock-adjustment/list";
    }

    @GetMapping({OWNER_BASE + "/create", PHARMACIST_BASE + "/create"})
    public String createPage(@RequestParam(name = "keyword", required = false) String keyword,
                             HttpServletRequest request,
                             Model model) {
        model.addAttribute("form", new StockAdjustmentCreateRequest());
        model.addAttribute("candidates", stockadjustmentService.listAvailableBatches(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("basePath", resolveBasePath(request));

        return "stock-adjustment/create";
    }

    @PostMapping({OWNER_BASE + "/create", PHARMACIST_BASE + "/create"})
    public String create(@ModelAttribute("form") StockAdjustmentCreateRequest form,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        String basePath = resolveBasePath(request);
        try {
            Integer adjustmentId = stockadjustmentService.createDestroyAdjustment(
                    form,
                    currentUserContext.getCurrentAccountId(),
                    currentUserContext.isOwner());

            redirectAttributes.addFlashAttribute("successMessage",
                    currentUserContext.isOwner()
                            ? "Tạo phiếu điều chỉnh kho thành công"
                            : "Tạo phiếu điều chỉnh kho thành công, đang chờ duyệt");
            return "redirect:" + basePath + "/" + adjustmentId;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("candidates", stockadjustmentService.listAvailableBatches(null));
            model.addAttribute("keyword", null);
            model.addAttribute("basePath", basePath);
            return "stock-adjustment/create";
        }
    }

    @GetMapping({OWNER_BASE + "/{adjustmentId}", PHARMACIST_BASE + "/{adjustmentId}"})
    public String detail(@PathVariable Integer adjustmentId,
                         HttpServletRequest request,
                         Model model) {
        StockAdjustmentDetailPageResponse detail = stockadjustmentService.getDetail(adjustmentId);

        model.addAttribute("detail", detail);
        model.addAttribute("basePath", resolveBasePath(request));

        return "stock-adjustment/detail";
    }

    @PostMapping(OWNER_BASE + "/{adjustmentId}/approve")
    public String approve(@PathVariable Integer adjustmentId,
                          RedirectAttributes redirectAttributes) {
        try {
            stockadjustmentService.approve(adjustmentId, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt phiếu điều chỉnh kho");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:" + OWNER_BASE + "/" + adjustmentId;
    }

    @PostMapping(OWNER_BASE + "/{adjustmentId}/reject")
    public String reject(@PathVariable Integer adjustmentId,
                         RedirectAttributes redirectAttributes) {
        try {
            stockadjustmentService.reject(adjustmentId, currentUserContext.getCurrentAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối phiếu điều chỉnh kho");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:" + OWNER_BASE + "/" + adjustmentId;
    }

    private String resolveBasePath(HttpServletRequest request) {
        return request.getRequestURI().startsWith(PHARMACIST_BASE) ? PHARMACIST_BASE : OWNER_BASE;
    }
}
