package com.easemanage.category.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.category.dto.CategoryRequest;
import com.easemanage.category.dto.CategoryResponse;
import com.easemanage.category.entity.Category;
import com.easemanage.category.repository.CategoryRepository;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getCategories(int page, int size, String search) {
        Page<Category> categories = categoryRepository.search(search,
            PageRequest.of(page, size, Sort.by("name")));
        return new PagedResponse<>(
            categories.getContent().stream().map(this::toResponse).toList(),
            categories.getNumber(), categories.getSize(),
            categories.getTotalElements(), categories.getTotalPages(), categories.isLast()
        );
    }

    @Cacheable("categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        return roots.stream().map(this::toTreeResponse).toList();
    }

    @Cacheable("categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllFlat() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return toResponse(findById(id));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category with name '" + request.name() + "' already exists");
        }
        Category category = Category.builder()
            .name(request.name())
            .description(request.description())
            .build();
        if (request.parentId() != null) {
            category.setParent(findById(request.parentId()));
        }
        Category saved = categoryRepository.save(category);
        auditService.log("Category", saved.getId(), "CREATE", null, "name=" + saved.getName());
        return toResponse(saved);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findById(id);
        if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category with name '" + request.name() + "' already exists");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            category.setParent(findById(request.parentId()));
        } else {
            category.setParent(null);
        }
        Category updated = categoryRepository.save(category);
        auditService.log("Category", id, "UPDATE", null, "name=" + updated.getName());
        return toResponse(updated);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = findById(id);
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with subcategories. Remove subcategories first.");
        }
        auditService.log("Category", id, "DELETE", "name=" + category.getName(), null);
        categoryRepository.delete(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
            c.getParent() != null ? c.getParent().getId() : null,
            c.getParent() != null ? c.getParent().getName() : null,
            c.getCreatedAt(), null);
    }

    private CategoryResponse toTreeResponse(Category c) {
        List<Category> children = categoryRepository.findByParentId(c.getId());
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
            c.getParent() != null ? c.getParent().getId() : null,
            c.getParent() != null ? c.getParent().getName() : null,
            c.getCreatedAt(),
            children.stream().map(this::toTreeResponse).toList());
    }
}
