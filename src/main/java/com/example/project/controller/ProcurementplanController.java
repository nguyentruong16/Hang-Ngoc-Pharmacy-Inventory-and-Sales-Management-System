package com.example.project.controller;

import com.example.project.dto.request.ProcurementPlanCreateRequest;
import com.example.project.dto.response.ProcurementPlanPrintPageResponse;
import com.example.project.dto.response.ProcurementPlanDetailRowView;
import com.example.project.dto.response.ProcurementProductSearchResponse;
import com.example.project.dto.response.ProcurementSupplierSearchResponse;
import com.example.project.dto.response.ProcurementplanResponse;
import com.example.project.dto.response.SupplierCostPriceResponse;
import com.example.project.entity.Supplier;
import com.example.project.service.ProcurementplanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProcurementplanController {
    private final ProcurementplanService procurementplanService;

    public ProcurementplanController(ProcurementplanService procurementplanService) {
        this.procurementplanService = procurementplanService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    @GetMapping("/procurement-plans")
    @ResponseBody
    public List<ProcurementplanResponse> getAll() {
        return procurementplanService.getAll();
    }

    @GetMapping("/owner/procurements")
    public String procurementPlanList(@RequestParam(name = "search", required = false) String search,
                                      @RequestParam(name = "fromDate", required = false) String fromDate,
                                      @RequestParam(name = "toDate", required = false) String toDate,
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

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")
                .and(Sort.by(Sort.Direction.DESC, "id")));
        Page<ProcurementplanResponse> procurementPage = procurementplanService.list(
                search, fromDate, toDate, status, pageable);
        String basePath = resolveBasePath(request);

        model.addAttribute("procurementPlans", procurementPage.getContent());
        model.addAttribute("totalProcurementPlans", procurementplanService.countAll());
        model.addAttribute("completedProcurementPlans", procurementplanService.countCompleted());
        model.addAttribute("inProgressProcurementPlans", procurementplanService.countInProgress());
        model.addAttribute("statuses", procurementplanService.listStatuses());
        model.addAttribute("search", search);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterStatus", status);
        model.addAttribute("currentPage", procurementPage.getNumber());
        model.addAttribute("totalPages", procurementPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", procurementPage.getTotalElements());
        model.addAttribute("pageTitle", "Danh sách dự trù mua hàng");
        model.addAttribute("basePath", basePath);
        return "owner/procurement-plan-list";
    }

    @GetMapping("/owner/procurements/products/search")
    @ResponseBody
    public List<ProcurementProductSearchResponse> searchProducts(@RequestParam(name = "keyword") String keyword,
                                                                 @RequestParam(name = "limit", defaultValue = "12") int limit) {
        return procurementplanService.searchProducts(keyword, limit);
    }

    @GetMapping("/owner/procurements/supplier-cost-price")
    @ResponseBody
    public SupplierCostPriceResponse getSupplierCostPrice(@RequestParam(name = "supplierId") Integer supplierId,
                                                          @RequestParam(name = "productId") Integer productId) {
        BigDecimal costPrice = procurementplanService.getSupplierCostPrice(supplierId, productId);
        return new SupplierCostPriceResponse(costPrice);
    }

    @GetMapping("/owner/procurements/suppliers/search")
    @ResponseBody
    public List<ProcurementSupplierSearchResponse> searchSuppliers(
            @RequestParam(name = "productId", required = false) Integer productId,
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return procurementplanService.searchSuppliersForProduct(productId, keyword);
    }

    @GetMapping("/owner/procurements/create-procurementplan")
    public String createProcurementPlanForm(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("procurementPlanForm")) {
            model.addAttribute("procurementPlanForm", new ProcurementPlanCreateRequest());
        }

        addFormPageData(request, model);
        model.addAttribute("pageTitle", "Tạo dự trù mua hàng");
        return "owner/create-procurementplan";
    }

    @PostMapping("/owner/procurements/create-procurementplan")
    public String createProcurementPlan(@Valid @ModelAttribute("procurementPlanForm") ProcurementPlanCreateRequest form,
                                        BindingResult bindingResult,
                                        HttpServletRequest request,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);

        if (bindingResult.hasErrors()) {
            addFormPageData(request, model);
            model.addAttribute("pageTitle", "Tạo dự trù mua hàng");
            return "owner/create-procurementplan";
        }

        try {
            procurementplanService.create(form);
            redirectAttributes.addFlashAttribute("success", "Tạo dự trù mua hàng thành công");
            return "redirect:" + basePath;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            addFormPageData(request, model);
            model.addAttribute("pageTitle", "Tạo dự trù mua hàng");
            return "owner/create-procurementplan";
        }
    }

    @GetMapping("/owner/procurements/update-procurementplan/{id}")
    public String updateProcurementPlanForm(@PathVariable Integer id,
                                            HttpServletRequest request,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);
        try {
            ProcurementplanResponse procurementPlan = procurementplanService.getById(id);
            boolean viewOnly = procurementplanService.isCompleted(id);

            if (!model.containsAttribute("procurementPlanForm")) {
                model.addAttribute("procurementPlanForm", procurementplanService.buildUpdateForm(id));
            }

            model.addAttribute("procurementPlan", procurementPlan);
            model.addAttribute("viewOnly", viewOnly);
            addFormPageData(request, model);
            model.addAttribute("pageTitle", viewOnly ? "Xem chi tiết dự trù mua hàng" : "Cập nhật dự trù mua hàng");
            return "owner/update-procurementplan";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:" + basePath;
        }
    }

    @PostMapping("/owner/procurements/update-procurementplan/{id}")
    public String updateProcurementPlan(@PathVariable Integer id,
                                        @Valid @ModelAttribute("procurementPlanForm") ProcurementPlanCreateRequest form,
                                        BindingResult bindingResult,
                                        HttpServletRequest request,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);

        if (bindingResult.hasErrors()) {
            model.addAttribute("procurementPlan", procurementplanService.getById(id));
            model.addAttribute("viewOnly", procurementplanService.isCompleted(id));
            addFormPageData(request, model);
            model.addAttribute("pageTitle", procurementplanService.isCompleted(id)
                    ? "Xem chi tiết dự trù mua hàng"
                    : "Cập nhật dự trù mua hàng");
            return "owner/update-procurementplan";
        }

        try {
            procurementplanService.update(id, form);
            String successMessage = "Cập nhật dự trù mua hàng thành công";
            if ("Đã hoàn thành".equals(form.getStatus())) {
                successMessage = "Cập nhật dự trù mua hàng thành công. Dự trù đã hoàn thành nên không thể cập nhật hoặc xóa.";
            }
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:" + basePath;
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().contains("đã hoàn thành")) {
                redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                return "redirect:" + basePath;
            }
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("procurementPlan", procurementplanService.getById(id));
            model.addAttribute("viewOnly", procurementplanService.isCompleted(id));
            addFormPageData(request, model);
            model.addAttribute("pageTitle", procurementplanService.isCompleted(id)
                    ? "Xem chi tiết dự trù mua hàng"
                    : "Cập nhật dự trù mua hàng");
            return "owner/update-procurementplan";
        }
    }

    @GetMapping("/owner/procurements/{id}/print")
    public String printPage(@PathVariable Integer id,
                            HttpServletRequest request,
                            Model model) {
        ProcurementPlanPrintPageResponse printData = procurementplanService.getPrintPage(id);
        model.addAttribute("printData", printData);
        model.addAttribute("basePath", resolveBasePath(request));
        return "owner/procurement-plan-print";
    }

    @PostMapping("/owner/procurements/delete/{id}")
    public String deleteProcurementPlan(@PathVariable Integer id,
                                        RedirectAttributes redirectAttributes) {
        try {
            procurementplanService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa dự trù mua hàng thành công");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/owner/procurements";
    }

    private void addFormPageData(HttpServletRequest request, Model model) {
        ProcurementPlanCreateRequest form = (ProcurementPlanCreateRequest) model.getAttribute("procurementPlanForm");
        model.addAttribute("initialProducts", procurementplanService.listProductsForDetails(form));
        model.addAttribute("initialDetailRows", buildInitialDetailRows(form));
        model.addAttribute("suppliers", toSupplierOptions(procurementplanService.listSuppliers()));
        model.addAttribute("basePath", resolveBasePath(request));
    }

    private List<ProcurementPlanDetailRowView> buildInitialDetailRows(ProcurementPlanCreateRequest form) {
        if (form == null || form.getDetails() == null) {
            return List.of();
        }

        return form.getDetails().stream()
                .filter(detail -> detail.getProductId() != null)
                .map(detail -> new ProcurementPlanDetailRowView(
                        detail.getProductId(),
                        detail.getRequestedQuantity(),
                        detail.getUnit(),
                        detail.getEstimatedPrice(),
                        detail.getSupplierId()
                ))
                .toList();
    }

    private List<Map<String, Object>> toSupplierOptions(List<Supplier> suppliers) {
        return suppliers.stream()
                .map(supplier -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("id", supplier.getId());
                    option.put("name", supplier.getName() != null ? supplier.getName() : "");
                    return option;
                })
                .toList();
    }

    private String resolveBasePath(HttpServletRequest request) {
        return "/owner/procurements";
    }
}
