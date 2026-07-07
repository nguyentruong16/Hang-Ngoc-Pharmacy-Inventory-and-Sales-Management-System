package com.example.project.service;

import com.example.project.constant.StockOutStatus;
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
public class StockadjustmentService {

    @Transactional(readOnly = true)
    public List<StockadjustmentResponse> getAll() {
        return stockadjustmentRepository.findAllWithRelations()
                .stream()
                .map(StockadjustmentResponse::from)
                .toList();
    }

    private final StockadjustmentRepository stockadjustmentRepository;
    private final StockadjustmentdetailRepository stockadjustmentdetailRepository;
    private final AccountRepository accountRepository;

    public StockadjustmentService(StockadjustmentRepository stockadjustmentRepository,
                                  StockadjustmentdetailRepository stockadjustmentdetailRepository,
                                  AccountRepository accountRepository) {
        this.stockadjustmentRepository = stockadjustmentRepository;
        this.stockadjustmentdetailRepository = stockadjustmentdetailRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<StockAdjustmentListItemResponse> searchStockOuts(String keyword,
                                                                 String fromDate,
                                                                 String toDate,
                                                                 String outType,
                                                                 Integer branchId,
                                                                 String status,
                                                                 Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);
        final LocalDate from = parseDate(fromDate);
        final LocalDate to = parseDate(toDate);

        List<Stockadjustment> stockOuts = stockadjustmentRepository.findAllWithRelations();
        List<Stockadjustmentdetail> allDetails = stockadjustmentdetailRepository.findAllWithRelations();

        Map<Integer, List<Stockadjustmentdetail>> detailMap = allDetails.stream()
                .filter(detail -> detail.getStockAdjustmentID() != null)
                .collect(Collectors.groupingBy(detail -> detail.getStockAdjustmentID().getId()));

        List<StockAdjustmentListItemResponse> filtered = stockOuts.stream()
                .filter(stockOut -> matchesKeyword(stockOut, detailMap.getOrDefault(stockOut.getId(), List.of()), normalizedKeyword))
                .filter(stockOut -> matchesDate(stockOut, from, to))
                .filter(stockOut -> outType == null || outType.isBlank() || outType.equals(stockOut.getAdjustmentType()))
                .filter(stockOut -> status == null || status.isBlank() || isStatus(getStatusName(stockOut), status))
                .map(stockOut -> toListItem(stockOut, detailMap.getOrDefault(stockOut.getId(), List.of())))
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
        List<Stockadjustment> stockOuts = stockadjustmentRepository.findAllWithRelations();

        YearMonth currentMonth = YearMonth.now();

        long monthlyCount = stockOuts.stream()
                .filter(stockOut -> stockOut.getDate() != null)
                .filter(stockOut -> YearMonth.from(toLocalDate(stockOut.getDate())).equals(currentMonth))
                .count();

        long draftCount = countByStatusName(stockOuts, StockOutStatus.DRAFT);
        long approvedCount = countByStatusName(stockOuts, StockOutStatus.APPROVED);
        long rejectedCount = countByStatusName(stockOuts, StockOutStatus.REJECTED);

        return new StockAdjustmentStatsResponse(
                monthlyCount,
                draftCount,
                approvedCount,
                rejectedCount
        );
    }

