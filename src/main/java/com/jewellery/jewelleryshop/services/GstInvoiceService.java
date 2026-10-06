package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.GstInvoiceDto;

import java.math.BigDecimal;
import java.util.List;

public interface GstInvoiceService {

    GstInvoiceDto createInvoice(GstInvoiceDto invoiceDto);

    GstInvoiceDto updateInvoice(
            String invoiceNumber,
            GstInvoiceDto invoiceDto
    );

    GstInvoiceDto getInvoiceByNumber(String invoiceNumber);

    List<GstInvoiceDto> getAllInvoices();

    List<GstInvoiceDto> getInvoicesByCustomerMobile(String customerMobile);

    List<GstInvoiceDto> getOutstandingInvoices();

    GstInvoiceDto payDue(
            String invoiceNumber,
            BigDecimal amount,
            String paymentMode
    );

    void deleteInvoice(String invoiceNumber);
}
