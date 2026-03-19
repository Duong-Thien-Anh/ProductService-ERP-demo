package vn.perp.producterpservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.perp.producterpservice.dto.response.InventoryValuationItemResponse;
import vn.perp.producterpservice.dto.response.InventoryValuationResponse;
import vn.perp.producterpservice.dto.response.PagedResponse;
import vn.perp.producterpservice.dto.response.StockHistoryResponse;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.StockHistory;
import vn.perp.producterpservice.model.StockTransactionType;
import vn.perp.producterpservice.repository.ProductRepository;
import vn.perp.producterpservice.repository.StockHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final DtoMapper mapper;

    public ReportService(ProductRepository productRepository,
                         StockHistoryRepository stockHistoryRepository,
                         DtoMapper mapper) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.mapper = mapper;
    }

    public InventoryValuationResponse getInventoryValuation() {
        List<Product> products = productRepository.findByDeletedAtIsNull();

        List<InventoryValuationItemResponse> items = products.stream()
                .map(product -> {
                    BigDecimal cost = product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
                    BigDecimal total = cost.multiply(BigDecimal.valueOf(product.getStockQuantity()));
                    return InventoryValuationItemResponse.builder()
                            .sku(product.getSku())
                            .name(product.getName())
                            .quantity(product.getStockQuantity())
                            .costPrice(cost)
                            .totalValue(total)
                            .build();
                })
                .toList();

        BigDecimal total = items.stream()
                .map(InventoryValuationItemResponse::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InventoryValuationResponse.builder()
                .reportDate(LocalDateTime.now())
                .totalInventoryValue(total)
                .products(items)
                .build();
    }

    public PagedResponse<StockHistoryResponse> getStockMovement(
            LocalDateTime startDate,
            LocalDateTime endDate,
            UUID productId,
            UUID categoryId,
            StockTransactionType transactionType,
            Pageable pageable
    ) {
        LocalDateTime start = startDate == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : startDate;
        LocalDateTime end = endDate == null ? LocalDateTime.now() : endDate;

        List<StockHistory> filtered = stockHistoryRepository.findByChangedAtBetween(start, end, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(item -> productId == null || item.getProduct().getId().equals(productId))
                .filter(item -> categoryId == null || (item.getProduct().getCategory() != null
                        && item.getProduct().getCategory().getId().equals(categoryId)))
                .filter(item -> transactionType == null || item.getTransactionType() == transactionType)
                .sorted((a, b) -> b.getChangedAt().compareTo(a.getChangedAt()))
                .toList();

        Page<StockHistoryResponse> page = toPage(filtered, pageable).map(mapper::toStockHistoryResponse);
        return PagedResponse.from(page);
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
