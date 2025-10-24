package org.ecom.customerservice.model;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String address1;

    private String address2;

    private String city;

    private String state;

    private String postalCode;

    private String country;

}
