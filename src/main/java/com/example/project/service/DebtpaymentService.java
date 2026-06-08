package com.example.project.service;

import com.example.project.entity.Debtpayment;
import com.example.project.repository.DebtpaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtpaymentService {
    private final DebtpaymentRepository debtpaymentRepository;

    public DebtpaymentService(DebtpaymentRepository debtpaymentRepository) {
        this.debtpaymentRepository = debtpaymentRepository;
    }

    public List<Debtpayment> getAll() {
        return debtpaymentRepository.findAll();
    }
}