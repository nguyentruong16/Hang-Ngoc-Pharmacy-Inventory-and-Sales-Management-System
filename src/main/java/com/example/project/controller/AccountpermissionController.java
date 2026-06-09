package com.example.project.controller;

import com.example.project.dto.response.AccountpermissionResponse;
import com.example.project.service.AccountpermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account-permissions")
public class AccountpermissionController {
    private final AccountpermissionService accountpermissionService;

    public AccountpermissionController(AccountpermissionService accountpermissionService) {
        this.accountpermissionService = accountpermissionService;
    }

    @GetMapping
    public List<AccountpermissionResponse> getAll() {
        return accountpermissionService.getAll();
    }
}