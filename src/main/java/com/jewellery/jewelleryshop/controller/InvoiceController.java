package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{billNumber}")
    public ResponseEntity<byte[]>downloadInvoice(@PathVariable String billNumber)
    {
        byte[] pdf=invoiceService.generateInvoice(billNumber);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment:filename=invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
