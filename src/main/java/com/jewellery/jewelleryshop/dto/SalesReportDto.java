package com.jewellery.jewelleryshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDto {

    private long totalBills;

    private BigDecimal totalSales;

    private BigDecimal totalPaid;

    private BigDecimal totalDue;

    private BigDecimal cashSales;

    private BigDecimal upiSales;

    private BigDecimal cardSales;

    private BigDecimal bankTransferSales;


    private BigDecimal todaySales;

    private BigDecimal yesterdaySales;
}