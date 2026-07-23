package com.example.project.service;

import com.example.project.constant.ExpenseStatus;
import com.example.project.constant.ExpenseType;
import com.example.project.dto.request.ExpenseCreateRequest;
import com.example.project.dto.response.ExpenseDetailResponse;
import com.example.project.dto.response.ExpenseListItemResponse;
import com.example.project.dto.response.ExpenseResponse;
import com.example.project.dto.response.ExpenseStatsResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Expense;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Expense ("Phiếu chi") — a standalone cash-outflow control screen. V1 scope (agreed with the
 * user 2026-07-23): plain manual entry, no auto-triggering from Return/PurchaseInvoice/
 * StockAdjustment even though those modules have optional FK slots for it — see
 * {@code StockadjustmentService}'s own deferred TODO on this and project memory
 * {@code expense-price-settings-open-questions} for the open cross-module question.
 *
 * <p>Workflow mirrors {@code StockadjustmentService}'s draft/submit/approve/reject shape, plus a
 * payment step ({@link ExpenseStatus#AWAITING_PAYMENT} → {@link ExpenseStatus#COMPLETED}) since an
 * Expense tracks real cash leaving the register, not just an approval.</p>
 */
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountRepository accountRepository;

    public ExpenseService(ExpenseRepository expenseRepository, AccountRepository accountRepository) {
        this.expenseRepository = expenseRepository;
        this.accountRepository = accountRepository;
    }

    // ------------------------------------------------------------------ generated-REST passthrough

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll() {
        return expenseRepository.findAll()
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    // ------------------------------------------------------------------ list / search

    @Transactional(readOnly = true)
    public Page<ExpenseListItemResponse> search(String keyword,
                                                 String fromDate,
                                                 String toDate,
                                                 String expenseType,
                                                 String status,
                                                 Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);
        final LocalDate from = parseDate(fromDate);
        final LocalDate to = parseDate(toDate);

        List<Expense> expenses = expenseRepository.findAll();

        List<ExpenseListItemResponse> filtered = expenses.stream()
                .filter(expense -> matchesKeyword(expense, normalizedKeyword))
                .filter(expense -> matchesDate(expense, from, to))
                .filter(expense -> expenseType == null || expenseType.isBlank()
                        || expenseType.equals(expense.getExpenseType()))
                .filter(expense -> status == null || status.isBlank() || status.equals(expense.getStatus()))
                .sorted((a, b) -> {
                    Instant da = a.getDate();
                    Instant db = b.getDate();
                    if (da == null || db == null) {
                        return 0;
                    }
                    return db.compareTo(da);
                })
                .map(this::toListItem)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<ExpenseListItemResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public ExpenseStatsResponse getStats() {
        List<Expense> expenses = expenseRepository.findAll();
        YearMonth currentMonth = YearMonth.now();

        List<Expense> thisMonth = expenses.stream()
                .filter(expense -> expense.getDate() != null)
                .filter(expense -> YearMonth.from(toLocalDate(expense.getDate())).equals(currentMonth))
                .toList();

        // Cancelled slips don't count as real cash out (same convention as
        // PurchaseinvoiceService.getStats() excluding cancelled invoices from its totals).
        BigDecimal monthlyPaidTotal = thisMonth.stream()
                .filter(expense -> !ExpenseStatus.CANCELLED.equals(expense.getStatus()))
                .map(Expense::getPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount = countByStatus(expenses, ExpenseStatus.PENDING);
        long awaitingPaymentCount = countByStatus(expenses, ExpenseStatus.AWAITING_PAYMENT);

        return new ExpenseStatsResponse(thisMonth.size(), monthlyPaidTotal, pendingCount, awaitingPaymentCount);
    }

    public List<String> listStatuses() {
        return ExpenseStatus.ALL;
    }

    public Map<String, String> expenseTypeLabels() {
        return ExpenseType.vietnameseLabels();
    }

    // ------------------------------------------------------------------ detail

    @Transactional(readOnly = true)
    public ExpenseDetailResponse getDetail(Integer expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu chi"));
        return toDetail(expense);
    }

    // ------------------------------------------------------------------ create

    /**
     * Creates a new expense slip. When {@code asDraft} is true it is saved as
     * {@link ExpenseStatus#DRAFT} regardless of role. Otherwise: the Owner's slip is auto-approved
     * (status resolved straight to {@link ExpenseStatus#AWAITING_PAYMENT} or
     * {@link ExpenseStatus#COMPLETED} depending on whether it's fully paid); anyone else's goes to
     * {@link ExpenseStatus#PENDING} for the Owner to approve.
     */
    @Transactional
    public Integer createExpense(ExpenseCreateRequest request, Integer currentAccountId, boolean isOwner,
                                  boolean asDraft) {
        // Every NOT NULL column (expenseType/reason/amount) must have a real value even for a
        // draft — unlike Stock Adjustment's items, Expense has no field that's genuinely optional
        // at the DB level, so "draft" only means "not yet sent for approval", not "incomplete data".
        validateRequest(request);

        Account applicant = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        Expense expense = new Expense();
        expense.setApplicantID(applicant);
        expense.setExpenseType(resolveExpenseType(request.getExpenseType()));
        expense.setDate(resolveDate(request.getDate()));
        expense.setReason(request.getReason() != null ? request.getReason().trim() : "");
        expense.setAmount(request.getAmount());
        expense.setNote(trimToNull(request.getNote()));

        BigDecimal paid = resolvePaid(request);
        BigDecimal[] split = resolveSplit(request, paid);
        expense.setPaid(paid);
        expense.setPaidByCash(split[0]);
        expense.setPaidByBanking(split[1]);

        if (asDraft) {
            expense.setStatus(ExpenseStatus.DRAFT);
        } else if (isOwner) {
            applyApproval(expense, applicant);
        } else {
            expense.setStatus(ExpenseStatus.PENDING);
        }

        expense.setExpenseCode(generateCode());
        Expense saved = expenseRepository.save(expense);
        // Re-stamp the human-facing code from the real generated id (matches Stock Adjustment/
        // Purchase Invoice convention: the placeholder above only reserves a slot in sequence).
        saved.setExpenseCode(formatCode(saved.getId()));
        return expenseRepository.save(saved).getId();
    }

    /** Sends a {@link ExpenseStatus#DRAFT} slip forward, same shape as {@code StockadjustmentService#submit}. */
    @Transactional
    public void submit(Integer expenseId, Integer currentAccountId, boolean isOwner) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu chi"));

        if (!ExpenseStatus.DRAFT.equals(expense.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể gửi duyệt phiếu đang ở trạng thái nháp");
        }

        Account actor = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        if (isOwner) {
            applyApproval(expense, actor);
        } else {
            expense.setStatus(ExpenseStatus.PENDING);
        }

        expenseRepository.save(expense);
    }

    @Transactional
    public void approve(Integer expenseId, Integer approverAccountId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu chi"));

        if (!ExpenseStatus.PENDING.equals(expense.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ duyệt");
        }

        Account approver = accountRepository.findById(approverAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        applyApproval(expense, approver);
        expenseRepository.save(expense);
    }

    @Transactional
    public void reject(Integer expenseId, Integer approverAccountId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu chi"));

        if (!ExpenseStatus.PENDING.equals(expense.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối phiếu đang ở trạng thái chờ duyệt");
        }

        Account approver = accountRepository.findById(approverAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setApprovedAt(Instant.now());
        expenseRepository.save(expense);
        // approver identity for a rejection isn't modeled separately from approvedAt/status;
        // Expense has no dedicated "rejectedBy" column (see Pharmacy-Database-Description.docx).
    }

    /**
     * Records an additional payment against an {@link ExpenseStatus#AWAITING_PAYMENT} slip.
     * {@code cashPortion}/{@code bankingPortion} are the amount being paid <em>now</em> (not the
     * cumulative total) — they're added to whatever was already paid. Moves to
     * {@link ExpenseStatus#COMPLETED} once {@code paid >= amount}.
     */
    @Transactional
    public void markPaid(Integer expenseId, BigDecimal cashPortion, BigDecimal bankingPortion) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu chi"));

        if (!ExpenseStatus.AWAITING_PAYMENT.equals(expense.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể ghi nhận thanh toán cho phiếu đang chờ thanh toán");
        }

        BigDecimal cash = cashPortion != null ? cashPortion : BigDecimal.ZERO;
        BigDecimal banking = bankingPortion != null ? bankingPortion : BigDecimal.ZERO;
        BigDecimal portion = cash.add(banking);

        if (portion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        BigDecimal previouslyPaid = expense.getPaid() != null ? expense.getPaid() : BigDecimal.ZERO;
        BigDecimal newPaid = previouslyPaid.add(portion);

        if (newPaid.compareTo(expense.getAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền thanh toán vượt quá số tiền cần chi còn lại");
        }

        expense.setPaid(newPaid);
        expense.setPaidByCash(nullToZero(expense.getPaidByCash()).add(cash));
        expense.setPaidByBanking(nullToZero(expense.getPaidByBanking()).add(banking));

        if (newPaid.compareTo(expense.getAmount()) >= 0) {
            expense.setStatus(ExpenseStatus.COMPLETED);
        }

        expenseRepository.save(expense);
    }

    /**
     * Internal correction for a wrongly-entered slip — same spirit as
     * {@code PurchaseinvoiceService.cancelPurchaseInvoice()}: not a real accounting reversal, just
     * marks the record void. Only allowed before it's fully paid.
     */
    @Transactional
    public void cancel(Integer expenseId, String reason) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu chi"));

        if (ExpenseStatus.COMPLETED.equals(expense.getStatus())) {
            throw new IllegalArgumentException("Không thể hủy phiếu chi đã hoàn thành");
        }
        if (ExpenseStatus.CANCELLED.equals(expense.getStatus())) {
            throw new IllegalArgumentException("Phiếu chi này đã bị hủy trước đó");
        }

        expense.setStatus(ExpenseStatus.CANCELLED);
        String trimmedReason = trimToNull(reason);
        if (trimmedReason != null) {
            String existingNote = expense.getNote();
            expense.setNote(existingNote == null || existingNote.isBlank()
                    ? "Lý do hủy: " + trimmedReason
                    : existingNote + " | Lý do hủy: " + trimmedReason);
        }

        expenseRepository.save(expense);
    }

    // ------------------------------------------------------------------ mapping

    private ExpenseListItemResponse toListItem(Expense expense) {
        return new ExpenseListItemResponse(
                expense.getId(),
                formatCode(expense.getId()),
                formatInstant(expense.getDate()),
                expense.getExpenseType(),
                ExpenseType.vietnameseName(expense.getExpenseType()),
                expense.getApplicantID() != null ? expense.getApplicantID().getName() : "Không rõ",
                expense.getAmount(),
                expense.getPaid(),
                expense.getStatus(),
                statusCssClass(expense.getStatus())
        );
    }

    private ExpenseDetailResponse toDetail(Expense expense) {
        return new ExpenseDetailResponse(
                expense.getId(),
                formatCode(expense.getId()),
                formatInstant(expense.getDate()),
                expense.getExpenseType(),
                ExpenseType.vietnameseName(expense.getExpenseType()),
                expense.getApplicantID() != null ? expense.getApplicantID().getName() : "Không rõ",
                expense.getReason(),
                expense.getAmount(),
                expense.getPaid(),
                expense.getPaidByCash(),
                expense.getPaidByBanking(),
                expense.getStatus(),
                statusCssClass(expense.getStatus()),
                approverName(expense),
                formatInstant(expense.getApprovedAt()),
                expense.getNote()
        );
    }

    private String approverName(Expense expense) {
        // Expense has no dedicated "approvedBy" FK (only approvedAt) — see docx. Nothing to show yet.
        return expense.getApprovedAt() != null ? "Chủ nhà thuốc" : "Chưa có";
    }

    // ------------------------------------------------------------------ helpers

    private void applyApproval(Expense expense, Account approver) {
        expense.setApprovedAt(Instant.now());
        BigDecimal paid = expense.getPaid() != null ? expense.getPaid() : BigDecimal.ZERO;
        expense.setStatus(paid.compareTo(expense.getAmount()) >= 0
                ? ExpenseStatus.COMPLETED
                : ExpenseStatus.AWAITING_PAYMENT);
    }

    private String resolveExpenseType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại phiếu chi");
        }
        String type = rawType.trim().toUpperCase(Locale.ROOT);
        if (!ExpenseType.isValid(type)) {
            throw new IllegalArgumentException("Loại phiếu chi không hợp lệ");
        }
        return type;
    }

    private Instant resolveDate(String rawDate) {
        LocalDate date = parseDate(rawDate);
        LocalDate resolved = date != null ? date : LocalDate.now();
        return resolved.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private BigDecimal resolvePaid(ExpenseCreateRequest request) {
        if (request.isFullyPaid()) {
            return request.getAmount();
        }
        BigDecimal paid = request.getPaid() != null ? request.getPaid() : BigDecimal.ZERO;
        if (paid.compareTo(BigDecimal.ZERO) < 0 || paid.compareTo(request.getAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền đã chi phải nằm trong khoảng 0 đến tổng số tiền cần chi");
        }
        return paid;
    }

    /** Returns {@code [paidByCash, paidByBanking]}, defaulting an unsplit amount entirely to cash. */
    private BigDecimal[] resolveSplit(ExpenseCreateRequest request, BigDecimal paid) {
        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }
        BigDecimal cash = request.getPaidByCash();
        BigDecimal banking = request.getPaidByBanking();
        if (cash == null && banking == null) {
            return new BigDecimal[]{paid, BigDecimal.ZERO};
        }
        cash = nullToZero(cash);
        banking = nullToZero(banking);
        if (cash.add(banking).setScale(2, RoundingMode.HALF_UP)
                .compareTo(paid.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("Tiền mặt + chuyển khoản phải bằng số tiền đã chi");
        }
        return new BigDecimal[]{cash, banking};
    }

    private void validateRequest(ExpenseCreateRequest request) {
        if (request.getExpenseType() == null || request.getExpenseType().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại phiếu chi");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do chi");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền cần chi phải lớn hơn 0");
        }
    }

    private boolean matchesKeyword(Expense expense, String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        return containsNormalized(formatCode(expense.getId()), normalizedKeyword)
                || containsNormalized(expense.getReason(), normalizedKeyword)
                || containsNormalized(expense.getApplicantID() != null ? expense.getApplicantID().getName() : null,
                        normalizedKeyword);
    }

    private boolean matchesDate(Expense expense, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        if (expense.getDate() == null) {
            return false;
        }
        LocalDate date = toLocalDate(expense.getDate());
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private long countByStatus(List<Expense> expenses, String status) {
        return expenses.stream().filter(expense -> status.equals(expense.getStatus())).count();
    }

    private String statusCssClass(String status) {
        if (status == null) {
            return "status-default";
        }
        if (ExpenseStatus.DRAFT.equals(status)) {
            return "status-draft";
        }
        if (ExpenseStatus.PENDING.equals(status)) {
            return "status-pending";
        }
        if (ExpenseStatus.REJECTED.equals(status)) {
            return "status-rejected";
        }
        if (ExpenseStatus.AWAITING_PAYMENT.equals(status)) {
            return "status-awaiting";
        }
        if (ExpenseStatus.COMPLETED.equals(status)) {
            return "status-approved";
        }
        if (ExpenseStatus.CANCELLED.equals(status)) {
            return "status-rejected";
        }
        return "status-default";
    }

    private String formatCode(Integer id) {
        if (id == null) {
            return "PC-000000";
        }
        return "PC-" + String.format("%06d", id);
    }

    private String generateCode() {
        int nextId = expenseRepository.findAll().stream()
                .map(Expense::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return "PC-" + String.format("%06d", nextId);
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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
