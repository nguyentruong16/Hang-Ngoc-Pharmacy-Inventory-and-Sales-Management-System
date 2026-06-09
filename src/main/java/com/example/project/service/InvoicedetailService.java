package com.example.project.service;

import com.example.project.dto.response.InvoicedetailResponse;
import com.example.project.repository.InvoicedetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoicedetailService {
    private final InvoicedetailRepository invoicedetailRepository;

    public InvoicedetailService(InvoicedetailRepository invoicedetailRepository) {
        this.invoicedetailRepository = invoicedetailRepository;
    }

    @Transactional(readOnly = true)
    public List<InvoicedetailResponse> getAll() {
        return invoicedetailRepository.findAll()
                .stream()
                .map(InvoicedetailResponse::from)
                .toList();
    }
}