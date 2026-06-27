package com.example.project.service;

import com.example.project.dto.request.PurchaseInvoiceToBatchItemRequest;
import com.example.project.dto.request.PurchaseInvoiceToBatchRequest;
import com.example.project.dto.response.PurchaseInvoiceToBatchItemResponse;
import com.example.project.dto.response.PurchaseInvoiceToBatchPageResponse;
import com.example.project.entity.*;
import com.example.project.repository.BatchRepository;
import com.example.project.repository.PurchasedetailRepository;
import com.example.project.repository.PurchaseinvoiceRepository;
import com.example.project.repository.ProductunitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseInvoiceBatchService {

    private static final int NEAR_EXPIRY_DAYS = 90;

    private final PurchaseinvoiceRepository purchaseinvoiceRepository;
    private final PurchasedetailRepository purchasedetailRepository;
    private final BatchRepository batchRepository;
    private final ProductunitRepository productunitRepository;

    public PurchaseInvoiceBatchService(PurchaseinvoiceRepository purchaseinvoiceRepository,
                                       PurchasedetailRepository purchasedetailRepository,
                                       BatchRepository batchRepository,
                                       ProductunitRepository productunitRepository) {
        this.purchaseinvoiceRepository = purchaseinvoiceRepository;
        this.purchasedetailRepository = purchasedetailRepository;
        this.batchRepository = batchRepository;
        this.productunitRepository = productunitRepository;
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceToBatchPageResponse getPage(Integer purchaseId) {
        Purchaseinvoice invoice = purchaseinvoiceRepository.findByIdWithRelations(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        List<Purchasedetail> details = purchasedetailRepository.findByPurchaseIdWithProduct(purchaseId);

        List<Integer> detailIds = details.stream()
                .map(Purchasedetail::getId)
                .toList();

        Set<Integer> createdDetailIds = detailIds.isEmpty()
                ? Set.of()
                : batchRepository.findByPurchaseDetailIds(detailIds)
                .stream()
                .map(Batch::getPurchaseDetailID)
                .filter(Objects::nonNull)
                .map(Purchasedetail::getId)
                .collect(Collectors.toSet());

        List<PurchaseInvoiceToBatchItemResponse> items = details.stream()
                .map(detail -> toItemResponse(detail, createdDetailIds.contains(detail.getId())))
                .toList();

        long productCount = details.size();

        long batchToCreateCount = items.stream()
                .filter(item -> !item.isBatchCreated())
                .count();

        long nearExpiryCount = items.stream()
                .filter(item -> item.getExpirationDate() != null)
                .filter(item -> !item.isBatchCreated())
                .filter(item -> !item.getExpirationDate().isAfter(LocalDate.now().plusDays(NEAR_EXPIRY_DAYS)))
                .count();

        boolean allBatchCreated = productCount > 0 && batchToCreateCount == 0;

        return new PurchaseInvoiceToBatchPageResponse(
                invoice.getId(),
                formatPurchaseCode(invoice.getId()),
                formatInstant(invoice),
                invoice.getSupplierID() != null ? invoice.getSupplierID().getName() : "Không có",
                invoice.getBranchID() != null ? invoice.getBranchID().getName() : "Không có",
                invoice.getEmployeeID() != null ? invoice.getEmployeeID().getName() : "Không rõ",
                safe(invoice.getTotalAmount()),
                productCount,
                batchToCreateCount,
                nearExpiryCount,
                allBatchCreated,
                items
        );
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceToBatchRequest buildForm(Integer purchaseId) {
        List<Purchasedetail> details = purchasedetailRepository.findByPurchaseIdWithProduct(purchaseId);

        PurchaseInvoiceToBatchRequest form = new PurchaseInvoiceToBatchRequest();

        for (Purchasedetail detail : details) {
            PurchaseInvoiceToBatchItemRequest item = new PurchaseInvoiceToBatchItemRequest();
            item.setPurchaseDetailId(detail.getId());
            item.setLotNumber(detail.getLotNumber());
            item.setProductionDate(detail.getProductionDate());
            item.setExpirationDate(detail.getExpirationDate());

            form.getItems().add(item);
        }

        return form;
    }

    @Transactional
    public void saveDraft(Integer purchaseId, PurchaseInvoiceToBatchRequest form) {
        Purchaseinvoice invoice = purchaseinvoiceRepository.findByIdWithRelations(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        List<Purchasedetail> details = purchasedetailRepository.findByPurchaseIdWithProduct(invoice.getId());

        Map<Integer, Purchasedetail> detailMap = details.stream()
                .collect(Collectors.toMap(Purchasedetail::getId, detail -> detail));

        for (PurchaseInvoiceToBatchItemRequest item : safeItems(form)) {
            Purchasedetail detail = detailMap.get(item.getPurchaseDetailId());

            if (detail == null) {
                continue;
            }

            detail.setLotNumber(trimToNull(item.getLotNumber()));
            detail.setProductionDate(item.getProductionDate());
            detail.setExpirationDate(item.getExpirationDate());

            purchasedetailRepository.save(detail);
        }
    }

    @Transactional
    public void createBatches(Integer purchaseId, PurchaseInvoiceToBatchRequest form) {
        Purchaseinvoice invoice = purchaseinvoiceRepository.findByIdWithRelations(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        if (invoice.getBranchID() == null) {
            throw new IllegalArgumentException("Phiếu nhập chưa có chi nhánh nhận");
        }

        saveDraft(purchaseId, form);

        List<Purchasedetail> details = purchasedetailRepository.findByPurchaseIdWithProduct(purchaseId);

        if (details.isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập chưa có sản phẩm");
        }

        int createdCount = 0;

        for (Purchasedetail detail : details) {
            if (batchRepository.existsByPurchaseDetailId(detail.getId())) {
                continue;
            }

            validateDetailBeforeCreateBatch(detail);

            Product product = detail.getProductID();

            Productunit importUnit = resolveImportUnit(product);

            BigDecimal importPrice = safe(detail.getImportPrice());
            BigDecimal importPricePerBase = calculateImportPricePerBase(importPrice, importUnit);
            int storageQuantity = calculateBaseQuantity(detail.getQuantity(), importUnit);

            Batch batch = new Batch();
            batch.setProductID(product);
            batch.setPurchaseDetailID(detail);
            batch.setBranchID(invoice.getBranchID());
            batch.setStorageQuantity(storageQuantity);
            batch.setImportUnitID(importUnit);
            batch.setImportQtyInUnit(detail.getQuantity());
            batch.setImportPrice(importPrice);
            batch.setImportPricePerBase(importPricePerBase);
            batch.setImportDate(invoice.getDate());
            batch.setProductionDate(detail.getProductionDate());
            batch.setExpirationDate(detail.getExpirationDate());
            batch.setLotNumber(trimToNull(detail.getLotNumber()));
            batch.setStatus(true);
            batch.setNote("Tạo từ phiếu nhập " + formatPurchaseCode(invoice.getId()));

            batchRepository.save(batch);
            createdCount++;
        }

        if (createdCount == 0) {
            throw new IllegalArgumentException("Tất cả sản phẩm trong phiếu nhập này đã được tạo lô");
        }
    }

    private void validateDetailBeforeCreateBatch(Purchasedetail detail) {
        if (detail.getProductID() == null) {
            throw new IllegalArgumentException("Chi tiết phiếu nhập thiếu sản phẩm");
        }

        if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        if (detail.getImportPrice() == null || detail.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá nhập phải lớn hơn 0");
        }

        if (detail.getLotNumber() == null || detail.getLotNumber().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập số lô cho sản phẩm " + detail.getProductID().getName());
        }

        if (detail.getExpirationDate() == null) {
            throw new IllegalArgumentException("Vui lòng nhập hạn sử dụng cho sản phẩm " + detail.getProductID().getName());
        }

        if (!detail.getExpirationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hạn sử dụng phải lớn hơn ngày hiện tại: " + detail.getProductID().getName());
        }

        if (detail.getProductionDate() != null
                && detail.getExpirationDate().isBefore(detail.getProductionDate())) {
            throw new IllegalArgumentException("Hạn sử dụng không được trước ngày sản xuất: " + detail.getProductID().getName());
        }
    }

    private PurchaseInvoiceToBatchItemResponse toItemResponse(Purchasedetail detail, boolean batchCreated) {
        Product product = detail.getProductID();
        Productunit unit = resolveImportUnitOrNull(product).orElse(null);

        String statusDisplay;
        String statusCssClass;

        if (batchCreated) {
            statusDisplay = "Đã tạo lô";
            statusCssClass = "status-created";
        } else if (detail.getLotNumber() == null || detail.getLotNumber().isBlank()
                || detail.getExpirationDate() == null) {
            statusDisplay = "Cần kiểm tra";
            statusCssClass = "status-warning";
        } else if (!detail.getExpirationDate().isAfter(LocalDate.now().plusDays(NEAR_EXPIRY_DAYS))) {
            statusDisplay = "Cần kiểm tra hạn";
            statusCssClass = "status-warning";
        } else {
            statusDisplay = "Sẵn sàng";
            statusCssClass = "status-ready";
        }

        return new PurchaseInvoiceToBatchItemResponse(
                detail.getId(),
                product != null ? product.getProductID() : "",
                product != null ? product.getName() : "Không rõ",
                detail.getQuantity(),
                unit != null ? unit.getUnitName() : "Đơn vị cơ sở",
                detail.getImportPrice(),
                detail.getLotNumber(),
                detail.getProductionDate(),
                formatLocalDate(detail.getProductionDate()),
                detail.getExpirationDate(),
                formatLocalDate(detail.getExpirationDate()),
                batchCreated,
                statusDisplay,
                statusCssClass
        );
    }

    private BigDecimal calculateImportPricePerBase(BigDecimal importPrice, Productunit importUnit) {
        if (importUnit == null
                || importUnit.getRatio() == null
                || importUnit.getRatio().compareTo(BigDecimal.ZERO) <= 0) {
            return importPrice;
        }

        return importPrice.divide(importUnit.getRatio(), 2, RoundingMode.HALF_UP);
    }

    private List<PurchaseInvoiceToBatchItemRequest> safeItems(PurchaseInvoiceToBatchRequest form) {
        if (form == null || form.getItems() == null) {
            return List.of();
        }

        return form.getItems();
    }

    private String formatPurchaseCode(Integer id) {
        if (id == null) {
            return "PINV-000000";
        }

        return "PINV-" + String.format("%06d", id);
    }

    private String formatInstant(Purchaseinvoice invoice) {
        if (invoice.getDate() == null) {
            return "";
        }

        return DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault())
                .format(invoice.getDate());
    }

    private String formatLocalDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private Productunit resolveImportUnit(Product product) {
        return resolveImportUnitOrNull(product)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sản phẩm " + (product != null ? product.getName() : "") + " chưa có đơn vị nhập trong ProductUnit"
                ));
    }

    private Optional<Productunit> resolveImportUnitOrNull(Product product) {
        if (product == null || product.getProductID() == null) {
            return Optional.empty();
        }

        return productunitRepository.findByProductId(product.getProductID())
                .stream()
                .filter(unit -> !Boolean.FALSE.equals(unit.getIsActive()))
                .sorted(Comparator
                        .comparingInt(this::importUnitPriority)
                        .thenComparing(unit -> unit.getId() == null ? Integer.MAX_VALUE : unit.getId()))
                .findFirst();
    }

    private int importUnitPriority(Productunit unit) {
        if (Boolean.TRUE.equals(unit.getIsDefault())) {
            return 0;
        }

        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            return 1;
        }

        return 2;
    }

    private int calculateBaseQuantity(Integer importQuantity, Productunit importUnit) {
        BigDecimal quantity = BigDecimal.valueOf(importQuantity == null ? 0 : importQuantity);

        BigDecimal ratio = BigDecimal.ONE;

        if (importUnit != null
                && importUnit.getRatio() != null
                && importUnit.getRatio().compareTo(BigDecimal.ZERO) > 0) {
            ratio = importUnit.getRatio();
        }

        return quantity
                .multiply(ratio)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}