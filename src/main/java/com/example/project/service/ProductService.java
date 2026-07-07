package com.example.project.service;

import com.example.project.constant.RoleConstants;
import com.example.project.context.CurrentUserContext;
import com.example.project.dto.request.ProductCreateRequest;
import com.example.project.dto.request.ProductIngredientCreateRequest;
import com.example.project.dto.request.ProductPositionCreateRequest;
import com.example.project.dto.request.ProductUnitCreateRequest;
import com.example.project.dto.response.ProductBatchDetailResponse;
import com.example.project.dto.response.ProductDetailResponse;
import com.example.project.dto.response.ProductListStatsResponse;
import com.example.project.dto.response.ProductRecentHistoryResponse;
import com.example.project.dto.response.ProductResponse;
import com.example.project.dto.response.ProductRowResponse;
import com.example.project.dto.response.ProductUnitDetailResponse;
import com.example.project.entity.Batch;
import com.example.project.entity.Invoice;
import com.example.project.entity.Invoicedetail;
import com.example.project.entity.Medicineapi;
import com.example.project.entity.Origin;
import com.example.project.entity.Position;
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
import com.example.project.repository.OriginRepository;
import com.example.project.repository.PositionRepository;
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
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final OriginRepository originRepository;
    private final PositionRepository positionRepository;
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
                          OriginRepository originRepository,
                          PositionRepository positionRepository,
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
        this.originRepository = originRepository;
        this.positionRepository = positionRepository;
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
     * Full Product Detail payload. The store is single, so stock is one system-wide total (no
     * per-branch breakdown). Returns {@link Optional#empty()} when the product does not exist, so
     * the controller can 404 / redirect.
     */
    @Transactional(readOnly = true)
    public Optional<ProductDetailResponse> getProductDetail(Integer productId) {
        Optional<Product> productOpt = productRepository.findDetailById(productId);
        if (productOpt.isEmpty()) {
            return Optional.empty();
        }
        Product product = productOpt.get();

        List<ProductUnitDetailResponse> units = productunitRepository.findByProductId(productId).stream()
                .sorted(Comparator.comparing(Productunit::getRatio,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toUnitDetail)
                .toList();

        List<String> ingredients = medicineapiRepository.findByProductId(productId).stream()
                .map(this::formatIngredient)
                .toList();

        long totalStock = batchRepository.sumStorageByProduct(productId);
        String totalStatus = stockStatusCode(product, totalStock);

        List<ProductBatchDetailResponse> batches =
                batchRepository.findInStockBatchesByProduct(productId).stream()
                        .map(this::toBatchDetail)
                        .toList();

        boolean canViewHistory = canViewRecentHistory();
        List<ProductRecentHistoryResponse> recentHistory =
                canViewHistory ? loadRecentHistory(productId) : List.of();

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
        response.setBatches(batches);
        response.setCanViewRecentHistory(canViewHistory);
        response.setRecentHistory(recentHistory);
        return Optional.of(response);
    }

    /**
     * Whether the current user may see the "recent inventory history" block. Owner and Chief
     * Pharmacist may; Pharmacist may not.
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

    @Transactional(readOnly = true)
    public List<Origin> listOrigins() {
        return originRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(origin -> origin.getName() == null ? "" : origin.getName()))
                .toList();
    }

    /** Existing ingredient names, for the Create Product autocomplete (reuse instead of retyping). */
    @Transactional(readOnly = true)
    public List<String> listIngredientNames() {
        return medicineapiRepository.findDistinctApiNames();
    }

    /** Existing ingredient strengths, for the Create Product autocomplete. */
    @Transactional(readOnly = true)
    public List<String> listIngredientStrengths() {
        return medicineapiRepository.findDistinctStrengths();
    }

    /** Preview of the next auto-generated internal product code, shown read-only on the create form. */
    @Transactional(readOnly = true)
    public String previewNextProductCode() {
        return formatProductCode(productRepository.findMaxProductCodeSequence() + 1);
    }

    /**
     * Next free internal product code: "SP" + the highest existing sequence + 1, skipping any code
     * already taken (defensive against gaps/races; the unique constraint is the final backstop).
     */
    private String generateNextProductCode() {
        long seq = productRepository.findMaxProductCodeSequence();
        String code;
        do {
            seq++;
            code = formatProductCode(seq);
        } while (productRepository.existsByCode(code));
        return code;
    }

    private String formatProductCode(long sequence) {
        return "SP" + String.format("%06d", sequence);
    }

    // --- Create Product ------------------------------------------------------

    /**
     * Creates a product master in one transaction: {@code Product} + its {@code ProductUnit}s,
     * plus optional {@code MedicineAPI} ingredients and {@code Position}s. It does NOT create any
     * stock ({@code Batch}) or purchase/invoice data — stock arises only from goods-receipt later.
     *
     * @return the new product id
     * @throws ProductValidationException when validation fails; the transaction rolls back so
     *                                    nothing is persisted.
     */
    @Transactional
    public Integer createProduct(ProductCreateRequest request) {
        List<String> errors = new ArrayList<>();

        String name = trimToNull(request.getName());
        String barcode = trimToNull(request.getBarcode());

        if (name == null) {
            errors.add("Tên hàng hóa không được để trống");
        }
        // Mã hàng is an internal code — auto-generated ("SP" + sequence), not entered by the user.
        if (barcode != null && productRepository.existsByBarcode(barcode)) {
            errors.add("Barcode '" + barcode + "' đã tồn tại");
        }
        if (request.getMinStock() != null && request.getMaxStock() != null
                && request.getMinStock() > request.getMaxStock()) {
            errors.add("Tồn tối thiểu không được lớn hơn tồn tối đa");
        }
        if (request.getTypeId() != null && !typeRepository.existsById(request.getTypeId())) {
            errors.add("Loại hàng không hợp lệ");
        }
        if (request.getProducerId() != null && !producerRepository.existsById(request.getProducerId())) {
            errors.add("Nhà sản xuất không hợp lệ");
        }
        if (request.getOriginId() != null && !originRepository.existsById(request.getOriginId())) {
            errors.add("Xuất xứ không hợp lệ");
        }

        List<ResolvedUnit> resolvedUnits = validateAndResolveUnits(request.getUnits(), errors);

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }

        Product product = new Product();
        product.setName(name);
        product.setCode(generateNextProductCode());
        product.setBarcode(barcode);
        product.setRegistrationNumber(trimToNull(request.getRegistrationNumber()));
        product.setMinStock(request.getMinStock());
        product.setMaxStock(request.getMaxStock());
        product.setStatus(request.getStatus() == null ? Boolean.TRUE : request.getStatus());
        product.setNote(trimToNull(request.getNote()));
        if (request.getTypeId() != null) {
            product.setTypeID(typeRepository.getReferenceById(request.getTypeId()));
        }
        if (request.getProducerId() != null) {
            product.setProducerID(producerRepository.getReferenceById(request.getProducerId()));
        }
        if (request.getOriginId() != null) {
            product.setOriginID(originRepository.getReferenceById(request.getOriginId()));
        }
        Product saved = productRepository.save(product);

        for (ResolvedUnit resolved : resolvedUnits) {
            Productunit unit = new Productunit();
            unit.setProductID(saved);
            unit.setUnitName(resolved.name());
            unit.setRatio(BigDecimal.valueOf(resolved.ratio()));
            unit.setSellPrice(resolved.sellPrice());
            unit.setIsBaseUnit(resolved.baseUnit());
            unit.setIsDefault(resolved.defaultUnit());
            unit.setIsActive(resolved.active());
            productunitRepository.save(unit);
        }

        if (request.getIngredients() != null) {
            for (ProductIngredientCreateRequest ingredient : request.getIngredients()) {
                String apiName = trimToNull(ingredient.getApiName());
                if (apiName == null) {
                    continue;
                }
                Medicineapi api = new Medicineapi();
                api.setProductID(saved);
                api.setApiName(apiName);
                api.setStrength(trimToNull(ingredient.getStrength()));
                medicineapiRepository.save(api);
            }
        }

        if (request.getPositions() != null) {
            for (ProductPositionCreateRequest position : request.getPositions()) {
                String positionName = trimToNull(position.getName());
                if (positionName == null) {
                    continue;
                }
                Position entity = new Position();
                entity.setProductID(saved);
                entity.setName(positionName);
                positionRepository.save(entity);
            }
        }

        return saved.getProductID();
    }

    // --- Edit Product ----------------------------------------------------------

    /**
     * Builds the Edit Product form, prefilled from the existing {@code Product} + its units,
     * ingredients and storage positions. Units are listed smallest-to-largest (matching the Create
     * form's convention) so {@code quantityRelativeToPrevious} can be reconstructed from the stored
     * cumulative ratios. Returns {@link Optional#empty()} when the product does not exist.
     */
    @Transactional(readOnly = true)
    public Optional<ProductCreateRequest> getEditForm(Integer productId) {
        Optional<Product> productOpt = productRepository.findDetailById(productId);
        if (productOpt.isEmpty()) {
            return Optional.empty();
        }
        Product product = productOpt.get();

        ProductCreateRequest form = new ProductCreateRequest();
        form.setName(product.getName());
        form.setCode(product.getCode());
        form.setBarcode(product.getBarcode());
        form.setTypeId(product.getTypeID() != null ? product.getTypeID().getId() : null);
        form.setProducerId(product.getProducerID() != null ? product.getProducerID().getId() : null);
        form.setOriginId(product.getOriginID() != null ? product.getOriginID().getId() : null);
        form.setRegistrationNumber(product.getRegistrationNumber());
        form.setMinStock(product.getMinStock());
        form.setMaxStock(product.getMaxStock());
        form.setStatus(product.getStatus());
        form.setNote(product.getNote());

        List<Productunit> units = productunitRepository.findByProductId(productId).stream()
                .sorted(Comparator.comparing(Productunit::getRatio, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        BigDecimal previousRatio = null;
        for (Productunit unit : units) {
            ProductUnitCreateRequest row = new ProductUnitCreateRequest();
            row.setUnitName(unit.getUnitName());
            row.setSellPrice(unit.getSellPrice());
            row.setBaseUnit(Boolean.TRUE.equals(unit.getIsBaseUnit()));
            row.setDefaultUnit(Boolean.TRUE.equals(unit.getIsDefault()));
            row.setActive(!Boolean.FALSE.equals(unit.getIsActive()));
            row.setQuantityRelativeToPrevious(previousRatio == null || previousRatio.signum() == 0
                    ? null
                    : unit.getRatio().divide(previousRatio, 0, RoundingMode.HALF_UP).intValue());
            previousRatio = unit.getRatio();
            form.getUnits().add(row);
        }

        for (Medicineapi api : medicineapiRepository.findByProductId(productId)) {
            ProductIngredientCreateRequest row = new ProductIngredientCreateRequest();
            row.setApiName(api.getApiName());
            row.setStrength(api.getStrength());
            form.getIngredients().add(row);
        }

        for (Position position : positionRepository.findByProductId(productId)) {
            ProductPositionCreateRequest row = new ProductPositionCreateRequest();
            row.setName(position.getName());
            form.getPositions().add(row);
        }

        return Optional.of(form);
    }

    /**
     * Saves edits to an existing product. Scope is deliberately narrower than
     * {@link #createProduct}: existing {@code ProductUnit} rows can only be edited in place (name /
     * ratio / sell price / flags) — units can never be added or removed here, because
     * {@code Batch}, {@code InvoiceDetail}, {@code StockOutDetail} and {@code ReturnDetail} all hold
     * FK references to a specific {@code ProductUnit} row, so changing the row count would risk
     * orphaning that history. Ingredients and storage positions carry no such references, so both
     * lists are simply replaced wholesale (delete-all-then-recreate), same as on create.
     *
     * @throws ProductValidationException when validation fails (including a unit count mismatch);
     *                                    the transaction rolls back so nothing is persisted.
     * @throws IllegalArgumentException   when {@code productId} does not refer to an existing product
     */
    @Transactional
    public void updateProduct(Integer productId, ProductCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa"));

        List<String> errors = new ArrayList<>();

        String name = trimToNull(request.getName());
        String barcode = trimToNull(request.getBarcode());

        if (name == null) {
            errors.add("Tên hàng hóa không được để trống");
        }
        if (barcode != null && productRepository.existsByBarcodeExcludingProduct(barcode, productId)) {
            errors.add("Barcode '" + barcode + "' đã tồn tại");
        }
        if (request.getMinStock() != null && request.getMaxStock() != null
                && request.getMinStock() > request.getMaxStock()) {
            errors.add("Tồn tối thiểu không được lớn hơn tồn tối đa");
        }
        if (request.getTypeId() != null && !typeRepository.existsById(request.getTypeId())) {
            errors.add("Loại hàng không hợp lệ");
        }
        if (request.getProducerId() != null && !producerRepository.existsById(request.getProducerId())) {
            errors.add("Nhà sản xuất không hợp lệ");
        }
        if (request.getOriginId() != null && !originRepository.existsById(request.getOriginId())) {
            errors.add("Xuất xứ không hợp lệ");
        }

        List<Productunit> existingUnits = productunitRepository.findByProductId(productId).stream()
                .sorted(Comparator.comparing(Productunit::getRatio, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<ProductUnitCreateRequest> requestedUnitRows = request.getUnits() == null ? List.of()
                : request.getUnits().stream().filter(u -> trimToNull(u.getUnitName()) != null).toList();
        if (requestedUnitRows.size() != existingUnits.size()) {
            errors.add("Không thể thêm hoặc bớt đơn vị tính khi sửa hàng hóa");
        }

        List<ResolvedUnit> resolvedUnits = validateAndResolveUnits(request.getUnits(), errors);

        if (!errors.isEmpty()) {
            throw new ProductValidationException(errors);
        }

        product.setName(name);
        product.setBarcode(barcode);
        product.setRegistrationNumber(trimToNull(request.getRegistrationNumber()));
        product.setMinStock(request.getMinStock());
        product.setMaxStock(request.getMaxStock());
        product.setStatus(request.getStatus() == null ? Boolean.TRUE : request.getStatus());
        product.setNote(trimToNull(request.getNote()));
        product.setTypeID(request.getTypeId() != null ? typeRepository.getReferenceById(request.getTypeId()) : null);
        product.setProducerID(request.getProducerId() != null
                ? producerRepository.getReferenceById(request.getProducerId()) : null);
        product.setOriginID(request.getOriginId() != null
                ? originRepository.getReferenceById(request.getOriginId()) : null);
        productRepository.save(product);

        for (int i = 0; i < existingUnits.size(); i++) {
            Productunit unit = existingUnits.get(i);
            ResolvedUnit resolved = resolvedUnits.get(i);
            unit.setUnitName(resolved.name());
            unit.setRatio(BigDecimal.valueOf(resolved.ratio()));
            unit.setSellPrice(resolved.sellPrice());
            unit.setIsBaseUnit(resolved.baseUnit());
            unit.setIsDefault(resolved.defaultUnit());
            unit.setIsActive(resolved.active());
            productunitRepository.save(unit);
        }

        medicineapiRepository.deleteAll(medicineapiRepository.findByProductId(productId));
        if (request.getIngredients() != null) {
            for (ProductIngredientCreateRequest ingredient : request.getIngredients()) {
                String apiName = trimToNull(ingredient.getApiName());
                if (apiName == null) {
                    continue;
                }
                Medicineapi api = new Medicineapi();
                api.setProductID(product);
                api.setApiName(apiName);
                api.setStrength(trimToNull(ingredient.getStrength()));
                medicineapiRepository.save(api);
            }
        }

        positionRepository.deleteAll(positionRepository.findByProductId(productId));
        if (request.getPositions() != null) {
            for (ProductPositionCreateRequest position : request.getPositions()) {
                String positionName = trimToNull(position.getName());
                if (positionName == null) {
                    continue;
                }
                Position entity = new Position();
                entity.setProductID(product);
                entity.setName(positionName);
                positionRepository.save(entity);
            }
        }
    }

    /**
     * Validates the unit rows and resolves each to a cumulative ratio (relative to the smallest
     * unit) and a final sell price (actual if given, else base price × ratio). Any problem is added
     * to {@code errors}; the returned list is only meaningful when {@code errors} stays empty.
     */
    private List<ResolvedUnit> validateAndResolveUnits(List<ProductUnitCreateRequest> units, List<String> errors) {
        List<ResolvedUnit> resolved = new ArrayList<>();

        List<ProductUnitCreateRequest> rows = units == null ? List.of()
                : units.stream().filter(u -> trimToNull(u.getUnitName()) != null).toList();
        if (rows.isEmpty()) {
            errors.add("Phải có ít nhất 1 đơn vị");
            return resolved;
        }

        long baseCount = rows.stream().filter(ProductUnitCreateRequest::isBaseUnit).count();
        if (baseCount == 0) {
            errors.add("Phải có đúng 1 đơn vị cơ bản (isBaseUnit)");
        } else if (baseCount > 1) {
            errors.add("Chỉ được có đúng 1 đơn vị cơ bản");
        }

        long defaultCount = rows.stream().filter(ProductUnitCreateRequest::isDefaultUnit).count();
        if (defaultCount == 0) {
            errors.add("Phải có đúng 1 đơn vị mặc định (isDefault)");
        } else if (defaultCount > 1) {
            errors.add("Chỉ được có đúng 1 đơn vị mặc định");
        }

        Set<String> seenNames = new HashSet<>();
        for (ProductUnitCreateRequest row : rows) {
            String key = normalize(row.getUnitName());
            if (!seenNames.add(key)) {
                errors.add("Tên đơn vị bị trùng: " + trimToNull(row.getUnitName()));
            }
        }

        // The base unit is the smallest unit → must be the first row (ratio = 1).
        if (baseCount == 1 && !rows.get(0).isBaseUnit()) {
            errors.add("Đơn vị cơ bản phải là đơn vị nhỏ nhất (dòng đầu tiên)");
        }

        long[] ratios = new long[rows.size()];
        ratios[0] = 1L;
        for (int i = 1; i < rows.size(); i++) {
            String unitName = trimToNull(rows.get(i).getUnitName());
            Integer qty = rows.get(i).getQuantityRelativeToPrevious();
            if (qty == null || qty < 1) {
                errors.add("Số lượng quy đổi của '" + unitName + "' phải là số nguyên dương");
                ratios[i] = ratios[i - 1];
            } else {
                ratios[i] = ratios[i - 1] * qty;
                if (ratios[i] <= ratios[i - 1]) {
                    errors.add("Đơn vị '" + unitName + "' phải có tỷ lệ quy đổi lớn hơn đơn vị trước");
                }
            }
        }

        BigDecimal basePrice = rows.get(0).getSellPrice();
        if (basePrice == null || basePrice.signum() <= 0) {
            errors.add("Giá bán đơn vị cơ bản phải lớn hơn 0");
        }

        for (int i = 0; i < rows.size(); i++) {
            ProductUnitCreateRequest row = rows.get(i);
            String unitName = trimToNull(row.getUnitName());
            BigDecimal actual = row.getSellPrice();
            BigDecimal finalPrice;
            if (actual != null && actual.signum() > 0) {
                finalPrice = actual;
            } else if (basePrice != null && basePrice.signum() > 0) {
                finalPrice = basePrice.multiply(BigDecimal.valueOf(ratios[i]));   // suggested price
            } else {
                finalPrice = null;
            }
            if (finalPrice == null || finalPrice.signum() <= 0) {
                errors.add("Giá bán của '" + unitName + "' phải lớn hơn 0");
                finalPrice = BigDecimal.ZERO;
            }
            resolved.add(new ResolvedUnit(unitName, ratios[i], finalPrice,
                    row.isBaseUnit(), row.isDefaultUnit(), row.isActive()));
        }

        return resolved;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** A unit row resolved to its cumulative ratio and final price, ready to persist. */
    private record ResolvedUnit(String name, long ratio, BigDecimal sellPrice,
                                boolean baseUnit, boolean defaultUnit, boolean active) {
    }

    // --- helpers -------------------------------------------------------------

    /** On-hand stock per product = SUM(Batch.storageQuantity) (single store — no branch scoping). */
    private Map<Integer, Long> loadStockByProduct() {
        List<Object[]> rows = batchRepository.sumStorageGroupedByProduct();

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

    private ProductBatchDetailResponse toBatchDetail(Batch batch) {
        return new ProductBatchDetailResponse(
                batch.getId(),
                batch.getBatchName(),
                batch.getLotNumber(),
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

    private List<ProductRecentHistoryResponse> loadRecentHistory(Integer productId) {
        Pageable top = PageRequest.of(0, RECENT_HISTORY_LIMIT);
        List<ProductRecentHistoryResponse> rows = new ArrayList<>();

        for (Batch batch : batchRepository.findRecentImportsByProduct(productId, top)) {
            rows.add(new ProductRecentHistoryResponse(
                    batch.getImportDate(), formatInstant(batch.getImportDate()), "Nhập kho",
                    batch.getBatchName(), batch.getLotNumber(),
                    importQuantity(batch), null));
        }

        for (Invoicedetail detail : invoicedetailRepository.findRecentSalesByProduct(productId, top)) {
            Invoice invoice = detail.getInvoiceID();
            rows.add(new ProductRecentHistoryResponse(
                    invoice.getDate(), formatInstant(invoice.getDate()), "Bán hàng",
                    invoice.getInvoiceCode(),
                    lotNumber(detail.getBatchID()), -nullSafe(detail.getBaseQtyDeducted()), null));
        }

        for (Stockoutdetail detail : stockoutdetailRepository.findRecentStockOutsByProduct(productId, top)) {
            Stockout stockOut = detail.getStockOutID();
            rows.add(new ProductRecentHistoryResponse(
                    stockOut.getDate(), formatInstant(stockOut.getDate()),
                    "Xuất kho - " + formatOutType(stockOut.getOutType()),
                    formatCode("SO", stockOut.getId()),
                    lotNumber(detail.getBatchID()), -nullSafe(detail.getBaseQtyDeducted()), detail.getNote()));
        }

        for (Returndetail detail : returndetailRepository.findRecentReturnsByProduct(productId, top)) {
            rows.add(new ProductRecentHistoryResponse(
                    detail.getReturnID().getReturnDate(), formatInstant(detail.getReturnID().getReturnDate()),
                    "Trả hàng",
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
