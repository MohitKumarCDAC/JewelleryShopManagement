package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.entity.GstPaymentHistory;

import java.util.List;

public interface GstPaymentHistoryService {

    GstPaymentHistory savePayment(
            GstPaymentHistory paymentHistory
    );

    List<GstPaymentHistory> getByMobile(
            String mobileNumber
    );

    List<GstPaymentHistory> getByName(
            String customerName
    );

    List<GstPaymentHistory> getByInvoiceNumber(
            String invoiceNumber
    );
}