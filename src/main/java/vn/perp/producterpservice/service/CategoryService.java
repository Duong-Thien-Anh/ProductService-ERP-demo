package vn.perp.producterpservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.perp.producterpservice.dto.request.CreateCategoryRequest;
import vn.perp.producterpservice.dto.response.CategoryResponse;
import vn.perp.producterpservice.exception.DuplicateResourceException;
import vn.perp.producterpservice.exception.ResourceNotFoundException;
import vn.perp.producterpservice.model.ProductCategory;
import vn.perp.producterpservice.repository.ProductCategoryRepository;
import vn.perp.producterpservice.repository.ProductRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final DtoMapper mapper;

    public CategoryService(ProductCategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           DtoMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> mapper.toCategoryResponse(category, productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId())))
                .toList();
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateResourceException("Category code already exists: " + request.code());
        }
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Category name already exists: " + request.name());
        }

        ProductCategory parent = null;
        if (request.parentCategoryId() != null) {
            parent = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        ProductCategory category = ProductCategory.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .parentCategory(parent)
                .imageUrl(request.imageUrl())
                .displayOrder(request.displayOrder() == null ? 0 : request.displayOrder())
                .isActive(true)
                .build();

        ProductCategory saved = categoryRepository.save(category);
        return mapper.toCategoryResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public ProductCategory getCategoryEntity(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    @Transactional(readOnly = true)
    public Set<UUID> collectCategoryAndChildren(UUID categoryId) {
        List<ProductCategory> allCategories = categoryRepository.findAll();
        Set<UUID> ids = new HashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>();
        queue.add(categoryId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (!ids.add(current)) {
                continue;
            }
            for (ProductCategory category : allCategories) {
                if (category.getParentCategory() != null && current.equals(category.getParentCategory().getId())) {
                    queue.add(category.getId());
                }
            }
        }

        return ids;
    }

    @Transactional(readOnly = true)
    public List<UUID> collectCategoryAndChildrenAsList(UUID categoryId) {
        return new ArrayList<>(collectCategoryAndChildren(categoryId));
    }
}
