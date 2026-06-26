package com.example.project.controller;

import com.example.project.dto.request.SupplierRequest;
import com.example.project.dto.response.SupplierResponse;
import com.example.project.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // ------------------------------------------------------------------ list

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       Model model) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<SupplierResponse> supplierPage = supplierService.list(keyword, pageable);
        SupplierService.SupplierStats stats = supplierService.getStats();

        model.addAttribute("suppliers", supplierPage.getContent());
        model.addAttribute("supplierPage", supplierPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", supplierPage.getNumber());
        model.addAttribute("totalPages", supplierPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", supplierPage.getTotalElements());

        model.addAttribute("totalSuppliers", stats.total());
        model.addAttribute("withProducts", stats.withProducts());
        model.addAttribute("withoutProducts", stats.withoutProducts());
        model.addAttribute("totalProducts", stats.totalProducts());

        model.addAttribute("pageTitle", "Danh sách nhà cung cấp");
        return "supplier-list";
    }

    // ------------------------------------------------------------------ create

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SupplierRequest());
        }
        model.addAttribute("pageTitle", "Tạo nhà cung cấp");
        return "supplier-create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") SupplierRequest form,
                         BindingResult bindingResult,
                         @RequestParam(name = "action", defaultValue = "create") String action,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tạo nhà cung cấp");
            return "supplier-create";
        }

        Integer newId;
        try {
            newId = supplierService.create(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Tạo nhà cung cấp");
            return "supplier-create";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Tạo nhà cung cấp thành công");

        if ("createAndAdd".equals(action)) {
            return "redirect:/supplier/" + newId;
        }
        return "redirect:/supplier";
    }

    // ------------------------------------------------------------------ detail / update

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        SupplierResponse supplier = supplierService.getById(id);
        model.addAttribute("supplier", supplier);

        if (!model.containsAttribute("form")) {
            SupplierRequest form = new SupplierRequest();
            form.setName(supplier.getName());
            form.setPhone(supplier.getPhone());
            form.setEmail(supplier.getEmail());
            form.setAddress(supplier.getAddress());
            model.addAttribute("form", form);
        }

        model.addAttribute("supplierProducts", supplierService.getProducts(id));
        model.addAttribute("pageTitle", "Chi tiết nhà cung cấp");
        return "supplier-detail";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") SupplierRequest form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplier", supplierService.getById(id));
            model.addAttribute("supplierProducts", supplierService.getProducts(id));
            model.addAttribute("showEditForm", true);
            model.addAttribute("pageTitle", "Chi tiết nhà cung cấp");
            return "supplier-detail";
        }

        try {
            supplierService.update(id, form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("supplier", supplierService.getById(id));
            model.addAttribute("supplierProducts", supplierService.getProducts(id));
            model.addAttribute("showEditForm", true);
            model.addAttribute("pageTitle", "Chi tiết nhà cung cấp");
            return "supplier-detail";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhà cung cấp thành công");
        return "redirect:/supplier/" + id;
    }
}
