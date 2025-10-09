package com.ecom.notification.kafka;

import com.ecom.notification.email.EmailService;
import com.ecom.notification.kafka.order.OrderConfirmation;
import com.ecom.notification.kafka.payment.PaymentConfirmation;
import com.ecom.notification.model.Notification;
import com.ecom.notification.model.NotificationType;
import com.ecom.notification.repository.NotificationRepo;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics="payment-topic")
    public void consumePaymentSuccesNotification(PaymentConfirmation paymentConfirmation)throws MessagingException {
        log.info("Consuming payment succes notification from payment topic");

        notificationRepo.save( Notification.builder()
                .notificationType(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                        .paymentConfirmation(paymentConfirmation)
                .build()
        );

        //send email
        var customerName=paymentConfirmation.customerFirstname()+" "+paymentConfirmation.customerLastname();
        emailService.sendPaymentSuccesEmail(
                paymentConfirmation.customerEmail(),
                customerName,
                paymentConfirmation.amount(),
                paymentConfirmation.orderReference()
        );


    }

    @KafkaListener(topics="orders")
    public void consumeOrderConfirmationNotification(OrderConfirmation orderConfirmation)throws MessagingException {
        log.info("Consuming payment succes notification from ORDER topic");

        notificationRepo.save( Notification.builder()
                .notificationType(NotificationType.ORDER_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .orderConfirmation(orderConfirmation)
                .build()
        );

        // todo send email
        var customerName=orderConfirmation.customer().firstname()+" "+orderConfirmation.customer().lastname();
        emailService.sendOrderConfirmationEmail(
                orderConfirmation.customer().email(),
                customerName,
                orderConfirmation.totalAmount(),
                orderConfirmation.orderReference(),
                orderConfirmation.products()
        );

    }
}
