package com.jewellery.jewelleryshop.repository;


import com.jewellery.jewelleryshop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);
    List<Payment> findByBill_BillNumber(String billNumber);
}
