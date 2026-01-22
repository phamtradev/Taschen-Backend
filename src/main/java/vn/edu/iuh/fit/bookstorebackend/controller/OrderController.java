package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateOrderStatusRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdatePaymentMethodRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) throws IdInvalidException {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        List<OrderResponse> orders = orderService.getMyOrders();
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) throws IdInvalidException {
        OrderResponse orderResponse = orderService.getOrderById(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) throws IdInvalidException {
        OrderResponse orderResponse = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @PutMapping("/{orderId}/payment-method")
    public ResponseEntity<OrderResponse> updatePaymentMethod(
            @PathVariable Long orderId,
            @RequestBody UpdatePaymentMethodRequest request) throws IdInvalidException {
        OrderResponse orderResponse = orderService.updatePaymentMethod(orderId, request.getPaymentMethod());
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrderByCustomer(@PathVariable Long orderId) throws IdInvalidException {
        OrderResponse orderResponse = orderService.cancelOrderByCustomer(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @PutMapping("/{orderId}/confirm-received")
    public ResponseEntity<OrderResponse> confirmReceivedByCustomer(@PathVariable Long orderId) throws IdInvalidException {
        OrderResponse orderResponse = orderService.confirmReceivedByCustomer(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }
}