    @Transactional(readOnly = true)
    public StockAdjustmentDetailPageResponse getDetail(Integer stockOutId) {
        Stockadjustment stockOut = stockadjustmentRepository.findByIdWithRelations(stockOutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất kho"));

        List<Stockadjustmentdetail> details =
                stockadjustmentdetailRepository.findByStockOutIdWithRelations(stockOutId);

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

        String statusName = getStatusName(stockOut);

        boolean sourceChecked;
        boolean targetChecked;
        boolean valueChecked;
        boolean reasonChecked;

        if (isStatus(statusName, StockOutStatus.APPROVED)) {
            sourceChecked = true;
            targetChecked = true;
            valueChecked = true;
            reasonChecked = true;
        } else if (isStatus(statusName, StockOutStatus.REJECTED)) {
            sourceChecked = true;
            targetChecked = !"INTERNAL_TRANSFER".equals(stockOut.getAdjustmentType());            valueChecked = false;
            reasonChecked = false;
        } else {
            sourceChecked = true;
            targetChecked = !"INTERNAL_TRANSFER".equals(stockOut.getAdjustmentType());            valueChecked = estimatedValue.compareTo(BigDecimal.ZERO) > 0;
            reasonChecked = false;
        }

        int done = 0;
        if (sourceChecked) done++;
        if (targetChecked) done++;
        if (valueChecked) done++;
        if (reasonChecked) done++;

        int total = 4;
        int percent = done * 100 / total;

        return new StockAdjustmentDetailPageResponse(
                stockOut.getId(),
                formatStockOutCode(stockOut.getId()),
                stockOut.getDate(),
                formatInstant(stockOut.getDate()),
                stockOut.getAdjustmentType(),
                formatOutType(stockOut.getAdjustmentType()),
                stockOut.getCreatedBy() != null ? stockOut.getCreatedBy().getName() : "Không rõ",
                stockOut.getApprovedBy() != null ? stockOut.getApprovedBy().getName() : "Chưa có",
                formatInstant(stockOut.getApprovedAt()),
                stockOut.getReason(),
                stockOut.getNote(),
                statusName,
                statusCssClass(statusName),
                totalItems,
                totalQuantity,
                estimatedValue,
                costImpactDisplay(stockOut),
                done,
                total,
                percent,
                sourceChecked,
                targetChecked,
                valueChecked,
                reasonChecked,
                itemResponses
        );
    }

    @Transactional
    public void approve(Integer stockOutId, Integer currentAccountId) {
        Stockadjustment stockOut = stockadjustmentRepository.findByIdWithRelations(stockOutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất kho"));

        Account account = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        stockOut.setStatus(StockOutStatus.APPROVED);
        stockOut.setApprovedBy(account);
        stockOut.setApprovedAt(Instant.now());

        stockadjustmentRepository.save(stockOut);
    }

    @Transactional
    public void reject(Integer stockOutId, Integer currentAccountId) {
        Stockadjustment stockOut = stockadjustmentRepository.findByIdWithRelations(stockOutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất kho"));

        stockOut.setStatus(StockOutStatus.REJECTED);

        if (stockOut.getNote() == null || stockOut.getNote().isBlank()) {
            stockOut.setNote("Đã từ chối phiếu xuất kho.");
        } else {
            stockOut.setNote(stockOut.getNote() + "\nĐã từ chối phiếu xuất kho.");
        }

        stockadjustmentRepository.save(stockOut);
    }

    @Transactional(readOnly = true)
    public List<Object> listBranches() {
        return List.of();
    }

    /** The fixed set of statuses a StockOut can be in, in workflow order, for the filter dropdown. */
    @Transactional(readOnly = true)
    public List<String> listStatuses() {
        return StockOutStatus.ALL;
    }

    public Map<String, String> outTypeLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("DESTROY", "Hủy hàng");
        labels.put("INTERNAL_TRANSFER", "Chuyển chi nhánh");
        labels.put("INTERNAL_USE", "Sử dụng nội bộ");
        labels.put("SAMPLE", "Hàng mẫu");
        labels.put("GIFT", "Quà tặng");
        return labels;
    }

    private StockAdjustmentListItemResponse toListItem(Stockadjustment stockOut, List<Stockadjustmentdetail> details) {
        BigDecimal estimatedValue = details.stream()
                .map(Stockadjustmentdetail::getLineCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String statusName = getStatusName(stockOut);

        return new StockAdjustmentListItemResponse(
                stockOut.getId(),
                formatStockOutCode(stockOut.getId()),
                stockOut.getDate(),
                formatInstant(stockOut.getDate()),
                stockOut.getAdjustmentType(),
                formatOutType(stockOut.getAdjustmentType()),
                stockOut.getCreatedBy() != null ? stockOut.getCreatedBy().getName() : "Không rõ",
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

    private boolean matchesKeyword(Stockadjustment stockOut,
                                   List<Stockadjustmentdetail> details,
                                   String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }

        if (containsNormalized(formatStockOutCode(stockOut.getId()), normalizedKeyword)
                || containsNormalized(stockOut.getReason(), normalizedKeyword)
                || containsNormalized(formatOutType(stockOut.getAdjustmentType()), normalizedKeyword)
                || containsNormalized(getStatusName(stockOut), normalizedKeyword)
                || containsNormalized(stockOut.getCreatedBy() != null ? stockOut.getCreatedBy().getName() : null, normalizedKeyword)) {
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

    private boolean matchesDate(Stockadjustment stockOut, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }

        if (stockOut.getDate() == null) {
            return false;
        }

        LocalDate date = toLocalDate(stockOut.getDate());

        if (from != null && date.isBefore(from)) {
            return false;
        }

        return to == null || !date.isAfter(to);
    }

    private long countByStatusName(List<Stockadjustment> stockOuts, String statusName) {
        return stockOuts.stream()
                .filter(stockOut -> isStatus(getStatusName(stockOut), statusName))
                .count();
    }

    private String getStatusName(Stockadjustment stockOut) {
        return stockOut.getStatus() != null ? stockOut.getStatus() : "Không rõ";
    }

    private boolean isStatus(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String statusCssClass(String statusName) {
        String normalized = normalize(statusName);

        if (normalized.contains(normalize(StockOutStatus.APPROVED))) {
            return "status-approved";
        }

        if (normalized.contains(normalize(StockOutStatus.REJECTED))) {
            return "status-rejected";
        }

        if (normalized.contains(normalize(StockOutStatus.DRAFT))) {
            return "status-draft";
        }

        return "status-default";
    }

    private String costImpactDisplay(Stockadjustment stockOut) {
        if ("INTERNAL_TRANSFER".equals(stockOut.getAdjustmentType())) {
            return "Không ghi nhận chi phí trực tiếp";
        }

        if (stockOut.getExpenseID() != null) {
            return "Có ghi nhận chi phí";
        }

        return "Chưa ghi nhận chi phí";
    }

    private String formatOutType(String outType) {
        if (outType == null) {
            return "Không rõ";
        }

        return switch (outType) {
            case "DESTROY" -> "Hủy hàng";
            case "INTERNAL_TRANSFER" -> "Chuyển chi nhánh";
            case "INTERNAL_USE" -> "Sử dụng nội bộ";
            case "SAMPLE" -> "Hàng mẫu";
            case "GIFT" -> "Quà tặng";
            default -> outType;
        };
    }

    private String formatStockOutCode(Integer id) {
        if (id == null) {
            return "SO-000000";
        }
        return "SO-" + String.format("%06d", id);
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }

        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant);
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