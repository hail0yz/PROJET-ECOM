package com.ecom.order.product;

import java.util.Map;

public record ReserveStockRequest(
        String orderId,
        Map<Long, Integer> items
) {
}