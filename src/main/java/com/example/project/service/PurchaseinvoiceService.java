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
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Creating a Purchase Invoice creates its {@link Batch} rows in the same transaction — per the
 * team's own process spec (sheet "Thay đổi Quy trình Phê duyệt", row "Duyệt Phiếu nhập hàng"):
 * the old 3-step flow (tạo hóa đơn → duyệt → chuyển thành lô) is "đã gộp thành 1 use case
 * 'Create Goods Receipt Note'". There is no separate "to-batch" confirmation step any more.
 */
@Service
public class PurchaseinvoiceService {

    private final PurchaseinvoiceRepository purchaseinvoiceRepository;
    private final PurchasedetailRepository purchasedetailRepository;
    private final SupplierRepository supplierRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ProductunitRepository productunitRepository;

    public PurchaseinvoiceService(PurchaseinvoiceRepository purchaseinvoiceRepository,
                                  PurchasedetailRepository purchasedetailRepository,
                                  SupplierRepository supplierRepository,
                                  AccountRepository accountRepository,
                                  ProductRepository productRepository,
                                  BatchRepository batchRepository,
                                  ProductunitRepository productunitRepository) {
        this.purchaseinvoiceRepository = purchaseinvoiceRepository;
        this.purchasedetailRepository = purchasedetailRepository;
        this.supplierRepository = supplierRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.productunitRepository = productunitRepository;
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

            Purchasedetail savedDetail = purchasedetailRepository.save(detail);

            createBatchForDetail(savedInvoice, savedDetail, product);
        }

