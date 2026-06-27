package com.example.project.controller;

import com.example.project.dto.request.BranchCreateRequest;
import com.example.project.dto.response.BranchResponse;
import com.example.project.service.BranchService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/branch-list")
    public String branchList(@RequestParam(name = "search", required = false) String search,
                             @RequestParam(name = "status", required = false) String status,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "5") int size,
                             Model model) {
        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 5;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<BranchResponse> branchPage = branchService.list(search, status, pageable);
        BranchService.BranchStats stats = branchService.getStats();

        model.addAttribute("branches", branchPage.getContent());
        model.addAttribute("totalBranches", stats.totalBranches());
        model.addAttribute("activeBranches", stats.activeBranches());
        model.addAttribute("inactiveBranches", stats.inactiveBranches());
        model.addAttribute("search", search);
        model.addAttribute("filterStatus", status);
        model.addAttribute("currentPage", branchPage.getNumber());
        model.addAttribute("totalPages", branchPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", branchPage.getTotalElements());
        model.addAttribute("pageTitle", "Danh sách chi nhánh");
        return "owner/branch-list";
    }

    //xem chi tiet chi nhanh
    @GetMapping("/branch-list/branch-detail/{id}")
    public String branchDetail(@PathVariable Integer id, Model model) {
        BranchResponse branch = branchService.getById(id);
        model.addAttribute("branch", branch);

        //do du lieu chi nhanh vao form de chinh sua
        if (!model.containsAttribute("branchForm")) {
            BranchCreateRequest form = new BranchCreateRequest();
            form.setName(branch.getName());
            form.setAddress(branch.getAddress());
            form.setStatusName(branch.getStatusName());
            model.addAttribute("branchForm", form);
        }

        model.addAttribute("pageTitle", "Chi tiết chi nhánh");
        return "owner/branch-detail";
    }

    //cap nhat chi nhanh
    @PostMapping("/branch-list/branch-detail/{id}")
    public String updateBranch(@PathVariable Integer id,
                               @Valid @ModelAttribute("branchForm") BranchCreateRequest form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        //check validate ben dto request
        if (bindingResult.hasErrors()) {
            model.addAttribute("branch", branchService.getById(id));
            model.addAttribute("pageTitle", "Chi tiết chi nhánh");
            return "owner/branch-detail";
        }

        branchService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Cập nhật chi nhánh thành công");
        return "redirect:/owner/branch-list/branch-detail/" + id;
    }

    //hien thi trang tao chi nhanh moi
    @GetMapping("/branch-list/create-branch")
    public String createBranchForm(Model model) {
        if (!model.containsAttribute("branchForm")) {
            model.addAttribute("branchForm", new BranchCreateRequest());
        }
        model.addAttribute("pageTitle", "Tạo chi nhánh");
        return "owner/create-branch";
    }

    //them chi nhanh
    @PostMapping("/branch-list/create-branch")
    public String createBranch(@Valid @ModelAttribute("branchForm") BranchCreateRequest form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        //check validate ben dto request
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Tạo chi nhánh");
            return "owner/create-branch";
        }

        // create(...) auto-assigns the Owner to the new branch in the same transaction; if that
        // fails (e.g. no Owner account) the branch is rolled back and we show the reason.
        try {
            branchService.create(form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("pageTitle", "Tạo chi nhánh");
            model.addAttribute("errorMessage", ex.getMessage());
            return "owner/create-branch";
        }

        redirectAttributes.addFlashAttribute("success", "Tạo chi nhánh thành công");
        return "redirect:/owner/branch-list";
    }
}
