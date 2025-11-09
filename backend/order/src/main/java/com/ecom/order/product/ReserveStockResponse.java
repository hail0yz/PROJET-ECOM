package com.ecom.order.product;

public record ReserveStockResponse(
        String orderId,
        boolean success,
        String message
) {}