        return savedInvoice.getId();
    }

    /** Creates the Batch (stock) row for one just-saved Purchasedetail — see class javadoc. */
    private void createBatchForDetail(Purchaseinvoice invoice, Purchasedetail detail, Product product) {
        Productunit importUnit = resolveImportUnit(product);

        BigDecimal importPrice = safe(detail.getImportPrice());
        BigDecimal importPricePerBase = calculateImportPricePerBase(importPrice, importUnit);
        int storageQuantity = calculateBaseQuantity(detail.getQuantity(), importUnit);

        Batch batch = new Batch();
        batch.setBatchCode(generateBatchCode(invoice.getId(), detail.getId()));
        batch.setBatchName(generateBatchName(product, detail));
        batch.setProductID(product);
        batch.setPurchaseDetailID(detail);
        batch.setStorageQuantity(storageQuantity);
        batch.setImportUnitID(importUnit);
        batch.setImportQtyInUnit(detail.getQuantity());
        batch.setImportPrice(importPrice);
        batch.setImportPricePerBase(importPricePerBase);
        batch.setImportDate(invoice.getDate());
        batch.setProductionDate(detail.getProductionDate());
        batch.setExpirationDate(detail.getExpirationDate());
        batch.setLotNumber(detail.getLotNumber());
        batch.setStatus(true);
        batch.setNote("Tạo từ phiếu nhập " + formatPurchaseCode(invoice.getId()));

        batchRepository.save(batch);
    }

    private Productunit resolveImportUnit(Product product) {
        return resolveImportUnitOrNull(product)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sản phẩm " + (product != null ? product.getName() : "") + " chưa có đơn vị nhập trong ProductUnit"
                ));
    }

    private Optional<Productunit> resolveImportUnitOrNull(Product product) {
        if (product == null || product.getProductID() == null) {
            return Optional.empty();
        }

        return productunitRepository.findByProductId(product.getProductID())
                .stream()
                .filter(unit -> !Boolean.FALSE.equals(unit.getIsActive()))
                .sorted(Comparator
                        .comparingInt(this::importUnitPriority)
                        .thenComparing(unit -> unit.getId() == null ? Integer.MAX_VALUE : unit.getId()))
                .findFirst();
    }

    private int importUnitPriority(Productunit unit) {
        if (Boolean.TRUE.equals(unit.getIsDefault())) {
            return 0;
        }

        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            return 1;
        }

        return 2;
    }

    private BigDecimal calculateImportPricePerBase(BigDecimal importPrice, Productunit importUnit) {
        if (importUnit == null
                || importUnit.getRatio() == null
                || importUnit.getRatio().compareTo(BigDecimal.ZERO) <= 0) {
            return importPrice;
        }

        return importPrice.divide(importUnit.getRatio(), 2, RoundingMode.HALF_UP);
    }

    private int calculateBaseQuantity(Integer importQuantity, Productunit importUnit) {
        BigDecimal quantity = BigDecimal.valueOf(importQuantity == null ? 0 : importQuantity);

        BigDecimal ratio = BigDecimal.ONE;

        if (importUnit != null
                && importUnit.getRatio() != null
                && importUnit.getRatio().compareTo(BigDecimal.ZERO) > 0) {
            ratio = importUnit.getRatio();
        }

        return quantity
                .multiply(ratio)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String generateBatchCode(Integer purchaseId, Integer purchaseDetailId) {
        return "BATCH-" + String.format("%06d", purchaseId == null ? 0 : purchaseId)
                + "-" + String.format("%03d", purchaseDetailId == null ? 0 : purchaseDetailId);
    }

    private String generateBatchName(Product product, Purchasedetail detail) {
        String productName = product != null && product.getName() != null
                ? product.getName()
                : "Sản phẩm";

        String lot = detail.getLotNumber() != null && !detail.getLotNumber().isBlank()
                ? detail.getLotNumber()
                : "Không số lô";

        String name = productName + " - " + lot;

        return name.length() > 50 ? name.substring(0, 50) : name;
    }

    /**
     * Known (lotNumber, expirationDate) pairs per product, with stock summed across every
     * {@link Batch} row sharing that pair — offered on the create-invoice form so re-supplying an
     * existing lot can reuse its label instead of the user retyping it (and risking a typo that
     * fragments the same physical lot under two labels).
     */
    @Transactional(readOnly = true)
    public Map<Integer, List<PurchaseInvoiceExistingLotResponse>> buildExistingLotsByProduct() {
        record LotKey(Integer productId, String lotNumber, LocalDate expirationDate) {
        }

        Map<LotKey, long[]> totalsByLot = new LinkedHashMap<>();
        Map<LotKey, LocalDate> productionDateByLot = new LinkedHashMap<>();

        for (Batch batch : batchRepository.findAll()) {
            if (batch.getProductID() == null
                    || batch.getLotNumber() == null
                    || batch.getLotNumber().isBlank()) {
                continue;
            }

            LotKey key = new LotKey(batch.getProductID().getProductID(), batch.getLotNumber(), batch.getExpirationDate());
            totalsByLot.computeIfAbsent(key, k -> new long[1])[0] +=
                    batch.getStorageQuantity() == null ? 0 : batch.getStorageQuantity();
            productionDateByLot.putIfAbsent(key, batch.getProductionDate());
        }

        Map<Integer, List<PurchaseInvoiceExistingLotResponse>> result = new LinkedHashMap<>();

        totalsByLot.forEach((key, total) -> result
                .computeIfAbsent(key.productId(), id -> new ArrayList<>())
                .add(new PurchaseInvoiceExistingLotResponse(
                        key.lotNumber(),
                        toIso(productionDateByLot.get(key)),
                        formatLocalDate(productionDateByLot.get(key)),
                        toIso(key.expirationDate()),
                        formatLocalDate(key.expirationDate()),
                        total[0]
                )));

        return result;
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

            if (detail.getLotNumber() == null || detail.getLotNumber().isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập số lô cho tất cả sản phẩm");
            }

            if (detail.getExpirationDate() == null) {
                throw new IllegalArgumentException("Vui lòng nhập hạn sử dụng cho tất cả sản phẩm");
            }

            if (!detail.getExpirationDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Hạn sử dụng phải lớn hơn ngày hiện tại");
            }

            if (detail.getProductionDate() != null
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

    /** ISO ({@code yyyy-MM-dd}) form, or {@code null} — matches what {@code <input type="date">} needs. */
    private String toIso(LocalDate date) {
        return date == null ? null : date.toString();
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