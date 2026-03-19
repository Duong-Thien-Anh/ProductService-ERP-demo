package vn.perp.producterpservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.perp.producterpservice.model.StockHistory;
import vn.perp.producterpservice.model.StockTransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID> {

    Page<StockHistory> findByProductId(UUID productId, Pageable pageable);

    Page<StockHistory> findByProductIdOrderByChangedAtDesc(UUID productId, Pageable pageable);

    Page<StockHistory> findByProductIdAndTransactionType(
            UUID productId,
            StockTransactionType transactionType,
            Pageable pageable
    );

    Page<StockHistory> findByChangedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
