package com.aditi.backendcapstoneproject.service;

import com.aditi.backendcapstoneproject.dto.CartResponseDto;
import com.aditi.backendcapstoneproject.dto.CartItemResponseDto;
import com.aditi.backendcapstoneproject.exception.CartItemNotFoundException;
import com.aditi.backendcapstoneproject.exception.ProductNotFoundException;
import com.aditi.backendcapstoneproject.exception.UserNotFoundException;
import com.aditi.backendcapstoneproject.model.Cart;
import com.aditi.backendcapstoneproject.model.CartItem;
import com.aditi.backendcapstoneproject.model.Product;
import com.aditi.backendcapstoneproject.model.User;
import com.aditi.backendcapstoneproject.repository.CartItemRepository;
import com.aditi.backendcapstoneproject.repository.CartRepository;
import com.aditi.backendcapstoneproject.repository.ProductRepository;
import com.aditi.backendcapstoneproject.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Cacheable(cacheNames = "carts", key = "#email")
    public CartResponseDto getCart(String email) throws UserNotFoundException {
        User user = getUserByEmail(email);
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    @CacheEvict(cacheNames = "carts", key = "#email")
    public CartResponseDto addItemToCart(String email, Long productId, Integer quantity) throws ProductNotFoundException, UserNotFoundException {
        User user = getUserByEmail(email);
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

    @CacheEvict(cacheNames = "carts", key = "#email")
    public CartResponseDto updateCartItem(String email, Long itemId, Integer quantity) throws CartItemNotFoundException, UserNotFoundException {
        User user = getUserByEmail(email);
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

    @CacheEvict(cacheNames = "carts", key = "#email")
    public CartResponseDto removeCartItem(String email, Long itemId) throws CartItemNotFoundException, UserNotFoundException {
        User user = getUserByEmail(email);
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
    @CacheEvict(cacheNames = "carts", key = "#email")
    public CartResponseDto clearCart(String email) throws UserNotFoundException {
        User user = getUserByEmail(email);
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

