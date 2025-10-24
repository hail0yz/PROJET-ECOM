package com.ecom.order.product;


import lombok.Data;

import java.util.UUID;

@Data
public class PurchaseResponse {
    private UUID productId;
    private String name;
    private String description;
    private double price;
    private int quantity;
}
