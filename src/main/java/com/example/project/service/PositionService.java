package com.example.project.service;

import com.example.project.dto.request.PositionCreateRequest;
import com.example.project.dto.response.PositionResponse;
import com.example.project.entity.Position;
import com.example.project.entity.Product;
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
    private final ProductRepository productRepository;

    public PositionService(PositionRepository positionRepository,
                           ProductRepository productRepository) {
        this.positionRepository = positionRepository;
        this.productRepository = productRepository;
    }

    //lấy ra danh sách vị trí (bao gồm cả tìm kiếm và phân trang)
    @Transactional(readOnly = true)
    public Page<PositionResponse> list(String search, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();   //nếu search null thì search bằng "" ngược lại thì trim
        return positionRepository.findFiltered(keyword, pageable)
                .map(PositionResponse::from);
    }

    //đếm số lượng tất cả vị trí
    @Transactional(readOnly = true)
    public long countAll() {
        return positionRepository.count();
    }

    // lấy danh sách sản phẩm
    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findAllWithRelations();
    }

    // tìm vị trí theo id
    @Transactional(readOnly = true)
    public PositionResponse getById(Integer id) {
        return positionRepository.findById(id)
                .map(PositionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí"));
    }

    // tạo vị trí
    @Transactional
    public PositionResponse create(PositionCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        Position entity = new Position();
        applyForm(entity, product, request);
        return PositionResponse.from(positionRepository.save(entity));
    }

    // chỉnh sửa vị trí
    @Transactional
    public PositionResponse update(Integer id, PositionCreateRequest request) {
        Position entity = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        applyForm(entity, product, request);
        return PositionResponse.from(positionRepository.save(entity));
    }

    // thêm hoặc update position
    private void applyForm(Position entity, Product product, PositionCreateRequest request) {
        entity.setProductID(product);
        entity.setName(request.getName().trim());
    }
}
