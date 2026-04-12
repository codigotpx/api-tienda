package com.tienda.universitaria.api.serivice;

import com.tienda.universitaria.api.api.dto.ProductDtos;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderItemsRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.ProductServiceImpl;
import com.tienda.universitaria.api.service.mapper.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;
    @Mock private OrderItemsRepository orderItemsRepository;

    @InjectMocks private ProductServiceImpl productService;

    @Test
    void create_shouldCreateProductAndReturnResponse() {
        UUID categoryId = UUID.randomUUID();
        var req = new ProductDtos.ProductCreateRequest(
                "SKU-1",
                "Laptop",
                "Desc",
                new BigDecimal("10.00"),
                categoryId,
                true
        );

        var category = Category.builder().id(categoryId).name("Cat").build();
        var entity = Product.builder().sku("SKU-1").name("Laptop").description("Desc").price(new BigDecimal("10.00")).build();
        var saved = Product.builder().id(UUID.randomUUID()).sku("SKU-1").name("Laptop").description("Desc").price(new BigDecimal("10.00")).active(true).category(category).build();
        var expected = new ProductDtos.ProductResponse(
                saved.getId(),
                "SKU-1",
                "Laptop",
                "Desc",
                new BigDecimal("10.00"),
                true,
                categoryId,
                "Cat"
        );

        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(req)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(expected);

        var actual = productService.create(req);

        assertEquals(expected, actual);
        assertSame(category, entity.getCategory());
        assertTrue(entity.getActive());
        verify(productRepository).findBySku("SKU-1");
        verify(categoryRepository).findById(categoryId);
        verify(productMapper).toEntity(req);
        verify(productRepository).save(entity);
        verify(productMapper).toResponse(saved);
    }

    @Test
    void create_shouldRejectWhenSkuExists() {
        var req = new ProductDtos.ProductCreateRequest(
                "SKU-1",
                "Laptop",
                "Desc",
                new BigDecimal("10.00"),
                UUID.randomUUID(),
                true
        );
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(Product.builder().build()));

        assertThrows(IllegalArgumentException.class, () -> productService.create(req));
        verify(productRepository).findBySku("SKU-1");
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectWhenPriceNotPositive() {
        var req = new ProductDtos.ProductCreateRequest(
                "SKU-1",
                "Laptop",
                "Desc",
                BigDecimal.ZERO,
                UUID.randomUUID(),
                true
        );
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productService.create(req));
        verify(productRepository).findBySku("SKU-1");
        verifyNoInteractions(categoryRepository, productMapper);
    }

    @Test
    void update_shouldPatchSetCategoryAndActive() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        var req = new ProductDtos.ProductUpdateRequest(
                "IGNORED",
                "New",
                null,
                new BigDecimal("20.00"),
                categoryId,
                false
        );

        var existing = Product.builder().id(productId).sku("SKU-1").active(true).build();
        var category = Category.builder().id(categoryId).build();
        var expected = new ProductDtos.ProductResponse(productId, "SKU-1", "New", null, new BigDecimal("20.00"), false, categoryId, null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(existing)).thenReturn(existing);
        when(productMapper.toResponse(existing)).thenReturn(expected);

        var actual = productService.update(productId, req);

        assertEquals(expected, actual);
        assertSame(category, existing.getCategory());
        assertFalse(existing.getActive());
        verify(productMapper).patch(existing, req);
        verify(productRepository).save(existing);
    }

    @Test
    void update_shouldRejectWhenPriceNotPositive() {
        UUID productId = UUID.randomUUID();
        var req = new ProductDtos.ProductUpdateRequest(null, null, null, new BigDecimal("-1.00"), null, null);
        assertThrows(IllegalArgumentException.class, () -> productService.update(productId, req));
        verifyNoInteractions(productRepository, categoryRepository, productMapper);
    }

    @Test
    void getByCategory_shouldThrowWhenCategoryMissing() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> productService.getByCategory(categoryId, PageRequest.of(0, 10)));
        verify(categoryRepository).existsById(categoryId);
        verifyNoInteractions(productRepository);
    }

    @Test
    void setActive_shouldRejectDeactivationWhenHasActiveOrders() {
        UUID productId = UUID.randomUUID();
        when(orderItemsRepository.existsByProductIdAndOrderStatusIn(eq(productId), anyList())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.setActive(productId, false));

        verify(orderItemsRepository).existsByProductIdAndOrderStatusIn(
                eq(productId),
                eq(List.of(OrderStatus.CREATED, OrderStatus.PAID, OrderStatus.SHIPPED))
        );
        verifyNoInteractions(productRepository);
    }

    @Test
    void setActive_shouldAllowActivationEvenIfHasActiveOrders() {
        UUID productId = UUID.randomUUID();
        var product = Product.builder().id(productId).active(false).build();
        var expected = new ProductDtos.ProductResponse(productId, "SKU", "N", "D", new BigDecimal("1.00"), true, UUID.randomUUID(), "C");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        var actual = productService.setActive(productId, true);

        assertEquals(expected, actual);
        assertTrue(product.getActive());
        verify(orderItemsRepository, never()).existsByProductIdAndOrderStatusIn(any(), anyList());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }
}

