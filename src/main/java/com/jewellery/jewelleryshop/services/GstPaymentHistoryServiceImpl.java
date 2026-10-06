package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.entity.GstPaymentHistory;
import com.jewellery.jewelleryshop.repository.GstPaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GstPaymentHistoryServiceImpl
        implements GstPaymentHistoryService {

    private final GstPaymentHistoryRepository
            gstPaymentHistoryRepository;


    @Override
    public GstPaymentHistory savePayment(
            GstPaymentHistory paymentHistory
    ) {

        return gstPaymentHistoryRepository.save(
                paymentHistory
        );
    }


    @Override
    public List<GstPaymentHistory> getByMobile(
            String mobileNumber
    ) {

        return gstPaymentHistoryRepository
                .findByCustomerMobileOrderByPaymentDateDesc(
                        mobileNumber
                );
    }


    @Override
    public List<GstPaymentHistory> getByName(
            String customerName
    ) {

        return gstPaymentHistoryRepository
                .findByCustomerNameContainingIgnoreCaseOrderByPaymentDateDesc(
                        customerName
                );
    }


    @Override
    public List<GstPaymentHistory> getByInvoiceNumber(
            String invoiceNumber
    ) {

        return gstPaymentHistoryRepository
                .findByInvoiceNumberOrderByPaymentDateDesc(
                        invoiceNumber
                );
    }
}