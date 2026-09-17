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

    // Old field - backward compatibility ke liye rakha gaya hai
    private BigDecimal makingChargePercent;

    // New making charge fields
    private String makingChargeType;

    private BigDecimal makingChargeValue;

    private BigDecimal gstPercent;

    private BigDecimal total;
}