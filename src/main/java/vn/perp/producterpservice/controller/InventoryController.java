package vn.perp.producterpservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.perp.producterpservice.dto.response.InventorySummaryResponse;
import vn.perp.producterpservice.dto.response.LowStockAlertResponse;
import vn.perp.producterpservice.dto.response.ProductResponse;
import vn.perp.producterpservice.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockAlertResponse>> lowStockProducts() {
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductResponse>> outOfStockProducts() {
        return ResponseEntity.ok(inventoryService.getOutOfStockProducts());
    }

    @GetMapping("/summary")
    public ResponseEntity<InventorySummaryResponse> summary() {
        return ResponseEntity.ok(inventoryService.getInventorySummary());
    }
}
