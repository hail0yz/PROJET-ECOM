package com.ecom.order.mapper;

import com.ecom.order.dto.OrderLineResponse;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.model.DeliveryInfo;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderLine;
import com.ecom.order.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void fromOrder_null_returnsNull() {
        assertThat(mapper.fromOrder(null)).isNull();
    }

    @Test
    void fromOrder_fullMapping_mapsAllFieldsAndLines() {
        UUID orderId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();

        OrderLine line = OrderLine.builder()
                .orderLineId(lineId)
                .productId(123L)
                .quantity(2)
                .build();

        DeliveryInfo delivery = new DeliveryInfo("addr1", "addr2", "city", "state", "zip", "country");

        Order order = Order.builder()
                .id(orderId)
                .cartId(55L)
                .customerId("cust-1")
                .totalAmount(new BigDecimal("42.50"))
                .status(OrderStatus.CONFIRMED)
                .deliveryInfo(delivery)
                .orderLines(List.of(line))
                .build();

        line.setOrder(order);

        OrderResponse resp = mapper.fromOrder(order);

        assertThat(resp).isNotNull();
        assertThat(resp.getOrderId()).isEqualTo(orderId.toString());
        assertThat(resp.getAmount()).isEqualByComparingTo(new BigDecimal("42.50"));
        assertThat(resp.getCartId()).isEqualTo(55L);
        assertThat(resp.getCustomerId()).isEqualTo("cust-1");
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // lines
        assertThat(resp.getLines()).hasSize(1);
        OrderLineResponse lr = resp.getLines().get(0);
        assertThat(lr.getId()).isEqualTo(lineId);
        assertThat(lr.getProductId()).isEqualTo(123L);
        assertThat(lr.getQuantity()).isEqualTo(2);

        // delivery info
        assertThat(resp.getDeliveryInfo()).isNotNull();
        assertThat(resp.getDeliveryInfo().address1()).isEqualTo("addr1");
        assertThat(resp.getDeliveryInfo().city()).isEqualTo("city");
        assertThat(resp.getDeliveryInfo().postalCode()).isEqualTo("zip");
    }

}
