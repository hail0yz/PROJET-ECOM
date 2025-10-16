package org.ecom.customerservice.model;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "customers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Customer extends User {

    private LocalDate dateOfBirth;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "email", column = @Column(name = "contact_email", length = 5)),
            @AttributeOverride(name = "phoneNumber.extension", column = @Column(name = "phone_number_extension")),
            @AttributeOverride(name = "phoneNumber.number", column = @Column(name = "phone_number_number"))
    })
    private Contact contact;

    @Embedded
    private Preferences preferences;

}
