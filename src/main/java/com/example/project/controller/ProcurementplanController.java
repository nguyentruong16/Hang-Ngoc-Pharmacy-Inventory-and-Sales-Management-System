package com.example.project.controller;

import com.example.project.dto.request.ProcurementPlanCreateRequest;
import com.example.project.dto.response.ProcurementProductSearchResponse;
import com.example.project.dto.response.ProcurementplanResponse;
import com.example.project.service.ProcurementplanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProcurementplanController {
    private final ProcurementplanService procurementplanService;

    public ProcurementplanController(ProcurementplanService procurementplanService) {
        this.procurementplanService = procurementplanService;
    }

    @GetMapping("/procurement-plans")
    @ResponseBody
    public List<ProcurementplanResponse> getAll() {
        return procurementplanService.getAll();
    }

    @GetMapping("/owner/procurements")
    public String procurementPlanList(@RequestParam(name = "search", required = false) String search,
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

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ProcurementplanResponse> procurementPage = procurementplanService.list(search, pageable);
        String basePath = resolveBasePath(request);

        model.addAttribute("procurementPlans", procurementPage.getContent());
        model.addAttribute("totalProcurementPlans", procurementplanService.countAll());
        model.addAttribute("search", search);
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

    @GetMapping("/owner/procurements/create-procurementplan")
    public String createProcurementPlanForm(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("procurementPlanForm")) {
            model.addAttribute("procurementPlanForm", new ProcurementPlanCreateRequest());
        }

        addCreatePageData(request, model);
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
            addCreatePageData(request, model);
            model.addAttribute("pageTitle", "Tạo dự trù mua hàng");
            return "owner/create-procurementplan";
        }

        try {
            procurementplanService.create(form);
            redirectAttributes.addFlashAttribute("success", "Tạo dự trù mua hàng thành công");
            return "redirect:" + basePath;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            addCreatePageData(request, model);
            model.addAttribute("pageTitle", "Tạo dự trù mua hàng");
            return "owner/create-procurementplan";
        }
    }

    private void addCreatePageData(HttpServletRequest request, Model model) {
        ProcurementPlanCreateRequest form = (ProcurementPlanCreateRequest) model.getAttribute("procurementPlanForm");
        model.addAttribute("initialProducts", procurementplanService.listProductsForDetails(form));
        model.addAttribute("suppliers", procurementplanService.listSuppliers());
        model.addAttribute("basePath", resolveBasePath(request));
    }

    private String resolveBasePath(HttpServletRequest request) {
        return "/owner/procurements";
    }
}
