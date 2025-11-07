package com.ecom.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

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

    public PaymentIntent createPaymentIntent(BigDecimal amount, String customerEmail, String description) throws StripeException {

        log.info("Creating Stripe PaymentIntent - Amount: {}, Email: {}", amount, customerEmail);

        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
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

        log.info("Stripe PaymentIntent created: {}", paymentIntent.getId());

        return paymentIntent;

    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException {

        log.info("Confirming Stripe PaymentIntent: {}", paymentIntentId);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .build();

        PaymentIntent confirmedIntent = paymentIntent.confirm(params);

        log.info("Stripe PaymentIntent confirmed: {}", confirmedIntent.getStatus());

        return confirmedIntent;

    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {

        log.info("Retrieving Stripe PaymentIntent: {}", paymentIntentId);

        return PaymentIntent.retrieve(paymentIntentId);

    }

    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {

        log.info("Cancelling Stripe PaymentIntent: {}", paymentIntentId);

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntent cancelledIntent = paymentIntent.cancel();

        log.info("Stripe PaymentIntent cancelled: {}", cancelledIntent.getId());

        return cancelledIntent;

    }

    public Refund createRefund(String chargeId, BigDecimal amount, String reason) throws StripeException {

        log.info("Creating Stripe Refund - Charge: {}, Amount: {}", chargeId, amount);

        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setCharge(chargeId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);

        if (amount != null) {

            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            paramsBuilder.setAmount(amountInCents);

        }

        RefundCreateParams params = paramsBuilder.build();

        Refund refund = Refund.create(params);

        log.info("Stripe Refund created: {}", refund.getId());

        return refund;

    }

    public String checkPaymentStatus(String paymentIntentId) throws StripeException {

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        return paymentIntent.getStatus();

    }

}
