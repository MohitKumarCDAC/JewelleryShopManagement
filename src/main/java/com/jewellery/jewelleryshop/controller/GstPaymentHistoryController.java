package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.entity.GstPaymentHistory;
import com.jewellery.jewelleryshop.services.GstPaymentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gst-payment-history")
@RequiredArgsConstructor
@CrossOrigin
public class GstPaymentHistoryController {

    private final GstPaymentHistoryService
            gstPaymentHistoryService;


    // =====================================================
    // SEARCH BY MOBILE
    // =====================================================

    @GetMapping("/customer/{mobileNumber}")
    public List<GstPaymentHistory> getByMobile(
            @PathVariable String mobileNumber
    ) {

        return gstPaymentHistoryService
                .getByMobile(mobileNumber);
    }


    // =====================================================
    // SEARCH BY CUSTOMER NAME
    // =====================================================

    @GetMapping("/name")
    public List<GstPaymentHistory> getByName(
            @RequestParam String name
    ) {

        return gstPaymentHistoryService
                .getByName(name);
    }


    // =====================================================
    // SEARCH BY INVOICE NUMBER
    // =====================================================

    @GetMapping("/invoice/{invoiceNumber}")
    public List<GstPaymentHistory> getByInvoice(
            @PathVariable String invoiceNumber
    ) {

        return gstPaymentHistoryService
                .getByInvoiceNumber(invoiceNumber);
    }
}