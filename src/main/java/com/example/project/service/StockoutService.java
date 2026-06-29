package com.example.project.service;

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
public class StockoutService {

    @Transactional(readOnly = true)
    public List<StockoutResponse> getAll() {
        return stockoutRepository.findAllWithRelations()
                .stream()
                .map(StockoutResponse::from)
                .toList();
    }

    private static final String STATUS_PENDING_RECONCILIATION = "Chờ đối chiếu";
    private static final String STATUS_RECONCILED = "Đã đối chiếu";
    private static final String STATUS_NEED_CHECK = "Cần kiểm tra";

    private final StockoutRepository stockoutRepository;
    private final StockoutdetailRepository stockoutdetailRepository;
    private final StatusRepository statusRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;

    public StockoutService(StockoutRepository stockoutRepository,
                           StockoutdetailRepository stockoutdetailRepository,
                           StatusRepository statusRepository,
                           AccountRepository accountRepository,
                           BranchRepository branchRepository) {
        this.stockoutRepository = stockoutRepository;
        this.stockoutdetailRepository = stockoutdetailRepository;
        this.statusRepository = statusRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public Page<StockOutListItemResponse> searchStockOuts(String keyword,
                                                          String fromDate,
                                                          String toDate,
                                                          String outType,
                                                          Integer branchId,
                                                          Integer statusId,
                                                          Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);
        final LocalDate from = parseDate(fromDate);
        final LocalDate to = parseDate(toDate);

        List<Stockout> stockOuts = stockoutRepository.findAllWithRelations();
        List<Stockoutdetail> allDetails = stockoutdetailRepository.findAllWithRelations();

        Map<Integer, List<Stockoutdetail>> detailMap = allDetails.stream()
                .filter(detail -> detail.getStockOutID() != null)
                .collect(Collectors.groupingBy(detail -> detail.getStockOutID().getId()));

        List<StockOutListItemResponse> filtered = stockOuts.stream()
                .filter(stockOut -> matchesKeyword(stockOut, detailMap.getOrDefault(stockOut.getId(), List.of()), normalizedKeyword))
                .filter(stockOut -> matchesDate(stockOut, from, to))
                .filter(stockOut -> outType == null || outType.isBlank() || outType.equals(stockOut.getOutType()))
                .filter(stockOut -> branchId == null || branchMatches(stockOut, branchId))
                .filter(stockOut -> statusId == null || statusMatches(stockOut, statusId))
                .map(stockOut -> toListItem(stockOut, detailMap.getOrDefault(stockOut.getId(), List.of())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<StockOutListItemResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public StockOutStatsResponse getStats() {
        List<Stockout> stockOuts = stockoutRepository.findAllWithRelations();

        YearMonth currentMonth = YearMonth.now();

        long monthlyCount = stockOuts.stream()
                .filter(stockOut -> stockOut.getDate() != null)
                .filter(stockOut -> YearMonth.from(toLocalDate(stockOut.getDate())).equals(currentMonth))
                .count();

        long pendingCount = countByStatusName(stockOuts, STATUS_PENDING_RECONCILIATION);
        long reconciledCount = countByStatusName(stockOuts, STATUS_RECONCILED);
        long needCheckCount = countByStatusName(stockOuts, STATUS_NEED_CHECK);

        return new StockOutStatsResponse(
                monthlyCount,
                pendingCount,
                reconciledCount,
                needCheckCount
        );
    }

    @Transactional(readOnly = true)
    public StockOutDetailPageResponse getDetail(Integer stockOutId) {
        Stockout stockOut = stockoutRepository.findByIdWithRelations(stockOutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất kho"));

        List<Stockoutdetail> details =
                stockoutdetailRepository.findByStockOutIdWithRelations(stockOutId);

        List<StockOutDetailItemResponse> itemResponses = details.stream()
                .map(this::toDetailItem)
                .toList();

        long totalItems = details.size();

        int totalQuantity = details.stream()
                .map(Stockoutdetail::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal estimatedValue = details.stream()
                .map(Stockoutdetail::getLineCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String statusName = getStatusName(stockOut);

        boolean sourceChecked;
        boolean targetChecked;
        boolean valueChecked;
        boolean reasonChecked;

        if (isStatus(statusName, STATUS_RECONCILED)) {
            sourceChecked = true;
            targetChecked = true;
            valueChecked = true;
            reasonChecked = true;
        } else if (isStatus(statusName, STATUS_NEED_CHECK)) {
            sourceChecked = true;
            targetChecked = stockOut.getTargetBranchID() != null || !"INTERNAL_TRANSFER".equals(stockOut.getOutType());
            valueChecked = false;
            reasonChecked = false;
        } else {
            sourceChecked = stockOut.getBranchID() != null;
            targetChecked = stockOut.getTargetBranchID() != null || !"INTERNAL_TRANSFER".equals(stockOut.getOutType());
            valueChecked = estimatedValue.compareTo(BigDecimal.ZERO) > 0;
            reasonChecked = false;
        }

        int done = 0;
        if (sourceChecked) done++;
        if (targetChecked) done++;
        if (valueChecked) done++;
        if (reasonChecked) done++;

        int total = 4;
        int percent = done * 100 / total;

        return new StockOutDetailPageResponse(
                stockOut.getId(),
                formatStockOutCode(stockOut.getId()),
                stockOut.getDate(),
                formatInstant(stockOut.getDate()),
                stockOut.getOutType(),
                formatOutType(stockOut.getOutType()),
                stockOut.getBranchID() != null ? stockOut.getBranchID().getName() : "Không có",
                stockOut.getTargetBranchID() != null ? stockOut.getTargetBranchID().getName() : "Không áp dụng",
                stockOut.getCreatedBy() != null ? stockOut.getCreatedBy().getName() : "Không rõ",
                stockOut.getApprovedBy() != null ? stockOut.getApprovedBy().getName() : "Chưa có",
                formatInstant(stockOut.getApprovedAt()),
                stockOut.getReason(),
                stockOut.getNote(),
                stockOut.getStatusID() != null ? stockOut.getStatusID().getId() : null,
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
    public void markAsReconciled(Integer stockOutId, Integer currentAccountId) {
        Stockout stockOut = stockoutRepository.findByIdWithRelations(stockOutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất kho"));

        Account account = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        Status reconciledStatus = statusRepository.findByNameIgnoreCase(STATUS_RECONCILED)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạng thái: " + STATUS_RECONCILED));

        stockOut.setStatusID(reconciledStatus);
        stockOut.setApprovedBy(account);
        stockOut.setApprovedAt(Instant.now());

        stockoutRepository.save(stockOut);
    }

    @Transactional
    public void requestCheck(Integer stockOutId, Integer currentAccountId) {
        Stockout stockOut = stockoutRepository.findByIdWithRelations(stockOutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất kho"));

        Status needCheckStatus = statusRepository.findByNameIgnoreCase(STATUS_NEED_CHECK)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạng thái: " + STATUS_NEED_CHECK));

        stockOut.setStatusID(needCheckStatus);

        if (stockOut.getNote() == null || stockOut.getNote().isBlank()) {
            stockOut.setNote("Đã gửi yêu cầu kiểm tra lại phiếu xuất kho.");
        } else {
            stockOut.setNote(stockOut.getNote() + "\nĐã gửi yêu cầu kiểm tra lại phiếu xuất kho.");
        }

        stockoutRepository.save(stockOut);
    }

    @Transactional(readOnly = true)
    public List<Branch> listBranches() {
        return branchRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(branch -> branch.getName() == null ? "" : branch.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Status> listStatuses() {
        return statusRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(status -> status.getName() == null ? "" : status.getName()))
                .toList();
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

    private StockOutListItemResponse toListItem(Stockout stockOut, List<Stockoutdetail> details) {
        BigDecimal estimatedValue = details.stream()
                .map(Stockoutdetail::getLineCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String statusName = getStatusName(stockOut);

        return new StockOutListItemResponse(
                stockOut.getId(),
                formatStockOutCode(stockOut.getId()),
                stockOut.getDate(),
                formatInstant(stockOut.getDate()),
                stockOut.getOutType(),
                formatOutType(stockOut.getOutType()),
                stockOut.getBranchID() != null ? stockOut.getBranchID().getName() : "Không có",
                stockOut.getTargetBranchID() != null ? stockOut.getTargetBranchID().getName() : "Không áp dụng",
                stockOut.getCreatedBy() != null ? stockOut.getCreatedBy().getName() : "Không rõ",
                details.size(),
                estimatedValue,
                stockOut.getStatusID() != null ? stockOut.getStatusID().getId() : null,
                statusName,
                statusCssClass(statusName)
        );
    }

    private StockOutDetailItemResponse toDetailItem(Stockoutdetail detail) {
        Product product = detail.getProductID();
        Productunit unit = detail.getProductUnitID();
        Batch batch = detail.getBatchID();

        return new StockOutDetailItemResponse(
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

    private boolean matchesKeyword(Stockout stockOut,
                                   List<Stockoutdetail> details,
                                   String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }

        if (containsNormalized(formatStockOutCode(stockOut.getId()), normalizedKeyword)
                || containsNormalized(stockOut.getReason(), normalizedKeyword)
                || containsNormalized(formatOutType(stockOut.getOutType()), normalizedKeyword)
                || containsNormalized(getStatusName(stockOut), normalizedKeyword)
                || containsNormalized(stockOut.getBranchID() != null ? stockOut.getBranchID().getName() : null, normalizedKeyword)
                || containsNormalized(stockOut.getTargetBranchID() != null ? stockOut.getTargetBranchID().getName() : null, normalizedKeyword)
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

    private boolean matchesDate(Stockout stockOut, LocalDate from, LocalDate to) {
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

    private boolean branchMatches(Stockout stockOut, Integer branchId) {
        Integer sourceBranchId = stockOut.getBranchID() != null ? stockOut.getBranchID().getId() : null;
        Integer targetBranchId = stockOut.getTargetBranchID() != null ? stockOut.getTargetBranchID().getId() : null;

        return branchId.equals(sourceBranchId) || branchId.equals(targetBranchId);
    }

    private boolean statusMatches(Stockout stockOut, Integer statusId) {
        return stockOut.getStatusID() != null && statusId.equals(stockOut.getStatusID().getId());
    }

    private long countByStatusName(List<Stockout> stockOuts, String statusName) {
        return stockOuts.stream()
                .filter(stockOut -> isStatus(getStatusName(stockOut), statusName))
                .count();
    }

    private String getStatusName(Stockout stockOut) {
        return stockOut.getStatusID() != null ? stockOut.getStatusID().getName() : "Không rõ";
    }

    private boolean isStatus(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String statusCssClass(String statusName) {
        String normalized = normalize(statusName);

        if (normalized.contains(normalize(STATUS_RECONCILED))) {
            return "status-reconciled";
        }

        if (normalized.contains(normalize(STATUS_NEED_CHECK))) {
            return "status-need-check";
        }

        if (normalized.contains(normalize(STATUS_PENDING_RECONCILIATION))) {
            return "status-pending";
        }

        return "status-default";
    }

    private String costImpactDisplay(Stockout stockOut) {
        if ("INTERNAL_TRANSFER".equals(stockOut.getOutType())) {
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