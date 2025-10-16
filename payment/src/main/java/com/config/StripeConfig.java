package com.config;

import com.stripe.Stripe;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
@Configuration
@Slf4j
public class StripeConfig {
    @Value("sk_test_51SIm6gReSF6nTokF1ZUsmPw7GhPTXTW9c25WbF5Jb6b5Pr6MD6wgm9SQeidpSoW0Hg80qvCieuW5YV62sUEcOt3H00DpgNd93r")
    private String stripeApiKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey= stripeApiKey;
    }
}
