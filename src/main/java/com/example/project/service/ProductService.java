package com.example.project.service;

import com.example.project.dto.response.ProductListStatsResponse;
import com.example.project.dto.response.ProductResponse;
import com.example.project.dto.response.ProductRowResponse;
import com.example.project.entity.Medicineapi;
import com.example.project.entity.Producer;
import com.example.project.entity.Product;
import com.example.project.entity.Productunit;
import com.example.project.entity.Type;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.MedicineapiRepository;
import com.example.project.repository.ProducerRepository;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.ProductunitRepository;
import com.example.project.repository.TypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ProductunitRepository productunitRepository;
    private final MedicineapiRepository medicineapiRepository;
    private final TypeRepository typeRepository;
    private final ProducerRepository producerRepository;

    public ProductService(ProductRepository productRepository,
                          BatchRepository batchRepository,
                          ProductunitRepository productunitRepository,
                          MedicineapiRepository medicineapiRepository,
                          TypeRepository typeRepository,
                          ProducerRepository producerRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.productunitRepository = productunitRepository;
        this.medicineapiRepository = medicineapiRepository;
        this.typeRepository = typeRepository;
        this.producerRepository = producerRepository;
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

        Map<String, Long> stockByProduct = loadStockByProduct();
        Map<Integer, Productunit> mainUnitByProduct = loadMainUnitByProduct();
        Map<String, String> ingredientByProduct = loadIngredientByProduct();

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
        Map<String, Long> stockByProduct = loadStockByProduct();

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

    private Map<String, Long> loadStockByProduct() {
        return batchRepository.sumStorageGroupedByProduct().stream()
                .filter(row -> row[0] != null)
                .collect(Collectors.toMap(
                        row -> (String) row[0],
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

    private Map<String, String> loadIngredientByProduct() {
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

    private ProductRowResponse toRow(Product product,
                                     Map<String, Long> stockByProduct,
                                     Map<String, Productunit> mainUnitByProduct,
                                     Map<String, String> ingredientByProduct) {
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
                || containsNormalized(product.getProductID(), normalizedKeyword);
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
