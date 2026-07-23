package com.example.project.service;

import com.example.project.dto.response.CustomerOptionResponse;
import com.example.project.dto.response.IncomeListItemResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Income;
import com.example.project.repository.IncomeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class IncomeService {

    private static final String STATUS_DRAFT = "Nháp";
    private static final String STATUS_PENDING = "Chờ duyệt";
    private static final String STATUS_APPROVED = "Duyệt";
    private static final String STATUS_REJECTED = "Từ chối";

    private static final String TYPE_SUPPLIER = "SUPPLIER";
    private static final String TYPE_EMPLOYEE = "EMPLOYEE";
    private static final String TYPE_CUSTOMER = "CUSTOMER";
    private static final String TYPE_OTHER = "OTHER";

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Transactional(readOnly = true)
    public Page<IncomeListItemResponse> list(String search,
                                       String fromDate,
                                       String toDate,
                                       String incomeType,
                                       String status,
                                       Integer applicantId,
                                       Pageable pageable) {
        String normalizedKeyword = normalize(search);
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        String normalizedStatus = status == null ? "" : status.trim();

        List<IncomeListItemResponse> filtered = incomeRepository.findAllWithRelations().stream()
                .filter(income -> matchesKeyword(income, normalizedKeyword))
                .filter(income -> matchesDate(income, from, to))
                .filter(income -> incomeType == null || incomeType.isBlank()
                        || incomeType.equals(resolveIncomeType(income)))
                .filter(income -> applicantId == null || matchesApplicant(income, applicantId))
                .filter(income -> normalizedStatus.isEmpty()
                        || normalize(normalizedStatus).equals(normalize(income.getStatus())))
                .sorted(Comparator.comparing(Income::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Income::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toListItem)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<IncomeListItemResponse> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public List<String> listStatuses() {
        return incomeRepository.findAll().stream()
                .map(Income::getStatus)
                .filter(item -> item != null && !item.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerOptionResponse> listApplicants() {
        Map<Integer, String> byId = new LinkedHashMap<>();
        for (Income income : incomeRepository.findAllWithRelations()) {
            Account applicant = income.getApplicantID();
            if (applicant != null && applicant.getId() != null) {
                byId.putIfAbsent(applicant.getId(), applicant.getName());
            }
        }
        return byId.entrySet().stream()
                .map(entry -> new CustomerOptionResponse(entry.getKey(), entry.getValue(), null))
                .sorted(Comparator.comparing(CustomerOptionResponse::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public Map<String, String> incomeTypeLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put(TYPE_SUPPLIER, "Nhà cung cấp");
        labels.put(TYPE_EMPLOYEE, "Nhân viên");
        labels.put(TYPE_CUSTOMER, "Khách hàng");
        labels.put(TYPE_OTHER, "Khác");
        return labels;
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return incomeRepository.count();
    }

    @Transactional(readOnly = true)
    public long countToday() {
        LocalDate today = LocalDate.now();
        return incomeRepository.findAll().stream()
                .filter(income -> income.getDate() != null && toLocalDate(income.getDate()).equals(today))
                .count();
    }

    @Transactional(readOnly = true)
    public BigDecimal sumTodayAmount() {
        LocalDate today = LocalDate.now();
        return incomeRepository.findAll().stream()
                .filter(income -> income.getDate() != null && toLocalDate(income.getDate()).equals(today))
                .map(Income::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return countByStatus(STATUS_PENDING);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumPendingAmount() {
        return sumAmountByStatus(STATUS_PENDING);
    }

    @Transactional(readOnly = true)
    public long countApproved() {
        return countByStatus(STATUS_APPROVED);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumApprovedAmount() {
        return sumAmountByStatus(STATUS_APPROVED);
    }

    private long countByStatus(String status) {
        return incomeRepository.findAll().stream()
                .filter(income -> isStatus(income.getStatus(), status))
                .count();
    }

    private BigDecimal sumAmountByStatus(String status) {
        return incomeRepository.findAll().stream()
                .filter(income -> isStatus(income.getStatus(), status))
                .map(Income::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private IncomeListItemResponse toListItem(Income income) {
        String statusName = income.getStatus() != null ? income.getStatus() : "Không rõ";
        return new IncomeListItemResponse(
                income.getId(),
                income.getIncomeCode(),
                income.getDate(),
                formatInstant(income.getDate()),
                formatIncomeType(resolveIncomeType(income)),
                displayReason(income),
                income.getApplicantID() != null ? income.getApplicantID().getName() : "Không rõ",
                income.getAmount(),
                paymentDisplay(income.getPaidByCash(), income.getPaidByBanking()),
                statusName,
                statusCssClass(statusName));
    }

    private String displayReason(Income income) {
        if (income.getReason() == null || income.getReason().isBlank()) {
            return "—";
        }
        return income.getReason();
    }

    /** {@code incomeType} by party collected from; falls back to FK hints when DB value is unknown. */
    private String resolveIncomeType(Income income) {
        String stored = income.getIncomeType();
        if (stored != null && incomeTypeLabels().containsKey(stored)) {
            return stored;
        }
        if (income.getSupplierID() != null) {
            return TYPE_SUPPLIER;
        }
        if (income.getCustomerID() != null) {
            return TYPE_CUSTOMER;
        }
        if (income.getAccountID() != null) {
            return TYPE_EMPLOYEE;
        }
        return TYPE_OTHER;
    }

    private String referenceCode(Income income) {
        if (income.getInvoiceID() != null && income.getInvoiceID().getInvoiceNumber() != null) {
            return income.getInvoiceID().getInvoiceNumber();
        }
        if (income.getReturnID() != null && income.getReturnID().getReturnCode() != null) {
            return income.getReturnID().getReturnCode();
        }
        if (income.getShiftReportID() != null && income.getShiftReportID().getShiftReportCode() != null) {
            return income.getShiftReportID().getShiftReportCode();
        }
        return "—";
    }

    private String paymentDisplay(BigDecimal paidByCash, BigDecimal paidByBanking) {
        boolean hasCash = paidByCash != null && paidByCash.compareTo(BigDecimal.ZERO) > 0;
        boolean hasBanking = paidByBanking != null && paidByBanking.compareTo(BigDecimal.ZERO) > 0;
        if (hasCash && hasBanking) {
            return "TM + CK";
        }
        if (hasBanking) {
            return "Chuyển khoản";
        }
        if (hasCash) {
            return "Tiền mặt";
        }
        return "—";
    }

    private boolean matchesKeyword(Income income, String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        return containsNormalized(income.getIncomeCode(), normalizedKeyword)
                || containsNormalized(income.getReason(), normalizedKeyword)
                || containsNormalized(formatIncomeType(resolveIncomeType(income)), normalizedKeyword)
                || containsNormalized(income.getStatus(), normalizedKeyword)
                || containsNormalized(referenceCode(income), normalizedKeyword)
                || containsNormalized(
                        income.getApplicantID() != null ? income.getApplicantID().getName() : null,
                        normalizedKeyword);
    }

    private boolean matchesDate(Income income, LocalDate from, LocalDate to) {
        if (income.getDate() == null) {
            return from == null && to == null;
        }
        LocalDate date = toLocalDate(income.getDate());
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private boolean matchesApplicant(Income income, Integer applicantId) {
        return income.getApplicantID() != null && applicantId.equals(income.getApplicantID().getId());
    }

    private boolean isStatus(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String statusCssClass(String statusName) {
        if (isStatus(statusName, STATUS_APPROVED)) {
            return "status-approved";
        }
        if (isStatus(statusName, STATUS_REJECTED)) {
            return "status-rejected";
        }
        if (isStatus(statusName, STATUS_PENDING)) {
            return "status-pending";
        }
        if (isStatus(statusName, STATUS_DRAFT)) {
            return "status-draft";
        }
        return "status-default";
    }

    private String formatIncomeType(String type) {
        if (type == null) {
            return "Không rõ";
        }
        return incomeTypeLabels().getOrDefault(type, type);
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
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
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
