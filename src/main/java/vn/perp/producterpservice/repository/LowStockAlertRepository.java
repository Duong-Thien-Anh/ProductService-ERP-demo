package vn.perp.producterpservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.perp.producterpservice.model.LowStockAlert;
import vn.perp.producterpservice.model.LowStockAlertStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, UUID> {

    List<LowStockAlert> findByAlertStatusOrderByNotifiedAtDesc(LowStockAlertStatus alertStatus);

    boolean existsByProductIdAndAlertStatus(UUID productId, LowStockAlertStatus alertStatus);
}
