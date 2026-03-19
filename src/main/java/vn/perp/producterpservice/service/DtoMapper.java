package vn.perp.producterpservice.service;

import org.springframework.stereotype.Component;
import vn.perp.producterpservice.dto.response.CategoryResponse;
import vn.perp.producterpservice.dto.response.ProductDetailResponse;
import vn.perp.producterpservice.dto.response.ProductResponse;
import vn.perp.producterpservice.dto.response.StockHistoryResponse;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.ProductCategory;
import vn.perp.producterpservice.model.StockHistory;

@Component
public class DtoMapper {

    public CategoryResponse toCategoryResponse(ProductCategory category, long productCount) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .isActive(Boolean.TRUE.equals(category.getIsActive()))
                .displayOrder(category.getDisplayOrder() == null ? 0 : category.getDisplayOrder())
                .productCount(productCount)
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .category(toCategoryResponse(product.getCategory(), 0))
                .price(product.getPrice())
                .currency(product.getCurrency())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .imageUrl(product.getImageUrl())
                .build();
    }

    public ProductDetailResponse toProductDetailResponse(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .name(product.getName())
                .description(product.getDescription())
                .category(toCategoryResponse(product.getCategory(), 0))
                .brand(product.getBrand())
                .manufacturer(product.getManufacturer())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .currency(product.getCurrency())
                .stockQuantity(product.getStockQuantity())
                .minimumStockLevel(product.getMinimumStockLevel())
                .maximumStockLevel(product.getMaximumStockLevel())
                .reorderPoint(product.getReorderPoint())
                .unit(product.getUnit())
                .weight(product.getWeight())
                .weightUnit(product.getWeightUnit())
                .dimensionsLength(product.getDimensionsLength())
                .dimensionsWidth(product.getDimensionsWidth())
                .dimensionsHeight(product.getDimensionsHeight())
                .dimensionUnit(product.getDimensionUnit())
                .taxRate(product.getTaxRate())
                .isTaxable(product.getIsTaxable())
                .isActive(product.getIsActive())
                .status(product.getStatus())
                .imageUrl(product.getImageUrl())
                .additionalImages(product.getAdditionalImages())
                .tags(product.getTags())
                .warrantyPeriod(product.getWarrantyPeriod())
                .warrantyDescription(product.getWarrantyDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public StockHistoryResponse toStockHistoryResponse(StockHistory history) {
        return StockHistoryResponse.builder()
                .id(history.getId())
                .transactionType(history.getTransactionType())
                .quantityChange(history.getQuantityChange())
                .previousQuantity(history.getPreviousQuantity())
                .newQuantity(history.getNewQuantity())
                .unitCost(history.getUnitCost())
                .totalCost(history.getTotalCost())
                .reason(history.getReason())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build();
    }
}
