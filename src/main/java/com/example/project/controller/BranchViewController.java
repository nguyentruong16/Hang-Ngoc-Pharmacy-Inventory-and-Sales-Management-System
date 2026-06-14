package com.example.project.controller;

import com.example.project.dto.response.BranchResponse;
import com.example.project.service.BranchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BranchViewController {
    private final BranchService branchService;

    public BranchViewController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping({"/branch-list"})
    public String branchList(Model model) {
        List<BranchResponse> branches = branchService.getAll();
        long activeBranches = branches.stream().filter(BranchResponse::isActive).count();

        model.addAttribute("branches", branches);
        model.addAttribute("totalBranches", branches.size());
        model.addAttribute("activeBranches", activeBranches);
        model.addAttribute("inactiveBranches", branches.size() - activeBranches);

        return "branch-list";
    }
}
