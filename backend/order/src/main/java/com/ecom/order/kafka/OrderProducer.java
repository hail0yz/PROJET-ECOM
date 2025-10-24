package com.ecom.order.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {


    private final KafkaTemplate<String,OrderConfirmation> template;

    public void sentOrderConfirmation(OrderConfirmation orderConfirmation){
         log.info("sending order confirmation");
        Message<OrderConfirmation> message= MessageBuilder
                .withPayload(orderConfirmation)
                .setHeader(TOPIC,"orders")
                .build();
        template.send(message);
    }
}
