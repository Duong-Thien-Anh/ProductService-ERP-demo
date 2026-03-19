package vn.perp.producterpservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.perp.producterpservice.dto.request.CreateCategoryRequest;
import vn.perp.producterpservice.dto.response.CategoryResponse;
import vn.perp.producterpservice.dto.response.PagedResponse;
import vn.perp.producterpservice.dto.response.ProductResponse;
import vn.perp.producterpservice.model.ProductStatus;
import vn.perp.producterpservice.service.CategoryService;
import vn.perp.producterpservice.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController extends BaseController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<PagedResponse<ProductResponse>> getCategoryProducts(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean includeSubcategories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock
    ) {
        categoryService.getCategoryEntity(id);

        List<UUID> categoryIds = includeSubcategories
                ? categoryService.collectCategoryAndChildrenAsList(id)
                : List.of(id);

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
}
