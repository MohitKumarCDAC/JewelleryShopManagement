package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.entity.PaymentHistory;

import java.util.List;

public interface PaymentHistoryService {

    PaymentHistory savePayment(PaymentHistory paymentHistory);

    List<PaymentHistory> getPaymentsByBillNumber(
            String billNumber
    );

    List<PaymentHistory> getPaymentsByCustomerMobile(
            String mobileNumber
    );
}