package com.ecom.bookService.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ReservationResult(
        boolean success,
        String orderId,
        Long reservationId,
        ReservationStatus status,
        @JsonInclude(JsonInclude.Include.NON_NULL) Metadata metadata,
        String message
) {

    public static ReservationResult success(String orderId, Long reservationId) {
        return new ReservationResult(
                true,
                orderId,
                reservationId,
                ReservationStatus.RESERVED,
                null,
                "Stock reserved successfully"
        );
    }

    public static ReservationResult success(Long reservationId) {
        return new ReservationResult(
                true,
                null,
                reservationId,
                ReservationStatus.RESERVED,
                null,
                "Stock reserved successfully"
        );
    }

    public static ReservationResult failed(String orderId, Long reservationId, String message) {
        return new ReservationResult(
                true,
                orderId,
                reservationId,
                ReservationStatus.FAILED,
                null,
                message
        );
    }

    public static ReservationResult failed(Long reservationId, String message) {
        return failed(null, reservationId, message);
    }

    public static ReservationResult failed(String message) {
        return failed(null, null, message);
    }

    public static ReservationResult failed(Long reservationId) {
        return failed(null, reservationId, "Failed to reserve stock");
    }

    public static ReservationResult alreadyReserved(Long reservationId) {
        return new ReservationResult(
                true,
                null,
                reservationId,
                ReservationStatus.ALREADY_RESERVED,
                null,
                "Stock already reserved for this order"
        );
    }

    public static ReservationResult insufficientStock(Long reservationId, Map<String, Integer> products) {
        List<Item> items = products.keySet().stream()
                .map(s -> new Item(s, products.get(s)))
                .toList();

        return new ReservationResult(
                false,
                null,
                reservationId,
                ReservationStatus.INSUFFICIENT_STOCK,
                new Items(items),
                "Insufficient stock"
        );
    }

    public static ReservationResult productNotFound(List<Long> booksIds) {
        return new ReservationResult(
                false,
                null,
                null,
                ReservationStatus.PRODUCT_NOT_FOUND,
                null,
                "At least one book not found"
        );
    }

    public enum ReservationStatus {
        RESERVED,
        PARTIALLY_RESERVED,
        ALREADY_RESERVED,
        INSUFFICIENT_STOCK,
        PRODUCT_NOT_FOUND,
        PRODUCT_INACTIVE,
        INVALID_QUANTITY,
        CONCURRENT_MODIFICATION,
        LOCK_ACQUISITION_FAILED,
        FAILED
    }

    public interface Metadata {
    }

    public record ProductNotFound(String productId) implements Metadata {
    }

    public record Item(String productId, Integer quantity) implements Metadata {
    }

    public record Items(List<Item> items) implements Metadata {
    }

}