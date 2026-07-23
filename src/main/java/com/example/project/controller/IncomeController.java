package com.example.project.controller;

import com.example.project.dto.response.IncomeListItemResponse;
import com.example.project.dto.response.IncomeResponse;
import com.example.project.service.IncomeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping({"/owner/incomes", "/pharmacist/incomes"})
    public String incomeList(@RequestParam(name = "search", required = false) String search,
                             @RequestParam(name = "fromDate", required = false) String fromDate,
                             @RequestParam(name = "toDate", required = false) String toDate,
                             @RequestParam(name = "incomeType", required = false) String incomeType,
                             @RequestParam(name = "status", required = false) String status,
                             @RequestParam(name = "applicantId", required = false) Integer applicantId,
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

        Page<IncomeListItemResponse> incomePage = incomeService.list(
                search, fromDate, toDate, incomeType, status, applicantId, PageRequest.of(page, size));
        String basePath = resolveBasePath(request);

        model.addAttribute("incomes", incomePage.getContent());
        model.addAttribute("totalIncomes", incomeService.countAll());
        model.addAttribute("todayIncomes", incomeService.countToday());
        model.addAttribute("todayAmount", incomeService.sumTodayAmount());
        model.addAttribute("pendingIncomes", incomeService.countPending());
        model.addAttribute("pendingAmount", incomeService.sumPendingAmount());
        model.addAttribute("approvedIncomes", incomeService.countApproved());
        model.addAttribute("approvedAmount", incomeService.sumApprovedAmount());
        model.addAttribute("statuses", incomeService.listStatuses());
        model.addAttribute("applicants", incomeService.listApplicants());
        model.addAttribute("incomeTypeLabels", incomeService.incomeTypeLabels());
        model.addAttribute("search", search);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterIncomeType", incomeType);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterApplicantId", applicantId);
        model.addAttribute("currentPage", incomePage.getNumber());
        model.addAttribute("totalPages", incomePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", incomePage.getTotalElements());
        model.addAttribute("pageTitle", "Danh sách khoản thu");
        model.addAttribute("basePath", basePath);
        return "income-list";
    }

    private String resolveBasePath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/pharmacist/incomes")
                ? "/pharmacist/incomes"
                : "/owner/incomes";
    }
}
