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
import com.example.project.entity.Supplierproduct;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.ProcurementplanRepository;
import com.example.project.repository.ProcurementplandetailRepository;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.ProductunitRepository;
import com.example.project.repository.SupplierRepository;
import com.example.project.repository.SupplierproductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Set<String> ALLOWED_STATUSES = Set.of(DEFAULT_STATUS, COMPLETED_STATUS);

    private final ProcurementplanRepository procurementplanRepository;
    private final ProcurementplandetailRepository procurementplandetailRepository;
    private final ProductRepository productRepository;
    private final ProductunitRepository productunitRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierproductRepository supplierproductRepository;
    private final BatchRepository batchRepository;

    public ProcurementplanService(ProcurementplanRepository procurementplanRepository,
                                  ProcurementplandetailRepository procurementplandetailRepository,
                                  ProductRepository productRepository,
                                  ProductunitRepository productunitRepository,
                                  SupplierRepository supplierRepository,
                                  SupplierproductRepository supplierproductRepository,
                                  BatchRepository batchRepository) {
        this.procurementplanRepository = procurementplanRepository;
        this.procurementplandetailRepository = procurementplandetailRepository;
        this.productRepository = productRepository;
        this.productunitRepository = productunitRepository;
        this.supplierRepository = supplierRepository;
        this.supplierproductRepository = supplierproductRepository;
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
    public Page<ProcurementplanResponse> list(String search,
                                              String fromDate,
                                              String toDate,
                                              String status,
                                              Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        String normalizedStatus = status == null ? "" : status.trim();

        List<ProcurementplanResponse> filtered = procurementplanRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .filter(plan -> keyword.isEmpty()
                        || (plan.getProcurementCode() != null
                        && plan.getProcurementCode().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))))
                .filter(plan -> matchesDate(plan, from, to))
                .filter(plan -> normalizedStatus.isEmpty() || normalizedStatus.equals(plan.getStatus()))
                .map(ProcurementplanResponse::from)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<ProcurementplanResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public List<String> listStatuses() {
        return List.copyOf(ALLOWED_STATUSES);
    }

    @Transactional(readOnly = true)
    public boolean isCompleted(Integer id) {
        return procurementplanRepository.findById(id)
                .map(plan -> COMPLETED_STATUS.equals(plan.getStatus()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return procurementplanRepository.count();
    }

    @Transactional(readOnly = true)
    public long countCompleted() {
        return procurementplanRepository.countByStatus(COMPLETED_STATUS);
    }

    @Transactional(readOnly = true)
    public long countInProgress() {
        return procurementplanRepository.countByStatus(DEFAULT_STATUS);
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
        Map<Integer, Productunit> baseUnitByProduct = loadBaseUnitByProduct();

        return productRepository.findAll()
                .stream()
                .filter(product -> Boolean.TRUE.equals(product.getStatus()))
                .filter(product -> matchesKeyword(product, normalizedKeyword))
                .sorted(Comparator.comparing(product -> product.getName() == null ? "" : product.getName()))
                .limit(maxResults)
                .map(product -> toSearchResponse(product, stockByProduct, mainUnitByProduct, baseUnitByProduct))
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
        Map<Integer, Productunit> baseUnitByProduct = loadBaseUnitByProduct();

        return productRepository.findAllById(productIds)
                .stream()
                .map(product -> toSearchResponse(product, stockByProduct, mainUnitByProduct, baseUnitByProduct))
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

    private Map<Integer, Productunit> loadBaseUnitByProduct() {
        Map<Integer, Productunit> baseUnitByProduct = new HashMap<>();
        for (Productunit unit : productunitRepository.findAllWithProduct()) {
            if (!Boolean.TRUE.equals(unit.getIsActive())
                    || !Boolean.TRUE.equals(unit.getIsBaseUnit())
                    || unit.getProductID() == null) {
                continue;
            }

            Integer productId = unit.getProductID().getProductID();
            baseUnitByProduct.putIfAbsent(productId, unit);
        }
        return baseUnitByProduct;
    }

    private boolean isPreferredUnit(Productunit candidate, Productunit current) {
        if (Boolean.TRUE.equals(candidate.getIsDefault()) && !Boolean.TRUE.equals(current.getIsDefault())) {
            return true;
        }
        return Boolean.TRUE.equals(candidate.getIsBaseUnit()) && !Boolean.TRUE.equals(current.getIsDefault());
    }

    private ProcurementProductSearchResponse toSearchResponse(Product product,
                                                              Map<Integer, Long> stockByProduct,
                                                              Map<Integer, Productunit> mainUnitByProduct,
                                                              Map<Integer, Productunit> baseUnitByProduct) {
        Productunit mainUnit = mainUnitByProduct.get(product.getProductID());
        Productunit baseUnit = baseUnitByProduct.get(product.getProductID());
        int stock = stockByProduct.getOrDefault(product.getProductID(), 0L).intValue();

        return new ProcurementProductSearchResponse(
                product.getProductID(),
                product.getName(),
                product.getCode(),
                product.getBarcode(),
                stock,
                baseUnit != null ? baseUnit.getUnitName() : null,
                mainUnit != null ? mainUnit.getUnitName() : null,
                mainUnit != null ? mainUnit.getRatio() : null,
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
    public BigDecimal getSupplierCostPrice(Integer supplierId, Integer productId) {
        if (supplierId == null || productId == null) {
            return null;
        }

        return supplierproductRepository.findBySupplierID_IdAndProductID_ProductID(supplierId, productId)
                .filter(this::isActiveSupplierProduct)
                .map(Supplierproduct::getCostPrice)
                .orElse(null);
    }

    private boolean isActiveSupplierProduct(Supplierproduct supplierProduct) {
        return supplierProduct.getIsActive() == null || Boolean.TRUE.equals(supplierProduct.getIsActive());
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

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
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
        plan.setDate(LocalDateTime.now(VN_ZONE));
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
            detail.setEstimatedPrice(resolveEstimatedPrice(item));
            detail.setSupplierID(supplier);
            detail.setCurrentStock((int) batchRepository.sumStorageByProduct(product.getProductID()));

            procurementplandetailRepository.save(detail);
        }
    }

    private BigDecimal resolveEstimatedPrice(ProcurementPlanDetailCreateRequest item) {
        if (item.getEstimatedPrice() != null) {
            return item.getEstimatedPrice().setScale(2, RoundingMode.HALF_UP);
        }

        Integer supplierId = item.getSupplierId();
        Integer productId = item.getProductId();
        Integer quantity = item.getRequestedQuantity();

        if (supplierId == null || productId == null || quantity == null || quantity <= 0) {
            return null;
        }

        BigDecimal unitCost = getSupplierCostPrice(supplierId, productId);
        if (unitCost == null) {
            return null;
        }

        return unitCost.multiply(BigDecimal.valueOf(quantity.longValue()))
                .setScale(2, RoundingMode.HALF_UP);
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

    private boolean matchesDate(Procurementplan plan, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        if (plan.getDate() == null) {
            return false;
        }
        LocalDate date = plan.getDate().toLocalDate();
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}
