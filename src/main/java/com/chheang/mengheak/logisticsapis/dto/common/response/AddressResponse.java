package com.chheang.mengheak.logisticsapis.dto.common.response;

import lombok.Data;

@Data
public class AddressResponse {
    private String line1;
    private String line2;
    private String city;
    private String province;
    private String postalCode;
    private String countryCode;
    private String contactName;
    private String contactPhone;
}
