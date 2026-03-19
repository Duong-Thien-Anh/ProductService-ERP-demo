package vn.perp.producterpservice.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.ProductStatus;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Product> withStatus(ProductStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Product> withCategory(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> withCategories(Collection<UUID> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Product> priceGte(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLte(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> inStock(Boolean inStock) {
        if (inStock == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return inStock
                ? (root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0)
                : (root, query, cb) -> cb.equal(root.get("stockQuantity"), 0);
    }

    public static Specification<Product> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String term = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), term),
                    cb.like(cb.lower(root.get("sku")), term),
                    cb.like(cb.lower(root.get("description")), term)
            );
        };
    }
}
