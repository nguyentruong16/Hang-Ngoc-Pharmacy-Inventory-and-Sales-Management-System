package com.example.project.controller;

import com.example.project.dto.request.PositionCreateRequest;
import com.example.project.dto.response.PositionResponse;
import com.example.project.service.PositionService;
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
public class PositionController {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/positions")
    @ResponseBody
    public List<PositionResponse> getAll() {
        return positionService.getAll();
    }

    @GetMapping("/owner/positions")
    public String positionList(@RequestParam(name = "search", required = false) String search,
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
        Page<PositionResponse> positionPage = positionService.list(search, pageable);
        String basePath = resolveBasePath(request);

        model.addAttribute("positions", positionPage.getContent());
        model.addAttribute("totalPositions", positionService.countAll());
        model.addAttribute("search", search);
        model.addAttribute("currentPage", positionPage.getNumber());
        model.addAttribute("totalPages", positionPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", positionPage.getTotalElements());
        model.addAttribute("pageTitle", "Danh sách vị trí");
        model.addAttribute("basePath", basePath);
        return "owner/position-list";
    }

    @GetMapping("/owner/positions/create-position")
    public String createPositionForm(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("positionForm")) {
            model.addAttribute("positionForm", new PositionCreateRequest());
        }
        populateCreateForm(model, request);
        return "owner/create-position";
    }

    @PostMapping("/owner/positions/create-position")
    public String createPosition(@Valid @ModelAttribute("positionForm") PositionCreateRequest form,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);
        if (bindingResult.hasErrors()) {
            populateCreateForm(model, request);
            return "owner/create-position";
        }

        positionService.create(form);
        redirectAttributes.addFlashAttribute("success", "Tạo vị trí thành công");
        return "redirect:" + basePath;
    }

    @GetMapping("/owner/positions/update-position/{id}")
    public String updatePositionForm(@PathVariable Integer id,
                                     HttpServletRequest request,
                                     Model model) {
        PositionResponse position = positionService.getById(id);
        model.addAttribute("position", position);

        if (!model.containsAttribute("positionForm")) {
            PositionCreateRequest form = new PositionCreateRequest();
            form.setProductId(position.getProductId());
            form.setName(position.getName());
            model.addAttribute("positionForm", form);
        }

        populateForm(model, request, "Cập nhật vị trí");
        return "owner/update-position";
    }

    @PostMapping("/owner/positions/update-position/{id}")
    public String updatePosition(@PathVariable Integer id,
                                 @Valid @ModelAttribute("positionForm") PositionCreateRequest form,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String basePath = resolveBasePath(request);
        if (bindingResult.hasErrors()) {
            model.addAttribute("position", positionService.getById(id));
            populateForm(model, request, "Cập nhật vị trí");
            return "owner/update-position";
        }

        positionService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Cập nhật vị trí thành công");
        return "redirect:" + basePath;
    }

    private void populateCreateForm(Model model, HttpServletRequest request) {
        populateForm(model, request, "Tạo vị trí");
    }

    private void populateForm(Model model, HttpServletRequest request, String pageTitle) {
        model.addAttribute("products", positionService.listProducts());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("basePath", resolveBasePath(request));
    }

    private String resolveBasePath(HttpServletRequest request) {
        return "/owner/positions";
    }
}
