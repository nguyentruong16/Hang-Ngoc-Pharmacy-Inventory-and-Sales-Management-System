package com.example.project.service;

import com.example.project.dto.request.SupplierRequest;
import com.example.project.dto.response.SupplierAvailableProductResponse;
import com.example.project.dto.response.SupplierResponse;
import com.example.project.dto.response.SupplierproductResponse;
import com.example.project.entity.Product;
import com.example.project.entity.Supplier;
import com.example.project.entity.Supplierproduct;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.SupplierRepository;
import com.example.project.repository.SupplierproductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierproductRepository supplierproductRepository;
    private final ProductRepository productRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierproductRepository supplierproductRepository,
                           ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierproductRepository = supplierproductRepository;
        this.productRepository = productRepository;
    }

    // ------------------------------------------------------------------ list

    @Transactional(readOnly = true)
    public Page<SupplierResponse> list(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<SupplierResponse> filtered = supplierRepository.findAll()
                .stream()
                .filter(s -> matchesKeyword(s, kw))
                .sorted((a, b) -> {
                    String nameA = a.getName() == null ? "" : a.getName();
                    String nameB = b.getName() == null ? "" : b.getName();
                    return nameA.compareToIgnoreCase(nameB);
                })
                .map(s -> {
                    SupplierResponse r = SupplierResponse.from(s);
                    r.setProductCount(supplierproductRepository.countBySupplierID_Id(s.getId()));
                    return r;
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<SupplierResponse> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    // ------------------------------------------------------------------ stats

    public record SupplierStats(long total, long withProducts, long withoutProducts, long totalProducts) {
    }

    @Transactional(readOnly = true)
    public SupplierStats getStats() {
        long total = supplierRepository.count();
        long totalProducts = supplierproductRepository.count();
        long withProducts = supplierproductRepository.countDistinctSuppliers();
        long withoutProducts = total - withProducts;
        return new SupplierStats(total, withProducts, withoutProducts, totalProducts);
    }

    // ------------------------------------------------------------------ getById

    @Transactional(readOnly = true)
    public SupplierResponse getById(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
        SupplierResponse r = SupplierResponse.from(supplier);
        r.setProductCount(supplierproductRepository.countBySupplierID_Id(id));
        return r;
    }

    @Transactional(readOnly = true)
    public List<SupplierproductResponse> getProducts(Integer supplierId) {
        return supplierproductRepository.findBySupplierID_Id(supplierId)
                .stream()
                .map(SupplierproductResponse::from)
                .toList();
    }

    // ------------------------------------------------ available products picker

    /**
     * Products that are NOT yet supplied by this supplier, feeding the
     * "Thêm sản phẩm cung ứng" picker modal. Sorted by name (search / filter /
     * paging are handled client-side inside the modal).
     */
    @Transactional(readOnly = true)
    public List<SupplierAvailableProductResponse> getAvailableProducts(Integer supplierId) {
        Set<Integer> linkedProductIds = linkedProductIds(supplierId);
        return productRepository.findAllWithRelations()
                .stream()
                .filter(p -> !linkedProductIds.contains(p.getProductID()))
                .map(SupplierAvailableProductResponse::from)
                .toList();
    }

    /**
     * Links the given products to the supplier. Ignores blank ids, products
     * already linked, and ids that no longer exist. Returns how many were added.
     */
    @Transactional
    public int addProducts(Integer supplierId, List<Integer> productIds) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }

        Set<Integer> linkedProductIds = linkedProductIds(supplierId);
        int added = 0;
        for (Integer productId : productIds) {
            if (productId == null || linkedProductIds.contains(productId)) {
                continue;
            }
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                continue;
            }
            Supplierproduct sp = new Supplierproduct();
            sp.setSupplierID(supplier);
            sp.setProductID(product);
            sp.setIsPreferred(false);
            sp.setIsActive(true);
            supplierproductRepository.save(sp);
            linkedProductIds.add(productId);
            added++;
        }
        return added;
    }

    private Set<Integer> linkedProductIds(Integer supplierId) {
        return supplierproductRepository.findBySupplierID_Id(supplierId)
                .stream()
                .map(sp -> sp.getProductID() != null ? sp.getProductID().getProductID() : null)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(java.util.HashSet::new));
    }

    // ------------------------------------------------------------------ create

    @Transactional
    public Integer create(SupplierRequest request) {
        validatePhone(request.getPhone(), null);
        validateEmail(request.getEmail(), null);

        Supplier supplier = new Supplier();
        supplier.setName(request.getName().trim());
        supplier.setPhone(request.getPhone().trim());
        supplier.setEmail(request.getEmail().trim());
        supplier.setAddress(request.getAddress().trim());
        supplier.setTaxCode(request.getTaxCode().trim());

        return supplierRepository.save(supplier).getId();
    }

    // ------------------------------------------------------------------ update

    @Transactional
    public void update(Integer id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));

        validatePhone(request.getPhone(), id);
        validateEmail(request.getEmail(), id);

        supplier.setName(request.getName().trim());
        supplier.setPhone(request.getPhone().trim());
        supplier.setEmail(request.getEmail().trim());
        supplier.setAddress(request.getAddress().trim());
        supplier.setTaxCode(request.getTaxCode().trim());

        supplierRepository.save(supplier);
    }

    // ------------------------------------------------------------------ legacy

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll()
                .stream()
                .map(SupplierResponse::from)
                .toList();
    }

    // ------------------------------------------------------------------ helpers

    private boolean matchesKeyword(Supplier s, String kw) {
        if (kw.isBlank()) return true;
        return contains(s.getName(), kw)
                || contains(s.getPhone(), kw)
                || contains(s.getEmail(), kw);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void validatePhone(String phone, Integer excludeId) {
        boolean duplicate = excludeId == null
                ? supplierRepository.existsByPhone(phone.trim())
                : supplierRepository.existsByPhoneAndIdNot(phone.trim(), excludeId);
        if (duplicate) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi nhà cung cấp khác");
        }
    }

    private void validateEmail(String email, Integer excludeId) {
        boolean duplicate = excludeId == null
                ? supplierRepository.existsByEmailIgnoreCase(email.trim())
                : supplierRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), excludeId);
        if (duplicate) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi nhà cung cấp khác");
        }
    }
}
