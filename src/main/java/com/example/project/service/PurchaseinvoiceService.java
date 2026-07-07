package com.example.project.service;

import com.example.project.constant.PurchaseInvoiceStatus;
import com.example.project.dto.request.PurchaseInvoiceCreateRequest;
import com.example.project.dto.request.PurchaseInvoiceDetailCreateRequest;
import com.example.project.dto.response.*;
import com.example.project.entity.*;
import com.example.project.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseinvoiceService {

    private final PurchaseinvoiceRepository purchaseinvoiceRepository;
    private final PurchasedetailRepository purchasedetailRepository;
    private final SupplierRepository supplierRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    public PurchaseinvoiceService(PurchaseinvoiceRepository purchaseinvoiceRepository,
                                  PurchasedetailRepository purchasedetailRepository,
                                  SupplierRepository supplierRepository,
                                  AccountRepository accountRepository,
                                  ProductRepository productRepository,
                                  BatchRepository batchRepository) {
        this.purchaseinvoiceRepository = purchaseinvoiceRepository;
        this.purchasedetailRepository = purchasedetailRepository;
        this.supplierRepository = supplierRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
    }

    /**
     * Giữ lại method cũ để PurchaseinvoiceController REST không bị lỗi compile.
     */
    @Transactional(readOnly = true)
    public List<PurchaseinvoiceResponse> getAll() {
        return purchaseinvoiceRepository.findAllWithRelations()
                .stream()
                .map(PurchaseinvoiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PurchaseInvoiceListItemResponse> searchPurchaseInvoices(String keyword,
                                                                        String fromDate,
                                                                        String toDate,
                                                                        Integer supplierId,
                                                                        Integer branchId,
                                                                        String paymentStatus,
                                                                        Pageable pageable) {
        String normalizedKeyword = normalize(keyword);
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        List<Purchaseinvoice> invoices = purchaseinvoiceRepository.findAllWithRelations();
        List<Purchasedetail> allDetails = purchasedetailRepository.findAllWithRelations();

        Map<Integer, List<Purchasedetail>> detailMap = allDetails.stream()
                .filter(detail -> detail.getPurchaseID() != null)
                .collect(Collectors.groupingBy(detail -> detail.getPurchaseID().getId()));

        List<PurchaseInvoiceListItemResponse> filtered = invoices.stream()
                .filter(invoice -> matchesKeyword(invoice, detailMap.getOrDefault(invoice.getId(), List.of()), normalizedKeyword))
                .filter(invoice -> matchesDate(invoice, from, to))
                .filter(invoice -> supplierId == null || supplierMatches(invoice, supplierId))
                .map(invoice -> toListItem(invoice, detailMap.getOrDefault(invoice.getId(), List.of())))
                .filter(item -> paymentStatus == null || paymentStatus.isBlank()
                        || paymentStatus.equals(item.getPaymentStatus()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<PurchaseInvoiceListItemResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceStatsResponse getStats() {
        List<Purchaseinvoice> invoices = purchaseinvoiceRepository.findAllWithRelations();

        LocalDate today = LocalDate.now();

        long todayCount = invoices.stream()
                .filter(invoice -> invoice.getDate() != null)
                .filter(invoice -> toLocalDate(invoice.getDate()).equals(today))
                .count();

        BigDecimal totalAmount = invoices.stream()
                .map(this::safeTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = invoices.stream()
                .map(this::safePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal debtAmount = totalAmount.subtract(paidAmount);
        if (debtAmount.compareTo(BigDecimal.ZERO) < 0) {
            debtAmount = BigDecimal.ZERO;
        }

        return new PurchaseInvoiceStatsResponse(
                todayCount,
                totalAmount,
                paidAmount,
                debtAmount
        );
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceDetailPageResponse getDetail(Integer purchaseId) {
        Purchaseinvoice invoice = purchaseinvoiceRepository.findByIdWithRelations(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        List<Purchasedetail> details = purchasedetailRepository.findByPurchaseIdWithProduct(purchaseId);

        BigDecimal subtotal = calculateSubtotal(details);
        BigDecimal additionCost = safe(invoice.getAdditionCost());
        BigDecimal discount = safe(invoice.getDiscount());
        BigDecimal totalAmount = safeTotalAmount(invoice);
        BigDecimal paid = safePaid(invoice);
        BigDecimal debtAmount = totalAmount.subtract(paid);

        if (debtAmount.compareTo(BigDecimal.ZERO) < 0) {
            debtAmount = BigDecimal.ZERO;
        }

        List<PurchaseInvoiceDetailItemResponse> items = details.stream()
                .map(this::toDetailItem)
                .toList();

        int totalQuantity = details.stream()
                .map(Purchasedetail::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        String paymentStatus = resolvePaymentStatus(totalAmount, paid);

        Supplier supplier = invoice.getSupplierID();

        return new PurchaseInvoiceDetailPageResponse(
                invoice.getId(),
                formatPurchaseCode(invoice.getId()),
                invoice.getDate(),
                formatInstant(invoice.getDate()),
                supplier != null ? supplier.getName() : "Không có",
                supplier != null ? supplier.getPhone() : "",
                supplier != null ? supplier.getEmail() : "",
                invoice.getEmployeeID() != null ? invoice.getEmployeeID().getName() : "Không rõ",
                subtotal,
                additionCost,
                discount,
                totalAmount,
                paid,
                debtAmount,
                paymentStatus,
                statusCssClass(paymentStatus),
                invoice.getNote(),
                details.size(),
                totalQuantity,
                items
        );
    }

    @Transactional
    public Integer createPurchaseInvoice(PurchaseInvoiceCreateRequest request, Integer currentAccountId) {
        validateCreateRequest(request);

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));

        Account employee = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        BigDecimal subtotal = request.getDetails()
                .stream()
                .map(detail -> safe(detail.getImportPrice())
                        .multiply(BigDecimal.valueOf(detail.getQuantity() == null ? 0 : detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal additionCost = safe(request.getAdditionCost());
        BigDecimal discount = safe(request.getDiscount());
        BigDecimal totalAmount = subtotal.add(additionCost).subtract(discount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền phiếu nhập không hợp lệ");
        }

        BigDecimal paid = safe(request.getPaid());

        if (paid.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("Số tiền đã trả không được lớn hơn tổng tiền phiếu nhập");
        }

        Purchaseinvoice invoice = new Purchaseinvoice();
        invoice.setPurchaseInvoiceCode(generatePurchaseInvoiceCode());
        invoice.setDate(Instant.now());
        invoice.setSupplierID(supplier);
        invoice.setEmployeeID(employee);
        invoice.setAdditionCost(additionCost);
        invoice.setDiscount(discount);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaid(paid);
        invoice.setStatus(resolveInvoiceStatus(totalAmount, paid));
        invoice.setNote(request.getNote());

        Purchaseinvoice savedInvoice = purchaseinvoiceRepository.save(invoice);

        for (PurchaseInvoiceDetailCreateRequest item : request.getDetails()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + item.getProductId()));

            Purchasedetail detail = new Purchasedetail();
            detail.setPurchaseID(savedInvoice);
            detail.setProductID(product);
            detail.setQuantity(item.getQuantity());
            detail.setImportPrice(item.getImportPrice());
            detail.setProductionDate(item.getProductionDate());
            detail.setExpirationDate(item.getExpirationDate());
            detail.setLotNumber(trimToNull(item.getLotNumber()));

            purchasedetailRepository.save(detail);
        }

        return savedInvoice.getId();
    }

    @Transactional(readOnly = true)
    public List<Supplier> listSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(supplier -> supplier.getName() == null ? "" : supplier.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Object> listBranches() {
        return List.of();
    }

    /** The fixed set of statuses a PurchaseInvoice can be in, for the filter dropdown. */
    public List<String> listPaymentStatuses() {
        return PurchaseInvoiceStatus.ALL;
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findAll()
                .stream()
                .filter(product -> Boolean.TRUE.equals(product.getStatus()))
                .sorted(Comparator.comparing(product -> product.getName() == null ? "" : product.getName()))
                .toList();
    }

    private void validateCreateRequest(PurchaseInvoiceCreateRequest request) {
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một sản phẩm");
        }

        for (PurchaseInvoiceDetailCreateRequest detail : request.getDetails()) {
            if (detail.getProductId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn sản phẩm");
            }

            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
            }

            if (detail.getImportPrice() == null || detail.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giá nhập phải lớn hơn 0");
            }

            if (detail.getProductionDate() != null
                    && detail.getExpirationDate() != null
                    && detail.getExpirationDate().isBefore(detail.getProductionDate())) {
                throw new IllegalArgumentException("Hạn sử dụng không được trước ngày sản xuất");
            }
        }
    }

    private PurchaseInvoiceListItemResponse toListItem(Purchaseinvoice invoice, List<Purchasedetail> details) {
        BigDecimal totalAmount = safeTotalAmount(invoice);
        BigDecimal paid = safePaid(invoice);
        BigDecimal debtAmount = totalAmount.subtract(paid);

        if (debtAmount.compareTo(BigDecimal.ZERO) < 0) {
            debtAmount = BigDecimal.ZERO;
        }

        String paymentStatus = resolvePaymentStatus(totalAmount, paid);

        return new PurchaseInvoiceListItemResponse(
                invoice.getId(),
                formatPurchaseCode(invoice.getId()),
                invoice.getDate(),
                formatInstant(invoice.getDate()),
                invoice.getSupplierID() != null ? invoice.getSupplierID().getName() : "Không có",
                invoice.getEmployeeID() != null ? invoice.getEmployeeID().getName() : "Không rõ",
                details.size(),
                totalAmount,
                paid,
                debtAmount,
                paymentStatus,
                statusCssClass(paymentStatus)
        );
    }

    private PurchaseInvoiceDetailItemResponse toDetailItem(Purchasedetail detail) {
        Product product = detail.getProductID();
        BigDecimal lineTotal = safe(detail.getImportPrice())
                .multiply(BigDecimal.valueOf(detail.getQuantity() == null ? 0 : detail.getQuantity()));

        return new PurchaseInvoiceDetailItemResponse(
                product != null ? product.getProductID() : null,
                product != null ? product.getName() : "Không rõ",
                detail.getLotNumber(),
                detail.getProductionDate(),
                formatLocalDate(detail.getProductionDate()),
                detail.getExpirationDate(),
                formatLocalDate(detail.getExpirationDate()),
                detail.getQuantity(),
                detail.getImportPrice(),
                lineTotal
        );
    }

    private boolean matchesKeyword(Purchaseinvoice invoice,
                                   List<Purchasedetail> details,
                                   String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        if (containsNormalized(formatPurchaseCode(invoice.getId()), keyword)
                || containsNormalized(invoice.getSupplierID() != null ? invoice.getSupplierID().getName() : null, keyword)
                || containsNormalized(invoice.getEmployeeID() != null ? invoice.getEmployeeID().getName() : null, keyword)
                || containsNormalized(invoice.getNote(), keyword)) {
            return true;
        }

        return details.stream().anyMatch(detail -> {
            Product product = detail.getProductID();
            return product != null
                    && (containsNormalized(String.valueOf(product.getProductID()), keyword)
                    || containsNormalized(product.getName(), keyword)
                    || containsNormalized(product.getCode(), keyword)
                    || containsNormalized(product.getBarcode(), keyword));
        });
    }

    private boolean matchesDate(Purchaseinvoice invoice, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }

        if (invoice.getDate() == null) {
            return false;
        }

        LocalDate date = toLocalDate(invoice.getDate());

        if (from != null && date.isBefore(from)) {
            return false;
        }

        return to == null || !date.isAfter(to);
    }

    private boolean supplierMatches(Purchaseinvoice invoice, Integer supplierId) {
        return invoice.getSupplierID() != null && supplierId.equals(invoice.getSupplierID().getId());
    }

    private BigDecimal calculateSubtotal(List<Purchasedetail> details) {
        return details.stream()
                .map(detail -> safe(detail.getImportPrice())
                        .multiply(BigDecimal.valueOf(detail.getQuantity() == null ? 0 : detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safeTotalAmount(Purchaseinvoice invoice) {
        return safe(invoice.getTotalAmount());
    }

    private BigDecimal safePaid(Purchaseinvoice invoice) {
        return safe(invoice.getPaid());
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String resolvePaymentStatus(BigDecimal totalAmount, BigDecimal paid) {
        return resolveInvoiceStatus(totalAmount, paid);
    }

    /**
     * Computes the {@code Purchaseinvoice.status} value from the paid-vs-total thresholds — used
     * both to persist the status on creation and to render it (as {@code paymentStatus}) on the
     * list/detail screens, so the two never drift apart.
     */
    private String resolveInvoiceStatus(BigDecimal totalAmount, BigDecimal paid) {
        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            return PurchaseInvoiceStatus.DEBT;
        }

        if (paid.compareTo(totalAmount) >= 0) {
            return PurchaseInvoiceStatus.COMPLETED;
        }

        return PurchaseInvoiceStatus.PARTIAL_DEBT;
    }

    private String statusCssClass(String paymentStatus) {
        return switch (paymentStatus) {
            case PurchaseInvoiceStatus.COMPLETED -> "status-completed";
            case PurchaseInvoiceStatus.PARTIAL_DEBT -> "status-partial";
            default -> "status-pending";
        };
    }

    private String formatPurchaseCode(Integer id) {
        if (id == null) {
            return "PINV-000000";
        }

        return "PINV-" + String.format("%06d", id);
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }

        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    private String formatLocalDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private boolean containsNormalized(String value, String keyword) {
        return value != null && normalize(value).contains(keyword);
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String generatePurchaseInvoiceCode() {
        int nextId = purchaseinvoiceRepository.findAll().stream()
                .map(Purchaseinvoice::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        return "PINV-" + String.format("%06d", nextId);
    }
}