package com.jewellery.jewelleryshop.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBillingInfoDto {

    private String customerName;

    private String mobileNumber;

    private BigDecimal totalDueAmount;
}