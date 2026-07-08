package com.example.project.service;

import com.example.project.dto.response.ProcurementplanResponse;
import com.example.project.entity.Procurementplan;
import com.example.project.repository.ProcurementplanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcurementplanService {
    private final ProcurementplanRepository procurementplanRepository;

    public ProcurementplanService(ProcurementplanRepository procurementplanRepository) {
        this.procurementplanRepository = procurementplanRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcurementplanResponse> getAll() {
        return procurementplanRepository.findAll()
                .stream()
                .map(ProcurementplanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProcurementplanResponse> list(String search, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        Page<Procurementplan> page = keyword.isEmpty()
                ? procurementplanRepository.findAll(pageable)
                : procurementplanRepository.findByProcurementCodeContainingIgnoreCase(keyword, pageable);
        return page.map(ProcurementplanResponse::from);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return procurementplanRepository.count();
    }
}
