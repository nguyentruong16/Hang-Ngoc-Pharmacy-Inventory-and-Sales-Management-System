package com.example.project.service;

import com.example.project.dto.response.DebtpaymentResponse;
import com.example.project.repository.DebtpaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DebtpaymentService {
    private final DebtpaymentRepository debtpaymentRepository;

    public DebtpaymentService(DebtpaymentRepository debtpaymentRepository) {
        this.debtpaymentRepository = debtpaymentRepository;
    }

    @Transactional(readOnly = true)
    public List<DebtpaymentResponse> getAll() {
        return debtpaymentRepository.findAll()
                .stream()
                .map(DebtpaymentResponse::from)
                .toList();
    }
}