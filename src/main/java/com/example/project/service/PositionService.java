package com.example.project.service;

import com.example.project.dto.request.PositionCreateRequest;
import com.example.project.dto.response.PositionResponse;
import com.example.project.entity.Branch;
import com.example.project.entity.Position;
import com.example.project.entity.Product;
import com.example.project.repository.BranchRepository;
import com.example.project.repository.PositionRepository;
import com.example.project.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PositionService {
    private final PositionRepository positionRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public PositionService(PositionRepository positionRepository,
                           BranchRepository branchRepository,
                           ProductRepository productRepository) {
        this.positionRepository = positionRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getAll() {
        return positionRepository.findAll()
                .stream()
                .map(PositionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> list(String search, Integer branchId, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        Integer branchFilter = branchId == null || branchId <= 0 ? null : branchId;
        return positionRepository.findFiltered(keyword, branchFilter, pageable)
                .map(PositionResponse::from);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return positionRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Branch> listBranches() {
        return branchRepository.findAllWithStatus();
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findAllWithRelations();
    }

    @Transactional(readOnly = true)
    public PositionResponse getById(Integer id) {
        return positionRepository.findById(id)
                .map(PositionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí"));
    }

    @Transactional
    public PositionResponse create(PositionCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        Position entity = new Position();
        applyForm(entity, product, request);
        return PositionResponse.from(positionRepository.save(entity));
    }

    @Transactional
    public PositionResponse update(Integer id, PositionCreateRequest request) {
        Position entity = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        applyForm(entity, product, request);
        return PositionResponse.from(positionRepository.save(entity));
    }

    private void applyForm(Position entity, Product product, PositionCreateRequest request) {
        entity.setProductID(product);
        entity.setName(request.getName().trim());
        if (request.getBranchId() != null && request.getBranchId() > 0) {
            entity.setBranchID(branchRepository.getReferenceById(request.getBranchId()));
        } else {
            entity.setBranchID(null);
        }
    }
}
