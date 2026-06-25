package com.example.project.controller;

import com.example.project.dto.request.BranchCreateRequest;
import com.example.project.dto.response.BranchResponse;
import com.example.project.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    //hien ra danh sach chi nhanh
    @GetMapping("/branch-list")
    public String branchList(Model model) {

        //lay danh sach chi nhanh
        List<BranchResponse> branches = branchService.getAll();

        //dem cac chi nhanh dang hoat dong
        long activeBranches = branchService.countActiveBranches(branches);

        model.addAttribute("branches", branches); //danh sach chi nhanh
        model.addAttribute("totalBranches", branches.size()); //so luong chi nhanh
        model.addAttribute("activeBranches", activeBranches); //so luong chi nhanh dang hoat dong
        model.addAttribute("inactiveBranches", branches.size() - activeBranches); //so luong chi nhanh ngung hoat dong
        model.addAttribute("pageTitle", "Danh sách chi nhánh");
        return "branch-list";
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
        return "branch-detail";
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
            return "branch-detail";
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
        return "create-branch";
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
            return "create-branch";
        }

        // create(...) auto-assigns the Owner to the new branch in the same transaction; if that
        // fails (e.g. no Owner account) the branch is rolled back and we show the reason.
        try {
            branchService.create(form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("pageTitle", "Tạo chi nhánh");
            model.addAttribute("errorMessage", ex.getMessage());
            return "create-branch";
        }

        redirectAttributes.addFlashAttribute("success", "Tạo chi nhánh thành công");
        return "redirect:/owner/branch-list";
    }
}
