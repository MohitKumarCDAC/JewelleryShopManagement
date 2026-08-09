
package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.entity.PaymentHistory;
import com.jewellery.jewelleryshop.services.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-history")
@RequiredArgsConstructor
@CrossOrigin
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;


    // ==========================================
    // GET PAYMENTS BY BILL NUMBER
    // ==========================================

    @GetMapping("/bill/{billNumber}")
    public List<PaymentHistory> getPaymentsByBillNumber(
            @PathVariable String billNumber
    ) {

        return paymentHistoryService
                .getPaymentsByBillNumber(billNumber);
    }


    // ==========================================
    // GET PAYMENTS BY CUSTOMER MOBILE
    // ==========================================

    @GetMapping("/customer/{mobileNumber}")
    public List<PaymentHistory> getPaymentsByCustomerMobile(
            @PathVariable String mobileNumber
    ) {

        return paymentHistoryService
                .getPaymentsByCustomerMobile(mobileNumber);
    }
}

