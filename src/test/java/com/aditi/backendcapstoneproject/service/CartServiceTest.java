package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.CartResponseDto;
import com.aditi.backendcapstoneproject.exception.CartItemNotFoundException;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Cart;
import com.aditi.backendcapstoneproject.model.CartItem;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.CartItemRepository;
import com.aditi.backendcapstoneproject.repository.CartRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setCreatedAt(new Date());
        testCart.setLastModified(new Date());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(999.99);
        testProduct.setDescription("High performance laptop");

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setCreatedAt(new Date());
        testCartItem.setLastModified(new Date());
        testCartItem.setDeleted(false);
    }

    @Test
    void testGetCart_ExistingCart() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.singletonList(testCartItem));

        // When
        CartResponseDto result = cartService.getCart(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1L);
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getTotalAmount()).isEqualTo(1999.98);
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartItemRepository, times(1)).findByCart(testCart);
    }

    @Test
    void testGetCart_NewCart() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.emptyList());

        // When
        CartResponseDto result = cartService.getCart(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1L);
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(0.0);
        verify(cartRepository, times(1)).findByUser(testUser);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_NewItem() throws ProductNotFoundException {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.singletonList(testCartItem));

        // When
        CartResponseDto result = cartService.addItemToCart(testUser, 1L, 2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(2);
        verify(productRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_ExistingItem() throws ProductNotFoundException {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.singletonList(testCartItem));

        // When
        CartResponseDto result = cartService.addItemToCart(testUser, 1L, 1);

        // Then
        assertThat(result).isNotNull();
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_ProductNotFound() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.addItemToCart(testUser, 999L, 1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product with id 999 not found");
    }

    @Test
    void testUpdateCartItem_Success() throws CartItemNotFoundException {
        // Given
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(1L);
        updatedCartItem.setCart(testCart);
        updatedCartItem.setProduct(testProduct);
        updatedCartItem.setQuantity(5);

        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(updatedCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.singletonList(updatedCartItem));

        // When
        CartResponseDto result = cartService.updateCartItem(testUser, 1L, 5);

        // Then
        assertThat(result).isNotNull();
        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_NotFound() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.updateCartItem(testUser, 999L, 5))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessageContaining("Cart item with id 999 not found");
        verify(cartItemRepository, times(1)).findById(999L);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void testUpdateCartItem_WrongUser() {
        // Given
        Cart otherCart = new Cart();
        otherCart.setId(2L);
        testCartItem.setCart(otherCart);

        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        // When & Then
        assertThatThrownBy(() -> cartService.updateCartItem(testUser, 1L, 5))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessageContaining("Cart item does not belong to user's cart");
        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void testRemoveCartItem_Success() throws CartItemNotFoundException {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.emptyList());

        // When
        CartResponseDto result = cartService.removeCartItem(testUser, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(0);
        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRemoveCartItem_NotFound() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.removeCartItem(testUser, 999L))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessageContaining("Cart item with id 999 not found");
        verify(cartItemRepository, times(1)).findById(999L);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void testClearCart_Success() {
        // Given
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteByCart(testCart);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart(testCart)).thenReturn(Collections.emptyList());

        // When
        CartResponseDto result = cartService.clearCart(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(0);
        verify(cartItemRepository, times(1)).deleteByCart(testCart);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
}

