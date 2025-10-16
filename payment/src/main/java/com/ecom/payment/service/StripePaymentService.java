package com.ecom.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*------------------------------------------
 * TO DO : update URL after confirming payment
 * 
 * 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {
    public PaymentIntent crPaymentIntent(BigDecimal amount, String customerEmail, String description){
        try {
            long amountInCents=amount.multiply(BigDecimal.valueOf(100)).longValue();
            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInCents)
                            .setCurrency("eur")
                            .setReceiptEmail(customerEmail)
                            .setDescription(description)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                            .setEnabled(true)
                                            .build()
                            )
                            .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent;
        } catch (CardException e) {
            System.out.printf("A payment error occurred: %s%n", e.getMessage());
          } catch (InvalidRequestException e) {
            System.out.printf("An invalid request occurred. (request id: %s)%n", e.getMessage());
          } catch (Exception e) {
           System.out.println("Another problem occurred, maybe unrelated to Stripe.");
          }
        return null;
    }
    public PaymentIntent confPaymentIntent(String paymentID,String pm){
        try {
            PaymentIntent resource = PaymentIntent.retrieve(paymentID);
            PaymentIntentConfirmParams params =
                    PaymentIntentConfirmParams.builder()
                            .setPaymentMethod(pm)
                            .setReturnUrl("https://www.example.com")
                            .build();
            PaymentIntent paymentIntent = resource.confirm(params);
            return paymentIntent;
          } catch (CardException e) {
            System.out.printf("A payment error occurred: %s%n", e.getMessage());
          } catch (InvalidRequestException e) {
            System.out.printf("An invalid request occurred. (request id: %s)%n", e.getMessage());
          } catch (Exception e) {
           System.out.println("Another problem occurred, maybe unrelated to Stripe.");
          }
        return null;
    }
    public PaymentIntent captPaymentIntent(String paymentID){
        try {
            PaymentIntent resource = PaymentIntent.retrieve(paymentID);
            PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder().build();
            PaymentIntent paymentIntent = resource.capture(params);
            return paymentIntent;
        } catch (CardException e) {
            System.out.printf("A payment error occurred: %s%n", e.getMessage());
          } catch (InvalidRequestException e) {
            System.out.printf("An invalid request occurred. (request id: %s)%n", e.getMessage());
          } catch (Exception e) {
           System.out.println("Another problem occurred, maybe unrelated to Stripe.");
          }
        return null;
    }
    public PaymentIntent cancelPaymentIntent(String paymentID){
        try {
            PaymentIntent resource = PaymentIntent.retrieve(paymentID);
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
            PaymentIntent paymentIntent = resource.cancel(params);
            return paymentIntent;
        } catch (CardException e) {
            System.out.printf("A payment error occurred: %s%n", e.getMessage());
          } catch (InvalidRequestException e) {
            System.out.printf("An invalid request occurred. (request id: %s)%n", e.getMessage());
          } catch (Exception e) {
           System.out.println("Another problem occurred, maybe unrelated to Stripe.");
          }
        return null;
    }
}
