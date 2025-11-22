package com.ecom.order.customer;

import lombok.Data;

@Data
public class CustomerResponse {
    private String cutomerId;

    private String first_name;

    private String last_name;

    private String email;


}
