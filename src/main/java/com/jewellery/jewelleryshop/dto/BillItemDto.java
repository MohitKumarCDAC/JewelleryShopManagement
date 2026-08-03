package com.jewellery.jewelleryshop.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemDto {

    private String itemCode;

    private Integer quantity;

    // Billing time values
    private BigDecimal metalRate;

    private BigDecimal makingChargePercent;

    private BigDecimal gstPercent;
}