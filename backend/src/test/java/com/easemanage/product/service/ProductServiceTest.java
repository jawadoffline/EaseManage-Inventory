package com.easemanage.product.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.category.repository.CategoryRepository;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.product.dto.ProductRequest;
import com.easemanage.product.dto.ProductResponse;
import com.easemanage.product.entity.Product;
import com.easemanage.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_success() {
        ProductRequest request = new ProductRequest(
                "Test Product", "A test product", null, "PCS",
                10, 100, 5,
                new BigDecimal("10.00"), new BigDecimal("20.00"),
                "1234567890", null, true
        );

        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
        verify(auditService).log(eq("Product"), eq(1L), eq("CREATE"), isNull(), anyString());
    }

    @Test
    void createProduct_duplicateName_throwsDuplicateResourceException() {
        ProductRequest request = new ProductRequest(
                "Existing Product", "desc", null, "PCS",
                0, null, 0,
                BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, true
        );

        when(productRepository.existsByName("Existing Product")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    void getProducts_returnsPagedResponse() {
        Product product = buildTestProduct(1L, "Product A");
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.search(
                isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(page);

        PagedResponse<ProductResponse> response = productService.getProducts(0, 10, null, null, null);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("Product A");
        assertThat(response.totalElements()).isEqualTo(1L);
    }

    private Product buildTestProduct(Long id, String name) {
        Product product = Product.builder()
                .name(name)
                .sku("PRD-000001")
                .description("Test description")
                .unitOfMeasure("PCS")
                .minStockLevel(0)
                .maxStockLevel(100)
                .reorderPoint(5)
                .costPrice(new BigDecimal("10.00"))
                .sellingPrice(new BigDecimal("20.00"))
                .isActive(true)
                .build();
        product.setId(id);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
