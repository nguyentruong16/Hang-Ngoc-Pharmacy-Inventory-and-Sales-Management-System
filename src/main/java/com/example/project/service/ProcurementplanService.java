package com.example.project.service;

import com.example.project.dto.request.ProcurementPlanCreateRequest;
import com.example.project.dto.request.ProcurementPlanDetailCreateRequest;
import com.example.project.dto.response.ProcurementProductSearchResponse;
import com.example.project.dto.response.ProcurementplanResponse;
import com.example.project.entity.Procurementplan;
import com.example.project.entity.Procurementplandetail;
import com.example.project.entity.Product;
import com.example.project.entity.Productunit;
import com.example.project.entity.Supplier;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.ProcurementplanRepository;
import com.example.project.repository.ProcurementplandetailRepository;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.ProductunitRepository;
import com.example.project.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ProcurementplanService {
    private static final String DEFAULT_STATUS = "Đang thực hiện";
    private static final String COMPLETED_STATUS = "Đã hoàn thành";
    private static final Set<String> ALLOWED_STATUSES = Set.of(DEFAULT_STATUS, COMPLETED_STATUS);

    private final ProcurementplanRepository procurementplanRepository;
    private final ProcurementplandetailRepository procurementplandetailRepository;
    private final ProductRepository productRepository;
    private final ProductunitRepository productunitRepository;
    private final SupplierRepository supplierRepository;
    private final BatchRepository batchRepository;

    public ProcurementplanService(ProcurementplanRepository procurementplanRepository,
                                  ProcurementplandetailRepository procurementplandetailRepository,
                                  ProductRepository productRepository,
                                  ProductunitRepository productunitRepository,
                                  SupplierRepository supplierRepository,
                                  BatchRepository batchRepository) {
        this.procurementplanRepository = procurementplanRepository;
        this.procurementplandetailRepository = procurementplandetailRepository;
        this.productRepository = productRepository;
        this.productunitRepository = productunitRepository;
        this.supplierRepository = supplierRepository;
        this.batchRepository = batchRepository;
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

    @Transactional(readOnly = true)
    public ProcurementplanResponse getById(Integer id) {
        return procurementplanRepository.findById(id)
                .map(ProcurementplanResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự trù mua hàng"));
    }

    @Transactional(readOnly = true)
    public ProcurementPlanCreateRequest buildUpdateForm(Integer id) {
        Procurementplan plan = procurementplanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự trù mua hàng"));
        ensureNotCompleted(plan);

        ProcurementPlanCreateRequest form = new ProcurementPlanCreateRequest();
        form.setNote(plan.getNote());
        form.setStatus(plan.getStatus());

        List<ProcurementPlanDetailCreateRequest> details = new ArrayList<>();
        for (Procurementplandetail detail : procurementplandetailRepository.findByProcurementID_Id(id)) {
            ProcurementPlanDetailCreateRequest item = new ProcurementPlanDetailCreateRequest();
            item.setProductId(detail.getProductID().getProductID());
            item.setRequestedQuantity(detail.getRequestedQuantity());
            item.setUnit(detail.getUnit());
            item.setEstimatedPrice(detail.getEstimatedPrice());
            if (detail.getSupplierID() != null) {
                item.setSupplierId(detail.getSupplierID().getId());
            }
            details.add(item);
        }
        form.setDetails(details);
        return form;
    }

    @Transactional(readOnly = true)
    public List<ProcurementProductSearchResponse> searchProducts(String keyword, int limit) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }

        int maxResults = limit <= 0 ? 12 : Math.min(limit, 30);
        Map<Integer, Long> stockByProduct = buildStockByProduct();
        Map<Integer, Productunit> mainUnitByProduct = loadMainUnitByProduct();

        return productRepository.findAll()
                .stream()
                .filter(product -> Boolean.TRUE.equals(product.getStatus()))
                .filter(product -> matchesKeyword(product, normalizedKeyword))
                .sorted(Comparator.comparing(product -> product.getName() == null ? "" : product.getName()))
                .limit(maxResults)
                .map(product -> toSearchResponse(product, stockByProduct, mainUnitByProduct))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcurementProductSearchResponse> listProductsForDetails(ProcurementPlanCreateRequest form) {
        if (form == null || form.getDetails() == null) {
            return List.of();
        }

        Set<Integer> productIds = new HashSet<>();
        for (ProcurementPlanDetailCreateRequest detail : form.getDetails()) {
            if (detail.getProductId() != null) {
                productIds.add(detail.getProductId());
            }
        }

        if (productIds.isEmpty()) {
            return List.of();
        }

        Map<Integer, Long> stockByProduct = buildStockByProduct();
        Map<Integer, Productunit> mainUnitByProduct = loadMainUnitByProduct();

        return productRepository.findAllById(productIds)
                .stream()
                .map(product -> toSearchResponse(product, stockByProduct, mainUnitByProduct))
                .toList();
    }

    private Map<Integer, Productunit> loadMainUnitByProduct() {
        Map<Integer, Productunit> mainUnitByProduct = new HashMap<>();
        for (Productunit unit : productunitRepository.findAllWithProduct()) {
            if (!Boolean.TRUE.equals(unit.getIsActive()) || unit.getProductID() == null) {
                continue;
            }

            Integer productId = unit.getProductID().getProductID();
            Productunit existing = mainUnitByProduct.get(productId);
            if (existing == null || isPreferredUnit(unit, existing)) {
                mainUnitByProduct.put(productId, unit);
            }
        }
        return mainUnitByProduct;
    }

    private boolean isPreferredUnit(Productunit candidate, Productunit current) {
        if (Boolean.TRUE.equals(candidate.getIsDefault()) && !Boolean.TRUE.equals(current.getIsDefault())) {
            return true;
        }
        return Boolean.TRUE.equals(candidate.getIsBaseUnit()) && !Boolean.TRUE.equals(current.getIsDefault());
    }

    private ProcurementProductSearchResponse toSearchResponse(Product product,
                                                              Map<Integer, Long> stockByProduct,
                                                              Map<Integer, Productunit> mainUnitByProduct) {
        Productunit mainUnit = mainUnitByProduct.get(product.getProductID());
        int stock = stockByProduct.getOrDefault(product.getProductID(), 0L).intValue();

        return new ProcurementProductSearchResponse(
                product.getProductID(),
                product.getName(),
                product.getCode(),
                product.getBarcode(),
                stock,
                mainUnit != null ? mainUnit.getUnitName() : null,
                mainUnit != null ? mainUnit.getSellPrice() : null
        );
    }

    private boolean matchesKeyword(Product product, String normalizedKeyword) {
        return containsNormalized(product.getCode(), normalizedKeyword)
                || containsNormalized(product.getName(), normalizedKeyword)
                || containsNormalized(product.getBarcode(), normalizedKeyword);
    }

    private boolean containsNormalized(String value, String normalizedKeyword) {
        return value != null && normalize(value).contains(normalizedKeyword);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replace("Đ", "D").replace("đ", "d");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    @Transactional(readOnly = true)
    public List<Supplier> listSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(supplier -> supplier.getName() == null ? "" : supplier.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> buildStockByProduct() {
        Map<Integer, Long> stockByProduct = new HashMap<>();
        for (Object[] row : batchRepository.sumStorageGroupedByProduct()) {
            stockByProduct.put((Integer) row[0], (Long) row[1]);
        }
        return stockByProduct;
    }

    @Transactional
    public Integer create(ProcurementPlanCreateRequest request) {
        List<ProcurementPlanDetailCreateRequest> details = normalizeDetails(request);
        validateCreateRequest(details);

        Instant now = Instant.now();
        Procurementplan plan = new Procurementplan();
        plan.setProcurementCode(generateProcurementCode());
        plan.setDate(now);
        plan.setStatus(DEFAULT_STATUS);
        plan.setNote(trimToNull(request.getNote()));
        plan.setCreatedAt(now);

        Procurementplan savedPlan = procurementplanRepository.save(plan);
        saveDetails(savedPlan, details);

        return savedPlan.getId();
    }

    @Transactional
    public void update(Integer id, ProcurementPlanCreateRequest request) {
        Procurementplan plan = procurementplanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự trù mua hàng"));
        ensureNotCompleted(plan);

        List<ProcurementPlanDetailCreateRequest> details = normalizeDetails(request);
        validateCreateRequest(details);

        plan.setNote(trimToNull(request.getNote()));
        plan.setStatus(normalizeStatus(request.getStatus()));
        procurementplanRepository.save(plan);

        procurementplandetailRepository.deleteByProcurementID_Id(id);
        saveDetails(plan, details);
    }

    @Transactional
    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Không tìm thấy dự trù mua hàng");
        }

        Procurementplan plan = procurementplanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự trù mua hàng"));
        ensureNotCompleted(plan);

        procurementplandetailRepository.deleteByProcurementID_Id(id);
        procurementplanRepository.deleteById(id);
    }

    private void saveDetails(Procurementplan plan, List<ProcurementPlanDetailCreateRequest> details) {
        for (ProcurementPlanDetailCreateRequest item : details) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            Supplier supplier = null;
            if (item.getSupplierId() != null) {
                supplier = supplierRepository.findById(item.getSupplierId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
            }

            Procurementplandetail detail = new Procurementplandetail();
            detail.setProcurementID(plan);
            detail.setProductID(product);
            detail.setRequestedQuantity(item.getRequestedQuantity());
            detail.setUnit(trimToNull(item.getUnit()));
            detail.setEstimatedPrice(item.getEstimatedPrice());
            detail.setSupplierID(supplier);
            detail.setCurrentStock((int) batchRepository.sumStorageByProduct(product.getProductID()));

            procurementplandetailRepository.save(detail);
        }
    }

    private List<ProcurementPlanDetailCreateRequest> normalizeDetails(ProcurementPlanCreateRequest request) {
        if (request.getDetails() == null) {
            return List.of();
        }

        return request.getDetails().stream()
                .filter(item -> item.getProductId() != null)
                .toList();
    }

    private void validateCreateRequest(List<ProcurementPlanDetailCreateRequest> details) {
        if (details.isEmpty()) {
            throw new IllegalArgumentException("Dự trù mua hàng phải có ít nhất một sản phẩm");
        }

        for (ProcurementPlanDetailCreateRequest detail : details) {
            if (detail.getRequestedQuantity() == null || detail.getRequestedQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng dự trù phải lớn hơn 0");
            }

            if (detail.getEstimatedPrice() != null && detail.getEstimatedPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Giá dự kiến không được âm");
            }
        }
    }

    private String generateProcurementCode() {
        int nextId = procurementplanRepository.findAll().stream()
                .map(Procurementplan::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        return "DT-" + String.format("%06d", nextId);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS;
        }

        String normalized = status.trim();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Trạng thái dự trù không hợp lệ");
        }
        return normalized;
    }

    private void ensureNotCompleted(Procurementplan plan) {
        if (COMPLETED_STATUS.equals(plan.getStatus())) {
            throw new IllegalArgumentException("Dự trù mua hàng đã hoàn thành, không thể chỉnh sửa hoặc xóa");
        }
    }
}
