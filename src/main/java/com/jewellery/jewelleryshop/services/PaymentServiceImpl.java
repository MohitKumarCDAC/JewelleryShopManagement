package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.PaymentDto;
import com.jewellery.jewelleryshop.entity.Bill;
import com.jewellery.jewelleryshop.entity.Payment;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    //methods implements
    @Override
    public PaymentDto createPayment(PaymentDto paymentDto)
    {
        Bill bill=billRepository.findByBillNumber(paymentDto.getBillNumber())
                .orElseThrow(()->new RuntimeException("Bill not found"));

        Payment payment=Payment.builder()
                .paymentNumber(paymentDto.getPaymentNumber())
                .bill(bill)
                .amount(paymentDto.getAmount())
                .paymentMode(paymentDto.getPaymentMode())
                .build();

        Payment savedPayment=paymentRepository.save(payment);

        return PaymentDto.builder()
                .paymentNumber(savedPayment.getPaymentNumber())
                .billNumber(savedPayment.getBill().getBillNumber())
                .amount(savedPayment.getAmount())
                .paymentMode(savedPayment.getPaymentMode())
                .paymentDate(savedPayment.getPaymentDate())
                .build();
    }

    @Override
    public PaymentDto getPaymentByPaymentNumber(String paymentNumber){
        Payment payment=paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(()->new RuntimeException("Payment Not Found"));

        return PaymentDto.builder()
                .paymentNumber(payment.getPaymentNumber())
                .billNumber(payment.getBill().getBillNumber())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    @Override
    public List<PaymentDto> getAllPayments()
    {
        return paymentRepository.findAll().stream()
                .map(payment -> PaymentDto.builder()
                        .paymentNumber(payment.getPaymentNumber())
                        .billNumber(payment.getBill().getBillNumber())
                        .amount(payment.getAmount())
                        .paymentMode(payment.getPaymentMode())
                        .paymentDate(payment.getPaymentDate())
                        .build()).toList();
    }

    @Override
    public List<PaymentDto>getPaymentByBillNumber(String billNumber)
    {
        return paymentRepository.findByBill_BillNumber(billNumber)
                .stream().map(payment -> PaymentDto.builder()
                        .paymentNumber(payment.getPaymentNumber())
                        .billNumber(payment.getBill().getBillNumber())
                        .amount(payment.getAmount())
                        .paymentMode(payment.getPaymentMode())
                        .paymentDate(payment.getPaymentDate()).build()).toList();
    }

    @Override
    public void deletePayment(String paymentNumber)
    {
        Payment payment=paymentRepository.findByPaymentNumber(paymentNumber).orElseThrow(()->new RuntimeException("Payment Not Found"));
        paymentRepository.delete(payment);
    }
}
