package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository
        extends JpaRepository<PaymentHistory, Long> {

    // Bill ke saare payment records
    List<PaymentHistory> findByBill_BillNumberOrderByPaymentDateDesc(
            String billNumber
    );

    // Customer ke saare payment records
    List<PaymentHistory> findByCustomer_MobileNumberOrderByPaymentDateDesc(
            String mobileNumber
    );
}