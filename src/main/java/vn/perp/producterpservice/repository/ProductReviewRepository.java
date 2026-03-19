package vn.perp.producterpservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.perp.producterpservice.model.ProductReview;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    List<ProductReview> findByProductIdAndIsApprovedTrue(UUID productId);

    long countByProductIdAndIsApprovedTrue(UUID productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);
}
