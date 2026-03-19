package vn.perp.producterpservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Product> findBySkuAndDeletedAtIsNull(String sku);

    boolean existsBySkuIgnoreCaseAndDeletedAtIsNull(String sku);

    Page<Product> findByDeletedAtIsNull(Pageable pageable);

    List<Product> findByDeletedAtIsNull();

    Page<Product> findByStatusAndDeletedAtIsNull(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndDeletedAtIsNull(UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.stockQuantity <= p.minimumStockLevel")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT COALESCE(SUM(p.stockQuantity * COALESCE(p.costPrice, 0)), 0) FROM Product p WHERE p.deletedAt IS NULL")
    BigDecimal calculateTotalInventoryValue();

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndStatus(ProductStatus status);

    long countByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
