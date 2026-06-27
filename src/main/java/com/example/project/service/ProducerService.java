package com.example.project.service;

import com.example.project.dto.request.ProducerCreateRequest;
import com.example.project.dto.response.ProducerResponse;
import com.example.project.entity.Producer;
import com.example.project.repository.ProducerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProducerService {
    private final ProducerRepository producerRepository;

    public ProducerService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Transactional(readOnly = true)
    public List<ProducerResponse> getAll() {
        return producerRepository.findAll()
                .stream()
                .map(ProducerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProducerResponse> list(String search, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        Page<Producer> page = keyword.isEmpty()
                ? producerRepository.findAll(pageable)
                : producerRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return page.map(ProducerResponse::from);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return producerRepository.count();
    }

    @Transactional
    public ProducerResponse create(ProducerCreateRequest request) {
        Producer producer = new Producer();
        producer.setName(request.getName().trim());
        return ProducerResponse.from(producerRepository.save(producer));
    }
}
