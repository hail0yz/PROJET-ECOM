package com.ecom.bookService.dto;

import java.util.Map;

public record ReserveStockRequest(String orderId, Map<Long, Integer> items) {
}