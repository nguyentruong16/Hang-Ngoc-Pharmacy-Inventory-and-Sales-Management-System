package com.example.project.service;

import com.example.project.dto.request.InvoiceCreateRequest;
import com.example.project.dto.request.InvoiceDetailCreateRequest;
import com.example.project.dto.response.CustomerOptionResponse;
import com.example.project.dto.response.InvoiceLineResponse;
import com.example.project.dto.response.InvoiceListItemResponse;
import com.example.project.dto.response.InvoiceResponse;
import com.example.project.dto.response.SellProductOptionResponse;
import com.example.project.dto.response.SellUnitOptionResponse;
import com.example.project.entity.Account;
import com.example.project.entity.Batch;
import com.example.project.entity.Customer;
import com.example.project.entity.Invoice;
import com.example.project.entity.Invoicedetail;
import com.example.project.entity.Product;
import com.example.project.entity.Productunit;
import com.example.project.repository.AccountRepository;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.CustomerRepository;
import com.example.project.repository.InvoiceRepository;
import com.example.project.repository.InvoicedetailRepository;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.ProductunitRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class InvoiceService {
    private static final String RETURN_NONE = "NONE";
    private static final String RETURN_PARTIAL = "PARTIAL";
    private static final String RETURN_FULL = "FULL";

    private static final String INVOICE_TYPE_NORMAL = "normal";
    private static final String INVOICE_TYPE_RETURN = "return";

    private static final String PAYMENT_CASH = "CASH";
    private static final String PAYMENT_BANKING = "BANKING";
    private static final String PAYMENT_MIXED = "MIXED";
    private static final String PAYMENT_DEBT = "DEBT";

    private static final String STATUS_COMPLETED = "Hoàn thành";
    private static final String STATUS_DEBT = "Còn nợ";
    private static final String STATUS_RETURNED_FULL = "Đã trả hàng toàn bộ";
    private static final String STATUS_RETURNED_PARTIAL = "Đã trả hàng 1 phần";
    private static final String INVOICE_PATTERN = "HD";
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final InvoiceRepository invoiceRepository;
    private final InvoicedetailRepository invoicedetailRepository;
    private final ProductRepository productRepository;
    private final ProductunitRepository productunitRepository;
    private final BatchRepository batchRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoicedetailRepository invoicedetailRepository,
                          ProductRepository productRepository,
                          ProductunitRepository productunitRepository,
                          BatchRepository batchRepository,
                          CustomerRepository customerRepository,
                          AccountRepository accountRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoicedetailRepository = invoicedetailRepository;
        this.productRepository = productRepository;
        this.productunitRepository = productunitRepository;
        this.batchRepository = batchRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAll() {
        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InvoiceListItemResponse> list(String search,
                                              String fromDate,
                                              String toDate,
                                              String paymentType,
                                              String status,
                                              Integer sellerId,
                                              Pageable pageable) {
        String normalizedKeyword = normalize(search);
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        String normalizedStatus = status == null ? "" : status.trim();
        Map<Integer, String> returnStates = returnStateByInvoice();

        List<InvoiceListItemResponse> filtered = invoiceRepository.findAllWithRelations()
                .stream()
                .filter(invoice -> matchesKeyword(invoice, normalizedKeyword))
                .filter(invoice -> matchesDate(invoice, from, to))
                .filter(invoice -> matchesPaymentType(invoice, paymentType))
                .filter(invoice -> matchesSeller(invoice, sellerId))
                .filter(invoice -> normalizedStatus.isEmpty()
                        || normalize(normalizedStatus).equals(normalize(invoice.getStatus())))
                .sorted(Comparator.comparing(Invoice::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(invoice -> toListItem(invoice, returnStates))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<InvoiceListItemResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public List<String> listStatuses() {
        return invoiceRepository.findAll().stream()
                .map(Invoice::getStatus)
                .filter(status -> status != null && !status.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /** Distinct sellers (accounts) that have created at least one invoice, for the list filter. */
    @Transactional(readOnly = true)
    public List<CustomerOptionResponse> listSellers() {
        Map<Integer, String> byId = new LinkedHashMap<>();
        for (Invoice invoice : invoiceRepository.findAllWithRelations()) {
            Account employee = invoice.getEmployeeID();
            if (employee != null && employee.getId() != null) {
                byId.putIfAbsent(employee.getId(), employee.getName());
            }
        }
        return byId.entrySet().stream()
                .map(entry -> new CustomerOptionResponse(entry.getKey(), entry.getValue(), null))
                .sorted(Comparator.comparing(CustomerOptionResponse::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    /** Payment-method code → Vietnamese label, in dropdown order. */
    public Map<String, String> paymentTypeLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put(PAYMENT_CASH, "Tiền mặt");
        labels.put(PAYMENT_BANKING, "Chuyển khoản");
        labels.put(PAYMENT_MIXED, "TM + CK");
        labels.put(PAYMENT_DEBT, "Ghi nợ");
        return labels;
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return invoiceRepository.count();
    }

    @Transactional(readOnly = true)
    public long countToday() {
        LocalDate today = LocalDate.now();
        return invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getDate() != null && toLocalDate(invoice.getDate()).equals(today))
                .count();
    }

    @Transactional(readOnly = true)
    public BigDecimal sumTodayRevenue() {
        LocalDate today = LocalDate.now();
        return invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getDate() != null && toLocalDate(invoice.getDate()).equals(today))
                .filter(invoice -> !normalize(invoice.getStatus()).contains("huy"))
                .map(Invoice::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long countDebt() {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> isPositive(invoice.getDebtAmount()))
                .count();
    }

    @Transactional(readOnly = true)
    public BigDecimal sumDebtTotal() {
        return invoiceRepository.findAll().stream()
                .map(Invoice::getDebtAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long countReturned() {
        return returnStateByInvoice().values().stream()
                .filter(code -> RETURN_PARTIAL.equals(code) || RETURN_FULL.equals(code))
                .count();
    }

    /**
     * Return state per invoice, derived from how much of its lines have been returned:
     * {@code NONE} (nothing), {@code PARTIAL} (some), {@code FULL} (everything). Replaces the removed
     * {@code returnStatus} column.
     */
    private Map<Integer, String> returnStateByInvoice() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (Object[] row : invoicedetailRepository.sumQuantitiesGroupedByInvoice()) {
            Integer invoiceId = (Integer) row[0];
            long sold = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long returned = row[2] != null ? ((Number) row[2]).longValue() : 0;
            String code;
            if (returned <= 0) {
                code = RETURN_NONE;
            } else if (returned >= sold) {
                code = RETURN_FULL;
            } else {
                code = RETURN_PARTIAL;
            }
            map.put(invoiceId, code);
        }
        return map;
    }

    // ------------------------------------------------------------------ sell (create invoice)

    /** Active products with stock &gt; 0 and at least one active sell unit, for the POS picker. */
    @Transactional(readOnly = true)
    public List<SellProductOptionResponse> listSellableProducts() {
        Map<Integer, Long> stockByProduct = new LinkedHashMap<>();
        for (Object[] row : batchRepository.sumStorageGroupedByProduct()) {
            stockByProduct.put((Integer) row[0], (Long) row[1]);
        }

        Map<Integer, List<Productunit>> unitsByProduct = new LinkedHashMap<>();
        for (Productunit unit : productunitRepository.findAllWithProduct()) {
            if (Boolean.FALSE.equals(unit.getIsActive()) || unit.getProductID() == null) {
                continue;
            }
            unitsByProduct.computeIfAbsent(unit.getProductID().getProductID(), id -> new ArrayList<>()).add(unit);
        }

        List<SellProductOptionResponse> options = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (!Boolean.TRUE.equals(product.getStatus())) {
                continue;
            }
            long baseStock = stockByProduct.getOrDefault(product.getProductID(), 0L);
            List<Productunit> units = unitsByProduct.getOrDefault(product.getProductID(), List.of());
            if (baseStock <= 0 || units.isEmpty()) {
                continue;
            }

            List<SellUnitOptionResponse> unitOptions = units.stream()
                    .sorted(Comparator.comparing(Productunit::getRatio,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(unit -> new SellUnitOptionResponse(
                            unit.getId(),
                            unit.getUnitName(),
                            unit.getRatio(),
                            unit.getSellPrice(),
                            Boolean.TRUE.equals(unit.getIsDefault())))
                    .toList();

            options.add(new SellProductOptionResponse(
                    product.getProductID(),
                    product.getCode(),
                    product.getName(),
                    product.getBarcode(),
                    baseStock,
                    unitOptions));
        }

        options.sort(Comparator.comparing(SellProductOptionResponse::getName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return options;
    }

    @Transactional(readOnly = true)
    public List<CustomerOptionResponse> listCustomers() {
        return customerRepository.findAll().stream()
                .sorted(Comparator.comparing(Customer::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(customer -> new CustomerOptionResponse(
                        customer.getId(), customer.getName(), customer.getPhoneNumber()))
                .toList();
    }

    /**
     * Creates one sale invoice + its lines, deducting stock from the product's batches soonest-expiry
     * first (FEFO). Each line's stored quantity is in the chosen sell unit; {@code baseQtyDeducted}
     * is that quantity times the unit ratio. When a line spans several batches its {@code batchID}
     * references the first (soonest-expiry) batch consumed, matching how the schema keeps one batch
     * per detail row.
     *
     * @return the new invoice id, for the redirect.
     */
    @Transactional
    public Integer createSaleInvoice(InvoiceCreateRequest request, Integer currentAccountId) {
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một sản phẩm");
        }

        Account employee = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));
        }

        boolean prescriptionRequired = Boolean.TRUE.equals(request.getPrescriptionRequired());
        String prescriptionCode = trimToNull(request.getPrescriptionCode());
        if (prescriptionRequired && prescriptionCode == null) {
            throw new IllegalArgumentException("Vui lòng nhập mã đơn thuốc cho hóa đơn kê đơn");
        }
        if (prescriptionRequired && !prescriptionCode.matches("^[A-Za-z0-9]{12}-[cnhy]$")) {
            throw new IllegalArgumentException(
                    "Mã đơn thuốc không đúng định dạng (12 ký tự chữ/số, dấu \"-\", rồi c/n/h/y)");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoicePattern(INVOICE_PATTERN);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        // Store Vietnam wall-clock time (mirrors ProcurementplanService using LocalDateTime.now(VN_ZONE));
        // the date column is an Instant, so map the VN local time onto UTC to avoid the -7h drift.
        invoice.setDate(LocalDateTime.now(VN_ZONE).toInstant(ZoneOffset.UTC));
        invoice.setEmployeeID(employee);
        invoice.setCustomerID(customer);
        invoice.setInvoiceType(INVOICE_TYPE_NORMAL);
        invoice.setPrescriptionRequired(prescriptionRequired);
        invoice.setPrescriptionCode(prescriptionCode);
        invoice.setNote(trimToNull(request.getNote()));
        // NOT NULL money columns must hold a value on the first flush (the detail rows below need the
        // invoice id first); the real figures are computed and re-saved once the lines are priced.
        invoice.setSubtotal(BigDecimal.ZERO);
        invoice.setDiscount(BigDecimal.ZERO);
        invoice.setTotal(BigDecimal.ZERO);
        invoice.setPaidByCash(BigDecimal.ZERO);
        invoice.setPaidByBanking(BigDecimal.ZERO);
        invoice.setDebtAmount(BigDecimal.ZERO);
        invoice.setStatus(STATUS_COMPLETED);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceDetailCreateRequest item : request.getDetails()) {
            subtotal = subtotal.add(saveLineAndDeductStock(savedInvoice, item));
        }

        BigDecimal discount = maxZero(request.getDiscount());
        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Giảm giá không được lớn hơn tiền hàng");
        }
        BigDecimal total = subtotal.subtract(discount);

        BigDecimal paidByCash = maxZero(request.getPaidByCash());
        BigDecimal paidByBanking = maxZero(request.getPaidByBanking());
        BigDecimal paid = paidByCash.add(paidByBanking);
        if (paid.compareTo(total) > 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không được lớn hơn tổng tiền hóa đơn");
        }

        BigDecimal debt = total.subtract(paid);
        if (debt.compareTo(BigDecimal.ZERO) > 0 && customer == null) {
            throw new IllegalArgumentException("Khách lẻ phải thanh toán đủ; chọn khách hàng để ghi nợ");
        }

        savedInvoice.setSubtotal(subtotal);
        savedInvoice.setDiscount(discount);
        savedInvoice.setTotal(total);
        savedInvoice.setPaidByCash(paidByCash);
        savedInvoice.setPaidByBanking(paidByBanking);
        savedInvoice.setDebtAmount(debt);
        savedInvoice.setStatus(debt.compareTo(BigDecimal.ZERO) > 0 ? STATUS_DEBT : STATUS_COMPLETED);
        invoiceRepository.save(savedInvoice);

        return savedInvoice.getId();
    }

    /** Persists one line, deducts its base quantity across batches (FEFO), returns its subtotal. */
    private BigDecimal saveLineAndDeductStock(Invoice invoice, InvoiceDetailCreateRequest item) {
        if (item.getProductId() == null || item.getProductUnitId() == null) {
            throw new IllegalArgumentException("Dòng hàng chưa chọn sản phẩm hoặc đơn vị bán");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng bán phải lớn hơn 0");
        }

        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        Productunit unit = productunitRepository.findById(item.getProductUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị bán"));
        if (unit.getProductID() == null || !product.getProductID().equals(unit.getProductID().getProductID())) {
            throw new IllegalArgumentException("Đơn vị bán không thuộc sản phẩm đã chọn");
        }

        BigDecimal ratio = unit.getRatio() != null && unit.getRatio().compareTo(BigDecimal.ZERO) > 0
                ? unit.getRatio() : BigDecimal.ONE;
        int quantity = item.getQuantity();
        int baseQty = ratio.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP).intValue();

        BigDecimal unitSellPrice = item.getUnitSellPrice() != null && item.getUnitSellPrice().compareTo(BigDecimal.ZERO) >= 0
                ? item.getUnitSellPrice()
                : (unit.getSellPrice() != null ? unit.getSellPrice() : BigDecimal.ZERO);
        BigDecimal lineSubtotal = unitSellPrice.multiply(BigDecimal.valueOf(quantity));

        Batch firstBatch = deductStock(product, baseQty, quantity, ratio, unit.getUnitName());

        Invoicedetail detail = new Invoicedetail();
        detail.setInvoiceID(invoice);
        detail.setProductID(product);
        detail.setProductUnitID(unit);
        detail.setBatchID(firstBatch);
        detail.setQuantity(quantity);
        detail.setUnitName(unit.getUnitName());
        detail.setBaseQtyDeducted(baseQty);
        detail.setUnitSellPrice(unitSellPrice);
        detail.setSubtotal(lineSubtotal);
        detail.setReturnedQty(0);
        invoicedetailRepository.save(detail);

        return lineSubtotal;
    }

    /** Deducts {@code baseQty} from a product's in-stock batches (FEFO); returns the first batch used. */
    private Batch deductStock(Product product, int baseQty, int sellQuantity, BigDecimal ratio, String unitName) {
        List<Batch> batches = batchRepository.findInStockBatchesByProduct(product.getProductID());
        long available = batches.stream()
                .mapToLong(batch -> batch.getStorageQuantity() == null ? 0 : batch.getStorageQuantity())
                .sum();
        if (available < baseQty) {
            BigDecimal safeRatio = ratio != null && ratio.compareTo(BigDecimal.ZERO) > 0 ? ratio : BigDecimal.ONE;
            long availableInUnit = BigDecimal.valueOf(available)
                    .divide(safeRatio, 0, RoundingMode.DOWN).longValue();
            String unit = unitName != null ? unitName : "";
            throw new IllegalArgumentException("Sản phẩm \"" + product.getName()
                    + "\" không đủ tồn kho (còn " + availableInUnit + " " + unit
                    + ", cần " + sellQuantity + " " + unit + ")");
        }

        Batch firstBatch = null;
        int remaining = baseQty;
        for (Batch batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int inBatch = batch.getStorageQuantity() == null ? 0 : batch.getStorageQuantity();
            if (inBatch <= 0) {
                continue;
            }
            if (firstBatch == null) {
                firstBatch = batch;
            }
            int take = Math.min(inBatch, remaining);
            batch.setStorageQuantity(inBatch - take);
            batchRepository.save(batch);
            remaining -= take;
        }
        return firstBatch;
    }

    private String generateInvoiceNumber() {
        int nextId = invoiceRepository.findAll().stream()
                .map(Invoice::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        return INVOICE_PATTERN + String.format("%06d", nextId);
    }

    private BigDecimal maxZero(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** The lines of one invoice, for the quick-view modal (JSON). */
    @Transactional(readOnly = true)
    public List<InvoiceLineResponse> loadLines(Integer invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn");
        }
        return invoicedetailRepository.findByInvoiceIdWithRelations(invoiceId).stream()
                .map(this::toLine)
                .toList();
    }

    private InvoiceListItemResponse toListItem(Invoice invoice, Map<Integer, String> returnStates) {
        String statusName = invoice.getStatus() != null ? invoice.getStatus() : "Không rõ";
        String returnCode = returnStates.getOrDefault(invoice.getId(), RETURN_NONE);

        return new InvoiceListItemResponse(
                invoice.getId(),
                invoiceCode(invoice),
                invoice.getDate(),
                formatInstant(invoice.getDate()),
                invoice.getCustomerID() != null ? invoice.getCustomerID().getName() : "Khách lẻ",
                invoice.getEmployeeID() != null ? invoice.getEmployeeID().getName() : "Không rõ",
                invoiceTypeDisplay(invoice.getInvoiceType()),
                invoice.getTotal(),
                invoice.getDebtAmount(),
                paymentDisplay(invoice),
                Boolean.TRUE.equals(invoice.getPrescriptionRequired()),
                returnStatusDisplay(returnCode),
                returnStatusCssClass(returnCode),
                statusName,
                statusCssClass(statusName));
    }

    private InvoiceLineResponse toLine(Invoicedetail line) {
        Product product = line.getProductID();
        Batch batch = line.getBatchID();

        return new InvoiceLineResponse(
                line.getId(),
                product != null ? product.getName() : "Không rõ",
                batch != null ? batch.getLotNumber() : "",
                batch != null ? formatLocalDate(batch.getExpirationDate()) : "",
                line.getUnitName(),
                line.getQuantity(),
                line.getUnitSellPrice(),
                line.getSubtotal(),
                line.getReturnedQty() != null ? line.getReturnedQty() : 0);
    }

    /** The visible invoice number (the {@code invoiceNumber} column). */
    private String invoiceCode(Invoice invoice) {
        String number = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber().trim() : "";
        return number.isEmpty() ? "—" : number;
    }

    private boolean matchesKeyword(Invoice invoice, String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        return containsNormalized(invoiceCode(invoice), normalizedKeyword)
                || containsNormalized(invoice.getCustomerID() != null
                        ? invoice.getCustomerID().getName() : null, normalizedKeyword)
                || containsNormalized(invoice.getStatus(), normalizedKeyword)
                || containsNormalized(invoice.getNote(), normalizedKeyword);
    }

    private boolean matchesSeller(Invoice invoice, Integer sellerId) {
        if (sellerId == null) {
            return true;
        }
        return invoice.getEmployeeID() != null
                && sellerId.equals(invoice.getEmployeeID().getId());
    }

    private boolean matchesDate(Invoice invoice, LocalDate from, LocalDate to) {
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

    private boolean matchesPaymentType(Invoice invoice, String paymentType) {
        if (paymentType == null || paymentType.isBlank()) {
            return true;
        }
        boolean cash = isPositive(invoice.getPaidByCash());
        boolean banking = isPositive(invoice.getPaidByBanking());
        boolean debt = isPositive(invoice.getDebtAmount());
        return switch (paymentType.toUpperCase(Locale.ROOT)) {
            case PAYMENT_CASH -> cash && !banking;
            case PAYMENT_BANKING -> banking && !cash;
            case PAYMENT_MIXED -> cash && banking;
            case PAYMENT_DEBT -> debt;
            default -> true;
        };
    }

    private String invoiceTypeDisplay(String invoiceType) {
        if (invoiceType == null || invoiceType.isBlank()) {
            return "—";
        }
        return switch (invoiceType.toLowerCase(Locale.ROOT)) {
            case INVOICE_TYPE_NORMAL -> "Bán hàng";
            case INVOICE_TYPE_RETURN -> "Trả hàng";
            default -> invoiceType;
        };
    }

    private String paymentDisplay(Invoice invoice) {
        boolean cash = isPositive(invoice.getPaidByCash());
        boolean banking = isPositive(invoice.getPaidByBanking());
        boolean debt = isPositive(invoice.getDebtAmount());

        String paid;
        if (cash && banking) {
            paid = "TM + CK";
        } else if (cash) {
            paid = "Tiền mặt";
        } else if (banking) {
            paid = "Chuyển khoản";
        } else {
            paid = debt ? "Ghi nợ" : "—";
        }
        if (debt && (cash || banking)) {
            paid += " + Nợ";
        }
        return paid;
    }

    private String returnStatusDisplay(String returnStatus) {
        if (returnStatus == null || returnStatus.isBlank()) {
            return "Không";
        }
        return switch (returnStatus.toUpperCase(Locale.ROOT)) {
            case RETURN_PARTIAL -> "Trả một phần";
            case RETURN_FULL -> "Đã trả toàn bộ";
            default -> "Không";
        };
    }

    private String returnStatusCssClass(String returnStatus) {
        if (returnStatus == null) {
            return "return-none";
        }
        return switch (returnStatus.toUpperCase(Locale.ROOT)) {
            case RETURN_PARTIAL -> "return-partial";
            case RETURN_FULL -> "return-full";
            default -> "return-none";
        };
    }

    private String statusCssClass(String statusName) {
        if (isStatus(statusName, STATUS_RETURNED_FULL)) {
            return "status-return-full";
        }
        if (isStatus(statusName, STATUS_RETURNED_PARTIAL)) {
            return "status-return-partial";
        }
        String normalized = normalize(statusName);
        if (normalized.contains("hoan thanh")) {
            return "status-completed";
        }
        if (normalized.contains("huy")) {
            return "status-cancelled";
        }
        if (isStatus(statusName, STATUS_DEBT) || normalized.contains("con no")) {
            return "status-debt";
        }
        return "status-default";
    }

    private boolean isStatus(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
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
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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
