package com.example.project.service;

import com.example.project.entity.Invoicedetail;
import com.example.project.repository.InvoicedetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoicedetailService {
    private final InvoicedetailRepository invoicedetailRepository;

    public InvoicedetailService(InvoicedetailRepository invoicedetailRepository) {
        this.invoicedetailRepository = invoicedetailRepository;
    }

    public List<Invoicedetail> getAll() {
        return invoicedetailRepository.findAll();
    }
}