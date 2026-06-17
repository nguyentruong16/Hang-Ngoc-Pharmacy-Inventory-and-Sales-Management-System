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
        return "branch-list";
    }

    //xem chi tiet chi nhanh
    @GetMapping("/branch-list/branch-detail/{id}")
    public String branchDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("branch", branchService.getById(id));
        return "branch-detail";
    }

    //hien thi trang tao chi nhanh moi
    @GetMapping("/branch-list/create-branch")
    public String createBranchForm(Model model) {
        if (!model.containsAttribute("branchForm")) {
            model.addAttribute("branchForm", new BranchCreateRequest());
        }
        return "create-branch";
    }

    //them chi nhanh
    @PostMapping("/branch-list/create-branch")
    public String createBranch(@Valid @ModelAttribute("branchForm") BranchCreateRequest form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        //check validate ben dto
        if (bindingResult.hasErrors()) {
            return "create-branch";
        }

        branchService.create(form);
        redirectAttributes.addFlashAttribute("success", "Tạo chi nhánh thành công");
        return "redirect:/owner/branch-list";
    }
}
