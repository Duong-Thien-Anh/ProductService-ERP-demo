package vn.perp.producterpservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.perp.producterpservice.dto.request.StockAdjustmentRequest;
import vn.perp.producterpservice.dto.response.PagedResponse;
import vn.perp.producterpservice.dto.response.StockHistoryResponse;
import vn.perp.producterpservice.dto.response.StockTransactionResponse;
import vn.perp.producterpservice.exception.BadRequestException;
import vn.perp.producterpservice.exception.InsufficientStockException;
import vn.perp.producterpservice.model.LowStockAlert;
import vn.perp.producterpservice.model.LowStockAlertStatus;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.ProductStatus;
import vn.perp.producterpservice.model.StockHistory;
import vn.perp.producterpservice.model.StockTransactionType;
import vn.perp.producterpservice.repository.LowStockAlertRepository;
import vn.perp.producterpservice.repository.ProductRepository;
import vn.perp.producterpservice.repository.StockHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StockService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final LowStockAlertRepository lowStockAlertRepository;
    private final DtoMapper mapper;

    public StockService(ProductService productService,
                        ProductRepository productRepository,
                        StockHistoryRepository stockHistoryRepository,
                        LowStockAlertRepository lowStockAlertRepository,
                        DtoMapper mapper) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.lowStockAlertRepository = lowStockAlertRepository;
        this.mapper = mapper;
    }

    public StockTransactionResponse adjustStock(UUID productId, StockAdjustmentRequest request, UUID userId) {
        if (request.quantityChange() == 0) {
            throw new BadRequestException("quantityChange must not be zero");
        }

        Product product = productService.getProductEntity(productId);

        int previousQuantity = product.getStockQuantity();
        int newQuantity = previousQuantity + request.quantityChange();

        if (newQuantity < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + previousQuantity + ", Requested: " + Math.abs(request.quantityChange())
            );
        }

        product.setStockQuantity(newQuantity);
        product.setUpdatedBy(userId == null ? ProductService.SYSTEM_USER_ID : userId);

        if (newQuantity == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);

        StockHistory history = StockHistory.builder()
                .product(product)
                .transactionType(request.transactionType())
                .quantityChange(request.quantityChange())
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .unitCost(request.unitCost())
                .totalCost(calculateTotalCost(request.unitCost(), request.quantityChange()))
                .referenceType(request.referenceType())
                .referenceId(request.referenceId())
                .location(request.location())
                .reason(request.reason())
                .notes(request.notes())
                .changedBy(userId == null ? ProductService.SYSTEM_USER_ID : userId)
                .build();

        StockHistory savedHistory = stockHistoryRepository.save(history);

        createLowStockAlertIfNeeded(product);

        return StockTransactionResponse.builder()
                .transactionId(savedHistory.getId())
                .productId(product.getId())
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<StockHistoryResponse> getStockHistory(
            UUID productId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            StockTransactionType transactionType,
            Pageable pageable
    ) {
        productService.getProductEntity(productId);
        List<StockHistory> filtered = stockHistoryRepository
                .findByProductIdOrderByChangedAtDesc(productId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(item -> startDate == null || !item.getChangedAt().isBefore(startDate))
                .filter(item -> endDate == null || !item.getChangedAt().isAfter(endDate))
                .filter(item -> transactionType == null || item.getTransactionType() == transactionType)
                .toList();

        Page<StockHistoryResponse> page = toPage(filtered, pageable).map(mapper::toStockHistoryResponse);
        return PagedResponse.from(page);
    }

    private BigDecimal calculateTotalCost(BigDecimal unitCost, int quantityChange) {
        if (unitCost == null) {
            return null;
        }
        return unitCost.multiply(BigDecimal.valueOf(Math.abs(quantityChange)));
    }

    private void createLowStockAlertIfNeeded(Product product) {
        if (product.getStockQuantity() <= product.getMinimumStockLevel()) {
            boolean pendingExists = lowStockAlertRepository.existsByProductIdAndAlertStatus(
                    product.getId(), LowStockAlertStatus.PENDING
            );
            if (!pendingExists) {
                LowStockAlert alert = LowStockAlert.builder()
                        .product(product)
                        .currentStock(product.getStockQuantity())
                        .minimumStock(product.getMinimumStockLevel())
                        .alertStatus(LowStockAlertStatus.PENDING)
                        .build();
                lowStockAlertRepository.save(alert);
            }
        }
    }

    private <T> Page<T> toPage(List<T> source, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(source);
        }
        int start = (int) pageable.getOffset();
        if (start >= source.size()) {
            return new PageImpl<>(List.of(), pageable, source.size());
        }
        int end = Math.min(start + pageable.getPageSize(), source.size());
        return new PageImpl<>(source.subList(start, end), pageable, source.size());
    }
}
