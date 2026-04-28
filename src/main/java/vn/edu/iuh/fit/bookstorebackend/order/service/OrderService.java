package vn.edu.iuh.fit.bookstorebackend.order.service;
import vn.edu.iuh.fit.bookstorebackend.shared.common.PaymentMethod;

import vn.edu.iuh.fit.bookstorebackend.shared.common.OrderStatus;
import vn.edu.iuh.fit.bookstorebackend.order.dto.request.CreateOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request) throws IdInvalidException;

    List<OrderResponse> getMyOrders();

    OrderResponse getOrderById(Long orderId) throws IdInvalidException;

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) throws IdInvalidException;

    OrderResponse updatePaymentMethod(Long orderId, PaymentMethod newPaymentMethod) throws IdInvalidException;

    OrderResponse cancelOrderByCustomer(Long orderId) throws IdInvalidException;

    OrderResponse confirmReceivedByCustomer(Long orderId) throws IdInvalidException;

    OrderResponse updatePaymentCode(Long orderId, String paymentCode) throws IdInvalidException;

    void updatePaymentFromVnPayCallback(Long orderId, String transactionNo) throws IdInvalidException;

    OrderResponse payByCOD(Long orderId) throws IdInvalidException;

    List<OrderResponse> getAllOrders();
}

