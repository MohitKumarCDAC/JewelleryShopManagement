package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.PaymentDto;

import java.util.List;

public interface PaymentService {
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto getPaymentByPaymentNumber(String paymentNumber);
    List<PaymentDto>getAllPayments();
    List<PaymentDto>getPaymentByBillNumber(String billNumber);
    void deletePayment(String paymentNumber);
}
