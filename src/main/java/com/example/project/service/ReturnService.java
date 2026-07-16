package com.example.project.service;

import com.example.project.constant.ReturnStatus;
import com.example.project.dto.request.ReturnCreateRequest;
import com.example.project.dto.request.ReturnLineRequest;
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
 * Single service for the <em>customer</em> return feature: listing/searching, detail, creation and
 * the approve/reject workflow. Supplier returns (via {@code purchaseID}) are a later phase.
 *
 * <p>Statuses (see {@link ReturnStatus}): Nháp → Chờ duyệt → Nợ / Từ chối. There is no "Duyệt"
 * state — an approved return becomes a payable ("Nợ") because the pharmacy now owes the customer the
 * not-yet-paid refund. A Pharmacist submits to {@code Chờ duyệt}; the Owner approves to {@code Nợ}
 * (and Owner-created slips auto-approve straight to {@code Nợ}).</p>
 *
 * <p><strong>Approval changes stock</strong> (unlike the stock-adjustment slip): each restockable
 * line is returned into a brand-new batch cloned from the one it was sold from — returned goods are
 * kept in their own batch for traceability (there is no "returned" flag on {@code batch}). The
 * original invoice's {@code returnStatus} and each line's {@code returnedQty} are updated in the same
 * transaction. The cash payout itself lives on a separate Expense (phiếu chi), handled later; this
 * service only records the refund amounts on the slip.</p>
 */
@Service
public class ReturnService {

    /** A customer return is allowed only within this many days of the original invoice date. */
    private static final int RETURN_WINDOW_DAYS = 3;

    // Invoice.date is stored as VN wall-clock LocalDateTime (see InvoiceService) — the adjustment
    // invoice's own date must use the same convention, not a real UTC Instant.
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /** Only completed sale invoices are returnable. Matched accent/case-insensitively. */
    private static final String INVOICE_STATUS_COMPLETED = "Hoàn thành";
    // A signed invoice ("Đã ký") has been pushed to the tax authority — it must NOT be edited, so a
    // return against it emits an adjustment invoice (TH2) instead of touching the original.
    private static final String INVOICE_STATUS_SIGNED = "Đã ký";
    // The sale invoice no longer has a separate returnStatus column (removed by DB) — the return
    // state is written back into invoice.status using these values, per the DB owner (2026-07-14).
    private static final String INVOICE_STATUS_RETURNED_FULL = "Đã trả hàng toàn bộ";
    private static final String INVOICE_STATUS_RETURNED_PARTIAL = "Đã trả hàng 1 phần";

    // Only sale invoices are returnable. DB invoiceType: Bán hàng/Điều chỉnh — a return must not be
    // opened against an adjustment invoice (the negative slip emitted by TH2).
    private static final String INVOICE_TYPE_NORMAL = "Bán hàng";
    /** Legacy DB value before invoiceType was stored in Vietnamese. */
    private static final String INVOICE_TYPE_NORMAL_LEGACY = "normal";
    private static final String INVOICE_TYPE_ADJUSTMENT = "Điều chỉnh";
    /** Legacy DB value before invoiceType was stored in Vietnamese. */
    private static final String INVOICE_TYPE_ADJUSTMENT_LEGACY = "adjustment";

    private static final String INVOICE_RETURN_NONE = "NONE";
    private static final String INVOICE_RETURN_PARTIAL = "PARTIAL";
    private static final String INVOICE_RETURN_FULL = "FULL";

    private static final String TYPE_CASH = "CASH";
    private static final String TYPE_BANKING = "BANKING";
    private static final String TYPE_DEBT = "DEBT";

    // Product types (Type.sortType / Type.name) that cannot be returned, per BA 2026-07-09. Compared
    // accent/case-insensitively against normalize(...). Medical-device "máy" carries a warranty so it
    // is handled via warranty, not return; a combo is a bundle and is not taken back.
    private static final String SORT_COMBO = "combo";
    private static final String SORT_MEDICAL_DEVICE = "thiet bi y te";
    private static final String DEVICE_MACHINE_MARK = "may";

    private final ReturnRepository returnRepository;
    private final ReturndetailRepository returndetailRepository;
    private final AccountRepository accountRepository;
    private final BatchRepository batchRepository;
    // Sales invoice / detail rows are read for the TH1 flow; for TH2 (signed invoice) we also *create*
    // an adjustment invoice row here (see createAdjustmentInvoice) — mirroring how cloneReturnBatch
    // creates a Batch — without calling into InvoiceService.
    private final InvoiceRepository invoiceRepository;
    private final InvoicedetailRepository invoicedetailRepository;
    // Read-only: current tax revenue group (Nhóm 2/3) drives how the refund VAT is split.
    private final FinancialsettingRepository financialsettingRepository;

    public ReturnService(ReturnRepository returnRepository,
                         ReturndetailRepository returndetailRepository,
                         AccountRepository accountRepository,
                         BatchRepository batchRepository,
                         InvoiceRepository invoiceRepository,
                         InvoicedetailRepository invoicedetailRepository,
                         FinancialsettingRepository financialsettingRepository) {
        this.returnRepository = returnRepository;
        this.returndetailRepository = returndetailRepository;
        this.accountRepository = accountRepository;
        this.batchRepository = batchRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoicedetailRepository = invoicedetailRepository;
        this.financialsettingRepository = financialsettingRepository;
    }

    // ------------------------------------------------------------------ list / search

    @Transactional(readOnly = true)
    public Page<ReturnListItemResponse> search(String keyword,
                                               String fromDate,
                                               String toDate,
                                               String returnType,
                                               String status,
                                               Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);
        final LocalDate from = parseDate(fromDate);
        final LocalDate to = parseDate(toDate);

