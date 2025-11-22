package com.ecom.order.event;

import java.util.UUID;

public record OrderPlacedEvent(UUID orderId, String cartId, String customerId, String customerEmail) {
}
