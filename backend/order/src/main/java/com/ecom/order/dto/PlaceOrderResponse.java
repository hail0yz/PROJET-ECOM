package com.ecom.order.dto;

import java.util.UUID;

import com.ecom.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderResponse {

    private UUID orderId;

    private Long paymentId;

    private OrderStatus orderStatus;

}
