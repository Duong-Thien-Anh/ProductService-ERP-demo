package vn.perp.producterpservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.perp.producterpservice.dto.request.CreateProductRequest;
import vn.perp.producterpservice.dto.request.UpdateProductRequest;
import vn.perp.producterpservice.dto.response.ApiMessageResponse;
import vn.perp.producterpservice.dto.response.PagedResponse;
import vn.perp.producterpservice.dto.response.ProductDetailResponse;
import vn.perp.producterpservice.dto.response.ProductResponse;
import vn.perp.producterpservice.exception.DuplicateResourceException;
import vn.perp.producterpservice.exception.ResourceNotFoundException;
import vn.perp.producterpservice.model.Product;
import vn.perp.producterpservice.model.ProductCategory;
import vn.perp.producterpservice.model.ProductPricingHistory;
import vn.perp.producterpservice.model.ProductStatus;
import vn.perp.producterpservice.repository.ProductCategoryRepository;
import vn.perp.producterpservice.repository.ProductPricingHistoryRepository;
import vn.perp.producterpservice.repository.ProductRepository;
import vn.perp.producterpservice.specification.ProductSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductPricingHistoryRepository pricingHistoryRepository;
    private final DtoMapper mapper;

    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          ProductPricingHistoryRepository pricingHistoryRepository,
                          DtoMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.pricingHistoryRepository = pricingHistoryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> listProducts(
            Pageable pageable,
            ProductStatus status,
            List<UUID> categoryIds,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        Specification<Product> spec = ProductSpecification.notDeleted()
                .and(ProductSpecification.withStatus(status))
                .and(ProductSpecification.withCategories(categoryIds))
                .and(ProductSpecification.search(search))
                .and(ProductSpecification.priceGte(minPrice))
                .and(ProductSpecification.priceLte(maxPrice))
                .and(ProductSpecification.inStock(inStock));

        Page<ProductResponse> page = productRepository.findAll(spec, pageable).map(mapper::toProductResponse);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(UUID productId) {
        Product product = getProductEntity(productId);
        return mapper.toProductDetailResponse(product);
    }

    public ProductDetailResponse createProduct(CreateProductRequest request, UUID userId) {
        if (productRepository.existsBySkuIgnoreCaseAndDeletedAtIsNull(request.sku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.sku());
        }

        ProductCategory category = resolveCategory(request.categoryId());
        int stockQty = request.stockQuantity() == null ? 0 : request.stockQuantity();

        Product product = Product.builder()
                .sku(request.sku().trim())
                .barcode(request.barcode())
                .name(request.name().trim())
                .description(request.description())
                .category(category)
                .brand(request.brand())
                .manufacturer(request.manufacturer())
                .price(request.price())
                .costPrice(request.costPrice())
                .currency(request.currency() == null ? "USD" : request.currency())
                .stockQuantity(stockQty)
                .minimumStockLevel(defaultZero(request.minimumStockLevel()))
                .maximumStockLevel(request.maximumStockLevel())
                .reorderPoint(defaultZero(request.reorderPoint()))
                .unit(request.unit() == null ? "piece" : request.unit())
                .weight(request.weight())
                .weightUnit(request.weightUnit() == null ? "kg" : request.weightUnit())
                .dimensionsLength(request.dimensionsLength())
                .dimensionsWidth(request.dimensionsWidth())
                .dimensionsHeight(request.dimensionsHeight())
                .dimensionUnit(request.dimensionUnit() == null ? "cm" : request.dimensionUnit())
                .taxRate(request.taxRate() == null ? BigDecimal.ZERO : request.taxRate())
                .isTaxable(request.isTaxable() == null || request.isTaxable())
                .isActive(true)
                .status(stockQty == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE)
                .imageUrl(request.imageUrl())
                .additionalImages(request.additionalImages() == null ? new ArrayList<>() : new ArrayList<>(request.additionalImages()))
                .tags(request.tags() == null ? new ArrayList<>() : new ArrayList<>(request.tags()))
                .warrantyPeriod(request.warrantyPeriod())
                .warrantyDescription(request.warrantyDescription())
                .createdBy(defaultUserId(userId))
                .build();

        Product saved = productRepository.save(product);
        return mapper.toProductDetailResponse(saved);
    }

    public ProductDetailResponse updateProduct(UUID productId, UpdateProductRequest request, UUID userId) {
        Product product = getProductEntity(productId);

        BigDecimal oldPrice = product.getPrice();
        boolean priceChanged = request.price() != null && oldPrice != null && oldPrice.compareTo(request.price()) != 0;

        if (request.name() != null) {
            product.setName(request.name().trim());
        }
        if (request.barcode() != null) {
            product.setBarcode(request.barcode());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.categoryId() != null) {
            product.setCategory(resolveCategory(request.categoryId()));
        }
        if (request.brand() != null) {
            product.setBrand(request.brand());
        }
        if (request.manufacturer() != null) {
            product.setManufacturer(request.manufacturer());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.costPrice() != null) {
            product.setCostPrice(request.costPrice());
        }
        if (request.currency() != null) {
            product.setCurrency(request.currency());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
            if (request.stockQuantity() == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
            }
        }
        if (request.minimumStockLevel() != null) {
            product.setMinimumStockLevel(request.minimumStockLevel());
        }
        if (request.maximumStockLevel() != null) {
            product.setMaximumStockLevel(request.maximumStockLevel());
        }
        if (request.reorderPoint() != null) {
            product.setReorderPoint(request.reorderPoint());
        }
        if (request.unit() != null) {
            product.setUnit(request.unit());
        }
        if (request.weight() != null) {
            product.setWeight(request.weight());
        }
        if (request.weightUnit() != null) {
            product.setWeightUnit(request.weightUnit());
        }
        if (request.dimensionsLength() != null) {
            product.setDimensionsLength(request.dimensionsLength());
        }
        if (request.dimensionsWidth() != null) {
            product.setDimensionsWidth(request.dimensionsWidth());
        }
        if (request.dimensionsHeight() != null) {
            product.setDimensionsHeight(request.dimensionsHeight());
        }
        if (request.dimensionUnit() != null) {
            product.setDimensionUnit(request.dimensionUnit());
        }
        if (request.taxRate() != null) {
            product.setTaxRate(request.taxRate());
        }
        if (request.isTaxable() != null) {
            product.setIsTaxable(request.isTaxable());
        }
        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        if (request.additionalImages() != null) {
            product.setAdditionalImages(new ArrayList<>(request.additionalImages()));
        }
        if (request.tags() != null) {
            product.setTags(new ArrayList<>(request.tags()));
        }
        if (request.warrantyPeriod() != null) {
            product.setWarrantyPeriod(request.warrantyPeriod());
        }
        if (request.warrantyDescription() != null) {
            product.setWarrantyDescription(request.warrantyDescription());
        }

        product.setUpdatedBy(defaultUserId(userId));

        Product saved = productRepository.save(product);

        if (priceChanged && request.price() != null) {
            savePricingHistory(saved, oldPrice, request.price(), defaultUserId(userId));
        }

        return mapper.toProductDetailResponse(saved);
    }

    public ApiMessageResponse softDeleteProduct(UUID productId) {
        Product product = getProductEntity(productId);
        product.setDeletedAt(LocalDateTime.now());
        product.setIsActive(false);
        productRepository.save(product);

        return ApiMessageResponse.builder()
                .message("Product deleted successfully")
                .id(productId)
                .build();
    }

    @Transactional(readOnly = true)
    public Product getProductEntity(UUID productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private void savePricingHistory(Product product, BigDecimal oldPrice, BigDecimal newPrice, UUID userId) {
        BigDecimal percent = BigDecimal.ZERO;
        if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
            percent = newPrice.subtract(oldPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(oldPrice, 2, RoundingMode.HALF_UP);
        }

        ProductPricingHistory history = ProductPricingHistory.builder()
                .product(product)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .priceChangePercentage(percent)
                .reason("Product update")
                .changedBy(userId)
                .build();

        pricingHistoryRepository.save(history);
    }

    private ProductCategory resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private UUID defaultUserId(UUID userId) {
        return userId == null ? SYSTEM_USER_ID : userId;
    }

    @Transactional(readOnly = true)
    public List<Product> findActiveProducts() {
        List<Product> products = productRepository.findByDeletedAtIsNull();
        return products == null ? Collections.emptyList() : products;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProductResponses(Pageable pageable, List<Product> source) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), source.size());
        if (start >= source.size()) {
            return new PageImpl<>(List.of(), pageable, source.size());
        }
        List<ProductResponse> content = source.subList(start, end).stream().map(mapper::toProductResponse).toList();
        return new PageImpl<>(content, pageable, source.size());
    }
}
