package org.ecom.customerservice.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatsResponse {
    private long totalTickets;

    // Counts by priority (LOW, MEDIUM, HIGH, URGENT)
    private Map<String, Long> countsByPriority = new LinkedHashMap<>();

    // Counts by type (ORDER_ISSUE, PAYMENT_ISSUE, ...)
    private Map<String, Long> countsByType = new LinkedHashMap<>();

    // Weekly trend for the last N weeks. Key = "YYYY-'W'ww"
    private Map<String, Long> weeklyCounts = new LinkedHashMap<>();

    // Monthly trend for the last N months. Key = "YYYY-MM"
    private Map<String, Long> monthlyCounts = new LinkedHashMap<>();

}
