package vn.perp.producterpservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.perp.producterpservice.model.ProductPricingHistory;

import java.util.UUID;

@Repository
public interface ProductPricingHistoryRepository extends JpaRepository<ProductPricingHistory, UUID> {
}
