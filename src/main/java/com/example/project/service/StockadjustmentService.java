package com.example.project.service;

import com.example.project.constant.StockAdjustmentStatus;
import com.example.project.dto.request.StockAdjustmentCreateRequest;
import com.example.project.dto.request.StockAdjustmentItemRequest;
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

/**
 * Single service for the Stock Adjustment feature (formerly "stock out"): listing/searching,
 * detail, creation, and the approve/reject workflow that actually moves stock.
 *
 * <p>Adjustment types: {@code DESTROY / INTERNAL_USE / SAMPLE / GIFT} (outbound) plus
 * {@code COUNT_INCREASE / COUNT_DECREASE} which originate from a Stock Count (a later phase).
 * There is no {@code INTERNAL_TRANSFER} — the system is single-store.</p>
 */
@Service
public class StockadjustmentService {

    private static final String DIRECTION_IN = "IN";
    private static final String DIRECTION_OUT = "OUT";

    private final StockadjustmentRepository stockadjustmentRepository;
    private final StockadjustmentdetailRepository stockadjustmentdetailRepository;
    private final AccountRepository accountRepository;
    private final BatchRepository batchRepository;
    private final ProductunitRepository productunitRepository;

    public StockadjustmentService(StockadjustmentRepository stockadjustmentRepository,
                                  StockadjustmentdetailRepository stockadjustmentdetailRepository,
                                  AccountRepository accountRepository,
                                  BatchRepository batchRepository,
                                  ProductunitRepository productunitRepository) {
        this.stockadjustmentRepository = stockadjustmentRepository;
        this.stockadjustmentdetailRepository = stockadjustmentdetailRepository;
        this.accountRepository = accountRepository;
        this.batchRepository = batchRepository;
        this.productunitRepository = productunitRepository;
    }

    // ------------------------------------------------------------------ list / search

    @Transactional(readOnly = true)
    public Page<StockAdjustmentListItemResponse> search(String keyword,
                                                        String fromDate,
                                                        String toDate,
                                                        String adjustmentType,
                                                        String status,
                                                        Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);
        final LocalDate from = parseDate(fromDate);
        final LocalDate to = parseDate(toDate);

        List<Stockadjustment> adjustments = stockadjustmentRepository.findAllWithRelations();
        List<Stockadjustmentdetail> allDetails = stockadjustmentdetailRepository.findAllWithRelations();

        Map<Integer, List<Stockadjustmentdetail>> detailMap = allDetails.stream()
                .filter(detail -> detail.getStockAdjustmentID() != null)
                .collect(Collectors.groupingBy(detail -> detail.getStockAdjustmentID().getId()));