        List<Return> returns = returnRepository.findAllWithRelations();
        Map<Integer, List<Returndetail>> detailMap = returndetailRepository.findAllWithRelations().stream()
                .filter(detail -> detail.getReturnID() != null)
                .collect(Collectors.groupingBy(detail -> detail.getReturnID().getId()));

        List<ReturnListItemResponse> filtered = returns.stream()
                // Exclude supplier returns (purchaseID set) — they share the table but have their own screens.
                .filter(ret -> ret.getInvoiceID() != null)
                .filter(ret -> matchesKeyword(ret, detailMap.getOrDefault(ret.getId(), List.of()), normalizedKeyword))
                .filter(ret -> matchesDate(ret, from, to))
                .filter(ret -> returnType == null || returnType.isBlank() || returnType.equals(ret.getReturnType()))
                .filter(ret -> status == null || status.isBlank() || isStatus(getStatusName(ret), status))
                .sorted(Comparator.comparing(Return::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ret -> toListItem(ret, detailMap.getOrDefault(ret.getId(), List.of())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ReturnListItemResponse> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public ReturnStatsResponse getStats() {
        List<Return> returns = returnRepository.findAllWithRelations().stream()
                .filter(ret -> ret.getInvoiceID() != null)
                .toList();
        YearMonth currentMonth = YearMonth.now();

        long monthlyCount = returns.stream()
                .filter(ret -> ret.getReturnDate() != null)
                .filter(ret -> YearMonth.from(toLocalDate(ret.getReturnDate())).equals(currentMonth))
                .count();

        return new ReturnStatsResponse(
                monthlyCount,
                countByStatus(returns, ReturnStatus.DRAFT),
                countByStatus(returns, ReturnStatus.PENDING),
                countByStatus(returns, ReturnStatus.DEBT),
                countByStatus(returns, ReturnStatus.REJECTED));
    }

    public List<String> listStatuses() {
        return ReturnStatus.ALL;
    }

    /** Refund-method code → Vietnamese label, in dropdown order. */
    public Map<String, String> returnTypeLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put(TYPE_CASH, "Tiền mặt");
        labels.put(TYPE_BANKING, "Chuyển khoản");
        labels.put(TYPE_DEBT, "Trừ công nợ");
        return labels;
    }

    // ------------------------------------------------------------------ create screen sources

    /**
     * Sale invoices a customer may still return against: completed, within the return window and not
     * already fully returned. Read-only over the Invoice module.
     */
    @Transactional(readOnly = true)
    public List<ReturnableInvoiceResponse> listReturnableInvoices(String keyword) {
        String normalizedKeyword = normalize(keyword);

        return invoiceRepository.findAll().stream()
                .filter(this::isReturnable)
                .filter(invoice -> matchesInvoiceKeyword(invoice, normalizedKeyword))
                .sorted(Comparator.comparing(Invoice::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(invoice -> new ReturnableInvoiceResponse(
                        invoice.getId(),
                        invoice.getInvoicePattern(),
                        formatLocalDateTime(invoice.getDate()),
                        invoice.getEmployeeID() != null ? invoice.getEmployeeID().getName() : "Không rõ",
                        invoice.getCustomerID() != null ? invoice.getCustomerID().getName() : "Khách lẻ",
                        invoice.getTotal(),
                        returnStatusDisplay(invoiceReturnCode(invoice))))
                .toList();
    }

    /** The still-returnable lines of one invoice, for the create screen (JSON). */
    @Transactional(readOnly = true)
    public List<ReturnInvoiceLineResponse> loadInvoiceLines(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        return invoiceLinesOf(invoice.getId()).stream()
                .filter(line -> isReturnableProductType(line.getProductID()))
                .map(this::toInvoiceLine)
                .filter(line -> line.getReturnableQty() != null && line.getReturnableQty() > 0)
                .toList();
    }

    /**
     * Whether a product may be returned at all, by its {@link Type}. Blocks combos and the
     * medical-device "máy" sub-type (warranty items); other medical devices (có hạn / không hạn) and
     * all drugs / goods are allowed. Unknown/missing type → allowed (don't over-block).
     */
    private boolean isReturnableProductType(Product product) {
        if (product == null || product.getTypeID() == null) {
            return true;
        }
        Type type = product.getTypeID();
        String sort = normalize(type.getSortType());
        String name = normalize(type.getName());
        if (SORT_COMBO.equals(sort)) {
            return false;
        }
        return !(SORT_MEDICAL_DEVICE.equals(sort) && name.contains(DEVICE_MACHINE_MARK));
    }

    // ------------------------------------------------------------------ create

    /**
     * Creates one customer-return slip from a chosen completed invoice.
     *
     * <p>Resulting status: {@code asDraft} → Nháp; otherwise the Owner auto-approves to Nợ and a
     * Pharmacist submits to Chờ duyệt. When the slip lands in Nợ, stock is restored and the invoice's
     * return status is updated (see {@link #applyReturnEffect}).</p>
     *
     * @return the id of the created slip, for the redirect.
     */
    @Transactional
    public Integer createReturn(ReturnCreateRequest request,
                                Integer currentAccountId,
                                boolean isOwner,
                                boolean asDraft) {
        if (request.getInvoiceId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn hóa đơn cần trả");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do trả hàng");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        assertReturnable(invoice);

        Account creator = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        Map<Integer, Invoicedetail> lineById = invoiceLinesOf(invoice.getId()).stream()
                .collect(Collectors.toMap(Invoicedetail::getId, line -> line, (a, b) -> a));

        // Collapse the posted rows onto the invoice lines, keeping only positive, validated quantities.
        Map<Integer, PreparedLine> prepared = new LinkedHashMap<>();
        for (ReturnLineRequest item : request.getItems()) {
            if (item == null || item.getInvoiceDetailId() == null
                    || item.getReturnQty() == null || item.getReturnQty() <= 0) {
                continue;
            }
            Invoicedetail line = lineById.get(item.getInvoiceDetailId());
            if (line == null) {
                throw new IllegalArgumentException("Dòng hóa đơn không thuộc hóa đơn đã chọn");
            }
            if (!isReturnableProductType(line.getProductID())) {
                throw new IllegalArgumentException("Sản phẩm \"" + productName(line)
                        + "\" không được phép trả (thiết bị y tế máy hoặc combo)");
            }
            int alreadyReturned = line.getReturnedQty() != null ? line.getReturnedQty() : 0;
            int returnable = line.getQuantity() - alreadyReturned;
            int qty = item.getReturnQty();
            if (qty > returnable) {
                throw new IllegalArgumentException("Số lượng trả của \"" + productName(line)
                        + "\" vượt quá số còn có thể trả (" + returnable + ")");
            }
            // Restockable is hard-coded by item type (BA 2026-07-12): only the manufacturer's default
            // packaging unit (productunit.isDefault) goes back to stock; loose units do not. No manual
            // checkbox — the client value is ignored. (Combo / medical-device "máy" / prescription
            // invoices are already blocked from return upstream.)
            boolean restockable = isRestockableUnit(line);
            prepared.put(line.getId(), preparedLineOf(line, qty, restockable));
        }

        if (prepared.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một dòng hàng cần trả");
        }

        BigDecimal totalRefund = prepared.values().stream()
                .map(PreparedLine::lineRefund)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVATRefund = prepared.values().stream()
                .map(PreparedLine::vatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String returnType = resolveReturnType(request.getReturnType());
        String status = asDraft ? ReturnStatus.DRAFT : (isOwner ? ReturnStatus.DEBT : ReturnStatus.PENDING);
        boolean approvedNow = ReturnStatus.DEBT.equals(status);

        Return ret = new Return();
        ret.setReturnCode(generateCode());
        ret.setInvoiceID(invoice);
        ret.setPurchaseID(null);
        ret.setReturnedBy(creator);
        ret.setReturnDate(Instant.now());
        ret.setReturnType(returnType);
        ret.setRefundCash(TYPE_CASH.equals(returnType) ? totalRefund : BigDecimal.ZERO);
        ret.setRefundBanking(TYPE_BANKING.equals(returnType) ? totalRefund : BigDecimal.ZERO);
        ret.setRefundCredit(TYPE_DEBT.equals(returnType) ? totalRefund : BigDecimal.ZERO);
        ret.setTotalRefund(totalRefund);
        ret.setTotalVATRefund(totalVATRefund);
        ret.setOffsetDebtAmount(BigDecimal.ZERO);
        ret.setReason(request.getReason().trim());
        ret.setNote(trimToNull(request.getNote()));
        ret.setStatus(status);
        if (approvedNow) {
            ret.setApprovedAt(Instant.now());
        }

        Return savedReturn = returnRepository.save(ret);

        List<Returndetail> details = new ArrayList<>();
        for (PreparedLine line : prepared.values()) {
            Returndetail detail = new Returndetail();
            detail.setReturnID(savedReturn);
            detail.setInvoiceDetailID(line.invoiceLine());
            detail.setProductID(line.invoiceLine().getProductID());
            detail.setProductUnitID(line.invoiceLine().getProductUnitID());
            // Temporarily the batch it was sold from; repointed to a fresh return-batch on approval.
            detail.setBatchID(line.invoiceLine().getBatchID());
            detail.setReturnQty(line.qty());
            detail.setBaseQtyRestored(line.baseQtyRestored());
            detail.setUnitSellPrice(line.unitSellPrice());
            detail.setLineRefund(line.lineRefund());
            detail.setVatRate(line.vatRate());
            detail.setPreTaxAmount(line.preTaxAmount());
            detail.setVatAmount(line.vatAmount());
            detail.setRestockable(line.restockable());
            details.add(returndetailRepository.save(detail));
        }

        if (approvedNow) {
            applyReturnEffect(savedReturn, details);
        }

        return savedReturn.getId();
    }

    // ------------------------------------------------------------------ submit / approve / reject

    /** Sends a Nháp slip forward: Owner auto-approves to Nợ (and restocks), Pharmacist moves to Chờ duyệt. */
    @Transactional
    public void submit(Integer returnId, boolean isOwner) {
        Return ret = requireReturn(returnId);
        if (!isStatus(getStatusName(ret), ReturnStatus.DRAFT)) {
            throw new IllegalArgumentException("Chỉ có thể gửi duyệt phiếu đang ở trạng thái nháp");
        }
        if (isOwner) {
            markApproved(ret);
        } else {
            ret.setStatus(ReturnStatus.PENDING);
            returnRepository.save(ret);
        }
    }

    /** Owner approves a pending slip → Nợ, restoring stock and updating the invoice's return status. */
    @Transactional
    public void approve(Integer returnId) {
        Return ret = requireReturn(returnId);
        if (!isStatus(getStatusName(ret), ReturnStatus.PENDING)) {
            throw new IllegalArgumentException("Chỉ có thể duyệt phiếu đang ở trạng thái chờ duyệt");
        }
        markApproved(ret);
    }

    /** Owner rejects a pending slip → Từ chối. No stock change. */
    @Transactional
    public void reject(Integer returnId) {
        Return ret = requireReturn(returnId);
        if (!isStatus(getStatusName(ret), ReturnStatus.PENDING)) {
            throw new IllegalArgumentException("Chỉ có thể từ chối phiếu đang ở trạng thái chờ duyệt");
        }
        ret.setStatus(ReturnStatus.REJECTED);
        ret.setApprovedAt(Instant.now());
        returnRepository.save(ret);
    }

    private void markApproved(Return ret) {
        ret.setStatus(ReturnStatus.DEBT);
        ret.setApprovedAt(Instant.now());
        returnRepository.save(ret);
        applyReturnEffect(ret, returndetailRepository.findByReturnIdWithRelations(ret.getId()));
    }

    /**
     * Restores stock for an approved return: each restockable line goes into a fresh batch cloned from the
     * one it was sold from, and the original invoice line's {@code returnedQty} is bumped. Then, depending
     * on whether the original invoice is signed:
     * <ul>
     *   <li><b>TH1 (chưa ký):</b> the original invoice's status is adjusted ("Đã trả hàng 1 phần/toàn bộ").</li>
     *   <li><b>TH2 (đã ký):</b> an adjustment invoice with negative lines is emitted; the signed original is
     *       left untouched (it was already pushed to the tax authority).</li>
     * </ul>
     * TODO(finance): create the Expense (phiếu chi) payout here once that module's contract is agreed; for
     * now only the refund amounts on the slip are recorded.
     */
    private void applyReturnEffect(Return ret, List<Returndetail> details) {
        boolean signed = isSigned(ret.getInvoiceID());
        int seq = 1;
        for (Returndetail detail : details) {
            Invoicedetail invoiceLine = detail.getInvoiceDetailID();
            int already = invoiceLine.getReturnedQty() != null ? invoiceLine.getReturnedQty() : 0;
            invoiceLine.setReturnedQty(already + detail.getReturnQty());
            invoicedetailRepository.save(invoiceLine);

            if (Boolean.TRUE.equals(detail.getRestockable()) && detail.getBaseQtyRestored() != null
                    && detail.getBaseQtyRestored() > 0) {
                Batch returnBatch = cloneReturnBatch(detail.getBatchID(), detail.getBaseQtyRestored(), ret, seq++);
                detail.setBatchID(returnBatch);
                returndetailRepository.save(detail);
            }
        }
        if (signed) {
            createAdjustmentInvoice(ret, ret.getInvoiceID(), details);
        } else {
            updateInvoiceReturnStatus(ret.getInvoiceID());
        }
    }

    /**
     * TH2 — emits the adjustment invoice (invoiceType=Điều chỉnh) for a return against a signed invoice.
     * Lines carry NEGATIVE quantities/amounts (a reduction of the customer's original invoice); it does NOT
     * deduct stock (the returned goods were already restocked into a fresh batch above). The signed original
     * is left untouched. Linked both ways via {@code originalInvoiceID} + {@code returnID}.
     */
    private void createAdjustmentInvoice(Return ret, Invoice original, List<Returndetail> details) {
        BigDecimal refund = nz(ret.getTotalRefund());
        BigDecimal vatRefund = nz(ret.getTotalVATRefund());

        Invoice adj = new Invoice();
        // Cùng ký hiệu (mẫu số / serie) với hóa đơn gốc; số hóa đơn mới, duy nhất.
        adj.setInvoicePattern(original.getInvoicePattern());
        adj.setInvoiceNumber(generateInvoiceNumber());
        adj.setDate(LocalDateTime.now(VN_ZONE));
        adj.setEmployeeID(ret.getReturnedBy());
        adj.setCustomerID(original.getCustomerID());
        adj.setInvoiceType(INVOICE_TYPE_ADJUSTMENT);
        adj.setOriginalInvoiceID(original);
        adj.setReturnID(ret);
        adj.setPrescriptionRequired(false);
        // Phát hành ở trạng thái "Hoàn thành" (chờ ký) — Kế toán/Owner review rồi ký đẩy thuế như hóa đơn thường.
        adj.setStatus(INVOICE_STATUS_COMPLETED);
        adj.setDiscount(BigDecimal.ZERO);
        adj.setSubtotal(refund.negate());
        adj.setTotal(refund.negate());
        adj.setTotalVATOutput(vatRefund.negate());
        adj.setPaidByCash(BigDecimal.ZERO);
        adj.setPaidByBanking(BigDecimal.ZERO);
        adj.setDebtAmount(BigDecimal.ZERO);
        adj.setNote(buildAdjustmentNote(ret, original));

        Invoice savedAdj = invoiceRepository.save(adj);

        for (Returndetail detail : details) {
            Productunit unit = detail.getProductUnitID();
            int qty = detail.getReturnQty() != null ? detail.getReturnQty() : 0;
            int baseQty = detail.getBaseQtyRestored() != null ? detail.getBaseQtyRestored() : 0;

            Invoicedetail line = new Invoicedetail();
            line.setInvoiceID(savedAdj);
            line.setProductID(detail.getProductID());
            line.setProductUnitID(unit);
            line.setBatchID(detail.getBatchID());
            line.setQuantity(-qty);
            line.setUnitName(unit != null && unit.getUnitName() != null ? truncate(unit.getUnitName(), 20) : "");
            line.setBaseQtyDeducted(-baseQty);
            line.setUnitSellPrice(nz(detail.getUnitSellPrice()));
            line.setSubtotal(nz(detail.getLineRefund()).negate());
            line.setReturnedQty(0);
            line.setVatRate(detail.getVatRate());
            line.setPreTaxAmount(nz(detail.getPreTaxAmount()).negate());
            line.setVatAmount(nz(detail.getVatAmount()).negate());
            invoicedetailRepository.save(line);
        }
    }

    /**
     * Writes the return state into the sale invoice's {@code status} — the dedicated {@code returnStatus}
     * column was removed by the DB owner (2026-07-14), who asked to reuse {@code invoice.status}
     * ("Đã trả hàng 1 phần" / "Đã trả hàng toàn bộ"). Left untouched when nothing has been returned.
     */
    private void updateInvoiceReturnStatus(Invoice invoice) {
        if (invoice == null) {
            return;
        }
        String code = invoiceReturnCode(invoice);
        if (INVOICE_RETURN_FULL.equals(code)) {
            invoice.setStatus(INVOICE_STATUS_RETURNED_FULL);
            invoiceRepository.save(invoice);
        } else if (INVOICE_RETURN_PARTIAL.equals(code)) {
            invoice.setStatus(INVOICE_STATUS_RETURNED_PARTIAL);
            invoiceRepository.save(invoice);
        }
    }

    private Batch cloneReturnBatch(Batch original, int quantity, Return ret, int seq) {
        Batch batch = new Batch();
        // TR = batch "hàng trả"; số = id phiếu trả (khớp mã TH-xxxxxx); seq = thứ tự dòng restock trong phiếu.
        batch.setBatchCode(truncate("TR-" + String.format("%06d", ret.getId()) + "-" + seq, 50));
        batch.setBatchName(truncate("Hàng trả " + (original != null && original.getBatchName() != null
                ? original.getBatchName() : ""), 50));
        batch.setProductID(original != null ? original.getProductID() : null);
        batch.setPurchaseDetailID(null);
        batch.setStorageQuantity(quantity);
        batch.setImportUnitID(original != null ? original.getImportUnitID() : null);
        batch.setImportQtyInUnit(original != null ? original.getImportQtyInUnit() : null);
        batch.setImportPrice(original != null && original.getImportPrice() != null
                ? original.getImportPrice() : BigDecimal.ZERO);
        batch.setImportPricePerBase(original != null && original.getImportPricePerBase() != null
                ? original.getImportPricePerBase() : BigDecimal.ZERO);
        batch.setImportDate(Instant.now());
        batch.setProductionDate(original != null ? original.getProductionDate() : null);
        batch.setExpirationDate(original != null ? original.getExpirationDate() : null);
        batch.setLotNumber(original != null ? original.getLotNumber() : null);
        batch.setStatus(true);
        batch.setNote("Hàng trả từ phiếu " + formatCode(ret.getId()));
        return batchRepository.save(batch);
    }

    /**
     * NONE / PARTIAL / FULL derived from how much of the invoice's lines have been returned. Replaces
     * the removed {@code invoice.returnStatus} column (the sale {@code status} is left untouched).
     */
    private String invoiceReturnCode(Invoice invoice) {
        List<Invoicedetail> lines = invoiceLinesOf(invoice.getId());
        boolean anyReturned = false;
        boolean allReturned = !lines.isEmpty();
        for (Invoicedetail line : lines) {
            int returned = line.getReturnedQty() != null ? line.getReturnedQty() : 0;
            int quantity = line.getQuantity() != null ? line.getQuantity() : 0;
            if (returned > 0) {
                anyReturned = true;
            }
            if (returned < quantity) {
                allReturned = false;
            }
        }
        return allReturned ? INVOICE_RETURN_FULL
                : (anyReturned ? INVOICE_RETURN_PARTIAL : INVOICE_RETURN_NONE);
    }

    private boolean isFullyReturned(Invoice invoice) {
        return INVOICE_RETURN_FULL.equals(invoiceReturnCode(invoice));
    }

    // ------------------------------------------------------------------ detail

    @Transactional(readOnly = true)
    public ReturnDetailPageResponse getDetail(Integer returnId) {
        Return ret = returnRepository.findByIdWithRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu trả hàng"));

        List<Returndetail> details = returndetailRepository.findByReturnIdWithRelations(returnId);
        List<ReturnDetailItemResponse> items = details.stream().map(this::toDetailItem).toList();

        int totalQuantity = details.stream()
                .map(Returndetail::getReturnQty)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        String statusName = getStatusName(ret);
        boolean invoiceChecked = ret.getInvoiceID() != null;
        boolean itemsChecked = !details.isEmpty();
        boolean refundChecked = ret.getTotalRefund() != null && ret.getTotalRefund().compareTo(BigDecimal.ZERO) > 0;
        boolean approvalChecked = isStatus(statusName, ReturnStatus.DEBT);

        int done = (invoiceChecked ? 1 : 0) + (itemsChecked ? 1 : 0)
                + (refundChecked ? 1 : 0) + (approvalChecked ? 1 : 0);
        int total = 4;

        Invoice invoice = ret.getInvoiceID();
        Customer customer = invoice != null ? invoice.getCustomerID() : null;

        return new ReturnDetailPageResponse(
                ret.getId(),
                formatCode(ret.getId()),
                ret.getReturnDate(),
                formatInstant(ret.getReturnDate()),
                invoice != null ? invoice.getId() : null,
                invoice != null ? invoice.getInvoicePattern() : "—",
                customer != null ? customer.getName() : "Khách lẻ",
                ret.getReturnedBy() != null ? ret.getReturnedBy().getName() : "Không rõ",
                ret.getReturnType(),
                returnTypeDisplay(ret.getReturnType()),
                ret.getReason(),
                ret.getNote(),
                statusName,
                statusCssClass(statusName),
                formatInstant(ret.getApprovedAt()),
                details.size(),
                totalQuantity,
                ret.getTotalRefund(),
                ret.getRefundCash(),
                ret.getRefundBanking(),
                ret.getRefundCredit(),
                ret.getOffsetDebtAmount(),
                done,
                total,
                done * 100 / total,
                invoiceChecked,
                itemsChecked,
                refundChecked,
                approvalChecked,
                items);
    }

    // ------------------------------------------------------------------ mapping helpers

    private ReturnListItemResponse toListItem(Return ret, List<Returndetail> details) {
        Invoice invoice = ret.getInvoiceID();
        Customer customer = invoice != null ? invoice.getCustomerID() : null;
        String statusName = getStatusName(ret);

        return new ReturnListItemResponse(
                ret.getId(),
                formatCode(ret.getId()),
                ret.getReturnDate(),
                formatInstant(ret.getReturnDate()),
                invoice != null ? invoice.getInvoicePattern() : "—",
                customer != null ? customer.getName() : "Khách lẻ",
                ret.getReturnedBy() != null ? ret.getReturnedBy().getName() : "Không rõ",
                details.size(),
                ret.getTotalRefund(),
                ret.getReturnType(),
                returnTypeDisplay(ret.getReturnType()),
                statusName,
                statusCssClass(statusName));
    }

    private ReturnDetailItemResponse toDetailItem(Returndetail detail) {
        Product product = detail.getProductID();
        Productunit unit = detail.getProductUnitID();
        Batch batch = detail.getBatchID();
        boolean restockable = Boolean.TRUE.equals(detail.getRestockable());

        return new ReturnDetailItemResponse(
                product != null ? product.getProductID() : null,
                product != null ? product.getName() : "Không rõ",
                batch != null ? batch.getLotNumber() : "",
                batch != null ? formatLocalDate(batch.getExpirationDate()) : "",
                unit != null ? unit.getUnitName() : "",
                detail.getReturnQty(),
                detail.getUnitSellPrice(),
                detail.getLineRefund(),
                restockable,
                restockable ? "Nhập lại kho" : "Không nhập lại");
    }

    private ReturnInvoiceLineResponse toInvoiceLine(Invoicedetail line) {
        Product product = line.getProductID();
        Batch batch = line.getBatchID();
        int already = line.getReturnedQty() != null ? line.getReturnedQty() : 0;

        return new ReturnInvoiceLineResponse(
                line.getId(),
                product != null ? product.getProductID() : null,
                product != null ? product.getName() : "Không rõ",
                batch != null ? batch.getLotNumber() : "",
                batch != null ? formatLocalDate(batch.getExpirationDate()) : "",
                line.getUnitName(),
                line.getQuantity(),
                already,
                line.getQuantity() - already,
                line.getUnitSellPrice(),
                isRestockableUnit(line));
    }

    /**
     * Restockable only when the sold unit is the manufacturer's default packaging unit
     * ({@code productunit.isDefault}) — BA 2026-07-12. Loose units (isDefault=false) cannot go back to
     * stock; combo / medical-device "máy" / prescription invoices are already blocked from return
     * upstream. This is hard-coded (no manual checkbox).
     */
    private boolean isRestockableUnit(Invoicedetail line) {
        Productunit unit = line.getProductUnitID();
        return unit != null && Boolean.TRUE.equals(unit.getIsDefault());
    }

    /**
     * Builds a priced return line. The refund money ({@code lineRefund}) is the gross (VAT-inclusive)
     * value of the returned quantity, prorated from the original sale line — unchanged across tax groups.
     * Only the VAT breakdown differs by revenue group (docx sheet 02/11):
     * <ul>
     *   <li><b>Nhóm 3 (khấu trừ):</b> tách net/VAT theo thuế suất dòng gốc — vatAmount giảm thuế GTGT đầu ra
     *       trong kỳ trả hàng (gộp vào {@code Return.totalVATRefund}).</li>
     *   <li><b>Nhóm 2 (trực tiếp):</b> hóa đơn bán hàng không tách thuế suất — toàn bộ tiền hoàn là giảm
     *       doanh thu; vatRate/vatAmount = 0, totalVATRefund = 0.</li>
     * </ul>
     */
    private PreparedLine preparedLineOf(Invoicedetail line, int qty, boolean restockable) {
        BigDecimal saleRate = line.getVatRate() != null ? line.getVatRate() : BigDecimal.ZERO;
        BigDecimal gross = grossRefundOf(line, qty);

        BigDecimal rate;
        BigDecimal preTax;
        BigDecimal vat;
        if (isDeductionGroup() && saleRate.compareTo(BigDecimal.ZERO) > 0) {
            rate = saleRate;
            BigDecimal divisor = BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            preTax = gross.divide(divisor, 2, RoundingMode.HALF_UP);
            vat = gross.subtract(preTax);
        } else {
            rate = BigDecimal.ZERO;
            preTax = gross;
            vat = BigDecimal.ZERO;
        }
        return new PreparedLine(line, qty, restockable, rate, preTax, vat, gross);
    }

    /** Gross (VAT-inclusive) refund of {@code qty} units, prorated from the sale line's gross subtotal. */
    private BigDecimal grossRefundOf(Invoicedetail line, int qty) {
        Integer soldQty = line.getQuantity();
        if (line.getSubtotal() != null && soldQty != null && soldQty > 0) {
            BigDecimal ratio = BigDecimal.valueOf(qty).divide(BigDecimal.valueOf(soldQty), 10, RoundingMode.HALF_UP);
            return line.getSubtotal().multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal unit = line.getUnitSellPrice() != null ? line.getUnitSellPrice() : BigDecimal.ZERO;
        return unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ invoice read-only access

    /** All detail lines of one invoice (read-only over the Invoice module — no custom repo methods). */
    private List<Invoicedetail> invoiceLinesOf(Integer invoiceId) {
        return invoicedetailRepository.findAll().stream()
                .filter(line -> line.getInvoiceID() != null && invoiceId.equals(line.getInvoiceID().getId()))
                .toList();
    }

    /**
     * Returnable when: completed (chưa ký, TH1), signed (đã ký, TH2), or already partially returned.
     * A TH1 partial return moves the status to "Đã trả hàng 1 phần"; a TH2 (signed) partial return keeps
     * "Đã ký" — both stay eligible for further partial returns.
     */
    private boolean isReturnEligibleStatus(Invoice invoice) {
        return isStatus(invoice.getStatus(), INVOICE_STATUS_COMPLETED)
                || isStatus(invoice.getStatus(), INVOICE_STATUS_SIGNED)
                || isStatus(invoice.getStatus(), INVOICE_STATUS_RETURNED_PARTIAL);
    }

    /** Signed ("Đã ký") = pushed to tax → return must emit an adjustment invoice (TH2), not edit the original. */
    private boolean isSigned(Invoice invoice) {
        return invoice != null && isStatus(invoice.getStatus(), INVOICE_STATUS_SIGNED);
    }

    /** Only sale invoices can be returned — never an adjustment invoice. (invoiceType is NOT NULL.) */
    private boolean isNormalInvoice(Invoice invoice) {
        if (invoice == null) {
            return false;
        }
        String type = invoice.getInvoiceType();
        return type == null
                || INVOICE_TYPE_NORMAL.equalsIgnoreCase(type)
                || INVOICE_TYPE_NORMAL_LEGACY.equalsIgnoreCase(type);
    }

    /** Current tax revenue group of the household (1/2/3/4), read from the financial setting singleton. */
    private int revenueGroup() {
        return financialsettingRepository.findFirstByOrderByIdAsc()
                .map(Financialsetting::getRevenueGroup)
                .orElse(2);
    }

    /** Nhóm 3/4 = deduction method (thuế GTGT khấu trừ) → the refund's output VAT is split out and tracked. */
    private boolean isDeductionGroup() {
        return revenueGroup() >= 3;
    }

    private boolean isReturnable(Invoice invoice) {
        if (!isNormalInvoice(invoice)) {
            return false;
        }
        if (!isReturnEligibleStatus(invoice)) {
            return false;
        }
        if (Boolean.TRUE.equals(invoice.getPrescriptionRequired())) {
            return false;
        }
        if (isFullyReturned(invoice)) {
            return false;
        }
        return withinReturnWindow(invoice.getDate());
    }

    private void assertReturnable(Invoice invoice) {
        if (!isNormalInvoice(invoice)) {
            throw new IllegalArgumentException("Chỉ trả được hóa đơn bán hàng (không phải hóa đơn điều chỉnh)");
        }
        if (!isReturnEligibleStatus(invoice)) {
            throw new IllegalArgumentException("Chỉ trả được hóa đơn đã hoàn thành");
        }
        if (Boolean.TRUE.equals(invoice.getPrescriptionRequired())) {
            throw new IllegalArgumentException("Không thể trả hóa đơn thuốc kê đơn");
        }
        if (isFullyReturned(invoice)) {
            throw new IllegalArgumentException("Hóa đơn này đã được trả toàn bộ");
        }
        if (!withinReturnWindow(invoice.getDate())) {
            throw new IllegalArgumentException("Quá thời hạn trả hàng (chỉ trong "
                    + RETURN_WINDOW_DAYS + " ngày kể từ ngày lập hóa đơn)");
        }
    }

    private boolean withinReturnWindow(LocalDateTime invoiceDate) {
        if (invoiceDate == null) {
            return false;
        }
        LocalDate cutoff = LocalDate.now().minusDays(RETURN_WINDOW_DAYS);
        return !toLocalDate(invoiceDate).isBefore(cutoff);
    }

    // ------------------------------------------------------------------ filtering / formatting

    private boolean matchesKeyword(Return ret, List<Returndetail> details, String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        Invoice invoice = ret.getInvoiceID();
        Customer customer = invoice != null ? invoice.getCustomerID() : null;
        if (containsNormalized(formatCode(ret.getId()), normalizedKeyword)
                || containsNormalized(invoice != null ? invoice.getInvoicePattern() : null, normalizedKeyword)
                || containsNormalized(customer != null ? customer.getName() : null, normalizedKeyword)
                || containsNormalized(ret.getReason(), normalizedKeyword)
                || containsNormalized(getStatusName(ret), normalizedKeyword)
                || containsNormalized(ret.getReturnedBy() != null ? ret.getReturnedBy().getName() : null, normalizedKeyword)) {
            return true;
        }
        return details.stream().anyMatch(detail -> {
            Product product = detail.getProductID();
            return product != null
                    && (containsNormalized(product.getName(), normalizedKeyword)
                    || containsNormalized(product.getCode(), normalizedKeyword)
                    || containsNormalized(product.getBarcode(), normalizedKeyword));
        });
    }

    private boolean matchesInvoiceKeyword(Invoice invoice, String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        Customer customer = invoice.getCustomerID();
        return containsNormalized(invoice.getInvoicePattern(), normalizedKeyword)
                || containsNormalized(customer != null ? customer.getName() : null, normalizedKeyword)
                || containsNormalized(customer != null ? customer.getPhoneNumber() : null, normalizedKeyword);
    }

    private boolean matchesDate(Return ret, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        if (ret.getReturnDate() == null) {
            return false;
        }
        LocalDate date = toLocalDate(ret.getReturnDate());
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private long countByStatus(List<Return> returns, String statusName) {
        return returns.stream().filter(ret -> isStatus(getStatusName(ret), statusName)).count();
    }

    private String getStatusName(Return ret) {
        return ret.getStatus() != null ? ret.getStatus() : "Không rõ";
    }

    private boolean isStatus(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String statusCssClass(String statusName) {
        if (isStatus(statusName, ReturnStatus.DEBT)) {
            return "status-debt";
        }
        if (isStatus(statusName, ReturnStatus.REJECTED)) {
            return "status-rejected";
        }
        if (isStatus(statusName, ReturnStatus.PENDING)) {
            return "status-pending";
        }
        if (isStatus(statusName, ReturnStatus.DRAFT)) {
            return "status-draft";
        }
        return "status-default";
    }

    private String resolveReturnType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return TYPE_CASH;
        }
        String type = rawType.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case TYPE_CASH, TYPE_BANKING, TYPE_DEBT -> type;
            default -> throw new IllegalArgumentException("Hình thức hoàn tiền không hợp lệ");
        };
    }

    private String returnTypeDisplay(String type) {
        if (type == null) {
            return "—";
        }
        return switch (type.toUpperCase(Locale.ROOT)) {
            case TYPE_CASH -> "Tiền mặt";
            case TYPE_BANKING -> "Chuyển khoản";
            case TYPE_DEBT -> "Trừ công nợ";
            default -> type;
        };
    }

    private String returnStatusDisplay(String returnStatus) {
        if (returnStatus == null || returnStatus.isBlank()) {
            return "Chưa trả";
        }
        return switch (returnStatus.toUpperCase(Locale.ROOT)) {
            case INVOICE_RETURN_PARTIAL -> "Trả một phần";
            case INVOICE_RETURN_FULL -> "Đã trả toàn bộ";
            default -> "Chưa trả";
        };
    }

    private String productName(Invoicedetail line) {
        Product product = line.getProductID();
        return product != null && product.getName() != null ? product.getName() : "Sản phẩm";
    }

    private String generateCode() {
        int nextId = returnRepository.findAll().stream()
                .map(Return::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return "TH-" + String.format("%06d", nextId);
    }

    private String formatCode(Integer id) {
        return id == null ? "TH-000000" : "TH-" + String.format("%06d", id);
    }

    /** Unique sale-invoice number for the adjustment invoice — {@code HD} + 6-digit next id (mirrors InvoiceService). */
    private String generateInvoiceNumber() {
        int nextId = invoiceRepository.findAll().stream()
                .map(Invoice::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return "HD" + String.format("%06d", nextId);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String safeStr(String value) {
        return value != null ? value : "";
    }

    /**
     * Nội dung hóa đơn điều chỉnh theo NĐ 70/2025 (trường hợp 12/13 — người mua trả lại hàng). Ghi rõ mẫu
     * số + ký hiệu + số + ngày của hóa đơn gốc, và trả toàn bộ hay một phần. VD:
     * "Điều chỉnh giảm cho hóa đơn Mẫu số 2, ký hiệu K26MYY, số HD000001, ngày 16 tháng 07 năm 2026,
     * do người mua trả lại hàng một phần (phiếu trả TH-000002)".
     */
    private String buildAdjustmentNote(Return ret, Invoice original) {
        String pattern = safeStr(original.getInvoicePattern());
        // invoicePattern 7 ký tự = mẫu số (1 ký tự đầu) + ký hiệu (6 ký tự còn lại).
        String formNo = pattern.isEmpty() ? "" : pattern.substring(0, 1);
        String serial = pattern.length() > 1 ? pattern.substring(1) : "";
        String scope = isFullyReturned(original) ? "toàn bộ" : "một phần";
        return "Điều chỉnh giảm cho hóa đơn Mẫu số " + formNo + ", ký hiệu " + serial
                + ", số " + safeStr(original.getInvoiceNumber()) + ", " + formatVnDateWords(original.getDate())
                + ", do người mua trả lại hàng " + scope + " (phiếu trả " + formatCode(ret.getId()) + ")";
    }

    /** "ngày dd tháng MM năm yyyy" theo giờ VN — dùng cho nội dung hóa đơn điều chỉnh. */
    private String formatVnDateWords(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        LocalDate date = toLocalDate(dateTime);
        return String.format("ngày %02d tháng %02d năm %04d",
                date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private Return requireReturn(Integer returnId) {
        return returnRepository.findByIdWithRelations(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu trả hàng"));
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    /** Invoice.date is stored as a VN wall-clock LocalDateTime — format directly, no zone conversion. */
    private String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime);
    }

    private String formatLocalDate(LocalDate date) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime.toLocalDate();
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
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
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

    /** A validated, priced return line (with its VAT split) ready to be persisted. */
    private record PreparedLine(Invoicedetail invoiceLine, int qty, boolean restockable,
                                BigDecimal vatRate, BigDecimal preTaxAmount, BigDecimal vatAmount,
                                BigDecimal lineRefund) {
        BigDecimal unitSellPrice() {
            return invoiceLine.getUnitSellPrice() != null ? invoiceLine.getUnitSellPrice() : BigDecimal.ZERO;
        }

        int baseQtyRestored() {
            Integer baseDeducted = invoiceLine.getBaseQtyDeducted();
            int quantity = invoiceLine.getQuantity() != null ? invoiceLine.getQuantity() : 0;
            if (baseDeducted == null || quantity <= 0) {
                return qty;
            }
            return baseDeducted * qty / quantity;
        }
    }
}
