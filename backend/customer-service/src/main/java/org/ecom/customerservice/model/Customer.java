package org.ecom.customerservice.model;

import jakarta.persistence.Embedded;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class Customer extends User {

    @Embedded
    private Contact contact;

    @Embedded
    private Preferences preferences;

}
