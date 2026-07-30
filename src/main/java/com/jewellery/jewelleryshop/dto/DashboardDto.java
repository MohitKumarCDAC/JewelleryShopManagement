package com.jewellery.jewelleryshop.dto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private Long totalCustomers;
    private Long totalJewelleryItems;
    private Long totalBills;
    private Long totalPayments;
    private BigDecimal totalSales;
    private BigDecimal totalDueAmount;
}
