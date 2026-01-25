package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.OrderStatus;
import vn.edu.iuh.fit.bookstorebackend.common.PromotionStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.*;
import vn.edu.iuh.fit.bookstorebackend.mapper.OrderMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.*;
import vn.edu.iuh.fit.bookstorebackend.service.NotificationService;
import vn.edu.iuh.fit.bookstorebackend.service.OrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) throws IdInvalidException {
        User currentUser = getCurrentUser();
        Cart cart = findCartByUser(currentUser);
        List<CartItem> selectedItems = getSelectedCartItems(cart, request.getCartItemIds());
        Address deliveryAddress = getAndValidateDeliveryAddress(request.getAddressId(), currentUser);
        Promotion promotion = getAndValidatePromotion(request.getPromotionCode());
        
        Order order = createOrderFromRequest(currentUser, request, promotion, deliveryAddress);
        List<OrderDetail> orderDetails = createOrderDetails(selectedItems, order);
        double totalAmount = calculateOrderTotal(orderDetails, promotion);
        
        order.setTotalAmount(totalAmount);
        order.setOrderDetails(orderDetails);
        
        Order savedOrder = orderRepository.save(order);
        orderDetailRepository.saveAll(orderDetails);
        removeSelectedItemsFromCart(selectedItems, cart);
        sendOrderCreatedNotification(savedOrder, currentUser);
        
        return orderMapper.toOrderResponse(savedOrder);
    }
    
    private Cart findCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + user.getId()));
    }
    
    private List<CartItem> getSelectedCartItems(Cart cart, List<Long> cartItemIds) {
        List<CartItem> allItems = cartItemRepository.findByCart(cart);
        if (allItems == null || allItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return allItems;
        }
        
        List<CartItem> selectedItems = allItems.stream()
                .filter(ci -> cartItemIds.contains(ci.getId()))
                .collect(Collectors.toList());
        
        if (selectedItems.isEmpty()) {
            throw new IllegalStateException("Không có sản phẩm nào được chọn để đặt hàng");
        }
        
        return selectedItems;
    }
    
    private Address getAndValidateDeliveryAddress(Long addressId, User currentUser) throws IdInvalidException {
        if (addressId == null) {
            return null;
        }
        
        if (addressId <= 0) {
            throw new IdInvalidException("Address identifier is invalid: " + addressId);
        }
        
        Address deliveryAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
        
        if (!Objects.equals(deliveryAddress.getUser().getId(), currentUser.getId())) {
            throw new IllegalStateException("Address does not belong to current user");
        }
        
        return deliveryAddress;
    }
    
    private Promotion getAndValidatePromotion(String promotionCode) {
        if (promotionCode == null || promotionCode.trim().isEmpty()) {
            return null;
        }
        
        String code = promotionCode.trim();
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + code));
        
        validatePromotion(promotion);
        return promotion;
    }
    
    private void validatePromotion(Promotion promotion) {
        LocalDate today = LocalDate.now();
        if (!Boolean.TRUE.equals(promotion.getIsActive())
                || promotion.getStatus() != PromotionStatus.ACTIVE
                || today.isBefore(promotion.getStartDate())
                || today.isAfter(promotion.getEndDate())) {
            throw new IllegalStateException("Invalid or expired promotion code");
        }
        
        if (promotion.getQuantity() <= 0) {
            throw new IllegalStateException("Promotion code has been fully used");
        }
    }
    
    private Order createOrderFromRequest(User currentUser, CreateOrderRequest request, 
                                        Promotion promotion, Address deliveryAddress) {
        Order order = new Order();
        order.setUser(currentUser);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(determineInitialOrderStatus(request.getPaymentMethod()));
        
        if (promotion != null) {
            order.setPromotion(promotion);
            applyPromotion(promotion);
        }
        
        if (deliveryAddress != null) {
            order.setDeliveryAddress(deliveryAddress);
        }
        
        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        }
        
        return order;
    }
    
    private OrderStatus determineInitialOrderStatus(String paymentMethod) {
        boolean isVnPay = paymentMethod != null && "VNPAY".equalsIgnoreCase(paymentMethod);
        return isVnPay ? OrderStatus.UNPAID : OrderStatus.PENDING;
    }
    
    private void applyPromotion(Promotion promotion) {
        promotion.setQuantity(promotion.getQuantity() - 1);
        promotionRepository.save(promotion);
    }
    
    private List<OrderDetail> createOrderDetails(List<CartItem> selectedItems, Order order) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        
        for (CartItem cartItem : selectedItems) {
            Book book = cartItem.getBook();
            int quantity = cartItem.getQuantity();
            
            validateBookForOrder(book, quantity);
            updateBookStock(book, quantity);
            
            OrderDetail detail = createOrderDetail(order, book, quantity);
            orderDetails.add(detail);
        }
        
        return orderDetails;
    }
    
    private void validateBookForOrder(Book book, int quantity) {
        if (book.getIsActive() == null || !book.getIsActive()) {
            throw new RuntimeException("Book is not active: " + book.getId());
        }
        if (book.getStockQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock for book: " + book.getTitle());
        }
    }
    
    private void updateBookStock(Book book, int quantity) {
        book.setStockQuantity(book.getStockQuantity() - quantity);
        bookRepository.save(book);
    }
    
    private OrderDetail createOrderDetail(Order order, Book book, int quantity) {
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setBook(book);
        detail.setQuantity(quantity);
        detail.setPriceAtPurchase(book.getPrice());
        return detail;
    }
    
    private double calculateOrderTotal(List<OrderDetail> orderDetails, Promotion promotion) {
        double subtotal = orderDetails.stream()
                .mapToDouble(detail -> detail.getPriceAtPurchase() * detail.getQuantity())
                .sum();
        
        if (promotion == null) {
            return subtotal;
        }
        
        double discountAmount = subtotal * (promotion.getDiscountPercent() / 100.0);
        return subtotal - discountAmount;
    }
    
    private void removeSelectedItemsFromCart(List<CartItem> selectedItems, Cart cart) {
        cartItemRepository.deleteAll(selectedItems);
        recalculateCartTotal(cart);
    }
    
    private void sendOrderCreatedNotification(Order order, User user) {
        String title = "Đơn hàng #" + order.getId() + " đã được tạo";
        String content = "Đặt hàng thành công, đơn hàng của bạn đang chờ xác nhận";
        notificationService.createNotification(null, user, title, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User currentUser = getCurrentUser();
        List<Order> orders = orderRepository.findByUserId(currentUser.getId());
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) throws IdInvalidException {
        validateOrderId(orderId);
        
        User currentUser = getCurrentUser();
        Order order = findOrderById(orderId);
        validateOrderAccess(order, currentUser);
        
        return orderMapper.toOrderResponse(order);
    }
    
    private void validateOrderId(Long orderId) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }
    }
    
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
    
    private void validateOrderAccess(Order order, User currentUser) {
        boolean isAdminOrStaff = isAdminOrStaff(currentUser);
        boolean isOwner = Objects.equals(order.getUser().getId(), currentUser.getId());
        
        if (!isAdminOrStaff && !isOwner) {
            throw new IllegalStateException("You don't have permission to view this order");
        }
    }
    
    private boolean isAdminOrStaff(User user) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role ->
                        "ADMIN".equalsIgnoreCase(role.getCode())
                                || "SELLER_STAFF".equalsIgnoreCase(role.getCode())
                                || "WAREHOUSE_STAFF".equalsIgnoreCase(role.getCode())
                );
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) throws IdInvalidException {
        validateOrderId(orderId);
        validateNewStatus(newStatus);
        
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        
        if (oldStatus == newStatus) {
            return orderMapper.toOrderResponse(order);
        }
        
        validateStatusTransition(oldStatus, newStatus);
        handleInventoryReturnIfNeeded(oldStatus, newStatus, order);
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        createStatusChangeNotification(updatedOrder, newStatus);
        
        return orderMapper.toOrderResponse(updatedOrder);
    }
    
    private void validateNewStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status is required");
        }
    }
    
    private void validateStatusTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể cập nhật trạng thái đơn hàng đã bị hủy");
        }
        if (oldStatus == OrderStatus.RETURNED) {
            throw new IllegalStateException("Không thể cập nhật trạng thái đơn hàng đã trả lại");
        }
        if (oldStatus == OrderStatus.COMPLETED && newStatus != OrderStatus.RETURNED) {
            throw new IllegalStateException("Không thể cập nhật trạng thái đơn hàng đã hoàn thành");
        }
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new IllegalStateException(
                    "Chuyển trạng thái không hợp lệ từ " + oldStatus + " sang " + newStatus);
        }
    }
    
    private void handleInventoryReturnIfNeeded(OrderStatus oldStatus, OrderStatus newStatus, Order order) {
        if ((newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED)
                || (newStatus == OrderStatus.RETURNED && oldStatus != OrderStatus.RETURNED)) {
            returnInventory(order);
        }
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentMethod(Long orderId, String newPaymentMethod) throws IdInvalidException {
        validateOrderId(orderId);
        String normalizedPaymentMethod = validateAndNormalizePaymentMethod(newPaymentMethod);
        
        User currentUser = getCurrentUser();
        Order order = findOrderById(orderId);
        validatePaymentMethodUpdatePermission(order, currentUser);
        validateOrderStatusForPaymentMethodChange(order);
        
        updateOrderPaymentMethod(order, normalizedPaymentMethod);
        Order savedOrder = orderRepository.save(order);
        
        return orderMapper.toOrderResponse(savedOrder);
    }
    
    private String validateAndNormalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        String normalized = paymentMethod.trim().toUpperCase();
        if (!"CASH".equals(normalized) && !"VNPAY".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        return normalized;
    }
    
    private void validatePaymentMethodUpdatePermission(Order order, User currentUser) {
        boolean isAdminOrStaff = isAdminOrStaff(currentUser);
        boolean isOwner = Objects.equals(order.getUser().getId(), currentUser.getId());
        
        if (!isAdminOrStaff && !isOwner) {
            throw new IllegalStateException("You don't have permission to update payment method for this order");
        }
    }
    
    private void validateOrderStatusForPaymentMethodChange(Order order) {
        if (order.getStatus() != OrderStatus.UNPAID && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Không thể đổi phương thức với trạng thái hiện tại: " + order.getStatus());
        }
    }
    
    private void updateOrderPaymentMethod(Order order, String normalizedPaymentMethod) {
        order.setPaymentMethod(normalizedPaymentMethod);
        
        if ("CASH".equals(normalizedPaymentMethod) && order.getStatus() == OrderStatus.UNPAID) {
            order.setStatus(OrderStatus.PENDING);
        }
        
        if ("VNPAY".equals(normalizedPaymentMethod)) {
            order.setStatus(OrderStatus.UNPAID);
            order.setPaymentCode(null);
        }
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderByCustomer(Long orderId) throws IdInvalidException {
        validateOrderId(orderId);
        
        User currentUser = getCurrentUser();
        Order order = findOrderById(orderId);
        validateOrderOwnership(order, currentUser);
        validateOrderCanBeCancelled(order);
        
        cancelOrder(order);
        Order updatedOrder = orderRepository.save(order);
        createStatusChangeNotification(updatedOrder, OrderStatus.CANCELLED);
        
        return orderMapper.toOrderResponse(updatedOrder);
    }
    
    private void validateOrderOwnership(Order order, User currentUser) {
        if (!Objects.equals(order.getUser().getId(), currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to cancel this order");
        }
    }
    
    private void validateOrderCanBeCancelled(Order order) {
        if (!(order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.UNPAID)) {
            throw new IllegalStateException(
                    "Chỉ có thể hủy đơn hàng khi trạng thái là Chờ xác nhận/Chưa thanh toán");
        }
    }
    
    private void cancelOrder(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        returnInventory(order);
    }

    @Override
    @Transactional
    public OrderResponse confirmReceivedByCustomer(Long orderId) throws IdInvalidException {
        validateOrderId(orderId);
        
        User currentUser = getCurrentUser();
        Order order = findOrderById(orderId);
        validateOrderOwnership(order, currentUser);
        validateOrderCanBeConfirmed(order);
        
        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);
        createStatusChangeNotification(updatedOrder, OrderStatus.COMPLETED);
        
        return orderMapper.toOrderResponse(updatedOrder);
    }
    
    private void validateOrderCanBeConfirmed(Order order) {
        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException(
                    "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng đang trong trạng thái Đang giao");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    private boolean isValidTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        return switch (oldStatus) {
            case UNPAID -> newStatus == OrderStatus.PENDING || newStatus == OrderStatus.CANCELLED;
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.DELIVERING || newStatus == OrderStatus.CANCELLED;
            case DELIVERING -> newStatus == OrderStatus.COMPLETED
                    || newStatus == OrderStatus.PROCESSING
                    || newStatus == OrderStatus.CANCELLED;
            case COMPLETED -> newStatus == OrderStatus.RETURNED;
            case CANCELLED, RETURNED -> false;
        };
    }

    private void createStatusChangeNotification(Order order, OrderStatus newStatus) {
        String title = "Đơn hàng #" + order.getId() + " đã được cập nhật";
        String content;
        if (newStatus == OrderStatus.PENDING) {
            content = "Đặt hàng thành công, đơn hàng của bạn đang chờ xác nhận";
        } else if (newStatus == OrderStatus.PROCESSING) {
            content = "Đơn hàng đã được xác nhận và đang được chuẩn bị";
        } else if (newStatus == OrderStatus.COMPLETED) {
            content = "Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua sắm!";
        } else if (newStatus == OrderStatus.CANCELLED) {
            content = "Đơn hàng đã được hủy";
        } else if (newStatus == OrderStatus.RETURNED) {
            content = "Đơn hàng đã được trả lại";
        } else if (newStatus == OrderStatus.DELIVERING) {
            content = "Đơn hàng đang trên đường giao đến bạn";
        } else {
            content = "Đơn hàng của bạn đã được chuyển sang trạng thái: " + newStatus;
        }

        notificationService.createNotification(null, order.getUser(), title, content);
    }

    private void returnInventory(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }
        for (OrderDetail detail : order.getOrderDetails()) {
            Book book = detail.getBook();
            int quantityToReturn = detail.getQuantity();
            book.setStockQuantity(book.getStockQuantity() + quantityToReturn);
            bookRepository.save(book);
        }
    }

    private void recalculateCartTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        double totalPrice = calculateCartTotalPrice(items);
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
    }
    
    private double calculateCartTotalPrice(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        return items.stream()
                .mapToDouble(ci -> ci.getUnitPrice() * ci.getQuantity())
                .sum();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        validateAuthentication(auth);
        
        String email = extractEmailFromAuth(auth);
        User user = findUserByEmail(email);
        validateUserIsActive(user);
        
        return user;
    }
    
    private void validateAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
    }
    
    private String extractEmailFromAuth(Authentication auth) {
        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email is not found in authentication context.");
        }
        return email;
    }
    
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    private void validateUserIsActive(User user) {
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }
    }
}

