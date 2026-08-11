package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.SalesReportDto;
import com.jewellery.jewelleryshop.services.SalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sales-report")
@RequiredArgsConstructor
@CrossOrigin
public class SalesReportController {

    private final SalesReportService salesReportService;

    @GetMapping
    public SalesReportDto getSalesReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {

        return salesReportService.getSalesReport(
                startDate,
                endDate
        );
    }
}