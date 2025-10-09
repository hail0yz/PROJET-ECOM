package com.ecom.order.service;


import com.ecom.order.customer.CustomerResponse;
import com.ecom.order.customer.CutomerClient;
import com.ecom.order.dto.OrderLineRequest;
import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.exception.BusinessException;
import com.ecom.order.kafka.OrderConfirmation;
import com.ecom.order.kafka.OrderProducer;
import com.ecom.order.mapper.OrderMapper;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderLine;
import com.ecom.order.payment.PaymentInterface;
import com.ecom.order.payment.PaymentRequest;
import com.ecom.order.product.ProductClient;
import com.ecom.order.product.PurchaseRequest;
import com.ecom.order.product.PurchaseResponse;
import com.ecom.order.repository.OrderRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CutomerClient customerClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderLineService orderLineService;

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private PaymentInterface paymentInterface;

    public UUID createOrder(OrderRequest orderRequest) {
        CustomerResponse customer=customerClient.getCustomer(orderRequest.getCustomerId())
                .orElseThrow(() -> new BusinessException("Customer not found"));

        List<PurchaseResponse> products=productClient.purchaseProducts(orderRequest.getProducts());

        Order order=orderRepo.save(orderMapper.toOrder(orderRequest));

       for(PurchaseRequest purchaseRequest:orderRequest.getProducts()){
           orderLineService.saveOrderLine(
                   new OrderLineRequest(
                           order.getId(),
                           purchaseRequest.getProductId(),
                           purchaseRequest.getQuantity()
                   )
           );
       }

       //send orderconfirmation to payment microservice
        var paymentRequest = new PaymentRequest(
                order.getReference(),
                order.getId(),
                orderRequest.getPaymentMethod(),
                orderRequest.getAmmount(),
                customer
        );

        paymentInterface.requestOrderPayment(paymentRequest);


       //send orderconfirmation to notification microservice
        OrderConfirmation orderConfirmation=new OrderConfirmation(
                orderRequest.getReference(),
                orderRequest.getAmmount(),
                orderRequest.getPaymentMethod(),
                customer,
                products
        );
       orderProducer.sentOrderConfirmation(orderConfirmation);
       return order.getId();

    }


    public List<OrderResponse> findAllOrders() {
        return this.orderRepo.findAll()
                .stream()
                .map(this.orderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(UUID id) {
        return this.orderRepo.findById(id)
                .map(this.orderMapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", id)));
    }
}
