package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.SalesReportDto;

import java.time.LocalDate;

public interface SalesReportService {

    SalesReportDto getSalesReport(
            LocalDate startDate,
            LocalDate endDate
    );
}