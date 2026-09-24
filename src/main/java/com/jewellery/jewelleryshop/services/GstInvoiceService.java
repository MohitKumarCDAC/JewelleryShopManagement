package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.GstInvoiceDto;

import java.util.List;

public interface GstInvoiceService {

    GstInvoiceDto createInvoice(GstInvoiceDto invoiceDto);

    GstInvoiceDto getInvoiceByNumber(String invoiceNumber);

    List<GstInvoiceDto> getAllInvoices();

    void deleteInvoice(String invoiceNumber);
}