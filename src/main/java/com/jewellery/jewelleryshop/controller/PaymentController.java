package com.jewellery.jewelleryshop.controller;


import com.jewellery.jewelleryshop.dto.PaymentDto;
import com.jewellery.jewelleryshop.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public PaymentDto createPayment(@RequestBody PaymentDto paymentDto)
    {
        return paymentService.createPayment(paymentDto);
    }

    @GetMapping("/{paymentNumber}")
    public PaymentDto getPaymentByPaymentNumber(@PathVariable String paymentNumber)
    {
        return paymentService.getPaymentByPaymentNumber(paymentNumber);
    }

    @GetMapping
    public List<PaymentDto> getAllPayments()
    {
        return paymentService.getAllPayments();
    }
    @GetMapping("/bill/{billNumber}")
    public List<PaymentDto> getPaymentByBillNumber(@PathVariable String billNumber)
    {
        return paymentService.getPaymentByBillNumber(billNumber);
    }

    @DeleteMapping("/{paymentNumber}")
    public String deletePayment(@PathVariable String paymentNumber)
    {
        paymentService.deletePayment(paymentNumber);
        return "Payment Deleted Successfully";
    }
}
