package com.example.project.service;

import com.example.project.dto.request.OriginCreateRequest;
import com.example.project.dto.response.OriginResponse;
import com.example.project.entity.Origin;
import com.example.project.repository.OriginRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OriginService {
    private final OriginRepository originRepository;

    public OriginService(OriginRepository originRepository) {
        this.originRepository = originRepository;
    }

    @Transactional(readOnly = true)
    public List<OriginResponse> getAll() {
        return originRepository.findAll()
                .stream()
                .map(OriginResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OriginResponse> list(String search, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        Page<Origin> page = keyword.isEmpty()
                ? originRepository.findAll(pageable)
                : originRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return page.map(OriginResponse::from);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return originRepository.count();
    }

    @Transactional
    public OriginResponse create(OriginCreateRequest request) {
        Origin origin = new Origin();
        origin.setName(request.getName().trim());
        return OriginResponse.from(originRepository.save(origin));
    }
}