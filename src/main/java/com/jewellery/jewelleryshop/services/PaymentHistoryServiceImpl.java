package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.entity.PaymentHistory;
import com.jewellery.jewelleryshop.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceImpl
        implements PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;


    @Override
    public PaymentHistory savePayment(
            PaymentHistory paymentHistory
    ) {

        return paymentHistoryRepository.save(
                paymentHistory
        );
    }


    @Override
    public List<PaymentHistory> getPaymentsByBillNumber(
            String billNumber
    ) {

        return paymentHistoryRepository
                .findByBill_BillNumberOrderByPaymentDateDesc(
                        billNumber
                );
    }


    @Override
    public List<PaymentHistory> getPaymentsByCustomerMobile(
            String mobileNumber
    ) {

        return paymentHistoryRepository
                .findByCustomer_MobileNumberOrderByPaymentDateDesc(
                        mobileNumber
                );
    }
}