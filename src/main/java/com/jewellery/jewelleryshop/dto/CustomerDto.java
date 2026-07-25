package com.jewellery.jewelleryshop.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

    @NotBlank(message = "Customer Name is Required")
    private String customerName;

    @NotBlank(message = "Mobile Number Is Required")
    @Pattern(regexp = "^[0-9]{10}$",message = "Mobile Number Must be 10 Digits")
    private String mobileNumber;

    @NotBlank(message = "Place is Required")
    private String place;



}
