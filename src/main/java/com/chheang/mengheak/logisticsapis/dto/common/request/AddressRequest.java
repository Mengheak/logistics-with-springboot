package com.chheang.mengheak.logisticsapis.dto.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "address line 1 is required")
    @Size(max = 160, message = "address line 1 must be at most 160 characters")
    private String line1;

    @Size(max = 160, message = "address line 2 must be at most 160 characters")
    private String line2;

    @NotBlank(message = "city is required")
    @Size(max = 80, message = "city must be at most 80 characters")
    private String city;

    @Size(max = 80, message = "province must be at most 80 characters")
    private String province;

    @Size(max = 20, message = "postal code must be at most 20 characters")
    private String postalCode;

    @NotBlank(message = "country code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "country code must be a 2-letter ISO code in upper case")
    private String countryCode;

    @Size(max = 120, message = "contact name must be at most 120 characters")
    private String contactName;

    @Size(max = 30, message = "contact phone must be at most 30 characters")
    private String contactPhone;
}
