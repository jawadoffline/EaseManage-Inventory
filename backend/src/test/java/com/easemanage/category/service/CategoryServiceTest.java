package com.easemanage.category.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.category.dto.CategoryRequest;
import com.easemanage.category.dto.CategoryResponse;
import com.easemanage.category.entity.Category;
import com.easemanage.category.repository.CategoryRepository;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest("Electronics", "Electronic items", null);
        Category saved = buildCategory(1L, "Electronics", "Electronic items", null);

        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("Electronics", response.name());
        assertEquals(1L, response.id());
        verify(categoryRepository).save(any(Category.class));
        verify(auditService).log(eq("Category"), eq(1L), eq("CREATE"), isNull(), eq("name=Electronics"));
    }

    @Test
    void createCategory_duplicateName_throwsDuplicateResourceException() {
        CategoryRequest request = new CategoryRequest("Electronics", "desc", null);
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void deleteCategory_withChildren_throwsIllegalArgumentException() {
        Category parent = buildCategory(1L, "Parent", "desc", null);
        Category child = buildCategory(2L, "Child", "desc", parent);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(child));

        assertThrows(IllegalArgumentException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_noChildren_success() {
        Category category = buildCategory(1L, "Leaf", "desc", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.emptyList());

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
        verify(auditService).log(eq("Category"), eq(1L), eq("DELETE"), eq("name=Leaf"), isNull());
    }

    @Test
    void getCategories_returnsPagedResponse() {
        Category cat = buildCategory(1L, "Cat1", "desc", null);
        Page<Category> page = new PageImpl<>(List.of(cat), PageRequest.of(0, 10, Sort.by("name")), 1);

        when(categoryRepository.search(eq(""), any(Pageable.class))).thenReturn(page);

        PagedResponse<CategoryResponse> result = categoryService.getCategories(0, 10, "");

        assertEquals(1, result.content().size());
        assertEquals("Cat1", result.content().get(0).name());
        assertEquals(1, result.totalElements());
        assertTrue(result.last());
    }

    @Test
    void createCategory_withParent_success() {
        Category parent = buildCategory(1L, "Parent", "desc", null);
        CategoryRequest request = new CategoryRequest("Child", "child desc", 1L);
        Category saved = buildCategory(2L, "Child", "child desc", parent);

        when(categoryRepository.existsByName("Child")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("Child", response.name());
        assertEquals(1L, response.parentId());
    }

    private Category buildCategory(Long id, String name, String description, Category parent) {
        Category c = Category.builder()
                .id(id)
                .name(name)
                .description(description)
                .parent(parent)
                .build();
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
}
