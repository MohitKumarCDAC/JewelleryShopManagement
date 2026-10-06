package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.GstPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GstPaymentHistoryRepository
        extends JpaRepository<GstPaymentHistory, Long> {

    // Mobile number se GST payment history
    List<GstPaymentHistory>
    findByCustomerMobileOrderByPaymentDateDesc(
            String customerMobile
    );

    // Customer name se GST payment history
    List<GstPaymentHistory>
    findByCustomerNameContainingIgnoreCaseOrderByPaymentDateDesc(
            String customerName
    );

    // Invoice number se GST payment history
    List<GstPaymentHistory>
    findByInvoiceNumberOrderByPaymentDateDesc(
            String invoiceNumber
    );
}