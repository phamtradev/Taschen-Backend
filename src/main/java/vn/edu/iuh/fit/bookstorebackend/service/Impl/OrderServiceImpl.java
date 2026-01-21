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
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.*;
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

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) throws IdInvalidException {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + currentUser.getId()));

        List<CartItem> allItems = cartItemRepository.findByCart(cart);
        if (allItems == null || allItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<CartItem> selectedItems;
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            selectedItems = allItems.stream()
                    .filter(ci -> request.getCartItemIds().contains(ci.getId()))
                    .collect(Collectors.toList());
            if (selectedItems.isEmpty()) {
                throw new IllegalStateException("Không có sản phẩm nào được chọn để đặt hàng");
            }
        } else {
            selectedItems = allItems;
        }

        // Địa chỉ giao hàng
        Address deliveryAddress = null;
        if (request.getAddressId() != null) {
            if (request.getAddressId() <= 0) {
                throw new IdInvalidException("Address identifier is invalid: " + request.getAddressId());
            }
            deliveryAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Address not found with id: " + request.getAddressId()));
            if (!Objects.equals(deliveryAddress.getUser().getId(), currentUser.getId())) {
                throw new IllegalStateException("Address does not belong to current user");
            }
        }

        // Khuyến mãi
        Promotion promotion = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty()) {
            String code = request.getPromotionCode().trim();
            promotion = promotionRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + code));

            if (!Boolean.TRUE.equals(promotion.getIsActive())
                    || promotion.getStatus() != PromotionStatus.ACTIVE
                    || LocalDate.now().isBefore(promotion.getStartDate())
                    || LocalDate.now().isAfter(promotion.getEndDate())) {
                throw new IllegalStateException("Invalid or expired promotion code");
            }

            if (promotion.getQuantity() <= 0) {
                throw new IllegalStateException("Promotion code has been fully used");
            }
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setOrderDate(LocalDateTime.now());

        boolean isVnPay = request.getPaymentMethod() != null
                && "VNPAY".equalsIgnoreCase(request.getPaymentMethod());
        order.setStatus(isVnPay ? OrderStatus.UNPAID : OrderStatus.PENDING);

        if (promotion != null) {
            order.setPromotion(promotion);
            promotion.setQuantity(promotion.getQuantity() - 1);
            promotionRepository.save(promotion);
        }

        if (deliveryAddress != null) {
            order.setDeliveryAddress(deliveryAddress);
        }

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        double subtotal = 0;

        // Validate stock và trừ kho
        for (CartItem cartItem : selectedItems) {
            Book book = cartItem.getBook();
            int quantity = cartItem.getQuantity();

            if (book.getIsActive() == null || !book.getIsActive()) {
                throw new RuntimeException("Book is not active: " + book.getId());
            }
            if (book.getStockQuantity() < quantity) {
                throw new IllegalStateException("Not enough stock for book: " + book.getTitle());
            }

            book.setStockQuantity(book.getStockQuantity() - quantity);
            bookRepository.save(book);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setBook(book);
            detail.setQuantity(quantity);
            detail.setPriceAtPurchase(book.getPrice());
            orderDetails.add(detail);

            subtotal += book.getPrice() * quantity;
        }

        double discountAmount = 0;
        double totalAmount = subtotal;
        if (promotion != null) {
            discountAmount = subtotal * (promotion.getDiscountPercent() / 100.0);
            totalAmount = subtotal - discountAmount;
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(orderDetails);

        Order savedOrder = orderRepository.save(order);
        orderDetailRepository.saveAll(orderDetails);
   
        cartItemRepository.deleteAll(selectedItems);

        recalculateCartTotal(cart);

        String title = "Đơn hàng #" + savedOrder.getId() + " đã được tạo";
        String content = "Đặt hàng thành công, đơn hàng của bạn đang chờ xác nhận";
        notificationService.createNotification(null, currentUser, title, content);

        return convertToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User currentUser = getCurrentUser();
        List<Order> orders = orderRepository.findByUserId(currentUser.getId());
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }

        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        boolean isAdminOrStaff = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role ->
                        "ADMIN".equalsIgnoreCase(role.getCode())
                                || "SELLER_STAFF".equalsIgnoreCase(role.getCode())
                                || "WAREHOUSE_STAFF".equalsIgnoreCase(role.getCode())
                );

        if (!isAdminOrStaff && !Objects.equals(order.getUser().getId(), currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to view this order");
        }

        return convertToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) {
            return convertToOrderResponse(order);
        }

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

        if ((newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED)
                || (newStatus == OrderStatus.RETURNED && oldStatus != OrderStatus.RETURNED)) {
            returnInventory(order);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        createStatusChangeNotification(updatedOrder, newStatus);

        return convertToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentMethod(Long orderId, String newPaymentMethod) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }
        if (newPaymentMethod == null || newPaymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        String normalized = newPaymentMethod.trim().toUpperCase();
        if (!"CASH".equals(normalized) && !"VNPAY".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported payment method: " + newPaymentMethod);
        }

        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        boolean isAdminOrStaff = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role ->
                        "ADMIN".equalsIgnoreCase(role.getCode())
                                || "SELLER_STAFF".equalsIgnoreCase(role.getCode())
                                || "WAREHOUSE_STAFF".equalsIgnoreCase(role.getCode())
                );
        boolean isOwner = Objects.equals(order.getUser().getId(), currentUser.getId());

        if (!isAdminOrStaff && !isOwner) {
            throw new IllegalStateException("You don't have permission to update payment method for this order");
        }

        if (order.getStatus() != OrderStatus.UNPAID && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Không thể đổi phương thức với trạng thái hiện tại: " + order.getStatus());
        }

        order.setPaymentMethod(normalized);

        if ("CASH".equals(normalized) && order.getStatus() == OrderStatus.UNPAID) {
            order.setStatus(OrderStatus.PENDING);
        }

        if ("VNPAY".equals(normalized)) {
            order.setStatus(OrderStatus.UNPAID);
            order.setPaymentCode(null);
        }

        Order saved = orderRepository.save(order);
        return convertToOrderResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderByCustomer(Long orderId) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }

        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        Long userId = order.getUser().getId();
        if (!Objects.equals(userId, currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to cancel this order");
        }

        if (!(order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.UNPAID)) {
            throw new IllegalStateException(
                    "Chỉ có thể hủy đơn hàng khi trạng thái là Chờ xác nhận/Chưa thanh toán");
        }

        order.setStatus(OrderStatus.CANCELLED);
        returnInventory(order);

        Order updatedOrder = orderRepository.save(order);

        createStatusChangeNotification(updatedOrder, OrderStatus.CANCELLED);

        return convertToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse confirmReceivedByCustomer(Long orderId) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }

        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        Long userId = order.getUser().getId();
        if (!Objects.equals(userId, currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to confirm this order");
        }

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException(
                    "Chỉ có thể xác nhận đã nhận hàng khi đơn hàng đang trong trạng thái Đang giao");
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);

        createStatusChangeNotification(updatedOrder, OrderStatus.COMPLETED);

        return convertToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToOrderResponse)
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
        if (items == null || items.isEmpty()) {
            cart.setTotalPrice(0.0);
        } else {
            double total = items.stream()
                    .mapToDouble(ci -> ci.getUnitPrice() * ci.getQuantity())
                    .sum();
            cart.setTotalPrice(total);
        }
        cartRepository.save(cart);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentCode(order.getPaymentCode());

        if (order.getUser() != null) {
            response.setUserId(order.getUser().getId());
            String name;
            if (order.getUser().getFirstName() != null || order.getUser().getLastName() != null) {
                String fn = order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "";
                String ln = order.getUser().getLastName() != null ? order.getUser().getLastName() : "";
                name = (fn + " " + ln).trim();
            } else {
                name = order.getUser().getEmail();
            }
            response.setUserName(name);
        }

        if (order.getPromotion() != null) {
            response.setPromotionId(order.getPromotion().getId());
            response.setPromotionCode(order.getPromotion().getCode());
        }

        if (order.getDeliveryAddress() != null) {
            response.setAddressId(order.getDeliveryAddress().getId());
            String addr = order.getDeliveryAddress().getStreet();
            response.setDeliveryAddress(addr);
        }

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            List<OrderDetailResponse> detailResponses = order.getOrderDetails().stream()
                    .map(this::convertToOrderDetailResponse)
                    .collect(Collectors.toList());
            response.setOrderDetails(detailResponses);
        } else {
            response.setOrderDetails(new ArrayList<>());
        }

        return response;
    }

    private OrderDetailResponse convertToOrderDetailResponse(OrderDetail detail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(detail.getId());
        response.setPriceAtPurchase(detail.getPriceAtPurchase());
        response.setQuantity(detail.getQuantity());
        response.setTotalPrice(detail.getPriceAtPurchase() * detail.getQuantity());

        if (detail.getBook() != null) {
            response.setBookId(detail.getBook().getId());
            response.setBookTitle(detail.getBook().getTitle());
        }

        return response;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }

        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email is not found in authentication context.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }

        return user;
    }
}

