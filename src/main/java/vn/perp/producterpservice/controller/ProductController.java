package vn.perp.producterpservice.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.perp.producterpservice.dto.request.CreateProductRequest;
import vn.perp.producterpservice.dto.request.StockAdjustmentRequest;
import vn.perp.producterpservice.dto.request.UpdateProductRequest;
import vn.perp.producterpservice.dto.response.ApiMessageResponse;
import vn.perp.producterpservice.dto.response.PagedResponse;
import vn.perp.producterpservice.dto.response.ProductDetailResponse;
import vn.perp.producterpservice.dto.response.ProductResponse;
import vn.perp.producterpservice.dto.response.StockHistoryResponse;
import vn.perp.producterpservice.dto.response.StockTransactionResponse;
import vn.perp.producterpservice.model.ProductStatus;
import vn.perp.producterpservice.model.StockTransactionType;
import vn.perp.producterpservice.service.ProductService;
import vn.perp.producterpservice.service.StockService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController extends BaseController {

    private final ProductService productService;
    private final StockService stockService;

    public ProductController(ProductService productService, StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock
    ) {
        List<UUID> categoryIds = category == null ? Collections.emptyList() : List.of(category);
        return ResponseEntity.ok(productService.listProducts(
                buildPageable(page, size, sort),
                status,
                categoryIds,
                search,
                minPrice,
                maxPrice,
                inStock
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock
    ) {
        List<UUID> categoryIds = category == null ? Collections.emptyList() : List.of(category);
        return ResponseEntity.ok(productService.listProducts(
                buildPageable(page, size, sort),
                null,
                categoryIds,
                q,
                minPrice,
                maxPrice,
                inStock
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        ProductDetailResponse response = productService.createProduct(request, parseUserId(userIdHeader));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request, parseUserId(userIdHeader)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> deleteProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.softDeleteProduct(id));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<StockTransactionResponse> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        return ResponseEntity.ok(stockService.adjustStock(id, request, parseUserId(userIdHeader)));
    }

    @GetMapping("/{id}/stock-history")
    public ResponseEntity<PagedResponse<StockHistoryResponse>> getStockHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) StockTransactionType transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "changedAt,desc") String sort
    ) {
        return ResponseEntity.ok(stockService.getStockHistory(
                id,
                startDate,
                endDate,
                transactionType,
                buildPageable(page, size, sort)
        ));
    }
}
