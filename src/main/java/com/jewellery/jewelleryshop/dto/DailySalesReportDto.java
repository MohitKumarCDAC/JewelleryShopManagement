package com.jewellery.jewelleryshop.dto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesReportDto {

    private Long totalBills;
    private BigDecimal totalSales;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalDueAmount;
    private Long totalCustomers;
}
