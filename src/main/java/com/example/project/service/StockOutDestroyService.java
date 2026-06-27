package com.example.project.service;

import com.example.project.dto.request.StockOutDestroyCreateRequest;
import com.example.project.dto.request.StockOutDestroyItemRequest;
import com.example.project.dto.response.StockOutDestroyCandidateResponse;
import com.example.project.entity.*;
import com.example.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockOutDestroyService {

    private static final String OUT_TYPE_DESTROY = "DESTROY";

    private final StockoutRepository stockoutRepository;
    private final StockoutdetailRepository stockoutdetailRepository;
    private final BatchRepository batchRepository;
    private final BranchRepository branchRepository;
    private final AccountRepository accountRepository;
    private final StatusRepository statusRepository;

    public StockOutDestroyService(StockoutRepository stockoutRepository,
                                  StockoutdetailRepository stockoutdetailRepository,
                                  BatchRepository batchRepository,
                                  BranchRepository branchRepository,
                                  AccountRepository accountRepository,
                                  StatusRepository statusRepository) {
        this.stockoutRepository = stockoutRepository;
        this.stockoutdetailRepository = stockoutdetailRepository;
        this.batchRepository = batchRepository;
        this.branchRepository = branchRepository;
        this.accountRepository = accountRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional(readOnly = true)
    public List<StockOutDestroyCandidateResponse> listAvailableBatches(Integer branchId, String keyword) {
        String normalizedKeyword = normalize(keyword);

        return batchRepository.findAvailableBatchesForDestroy()
                .stream()
                .filter(batch -> branchId == null || branchMatches(batch, branchId))
                .filter(batch -> matchesKeyword(batch, normalizedKeyword))
                .map(this::toCandidateResponse)
                .toList();
    }

    @Transactional
    public Integer createDestroyStockOut(StockOutDestroyCreateRequest request, Integer currentAccountId) {
        validateRequest(request);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh"));

        Account creator = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản hiện tại"));

        Status pendingStatus = resolvePendingStatus();

        Map<Integer, StockOutDestroyItemRequest> itemMap = request.getItems()
                .stream()
                .collect(Collectors.toMap(
                        StockOutDestroyItemRequest::getBatchId,
                        item -> item,
                        (first, second) -> {
                            first.setQuantity(first.getQuantity() + second.getQuantity());

                            String firstReason = first.getReason() == null ? "" : first.getReason();
                            String secondReason = second.getReason() == null ? "" : second.getReason();

                            if (!secondReason.isBlank() && !firstReason.contains(secondReason)) {
                                first.setReason(firstReason.isBlank() ? secondReason : firstReason + ", " + secondReason);
                            }

                            return first;
                        }
                ));

        List<Batch> selectedBatches = batchRepository.findAllById(itemMap.keySet());

        if (selectedBatches.size() != itemMap.size()) {
            throw new IllegalArgumentException("Một số lô hàng không tồn tại");
        }

        validateSelectedBatches(branch, selectedBatches, itemMap);

        Stockout stockOut = new Stockout();
        stockOut.setOutType(OUT_TYPE_DESTROY);
        stockOut.setBranchID(branch);
        stockOut.setDate(Instant.now());
        stockOut.setCreatedBy(creator);
        stockOut.setReason(request.getReason().trim());
        stockOut.setTargetBranchID(null);
        stockOut.setExpenseID(null);
        stockOut.setStatusID(pendingStatus);
        stockOut.setNote(trimToNull(request.getNote()));

        Stockout savedStockOut = stockoutRepository.save(stockOut);

        for (Batch batch : selectedBatches) {
            StockOutDestroyItemRequest item = itemMap.get(batch.getId());

            Product product = batch.getProductID();
            Productunit unit = resolveUnit(batch, product);

            BigDecimal unitCost = resolveUnitCost(batch);
            BigDecimal lineCost = unitCost.multiply(BigDecimal.valueOf(item.getQuantity()));

            Stockoutdetail detail = new Stockoutdetail();
            detail.setStockOutID(savedStockOut);
            detail.setProductID(product);
            detail.setProductUnitID(unit);
            detail.setBatchID(batch);
            detail.setQuantity(item.getQuantity());
            detail.setBaseQtyDeducted(item.getQuantity());
            detail.setUnitCostPrice(unitCost);
            detail.setLineCost(lineCost);
            detail.setNote(trimToNull(item.getReason()));

            stockoutdetailRepository.save(detail);
        }

        return savedStockOut.getId();
    }

    @Transactional(readOnly = true)
    public List<Branch> listBranches() {
        return branchRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(branch -> branch.getName() == null ? "" : branch.getName()))
                .toList();
    }

    private void validateRequest(StockOutDestroyCreateRequest request) {
        if (request.getBranchId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn chi nhánh");
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một hàng hóa cần hủy");
        }

        for (StockOutDestroyItemRequest item : request.getItems()) {
            if (item.getBatchId() == null) {
                throw new IllegalArgumentException("Dữ liệu lô hàng không hợp lệ");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng hủy phải lớn hơn 0");
            }
        }
    }

    private void validateSelectedBatches(Branch branch,
                                         List<Batch> selectedBatches,
                                         Map<Integer, StockOutDestroyItemRequest> itemMap) {
        for (Batch batch : selectedBatches) {
            StockOutDestroyItemRequest item = itemMap.get(batch.getId());

            if (batch.getBranchID() == null || !Objects.equals(batch.getBranchID().getId(), branch.getId())) {
                throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " không thuộc chi nhánh đã chọn");
            }

            if (!Boolean.TRUE.equals(batch.getStatus())) {
                throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " không còn hoạt động");
            }

            if (batch.getStorageQuantity() == null || batch.getStorageQuantity() <= 0) {
                throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " đã hết tồn kho");
            }

            if (item.getQuantity() > batch.getStorageQuantity()) {
                throw new IllegalArgumentException(
                        "Số lượng hủy của lô " + displayBatch(batch)
                                + " không được vượt quá tồn hiện tại: " + batch.getStorageQuantity()
                );
            }
        }
    }

    private Status resolvePendingStatus() {
        List<String> possibleStatusNames = List.of(
                "Chờ xử lý",
                "Chờ phê duyệt",
                "Chờ đối chiếu",
                "PENDING"
        );

        for (String statusName : possibleStatusNames) {
            Optional<Status> status = statusRepository.findByNameIgnoreCase(statusName);

            if (status.isPresent()) {
                return status.get();
            }
        }

        throw new IllegalArgumentException("Không tìm thấy trạng thái chờ xử lý trong bảng Status");
    }

    private Productunit resolveUnit(Batch batch, Product product) {
        if (batch.getImportUnitID() != null) {
            return batch.getImportUnitID();
        }

        if (product != null && product.getBaseUnitID() != null) {
            return product.getBaseUnitID();
        }

        throw new IllegalArgumentException("Lô hàng " + displayBatch(batch) + " chưa có đơn vị tính");
    }

    private BigDecimal resolveUnitCost(Batch batch) {
        if (batch.getImportPricePerBase() != null) {
            return batch.getImportPricePerBase();
        }

        if (batch.getImportPrice() != null) {
            return batch.getImportPrice();
        }

        return BigDecimal.ZERO;
    }

    private StockOutDestroyCandidateResponse toCandidateResponse(Batch batch) {
        Product product = batch.getProductID();
        Productunit unit = resolveCandidateUnit(batch, product);

        return new StockOutDestroyCandidateResponse(
                batch.getId(),
                product != null ? product.getProductID() : "",
                product != null ? product.getName() : "Không rõ",
                batch.getBranchID() != null ? batch.getBranchID().getId() : null,
                batch.getBranchID() != null ? batch.getBranchID().getName() : "Không có",
                batch.getLotNumber(),
                batch.getExpirationDate(),
                formatLocalDate(batch.getExpirationDate()),
                batch.getStorageQuantity(),
                unit != null ? unit.getId() : null,
                unit != null ? unit.getUnitName() : "Đơn vị",
                resolveUnitCost(batch)
        );
    }

    private Productunit resolveCandidateUnit(Batch batch, Product product) {
        if (batch.getImportUnitID() != null) {
            return batch.getImportUnitID();
        }

        if (product != null) {
            return product.getBaseUnitID();
        }

        return null;
    }

    private boolean branchMatches(Batch batch, Integer branchId) {
        return batch.getBranchID() != null && Objects.equals(batch.getBranchID().getId(), branchId);
    }

    private boolean matchesKeyword(Batch batch, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        Product product = batch.getProductID();

        return containsNormalized(batch.getLotNumber(), keyword)
                || product != null && (
                containsNormalized(product.getProductID(), keyword)
                        || containsNormalized(product.getName(), keyword)
                        || containsNormalized(product.getCode(), keyword)
                        || containsNormalized(product.getBarcode(), keyword)
        );
    }

    private String displayBatch(Batch batch) {
        Product product = batch.getProductID();

        String productName = product != null ? product.getName() : "Không rõ sản phẩm";
        String lotNumber = batch.getLotNumber() != null ? batch.getLotNumber() : "Không có số lô";

        return productName + " - " + lotNumber;
    }

    private String formatLocalDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private boolean containsNormalized(String value, String keyword) {
        return value != null && normalize(value).contains(keyword);
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}