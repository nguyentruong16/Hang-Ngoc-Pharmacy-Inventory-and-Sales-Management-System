package com.example.project.service;

import com.example.project.dto.response.TypeResponse;
import com.example.project.repository.TypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TypeService {
    private final TypeRepository typeRepository;

    public TypeService(TypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    @Transactional(readOnly = true)
    public List<TypeResponse> getAll() {
        return typeRepository.findAll()
                .stream()
                .map(TypeResponse::from)
                .toList();
    }
}