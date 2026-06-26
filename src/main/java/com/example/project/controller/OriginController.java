package com.example.project.controller;

import com.example.project.dto.request.OriginCreateRequest;
import com.example.project.dto.response.OriginResponse;
import com.example.project.service.OriginService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class OriginController {
    private final OriginService originService;

    public OriginController(OriginService originService) {
        this.originService = originService;
    }

    @GetMapping("/origins")
    @ResponseBody
    public List<OriginResponse> getAll() {
        return originService.getAll();
    }

    @GetMapping("/owner/origins")
    public String originList(Model model) {
        List<OriginResponse> origins = originService.getAll();

        model.addAttribute("origins", origins);
        model.addAttribute("totalOrigins", origins.size());
        model.addAttribute("pageTitle", "Danh sách xuất xứ");
        return "owner/origin-list";
    }

    @GetMapping("/owner/origins/create-origin")
    public String createOriginForm(Model model) {
        if (!model.containsAttribute("originForm")) {
            model.addAttribute("originForm", new OriginCreateRequest());
        }
        model.addAttribute("pageTitle", "Tạo xuất xứ");
        return "owner/create-origin";
    }

    @PostMapping("/owner/origins/create-origin")
    public String createOrigin(@Valid @ModelAttribute("originForm") OriginCreateRequest form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tạo xuất xứ");
            return "owner/create-origin";
        }

        originService.create(form);
        redirectAttributes.addFlashAttribute("success", "Tạo xuất xứ thành công");
        return "redirect:/owner/origins";
    }
}
