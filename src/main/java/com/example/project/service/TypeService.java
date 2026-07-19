package com.example.project.service;

import com.example.project.dto.request.TypeCreateRequest;
import com.example.project.dto.response.TypeResponse;
import com.example.project.entity.Type;
import com.example.project.repository.TypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TypeService {
    private final TypeRepository typeRepository;

    public TypeService(TypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    //lấy danh sách loại
    @Transactional(readOnly = true)
    public Page<TypeResponse> list(String search, String sortType, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        String group = sortType == null ? "" : sortType.trim();
        return typeRepository.findFiltered(keyword, group, pageable)
                .map(TypeResponse::from);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return typeRepository.count();
    }

    //hiện danh sách sortType
    @Transactional(readOnly = true)
    public List<String> listSortTypes() {
        return typeRepository.findDistinctSortTypes()
                .stream()
                .filter(value -> value != null && !value.isBlank())
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    //tìm loại theo id
    @Transactional(readOnly = true)
    public TypeResponse getById(Integer id) {
        return typeRepository.findById(id)
                .map(TypeResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại hàng"));
    }

    //tạo loại
    @Transactional
    public TypeResponse create(TypeCreateRequest request) {
        Type type = new Type();
        type.setSortType(request.getSortType().trim());
        type.setName(request.getName().trim());
        type.setDefaultVATRate(request.getDefaultVATRate());
        return TypeResponse.from(typeRepository.save(type));
    }

    //sửa loại
    @Transactional
    public TypeResponse update(Integer id, TypeCreateRequest request) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại hàng"));
        type.setSortType(request.getSortType().trim());
        type.setName(request.getName().trim());
        type.setDefaultVATRate(request.getDefaultVATRate());
        return TypeResponse.from(typeRepository.save(type));
    }
}
