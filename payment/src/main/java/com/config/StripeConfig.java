package com.ecom.payment.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API Key configured: " +
            (stripeApiKey != null ? "sk_****..." : "NOT FOUND"));
    }
}