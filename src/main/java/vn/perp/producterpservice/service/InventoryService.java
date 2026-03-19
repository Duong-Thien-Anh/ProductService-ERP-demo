package vn.perp.producterpservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.perp.producterpservice.dto.response.InventorySummaryResponse;
import vn.perp.producterpservice.dto.response.LowStockAlertResponse;
import vn.perp.producterpservice.dto.response.ProductResponse;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.ProductStatus;
import vn.perp.producterpservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductRepository productRepository;
    private final DtoMapper mapper;

    public InventoryService(ProductRepository productRepository, DtoMapper mapper) {
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    public List<LowStockAlertResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(product -> LowStockAlertResponse.builder()
                        .product(toCompactProduct(product))
                        .currentStock(product.getStockQuantity())
                        .minimumStockLevel(product.getMinimumStockLevel())
                        .reorderPoint(product.getReorderPoint())
                        .recommendedOrderQuantity(recommendedOrderQuantity(product))
                        .alertCreatedAt(product.getUpdatedAt())
                        .build())
                .toList();
    }

    public List<ProductResponse> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts().stream()
                .map(this::toCompactProduct)
                .toList();
    }

    public InventorySummaryResponse getInventorySummary() {
        List<Product> products = productRepository.findByDeletedAtIsNull();

        List<InventorySummaryResponse.CategoryBreakdown> breakdown = products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory() == null ? "Uncategorized" : p.getCategory().getName()))
                .entrySet()
                .stream()
                .map(this::toBreakdown)
                .sorted(Comparator.comparing(InventorySummaryResponse.CategoryBreakdown::category))
                .toList();

        return InventorySummaryResponse.builder()
                .totalProducts(productRepository.countByDeletedAtIsNull())
                .totalStockValue(zeroIfNull(productRepository.calculateTotalInventoryValue()))
                .activeProducts(productRepository.countByDeletedAtIsNullAndStatus(ProductStatus.ACTIVE))
                .lowStockProducts(productRepository.findLowStockProducts().size())
                .outOfStockProducts(productRepository.findOutOfStockProducts().size())
                .discontinuedProducts(productRepository.countByDeletedAtIsNullAndStatus(ProductStatus.DISCONTINUED))
                .categoryBreakdown(breakdown)
                .build();
    }

    private ProductResponse toCompactProduct(Product product) {
        return mapper.toProductResponse(product);
    }

    private int recommendedOrderQuantity(Product product) {
        int target = product.getReorderPoint() == null ? 0 : product.getReorderPoint();
        int current = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int recommended = target - current;
        return Math.max(recommended, 0);
    }

    private InventorySummaryResponse.CategoryBreakdown toBreakdown(Map.Entry<String, List<Product>> entry) {
        BigDecimal total = entry.getValue().stream()
                .map(p -> {
                    BigDecimal cost = zeroIfNull(p.getCostPrice());
                    return cost.multiply(BigDecimal.valueOf(p.getStockQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InventorySummaryResponse.CategoryBreakdown.builder()
                .category(entry.getKey())
                .productCount(entry.getValue().size())
                .totalValue(total)
                .build();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
