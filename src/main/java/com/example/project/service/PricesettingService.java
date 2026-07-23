package com.example.project.service;

import com.example.project.dto.response.PriceSettingRowResponse;
import com.example.project.entity.Batch;
import com.example.project.entity.Product;
import com.example.project.entity.Productunit;
import com.example.project.entity.Type;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.ProductRepository;
import com.example.project.repository.ProductunitRepository;
import com.example.project.repository.TypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Cài đặt giá bán" (Price Settings) — lets the Owner change any product's sell price directly
 * from one screen instead of opening each product's own edit page. Per the user's own framing
 * (2026-07-23): "cho phép thay đổi giá bán của các sản phẩm ... thay vì phải mở chi tiết của từng
 * sản phẩm". Deliberately NOT a markup/cost-based price calculator — it edits
 * {@code Productunit.sellPrice} directly; the latest import price shown per row is read-only
 * reference info only, never written back or used in a formula. No schema change: reuses the
 * existing {@code Productunit}/{@code Product}/{@code Batch} tables.
 */
@Service
public class PricesettingService {

    private final ProductunitRepository productunitRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final TypeRepository typeRepository;

    public PricesettingService(ProductunitRepository productunitRepository,
                               ProductRepository productRepository,
                               BatchRepository batchRepository,
                               TypeRepository typeRepository) {
        this.productunitRepository = productunitRepository;
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.typeRepository = typeRepository;
    }

    @Transactional(readOnly = true)
    public Page<PriceSettingRowResponse> search(String keyword, Integer typeId, Pageable pageable) {
        final String normalizedKeyword = normalize(keyword);

        Map<Integer, Product> productById = productRepository.findAllWithRelations().stream()
                .collect(Collectors.toMap(Product::getProductID, product -> product));

        Map<Integer, Batch> latestBatchByProduct = latestBatchPerProduct();

        List<Productunit> units = productunitRepository.findAllWithProduct();

        List<PriceSettingRowResponse> filtered = units.stream()
                .filter(unit -> unit.getProductID() != null)
                .filter(unit -> productById.containsKey(unit.getProductID().getProductID()))
                .filter(unit -> matchesType(productById.get(unit.getProductID().getProductID()), typeId))
                .filter(unit -> matchesKeyword(productById.get(unit.getProductID().getProductID()), unit,
                        normalizedKeyword))
                .sorted(Comparator
                        .comparing((Productunit u) -> productById.get(u.getProductID().getProductID()).getName())
                        .thenComparing(u -> u.getRatio()))
                .map(unit -> toRow(unit, productById.get(unit.getProductID().getProductID()),
                        latestBatchByProduct.get(unit.getProductID().getProductID())))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<PriceSettingRowResponse> content = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    public List<Type> listTypes() {
        return typeRepository.findAll().stream()
                .sorted(Comparator.comparing(Type::getName))
                .toList();
    }

    /**
     * Updates a single {@code ProductUnit.sellPrice}. Each row on the screen saves independently
     * (same pattern as the Permission Table's per-cell save), so one bad value never blocks the
     * rest.
     *
     * <p><strong>Base-unit cascade:</strong> when the edited row is the product's base unit, every
     * sibling unit whose current price still exactly matches {@code oldBasePrice × ratio} is
     * recomputed to {@code newBasePrice × ratio} too — those units were never manually customized,
     * they were just following the base price. A sibling whose price does <em>not</em> match that
     * formula has clearly been hand-adjusted at some point and is left untouched. There is no
     * separate "manually overridden" flag in the schema; this is inferred purely from whether the
     * stored value still agrees with the ratio formula, so it needs no migration. Editing a
     * non-base unit never cascades to anything else.</p>
     *
     * @return how many sibling units were cascaded (0 for a non-base-unit edit or when nothing
     * qualified)
     */
    @Transactional
    public int updatePrice(Integer productUnitId, BigDecimal sellPrice) {
        if (sellPrice == null || sellPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }
        Productunit unit = productunitRepository.findById(productUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị sản phẩm"));

        BigDecimal oldBasePrice = unit.getSellPrice();
        unit.setSellPrice(sellPrice);
        productunitRepository.save(unit);

        if (!Boolean.TRUE.equals(unit.getIsBaseUnit()) || unit.getProductID() == null) {
            return 0;
        }
        return cascadeToSiblings(unit, oldBasePrice, sellPrice);
    }

    private int cascadeToSiblings(Productunit baseUnit, BigDecimal oldBasePrice, BigDecimal newBasePrice) {
        List<Productunit> siblings = productunitRepository.findByProductId(baseUnit.getProductID().getProductID());

        int cascaded = 0;
        for (Productunit sibling : siblings) {
            if (sibling.getId().equals(baseUnit.getId()) || sibling.getRatio() == null) {
                continue;
            }
            BigDecimal expectedOldPrice = roundMoney(oldBasePrice.multiply(sibling.getRatio()));
            BigDecimal currentPrice = roundMoney(sibling.getSellPrice());
            if (currentPrice.compareTo(expectedOldPrice) != 0) {
                continue; // hand-adjusted away from the ratio at some point — leave it alone
            }
            sibling.setSellPrice(roundMoney(newBasePrice.multiply(sibling.getRatio())));
            productunitRepository.save(sibling);
            cascaded++;
        }
        return cascaded;
    }

    private BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ helpers

    private Map<Integer, Batch> latestBatchPerProduct() {
        return batchRepository.findAll().stream()
                .filter(batch -> batch.getProductID() != null && batch.getImportDate() != null)
                .collect(Collectors.toMap(
                        batch -> batch.getProductID().getProductID(),
                        batch -> batch,
                        (a, b) -> a.getImportDate().isAfter(b.getImportDate()) ? a : b));
    }

    private boolean matchesType(Product product, Integer typeId) {
        if (typeId == null) {
            return true;
        }
        return product.getTypeID() != null && typeId.equals(product.getTypeID().getId());
    }

    private boolean matchesKeyword(Product product, Productunit unit, String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        return containsNormalized(product.getCode(), normalizedKeyword)
                || containsNormalized(product.getName(), normalizedKeyword)
                || containsNormalized(unit.getUnitName(), normalizedKeyword);
    }

    private PriceSettingRowResponse toRow(Productunit unit, Product product, Batch latestBatch) {
        return new PriceSettingRowResponse(
                unit.getId(),
                product.getProductID(),
                product.getCode(),
                product.getName(),
                product.getTypeID() != null ? product.getTypeID().getName() : "—",
                unit.getUnitName(),
                Boolean.TRUE.equals(unit.getIsBaseUnit()),
                unit.getSellPrice(),
                latestBatch != null ? latestBatch.getImportPricePerBase() : null,
                latestBatch != null ? formatInstant(latestBatch.getImportDate()) : null
        );
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault())
                .format(instant);
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
