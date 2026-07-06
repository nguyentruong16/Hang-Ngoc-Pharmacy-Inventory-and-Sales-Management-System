package com.example.project.controller;

import com.example.project.dto.request.TypeCreateRequest;
import com.example.project.dto.response.TypeResponse;
import com.example.project.service.TypeService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TypeController {
    private final TypeService typeService;

    public TypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    @GetMapping("/types")
    @ResponseBody
    public List<TypeResponse> getAll() {
        return typeService.getAll();
    }

    @GetMapping({
            "/owner/types",
            "/chief-pharmacist/types"
    })
    public String typeList(@RequestParam(name = "search", required = false) String search,
                           @RequestParam(name = "sortType", required = false) String sortType,
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

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<TypeResponse> typePage = typeService.list(search, sortType, pageable);
        String basePath = resolveBasePath(request);

        model.addAttribute("types", typePage.getContent());
        model.addAttribute("totalTypes", typeService.countAll());
        model.addAttribute("typeGroups", typeService.listSortTypes());
        model.addAttribute("search", search);
        model.addAttribute("filterSortType", sortType);
        model.addAttribute("currentPage", typePage.getNumber());
        model.addAttribute("totalPages", typePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", typePage.getTotalElements());
        model.addAttribute("pageTitle", "Danh sách loại hàng");
        model.addAttribute("basePath", basePath);
        return "owner/type-list";
    }

    @GetMapping({
            "/owner/types/create-type",
            "/chief-pharmacist/types/create-type"
    })
    public String createTypeForm(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("typeForm")) {
            model.addAttribute("typeForm", new TypeCreateRequest());
        }
        model.addAttribute("typeGroups", typeService.listSortTypes());
        model.addAttribute("pageTitle", "Tạo loại hàng");
        model.addAttribute("basePath", resolveBasePath(request));
        return "owner/create-type";
    }

    @PostMapping({
            "/owner/types/create-type",
            "/chief-pharmacist/types/create-type"
    })
    public String createType(@Valid @ModelAttribute("typeForm") TypeCreateRequest form,
                             BindingResult bindingResult,
                             HttpServletRequest request,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);
        if (bindingResult.hasErrors()) {
            model.addAttribute("typeGroups", typeService.listSortTypes());
            model.addAttribute("pageTitle", "Tạo loại hàng");
            model.addAttribute("basePath", basePath);
            return "owner/create-type";
        }

        typeService.create(form);
        redirectAttributes.addFlashAttribute("success", "Tạo loại hàng thành công");
        return "redirect:" + basePath;
    }

    @GetMapping({
            "/owner/types/update-type/{id}",
            "/chief-pharmacist/types/update-type/{id}"
    })
    public String updateTypeForm(@PathVariable Integer id,
                                 HttpServletRequest request,
                                 Model model) {
        TypeResponse type = typeService.getById(id);
        model.addAttribute("type", type);

        if (!model.containsAttribute("typeForm")) {
            TypeCreateRequest form = new TypeCreateRequest();
            form.setSortType(type.getSortType());
            form.setName(type.getName());
            model.addAttribute("typeForm", form);
        }

        model.addAttribute("typeGroups", typeService.listSortTypes());
        model.addAttribute("pageTitle", "Cập nhật loại hàng");
        model.addAttribute("basePath", resolveBasePath(request));
        return "owner/update-type";
    }

    @PostMapping({
            "/owner/types/update-type/{id}",
            "/chief-pharmacist/types/update-type/{id}"
    })
    public String updateType(@PathVariable Integer id,
                             @Valid @ModelAttribute("typeForm") TypeCreateRequest form,
                             BindingResult bindingResult,
                             HttpServletRequest request,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);
        if (bindingResult.hasErrors()) {
            model.addAttribute("type", typeService.getById(id));
            model.addAttribute("typeGroups", typeService.listSortTypes());
            model.addAttribute("pageTitle", "Cập nhật loại hàng");
            model.addAttribute("basePath", basePath);
            return "owner/update-type";
        }

        typeService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Cập nhật loại hàng thành công");
        return "redirect:" + basePath;
    }

    private String resolveBasePath(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/chief-pharmacist/types")) {
            return "/chief-pharmacist/types";
        }
        return "/owner/types";
    }
}
