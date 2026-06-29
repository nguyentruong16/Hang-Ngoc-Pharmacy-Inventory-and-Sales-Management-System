package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.context.CurrentUserContext;
import com.example.project.dto.response.ProductBatchDetailResponse;
import com.example.project.dto.response.ProductBranchStockResponse;
import com.example.project.dto.response.ProductDetailResponse;
import com.example.project.dto.response.ProductListStatsResponse;
import com.example.project.dto.response.ProductRecentHistoryResponse;
import com.example.project.dto.response.ProductResponse;
import com.example.project.dto.response.ProductRowResponse;
import com.example.project.dto.response.ProductUnitDetailResponse;
import com.example.project.entity.Batch;
import com.example.project.entity.Branch;
import com.example.project.entity.Invoice;
import com.example.project.entity.Invoicedetail;
import com.example.project.entity.Medicineapi;
import com.example.project.entity.Producer;
import com.example.project.entity.Product;
import com.example.project.entity.Productunit;
import com.example.project.entity.Returndetail;
import com.example.project.entity.Stockout;
import com.example.project.entity.Stockoutdetail;
import com.example.project.entity.Type;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.InvoicedetailRepository;
import com.example.project.repository.MedicineapiRepository;
import com.example.project.repository.ProducerRepository;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.ProductunitRepository;
import com.example.project.repository.ReturndetailRepository;
import com.example.project.repository.StockoutdetailRepository;
import com.example.project.repository.TypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    // Stock-status filter values exchanged with the screen.
    public static final String STOCK_STATUS_IN = "IN_STOCK";
    public static final String STOCK_STATUS_LOW = "LOW";
    public static final String STOCK_STATUS_OUT = "OUT";

    private static final String LABEL_IN = "Còn hàng";
    private static final String LABEL_LOW = "Sắp hết";
    private static final String LABEL_OUT = "Hết hàng";

    private static final String CSS_IN = "status-instock";
    private static final String CSS_LOW = "status-low";
    private static final String CSS_OUT = "status-out";

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int RECENT_HISTORY_LIMIT = 10;

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ProductunitRepository productunitRepository;
    private final MedicineapiRepository medicineapiRepository;
    private final TypeRepository typeRepository;
    private final ProducerRepository producerRepository;
    private final InvoicedetailRepository invoicedetailRepository;
    private final StockoutdetailRepository stockoutdetailRepository;
    private final ReturndetailRepository returndetailRepository;
    private final CurrentUserContext currentUserContext;

    public ProductService(ProductRepository productRepository,
                          BatchRepository batchRepository,
                          ProductunitRepository productunitRepository,
                          MedicineapiRepository medicineapiRepository,
                          TypeRepository typeRepository,
                          ProducerRepository producerRepository,
                          InvoicedetailRepository invoicedetailRepository,
                          StockoutdetailRepository stockoutdetailRepository,
                          ReturndetailRepository returndetailRepository,
                          CurrentUserContext currentUserContext) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.productunitRepository = productunitRepository;
        this.medicineapiRepository = medicineapiRepository;
        this.typeRepository = typeRepository;
        this.producerRepository = producerRepository;
        this.invoicedetailRepository = invoicedetailRepository;
        this.stockoutdetailRepository = stockoutdetailRepository;
        this.returndetailRepository = returndetailRepository;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * Product List search: keyword over code/name/barcode plus type / producer / stock-status
     * filters, then in-memory pagination. Mirrors the load-all + filter approach already used by
     * {@code StockoutService} for consistency across listing screens.
     */
    @Transactional(readOnly = true)
    public Page<ProductRowResponse> searchProducts(String keyword,
                                                   Integer typeId,
                                                   Integer producerId,
                                                   String stockStatus,
                                                   Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);

        Map<Integer, Long> stockByProduct = loadStockByProduct();
        Map<Integer, Productunit> mainUnitByProduct = loadMainUnitByProduct();
        Map<Integer, String> ingredientByProduct = loadIngredientByProduct();

        List<ProductRowResponse> filtered = productRepository.findAllWithRelations().stream()
                .filter(product -> matchesKeyword(product, normalizedKeyword))
                .filter(product -> typeMatches(product, typeId))
                .filter(product -> producerMatches(product, producerId))
                .map(product -> toRow(product, stockByProduct, mainUnitByProduct, ingredientByProduct))
                .filter(row -> stockStatusMatches(row, stockStatus))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<ProductRowResponse> content = start >= filtered.size()
                ? List.of()
                : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public ProductListStatsResponse getStats() {
        Map<Integer, Long> stockByProduct = loadStockByProduct();

        long total = 0;
        long inStock = 0;
        long low = 0;
        long out = 0;

        for (Product product : productRepository.findAll()) {
            total++;
            long stock = stockByProduct.getOrDefault(product.getProductID(), 0L);
            switch (stockStatusCode(product, stock)) {
                case STOCK_STATUS_IN -> inStock++;
                case STOCK_STATUS_LOW -> low++;
                default -> out++;
            }
        }

        return new ProductListStatsResponse(total, inStock, low, out);
    }

    /**
     * Full Product Detail payload, branch-scoped by {@link CurrentUserContext#getBranchFilter()}
     * (Owner = all branches; Chief Pharmacist / Pharmacist = active branch). Returns
     * {@link Optional#empty()} when the product does not exist, so the controller can 404 / redirect.
     */
    @Transactional(readOnly = true)
    public Optional<ProductDetailResponse> getProductDetail(Integer productId) {
        Optional<Product> productOpt = productRepository.findDetailById(productId);
        if (productOpt.isEmpty()) {
            return Optional.empty();
        }
        Product product = productOpt.get();
        Integer branchId = currentUserContext.getBranchFilter();

        List<ProductUnitDetailResponse> units = productunitRepository.findByProductId(productId).stream()
                .sorted(Comparator.comparing(Productunit::getRatio,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toUnitDetail)
                .toList();

        List<String> ingredients = medicineapiRepository.findByProductId(productId).stream()
                .map(this::formatIngredient)
                .toList();

        List<ProductBranchStockResponse> branchStocks =
                batchRepository.sumStorageByProductGroupedByBranch(productId, branchId).stream()
                        .map(row -> toBranchStock(product, row))
                        .toList();
        long totalStock = branchStocks.stream()
                .mapToLong(ProductBranchStockResponse::getTotalStock)
                .sum();
        String totalStatus = stockStatusCode(product, totalStock);

        List<ProductBatchDetailResponse> batches =
                batchRepository.findInStockBatchesByProduct(productId, branchId).stream()
                        .map(this::toBatchDetail)
                        .toList();

        boolean canViewHistory = canViewRecentHistory();
        List<ProductRecentHistoryResponse> recentHistory =
                canViewHistory ? loadRecentHistory(productId, branchId) : List.of();

        ProductDetailResponse response = new ProductDetailResponse();
        response.setProductId(product.getProductID());
        response.setCode(product.getCode());
        response.setName(product.getName());
        response.setBarcode(product.getBarcode());
        response.setTypeName(product.getTypeID() != null ? product.getTypeID().getName() : "—");
        response.setProducerName(product.getProducerID() != null ? product.getProducerID().getName() : "—");
        response.setOriginName(product.getOriginID() != null ? product.getOriginID().getName() : "—");
        response.setRegistrationNumber(product.getRegistrationNumber());
        response.setStatusActive(Boolean.TRUE.equals(product.getStatus()));
        response.setStatusLabel(Boolean.TRUE.equals(product.getStatus()) ? "Đang kinh doanh" : "Ngừng kinh doanh");
        response.setMinStock(product.getMinStock());
        response.setMaxStock(product.getMaxStock());
        response.setNote(product.getNote());
        response.setIngredients(ingredients);
        response.setUnits(units);
        response.setTotalStock(totalStock);
        response.setStockStatusLabel(stockStatusLabel(totalStatus));
        response.setStockStatusCss(stockStatusCss(totalStatus));
        response.setBranchStocks(branchStocks);
        response.setBatches(batches);
        response.setCanViewRecentHistory(canViewHistory);
        response.setRecentHistory(recentHistory);
        return Optional.of(response);
    }

    /**
     * Whether the current user may see the "recent inventory history" block. Owner and Chief
     * Pharmacist may; Pharmacist may not. (Chief Pharmacist's data is already branch-scoped via
     * {@link CurrentUserContext#getBranchFilter()}.)
     */
    public boolean canViewRecentHistory() {
        String role = currentUserContext.getCurrentRole();
        return RoleConstants.OWNER.equals(role) || RoleConstants.CHIEF_PHARMACIST.equals(role);
    }

    @Transactional(readOnly = true)
    public List<Type> listTypes() {
        return typeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(type -> type.getName() == null ? "" : type.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Producer> listProducers() {
        return producerRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(producer -> producer.getName() == null ? "" : producer.getName()))
                .toList();
    }

    // --- helpers -------------------------------------------------------------

    /**
     * On-hand stock per product, scoped to the active branch via {@link CurrentUserContext#getBranchFilter()}:
     * for Pharmacist / Chief Pharmacist this returns only their current branch's stock; for the Owner
     * (and unauthenticated callers) {@code getBranchFilter()} is {@code null}, so stock is summed across
     * all branches (system-wide total) — the documented Owner cross-branch behavior.
     */
    private Map<Integer, Long> loadStockByProduct() {
        Integer branchId = currentUserContext.getBranchFilter();
        List<Object[]> rows = branchId == null
                ? batchRepository.sumStorageGroupedByProduct()
                : batchRepository.sumStorageGroupedByProductAndBranch(branchId);

        return rows.stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private Map<Integer, Productunit> loadMainUnitByProduct() {
        return productunitRepository.findAllWithProduct().stream()
                .filter(unit -> unit.getProductID() != null)
                .collect(Collectors.groupingBy(
                        unit -> unit.getProductID().getProductID(),
                        Collectors.collectingAndThen(Collectors.toList(), this::pickMainUnit)
                ));
    }

    /** Default unit first, then base unit, then the lowest-id unit. */
    private Productunit pickMainUnit(List<Productunit> units) {
        return units.stream()
                .filter(unit -> Boolean.TRUE.equals(unit.getIsDefault()) && !Boolean.FALSE.equals(unit.getIsActive()))
                .findFirst()
                .or(() -> units.stream().filter(unit -> Boolean.TRUE.equals(unit.getIsBaseUnit())).findFirst())
                .orElseGet(() -> units.stream()
                        .min(Comparator.comparing(Productunit::getId))
                        .orElse(null));
    }

    private Map<Integer, String> loadIngredientByProduct() {
        return medicineapiRepository.findAllWithProduct().stream()
                .filter(api -> api.getProductID() != null)
                .collect(Collectors.groupingBy(
                        api -> api.getProductID().getProductID(),
                        Collectors.mapping(this::formatIngredient,
                                Collectors.collectingAndThen(Collectors.toList(),
                                        parts -> String.join(", ", parts)))
                ));
    }

    private String formatIngredient(Medicineapi api) {
        if (api.getStrength() == null || api.getStrength().isBlank()) {
            return api.getApiName();
        }
        return api.getApiName() + " " + api.getStrength();
    }

    // --- Product Detail mappers ---------------------------------------------

    private ProductUnitDetailResponse toUnitDetail(Productunit unit) {
        return new ProductUnitDetailResponse(
                unit.getUnitName(),
                unit.getRatio(),
                unit.getSellPrice(),
                Boolean.TRUE.equals(unit.getIsDefault()),
                Boolean.TRUE.equals(unit.getIsBaseUnit()),
                Boolean.TRUE.equals(unit.getIsActive())
        );
    }

    private ProductBranchStockResponse toBranchStock(Product product, Object[] row) {
        Integer branchId = row[0] != null ? ((Number) row[0]).intValue() : null;
        String branchName = row[1] != null ? (String) row[1] : "—";
        long stock = row[2] != null ? ((Number) row[2]).longValue() : 0L;
        String statusCode = stockStatusCode(product, stock);
        return new ProductBranchStockResponse(
                branchId, branchName, stock, stockStatusLabel(statusCode), stockStatusCss(statusCode));
    }

    private ProductBatchDetailResponse toBatchDetail(Batch batch) {
        return new ProductBatchDetailResponse(
                batch.getId(),
                batch.getBatchName(),
                batch.getLotNumber(),
                branchName(batch.getBranchID()),
                formatInstant(batch.getImportDate()),
                formatDate(batch.getProductionDate()),
                formatDate(batch.getExpirationDate()),
                batch.getStorageQuantity(),
                batch.getImportUnitID() != null ? batch.getImportUnitID().getUnitName() : "—",
                batch.getImportPrice(),
                Boolean.TRUE.equals(batch.getStatus()) ? "Còn hiệu lực" : "Ngừng",
                batch.getNote()
        );
    }

    // --- recent stock-movement preview (union of 4 sources) ------------------

    private List<ProductRecentHistoryResponse> loadRecentHistory(Integer productId, Integer branchId) {
        Pageable top = PageRequest.of(0, RECENT_HISTORY_LIMIT);
        List<ProductRecentHistoryResponse> rows = new ArrayList<>();

        for (Batch batch : batchRepository.findRecentImportsByProduct(productId, branchId, top)) {
            rows.add(new ProductRecentHistoryResponse(
                    batch.getImportDate(), formatInstant(batch.getImportDate()), "Nhập kho",
                    branchName(batch.getBranchID()), batch.getBatchName(), batch.getLotNumber(),
                    importQuantity(batch), null));
        }

        for (Invoicedetail detail : invoicedetailRepository.findRecentSalesByProduct(productId, branchId, top)) {
            Invoice invoice = detail.getInvoiceID();
            rows.add(new ProductRecentHistoryResponse(
                    invoice.getDate(), formatInstant(invoice.getDate()), "Bán hàng",
                    branchName(invoice.getBranchID()), invoice.getInvoiceCode(),
                    lotNumber(detail.getBatchID()), -nullSafe(detail.getBaseQtyDeducted()), null));
        }

        for (Stockoutdetail detail : stockoutdetailRepository.findRecentStockOutsByProduct(productId, branchId, top)) {
            Stockout stockOut = detail.getStockOutID();
            rows.add(new ProductRecentHistoryResponse(
                    stockOut.getDate(), formatInstant(stockOut.getDate()),
                    "Xuất kho - " + formatOutType(stockOut.getOutType()),
                    branchName(stockOut.getBranchID()), formatCode("SO", stockOut.getId()),
                    lotNumber(detail.getBatchID()), -nullSafe(detail.getBaseQtyDeducted()), detail.getNote()));
        }

        for (Returndetail detail : returndetailRepository.findRecentReturnsByProduct(productId, branchId, top)) {
            rows.add(new ProductRecentHistoryResponse(
                    detail.getReturnID().getReturnDate(), formatInstant(detail.getReturnID().getReturnDate()),
                    "Trả hàng", branchName(detail.getReturnID().getBranchID()),
                    formatCode("RT", detail.getReturnID().getId()),
                    lotNumber(detail.getBatchID()), nullSafe(detail.getBaseQtyRestored()), null));
        }

        return rows.stream()
                .sorted(Comparator.comparing(ProductRecentHistoryResponse::getOccurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_HISTORY_LIMIT)
                .toList();
    }

    private int importQuantity(Batch batch) {
        if (batch.getImportQtyInUnit() != null) {
            return batch.getImportQtyInUnit();
        }
        return batch.getStorageQuantity() != null ? batch.getStorageQuantity() : 0;
    }

    private int nullSafe(Integer value) {
        return value != null ? value : 0;
    }

    private String lotNumber(Batch batch) {
        return batch != null ? batch.getLotNumber() : null;
    }

    private String branchName(Branch branch) {
        return branch != null && branch.getName() != null ? branch.getName() : "—";
    }

    private String formatCode(String prefix, Integer id) {
        return id != null ? prefix + "-" + String.format("%06d", id) : prefix;
    }

    private String formatOutType(String outType) {
        if (outType == null) {
            return "Khác";
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

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return DATE_TIME.withZone(ZoneId.systemDefault()).format(instant);
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE) : "—";
    }

    private ProductRowResponse toRow(Product product,
                                     Map<Integer, Long> stockByProduct,
                                     Map<Integer, Productunit> mainUnitByProduct,
                                     Map<Integer, String> ingredientByProduct) {
        Integer productId = product.getProductID();
        long stock = stockByProduct.getOrDefault(productId, 0L);
        Productunit mainUnit = mainUnitByProduct.get(productId);

        String statusCode = stockStatusCode(product, stock);

        return new ProductRowResponse(
                productId,
                product.getCode(),
                product.getName(),
                product.getTypeID() != null ? product.getTypeID().getName() : "—",
                ingredientByProduct.getOrDefault(productId, ""),
                mainUnit != null ? mainUnit.getUnitName() : "—",
                mainUnit != null ? mainUnit.getSellPrice() : null,
                stock,
                stockStatusLabel(statusCode),
                stockStatusCss(statusCode),
                "—"
        );
    }

    private String stockStatusCode(Product product, long stock) {
        if (stock <= 0) {
            return STOCK_STATUS_OUT;
        }
        int min = product.getMinStock() == null ? 0 : product.getMinStock();
        if (stock <= min) {
            return STOCK_STATUS_LOW;
        }
        return STOCK_STATUS_IN;
    }

    private String stockStatusLabel(String code) {
        return switch (code) {
            case STOCK_STATUS_IN -> LABEL_IN;
            case STOCK_STATUS_LOW -> LABEL_LOW;
            default -> LABEL_OUT;
        };
    }

    private String stockStatusCss(String code) {
        return switch (code) {
            case STOCK_STATUS_IN -> CSS_IN;
            case STOCK_STATUS_LOW -> CSS_LOW;
            default -> CSS_OUT;
        };
    }

    private boolean matchesKeyword(Product product, String normalizedKeyword) {
        if (normalizedKeyword.isBlank()) {
            return true;
        }
        return containsNormalized(product.getCode(), normalizedKeyword)
                || containsNormalized(product.getName(), normalizedKeyword)
                || containsNormalized(product.getBarcode(), normalizedKeyword)
                || containsNormalized(String.valueOf(product.getProductID()), normalizedKeyword);
    }

    private boolean typeMatches(Product product, Integer typeId) {
        if (typeId == null) {
            return true;
        }
        return product.getTypeID() != null && typeId.equals(product.getTypeID().getId());
    }

    private boolean producerMatches(Product product, Integer producerId) {
        if (producerId == null) {
            return true;
        }
        return product.getProducerID() != null && producerId.equals(product.getProducerID().getId());
    }

    private boolean stockStatusMatches(ProductRowResponse row, String stockStatus) {
        if (stockStatus == null || stockStatus.isBlank()) {
            return true;
        }
        String label = switch (stockStatus) {
            case STOCK_STATUS_IN -> LABEL_IN;
            case STOCK_STATUS_LOW -> LABEL_LOW;
            case STOCK_STATUS_OUT -> LABEL_OUT;
            default -> null;
        };
        return label != null && label.equals(row.getStockStatusLabel());
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