        List<StockAdjustmentListItemResponse> filtered = adjustments.stream()
                .filter(adj -> matchesKeyword(adj, detailMap.getOrDefault(adj.getId(), List.of()), normalizedKeyword))
                .filter(adj -> matchesDate(adj, from, to))
                .filter(adj -> adjustmentType == null || adjustmentType.isBlank()
                        || adjustmentType.equals(adj.getAdjustmentType()))
                .filter(adj -> status == null || status.isBlank() || isStatus(getStatusName(adj), status))
                .map(adj -> toListItem(adj, detailMap.getOrDefault(adj.getId(), List.of())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<StockAdjustmentListItemResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public StockAdjustmentStatsResponse getStats() {
        List<Stockadjustment> adjustments = stockadjustmentRepository.findAllWithRelations();

        YearMonth currentMonth = YearMonth.now();

        long monthlyCount = adjustments.stream()
                .filter(adj -> adj.getDate() != null)
                .filter(adj -> YearMonth.from(toLocalDate(adj.getDate())).equals(currentMonth))
                .count();

        long pendingCount = countByStatusName(adjustments, StockAdjustmentStatus.PENDING);
        long approvedCount = countByStatusName(adjustments, StockAdjustmentStatus.APPROVED);
        long rejectedCount = countByStatusName(adjustments, StockAdjustmentStatus.REJECTED);

        return new StockAdjustmentStatsResponse(monthlyCount, pendingCount, approvedCount, rejectedCount);
    }

    /** The fixed set of statuses, in workflow order, for the filter dropdown. */
    @Transactional(readOnly = true)
    public List<String> listStatuses() {
        return StockAdjustmentStatus.ALL;
    }

    public Map<String, String> adjustmentTypeLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("DESTROY", "Hủy hàng");
        labels.put("INTERNAL_USE", "Sử dụng nội bộ");
        labels.put("SAMPLE", "Hàng mẫu");
        labels.put("GIFT", "Quà tặng");
        labels.put("COUNT_INCREASE", "Tăng theo kiểm kê");
        labels.put("COUNT_DECREASE", "Giảm theo kiểm kê");
        return labels;
    }

    // ------------------------------------------------------------------ detail

    @Transactional(readOnly = true)
    public StockAdjustmentDetailPageResponse getDetail(Integer adjustmentId) {
        Stockadjustment adjustment = stockadjustmentRepository.findByIdWithRelations(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chỉnh kho"));

        List<Stockadjustmentdetail> details =
                stockadjustmentdetailRepository.findByStockOutIdWithRelations(adjustmentId);

        List<StockAdjustmentDetailItemResponse> itemResponses = details.stream()
                .map(this::toDetailItem)
                .toList();

        long totalItems = details.size();

        int totalQuantity = details.stream()
                .map(Stockadjustmentdetail::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal estimatedValue = details.stream()
                .map(Stockadjustmentdetail::getLineCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String statusName = getStatusName(adjustment);

        boolean itemsChecked = totalItems > 0;
        boolean batchChecked = totalItems > 0;
        boolean valueChecked;
        boolean approvalChecked;

        if (isStatus(statusName, StockAdjustmentStatus.APPROVED)) {
            valueChecked = true;
            approvalChecked = true;
        } else if (isStatus(statusName, StockAdjustmentStatus.REJECTED)) {
            valueChecked = false;
            approvalChecked = false;
        } else {
            valueChecked = estimatedValue.compareTo(BigDecimal.ZERO) > 0;
            approvalChecked = false;
        }

        int done = 0;
        if (itemsChecked) done++;
        if (batchChecked) done++;
        if (valueChecked) done++;
        if (approvalChecked) done++;

        int total = 4;
        int percent = done * 100 / total;

        return new StockAdjustmentDetailPageResponse(
                adjustment.getId(),
                formatCode(adjustment.getId()),
                adjustment.getDate(),
                formatInstant(adjustment.getDate()),
                adjustment.getAdjustmentType(),
                formatAdjustmentType(adjustment.getAdjustmentType()),
                adjustment.getCreatedBy() != null ? adjustment.getCreatedBy().getName() : "Không rõ",
                adjustment.getApprovedBy() != null ? adjustment.getApprovedBy().getName() : "Chưa có",
                formatInstant(adjustment.getApprovedAt()),
                adjustment.getReason(),
                adjustment.getNote(),
                statusName,
                statusCssClass(statusName),
                totalItems,
                totalQuantity,
                estimatedValue,
                costImpactDisplay(adjustment),
                done,
                total,
                percent,
                itemsChecked,
                batchChecked,
                valueChecked,
                approvalChecked,
                itemResponses
        );
    }

    // ------------------------------------------------------------------ approve / reject

    /**
     * Owner approves a pending slip: records the approver and <strong>applies the stock effect</strong>
     * (deduct for outbound types, add for COUNT_INCREASE) in the same transaction.
     */
    @Transactional
    public void approve(Integer adjustmentId, Integer approverAccountId) {
        Stockadjustment adjustment = stockadjustmentRepository.findByIdWithRelations(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chỉnh kho"));

        if (!isStatus(getStatusName(adjustment), StockAdjustmentStatus.PENDING)) {
            throw new IllegalArgumentException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ duyệt");
        }

        Account approver = accountRepository.findById(approverAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        applyStockEffect(adjustmentId);

        adjustment.setStatus(StockAdjustmentStatus.APPROVED);
        adjustment.setApprovedBy(approver);
        adjustment.setApprovedAt(Instant.now());
        // TODO(finance): auto-create an Expense (and link expenseID) for DESTROY with lineCost total > 0.
        //   Deferred — the Expense entity/vocabulary (expenseCode, expenseType, status) is owned by the
        //   finance module; wire it here once that contract is agreed.

        stockadjustmentRepository.save(adjustment);
    }

    @Transactional
    public void reject(Integer adjustmentId, Integer approverAccountId) {
        Stockadjustment adjustment = stockadjustmentRepository.findByIdWithRelations(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chỉnh kho"));

        if (!isStatus(getStatusName(adjustment), StockAdjustmentStatus.PENDING)) {
            throw new IllegalArgumentException("Chỉ có thể từ chối phiếu đang ở trạng thái chờ duyệt");
        }

        Account approver = accountRepository.findById(approverAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        adjustment.setStatus(StockAdjustmentStatus.REJECTED);
        adjustment.setApprovedBy(approver);
        adjustment.setApprovedAt(Instant.now());

        stockadjustmentRepository.save(adjustment);
    }

    /**
     * Applies each line to its batch: outbound lines deduct (blocking negative stock), inbound lines
     * add. Uses {@code baseQtyDeducted} (already in base units) and the line {@code direction}.
     */
    private void applyStockEffect(Integer adjustmentId) {
        List<Stockadjustmentdetail> details =
                stockadjustmentdetailRepository.findByStockOutIdWithRelations(adjustmentId);

        for (Stockadjustmentdetail detail : details) {
            Batch batchRef = detail.getBatchID();
            if (batchRef == null || batchRef.getId() == null) {
                continue;
            }

            Batch batch = batchRepository.findById(batchRef.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lô hàng cần điều chỉnh"));

            int current = batch.getStorageQuantity() == null ? 0 : batch.getStorageQuantity();
            int delta = detail.getBaseQtyDeducted() == null ? 0 : detail.getBaseQtyDeducted();

            if (DIRECTION_IN.equalsIgnoreCase(detail.getDirection())) {
                batch.setStorageQuantity(current + delta);
            } else {
                if (delta > current) {
                    throw new IllegalArgumentException(
                            "Tồn kho không đủ để điều chỉnh lô " + displayBatch(batch)
                                    + " (tồn hiện tại: " + current + ")");
                }
                batch.setStorageQuantity(current - delta);
            }

            batchRepository.save(batch);
        }
    }

    // ------------------------------------------------------------------ create

    @Transactional(readOnly = true)
    public List<StockAdjustmentBatchCandidateResponse> listAvailableBatches(String keyword) {
        String normalizedKeyword = normalize(keyword);

        return batchRepository.findAvailableBatchesForDestroy()
                .stream()
                .filter(batch -> matchesBatchKeyword(batch, normalizedKeyword))
                .map(this::toCandidateResponse)
                .toList();
    }

    /**
     * Creates a DESTROY adjustment. When {@code autoApprove} is true (the Owner is creating), the
     * slip is approved immediately and the stock effect is applied; otherwise it is left
     * {@link StockAdjustmentStatus#PENDING} for the Owner to approve.
     */
    @Transactional
    public Integer createDestroyAdjustment(StockAdjustmentCreateRequest request,
                                           Integer currentAccountId,
                                           boolean autoApprove) {
        validateRequest(request);

        Account creator = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        Map<Integer, StockAdjustmentItemRequest> itemMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        StockAdjustmentItemRequest::getBatchId,
                        item -> item,
                        (first, second) -> {
                            first.setQuantity(first.getQuantity() + second.getQuantity());

                            String firstReason = first.getReason() == null ? "" : first.getReason();
                            String secondReason = second.getReason() == null ? "" : second.getReason();

                            if (!secondReason.isBlank() && !firstReason.contains(secondReason)) {
                                first.setReason(firstReason.isBlank() ? secondReason : firstReason + ", " + secondReason);
                            }

                            return first;
                        }
                ));

        List<Batch> selectedBatches = batchRepository.findAllById(itemMap.keySet());
        if (selectedBatches.size() != itemMap.size()) {
            throw new IllegalArgumentException("Một số lô hàng không tồn tại");
        }
        validateSelectedBatches(selectedBatches, itemMap);

        Stockadjustment adjustment = new Stockadjustment();
        adjustment.setStockAdjustmentCode(generateCode());
        adjustment.setAdjustmentType("DESTROY");
        adjustment.setDate(Instant.now());
        adjustment.setCreatedBy(creator);
        adjustment.setReason(request.getReason().trim());
        adjustment.setExpenseID(null);
        adjustment.setStatus(autoApprove ? StockAdjustmentStatus.APPROVED : StockAdjustmentStatus.PENDING);
        adjustment.setNote(trimToNull(request.getNote()));
        if (autoApprove) {
            adjustment.setApprovedBy(creator);
            adjustment.setApprovedAt(Instant.now());
        }

        Stockadjustment savedAdjustment = stockadjustmentRepository.save(adjustment);

        for (Batch batch : selectedBatches) {
            StockAdjustmentItemRequest item = itemMap.get(batch.getId());

            Product product = batch.getProductID();
            Productunit unit = resolveUnit(batch, product);

            BigDecimal unitCost = resolveUnitCost(batch);
            BigDecimal lineCost = unitCost.multiply(BigDecimal.valueOf(item.getQuantity()));

            Stockadjustmentdetail detail = new Stockadjustmentdetail();
            detail.setStockAdjustmentID(savedAdjustment);
            detail.setProductID(product);
            detail.setProductUnitID(unit);
            detail.setBatchID(batch);
            detail.setDirection(DIRECTION_OUT);
            detail.setQuantity(item.getQuantity());
            detail.setBaseQtyDeducted(item.getQuantity());
            detail.setUnitCostPrice(unitCost);
            detail.setLineCost(lineCost);
            detail.setNote(trimToNull(item.getReason()));

            stockadjustmentdetailRepository.save(detail);
        }

        if (autoApprove) {
            applyStockEffect(savedAdjustment.getId());
        }

        return savedAdjustment.getId();
    }

    private void validateRequest(StockAdjustmentCreateRequest request) {
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một hàng hóa cần hủy");
        }
        for (StockAdjustmentItemRequest item : request.getItems()) {
            if (item.getBatchId() == null) {
                throw new IllegalArgumentException("Dữ liệu lô hàng không hợp lệ");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng hủy phải lớn hơn 0");
            }
        }
    }

    private void validateSelectedBatches(List<Batch> selectedBatches,
                                         Map<Integer, StockAdjustmentItemRequest> itemMap) {
        for (Batch batch : selectedBatches) {
            StockAdjustmentItemRequest item = itemMap.get(batch.getId());

            if (!Boolean.TRUE.equals(batch.getStatus())) {
                throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " không còn hoạt động");
            }
            if (batch.getStorageQuantity() == null || batch.getStorageQuantity() <= 0) {
                throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " đã hết tồn kho");
            }
            if (item.getQuantity() > batch.getStorageQuantity()) {
                throw new IllegalArgumentException(
                        "Số lượng hủy của lô " + displayBatch(batch)
                                + " không được vượt quá tồn hiện tại: " + batch.getStorageQuantity());
            }
        }
    }

    // ------------------------------------------------------------------ mapping helpers

    private StockAdjustmentListItemResponse toListItem(Stockadjustment adjustment, List<Stockadjustmentdetail> details) {
        BigDecimal estimatedValue = details.stream()
                .map(Stockadjustmentdetail::getLineCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String statusName = getStatusName(adjustment);

        return new StockAdjustmentListItemResponse(
                adjustment.getId(),
                formatCode(adjustment.getId()),
                adjustment.getDate(),
                formatInstant(adjustment.getDate()),
                adjustment.getAdjustmentType(),
                formatAdjustmentType(adjustment.getAdjustmentType()),
                adjustment.getCreatedBy() != null ? adjustment.getCreatedBy().getName() : "Không rõ",
                details.size(),
                estimatedValue,
                statusName,
                statusCssClass(statusName)
        );
    }

    private StockAdjustmentDetailItemResponse toDetailItem(Stockadjustmentdetail detail) {
        Product product = detail.getProductID();
        Productunit unit = detail.getProductUnitID();
        Batch batch = detail.getBatchID();

        return new StockAdjustmentDetailItemResponse(
                product != null ? product.getProductID() : null,
                product != null ? product.getName() : "Không rõ",
                batch != null ? batch.getLotNumber() : "",
                batch != null ? batch.getExpirationDate() : null,
                batch != null && batch.getExpirationDate() != null
                        ? batch.getExpirationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "",
                unit != null ? unit.getUnitName() : "",
                detail.getQuantity(),
                detail.getUnitCostPrice(),
                detail.getLineCost(),
                detail.getNote()
        );
    }

    private StockAdjustmentBatchCandidateResponse toCandidateResponse(Batch batch) {
        Product product = batch.getProductID();
        Productunit unit = resolveCandidateUnit(batch, product);

        return new StockAdjustmentBatchCandidateResponse(
                batch.getId(),
                product != null ? product.getProductID() : null,
                product != null ? product.getName() : "Không rõ",
                batch.getLotNumber(),
                batch.getExpirationDate(),
                formatLocalDate(batch.getExpirationDate()),
                batch.getStorageQuantity(),
                unit != null ? unit.getId() : null,
                unit != null ? unit.getUnitName() : "Đơn vị",
                resolveUnitCost(batch)
        );
    }

    // ------------------------------------------------------------------ unit / cost resolution

    private Productunit resolveUnit(Batch batch, Product product) {
        Productunit baseUnit = findBaseUnit(product).orElse(null);
        if (baseUnit != null) {
            return baseUnit;
        }
        if (batch.getImportUnitID() != null) {
            return batch.getImportUnitID();
        }
        Productunit preferredUnit = findPreferredUnit(product).orElse(null);
        if (preferredUnit != null) {
            return preferredUnit;
        }
        throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " chưa có đơn vị tính");
    }

    private Productunit resolveCandidateUnit(Batch batch, Product product) {
        Productunit baseUnit = findBaseUnit(product).orElse(null);
        if (baseUnit != null) {
            return baseUnit;
        }
        if (batch.getImportUnitID() != null) {
            return batch.getImportUnitID();
        }
        return findPreferredUnit(product).orElse(null);
    }

    private BigDecimal resolveUnitCost(Batch batch) {
        if (batch.getImportPricePerBase() != null) {
            return batch.getImportPricePerBase();
        }
        if (batch.getImportPrice() != null) {
            return batch.getImportPrice();
        }
        return BigDecimal.ZERO;
    }

    private Optional<Productunit> findBaseUnit(Product product) {
        if (product == null || product.getProductID() == null) {
            return Optional.empty();
        }
        return productunitRepository.findByProductId(product.getProductID())
                .stream()
                .filter(unit -> !Boolean.FALSE.equals(unit.getIsActive()))
                .filter(unit -> Boolean.TRUE.equals(unit.getIsBaseUnit()))
                .findFirst();
    }

    private Optional<Productunit> findPreferredUnit(Product product) {
        if (product == null || product.getProductID() == null) {
            return Optional.empty();
        }
        return productunitRepository.findByProductId(product.getProductID())
                .stream()
                .filter(unit -> !Boolean.FALSE.equals(unit.getIsActive()))
                .sorted(Comparator
                        .comparingInt(this::unitPriority)
                        .thenComparing(unit -> unit.getId() == null ? Integer.MAX_VALUE : unit.getId()))
                .findFirst();
    }

    private int unitPriority(Productunit unit) {
        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            return 0;
        }
        if (Boolean.TRUE.equals(unit.getIsDefault())) {
            return 1;
        }
        return 2;
    }

    // ------------------------------------------------------------------ filtering / formatting

    private boolean matchesKeyword(Stockadjustment adjustment,
                                   List<Stockadjustmentdetail> details,
                                   String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        if (containsNormalized(formatCode(adjustment.getId()), normalizedKeyword)
                || containsNormalized(adjustment.getReason(), normalizedKeyword)
                || containsNormalized(formatAdjustmentType(adjustment.getAdjustmentType()), normalizedKeyword)
                || containsNormalized(getStatusName(adjustment), normalizedKeyword)
                || containsNormalized(adjustment.getCreatedBy() != null ? adjustment.getCreatedBy().getName() : null, normalizedKeyword)) {
            return true;
        }
        return details.stream().anyMatch(detail -> {
            Product product = detail.getProductID();
            return product != null
                    && (containsNormalized(String.valueOf(product.getProductID()), normalizedKeyword)
                    || containsNormalized(product.getName(), normalizedKeyword)
                    || containsNormalized(product.getCode(), normalizedKeyword)
                    || containsNormalized(product.getBarcode(), normalizedKeyword));
        });
    }

    private boolean matchesBatchKeyword(Batch batch, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        Product product = batch.getProductID();
        return containsNormalized(batch.getLotNumber(), keyword)
                || product != null && (
                containsNormalized(String.valueOf(product.getProductID()), keyword)
                        || containsNormalized(product.getName(), keyword)
                        || containsNormalized(product.getCode(), keyword)
                        || containsNormalized(product.getBarcode(), keyword));
    }

    private boolean matchesDate(Stockadjustment adjustment, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        if (adjustment.getDate() == null) {
            return false;
        }
        LocalDate date = toLocalDate(adjustment.getDate());
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private long countByStatusName(List<Stockadjustment> adjustments, String statusName) {
        return adjustments.stream()
                .filter(adj -> isStatus(getStatusName(adj), statusName))
                .count();
    }

    private String getStatusName(Stockadjustment adjustment) {
        return adjustment.getStatus() != null ? adjustment.getStatus() : "Không rõ";
    }

    private boolean isStatus(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String statusCssClass(String statusName) {
        String normalized = normalize(statusName);
        if (normalized.contains(normalize(StockAdjustmentStatus.APPROVED))) {
            return "status-approved";
        }
        if (normalized.contains(normalize(StockAdjustmentStatus.REJECTED))) {
            return "status-rejected";
        }
        if (normalized.contains(normalize(StockAdjustmentStatus.PENDING))) {
            return "status-draft";
        }
        return "status-default";
    }

    private String costImpactDisplay(Stockadjustment adjustment) {
        if (adjustment.getExpenseID() != null) {
            return "Có ghi nhận chi phí";
        }
        return "Chưa ghi nhận chi phí";
    }

    private String formatAdjustmentType(String type) {
        if (type == null) {
            return "Không rõ";
        }
        return switch (type) {
            case "DESTROY" -> "Hủy hàng";
            case "INTERNAL_USE" -> "Sử dụng nội bộ";
            case "SAMPLE" -> "Hàng mẫu";
            case "GIFT" -> "Quà tặng";
            case "COUNT_INCREASE" -> "Tăng theo kiểm kê";
            case "COUNT_DECREASE" -> "Giảm theo kiểm kê";
            default -> type;
        };
    }

    private String formatCode(Integer id) {
        if (id == null) {
            return "PDC-000000";
        }
        return "PDC-" + String.format("%06d", id);
    }

    private String generateCode() {
        int nextId = stockadjustmentRepository.findAll().stream()
                .map(Stockadjustment::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return "PDC-" + String.format("%06d", nextId);
    }

    private String displayBatch(Batch batch) {
        Product product = batch.getProductID();
        String productName = product != null ? product.getName() : "Không rõ sản phẩm";
        String lotNumber = batch.getLotNumber() != null ? batch.getLotNumber() : "Không có số lô";
        return productName + " - " + lotNumber;
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

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private boolean containsNormalized(String value, String normalizedKeyword) {
        return value != null && normalize(value).contains(normalizedKeyword);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
}
