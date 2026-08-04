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
    private String itemName;

    private Integer quantity;

    private BigDecimal weight;

    // Billing time values
    private BigDecimal metalRate;

    private BigDecimal makingChargePercent;

    private BigDecimal gstPercent;

    private BigDecimal total;
}