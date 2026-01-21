package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.common.OrderStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request) throws IdInvalidException;

    List<OrderResponse> getMyOrders();

    OrderResponse getOrderById(Long orderId) throws IdInvalidException;

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) throws IdInvalidException;

    OrderResponse updatePaymentMethod(Long orderId, String newPaymentMethod) throws IdInvalidException;

    OrderResponse cancelOrderByCustomer(Long orderId) throws IdInvalidException;

    OrderResponse confirmReceivedByCustomer(Long orderId) throws IdInvalidException;

    List<OrderResponse> getAllOrders();
}

