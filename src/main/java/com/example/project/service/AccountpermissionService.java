package com.example.project.service;

import com.example.project.dto.response.AccountpermissionResponse;
import com.example.project.repository.AccountpermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountpermissionService {
    private final AccountpermissionRepository accountpermissionRepository;

    public AccountpermissionService(AccountpermissionRepository accountpermissionRepository) {
        this.accountpermissionRepository = accountpermissionRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountpermissionResponse> getAll() {
        return accountpermissionRepository.findAll()
                .stream()
                .map(AccountpermissionResponse::from)
                .toList();
    }
}