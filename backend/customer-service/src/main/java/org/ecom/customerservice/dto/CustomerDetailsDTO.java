package org.ecom.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDetailsDTO {

    private Long id;

    private String externalId;

    private String firstname;

    private String lastname;

    private String email;

    private PaymentDetails paymentDetails;

    private AddressDetails addressDetails;

    private boolean active;

    private BlacklistDetails blacklistDetails;

    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
    @Builder
    public static class PaymentDetails {}

    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
    @Builder
    public static class AddressDetails {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlacklistDetails {
        private boolean blacklisted;
        private String reason;
        private String blacklistedBy; // ID of admin or SYSTEM if blacklisted systematically
    }

}
