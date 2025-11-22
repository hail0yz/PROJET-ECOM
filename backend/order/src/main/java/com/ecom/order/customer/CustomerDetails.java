package com.ecom.order.customer;

public record CustomerDetails(
        String id,
        String externalId,
        String firstname,
        String lastname,
        String email,
        PaymentDetails paymentDetails,
        AddressDetails addressDetails,
        boolean active,
        BlacklistDetails blacklistDetails
) {

    public record PaymentDetails() {
    }

    public record AddressDetails() {
    }

    public record BlacklistDetails(
            boolean blacklisted,
            String reason,
            String blacklistedBy
    ) {
    }

}
