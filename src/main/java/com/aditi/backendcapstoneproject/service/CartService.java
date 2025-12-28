package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.CartResponseDto;
import com.aditi.backendcapstoneproject.dto.CartItemResponseDto;
import com.aditi.backendcapstoneproject.exception.CartItemNotFoundException;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.model.Cart;
import com.aditi.backendcapstoneproject.model.CartItem;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.CartItemRepository;
import com.aditi.backendcapstoneproject.repository.CartRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponseDto getCart(User user) {
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    public CartResponseDto addItemToCart(User user, Long productId, Integer quantity) throws ProductNotFoundException {
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " not found"));

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCreatedAt(new Date());
            cartItem.setDeleted(false);
        }
        cartItem.setLastModified(new Date());
        cartItemRepository.save(cartItem);

        cart.setLastModified(new Date());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponseDto updateCartItem(User user, Long itemId, Integer quantity) throws CartItemNotFoundException {
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item with id " + itemId + " not found"));

        if (cartItem.getCart().getId() != cart.getId()) {
            throw new CartItemNotFoundException("Cart item does not belong to user's cart");
        }

        cartItem.setQuantity(quantity);
        cartItem.setLastModified(new Date());
        cartItemRepository.save(cartItem);

        cart.setLastModified(new Date());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponseDto removeCartItem(User user, Long itemId) throws CartItemNotFoundException {
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item with id " + itemId + " not found"));

        if (cartItem.getCart().getId() != cart.getId()) {
            throw new CartItemNotFoundException("Cart item does not belong to user's cart");
        }

        cartItemRepository.delete(cartItem);

        cart.setLastModified(new Date());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponseDto clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);

        cart.setLastModified(new Date());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    private Cart getOrCreateCart(User user) {
        Optional<Cart> cartOptional = cartRepository.findByUser(user);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        }

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(new Date());
        cart.setLastModified(new Date());
        cart.setDeleted(false);
        return cartRepository.save(cart);
    }

    private CartResponseDto buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        
        List<CartItemResponseDto> itemDtos = cartItems.stream()
                .map(CartItemResponseDto::from)
                .collect(Collectors.toList());

        Integer totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        Double totalAmount = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        CartResponseDto response = new CartResponseDto();
        response.setCartId(cart.getId());
        response.setItems(itemDtos);
        response.setTotalItems(totalItems);
        response.setTotalAmount(totalAmount);

        return response;
    }
}

