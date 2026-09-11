package com.chheang.mengheak.logisticsapis.entities.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A postal address plus the person to contact there. Embedded rather than a table of its own:
 * addresses are snapshots that must not change when a customer later edits their profile.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(length = 160)
    private String line1;

    @Column(length = 160)
    private String line2;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String province;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 2)
    private String countryCode;

    @Column(length = 120)
    private String contactName;

    @Column(length = 30)
    private String contactPhone;
}
