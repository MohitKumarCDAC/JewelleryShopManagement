package com.jewellery.jewelleryshop.controller;


import com.jewellery.jewelleryshop.dto.DailySalesReportDto;
import com.jewellery.jewelleryshop.services.DailySalesReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DailySalesReportController {

    private final DailySalesReportService dailySalesReportService;

    @GetMapping("/today")
    public DailySalesReportDto getTodaySalesReport()
    {
        return dailySalesReportService.getTodaySalesReport();
    }
}
