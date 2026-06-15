package com.example.project.controller;

import com.example.project.dto.response.BranchResponse;
import com.example.project.service.BranchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/owner")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/branch-list")
    public String branchList(Model model) {
        List<BranchResponse> branches = branchService.getAll();
        long activeBranches = branchService.countActiveBranches(branches);

        model.addAttribute("branches", branches);
        model.addAttribute("totalBranches", branches.size());
        model.addAttribute("activeBranches", activeBranches);
        model.addAttribute("inactiveBranches", branches.size() - activeBranches);
        return "branch-list";
    }

    @GetMapping("/branch-detail/{id}")
    public String branchDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("branch", branchService.getById(id));
        return "branch-detail";
    }
}
