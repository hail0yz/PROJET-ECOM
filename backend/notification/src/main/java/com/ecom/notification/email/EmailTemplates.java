package com.ecom.notification.email;

import lombok.Getter;

public enum EmailTemplates {
    PAYMENT_CONFIRMATION("payment-confirmation.html","payment successfuly processed"),
    ORDER_CONFIRMATION("order-confirmation.html","Order successfuly processed");

    @Getter
    private final String template;

    @Getter
    private final String subject;

    EmailTemplates(String subject, String template) {
        this.subject = subject;
        this.template = template;
    }
}
