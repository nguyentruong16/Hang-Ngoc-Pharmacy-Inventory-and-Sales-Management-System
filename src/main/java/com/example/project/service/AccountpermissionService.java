package com.example.project.service;

import com.example.project.entity.Accountpermission;
import com.example.project.repository.AccountpermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountpermissionService {
    private final AccountpermissionRepository accountpermissionRepository;

    public AccountpermissionService(AccountpermissionRepository accountpermissionRepository) {
        this.accountpermissionRepository = accountpermissionRepository;
    }

    public List<Accountpermission> getAll() {
        return accountpermissionRepository.findAll();
    }
}
