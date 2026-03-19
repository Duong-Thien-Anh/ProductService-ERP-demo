package vn.perp.producterpservice.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.perp.producterpservice.dto.response.InventoryValuationResponse;
import vn.perp.producterpservice.dto.response.PagedResponse;
import vn.perp.producterpservice.dto.response.StockHistoryResponse;
import vn.perp.producterpservice.model.StockTransactionType;
import vn.perp.producterpservice.service.ReportService;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController extends BaseController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/stock-movement")
    public ResponseEntity<PagedResponse<StockHistoryResponse>> stockMovement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) StockTransactionType transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "changedAt,desc") String sort
    ) {
        return ResponseEntity.ok(reportService.getStockMovement(
                startDate,
                endDate,
                productId,
                categoryId,
                transactionType,
                buildPageable(page, size, sort)
        ));
    }

    @GetMapping("/inventory-valuation")
    public ResponseEntity<InventoryValuationResponse> inventoryValuation() {
        return ResponseEntity.ok(reportService.getInventoryValuation());
    }
}
