package com.ecom.notification.kafka.order;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        int quantity
) {
}
