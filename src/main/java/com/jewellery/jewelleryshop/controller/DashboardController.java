package com.jewellery.jewelleryshop.controller;


import com.jewellery.jewelleryshop.dto.DashboardDto;
import com.jewellery.jewelleryshop.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDto getDashboard()
    {
        return dashboardService.getDashboardData();
    }
}
