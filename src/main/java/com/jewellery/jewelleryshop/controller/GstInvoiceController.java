package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.GstInvoiceDto;
import com.jewellery.jewelleryshop.services.GstInvoiceNumberService;
import com.jewellery.jewelleryshop.services.GstInvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gst-invoices")
@CrossOrigin(origins = "http://localhost:5173")
public class GstInvoiceController {

    private final GstInvoiceNumberService gstInvoiceNumberService;

    private final GstInvoiceService gstInvoiceService;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GstInvoiceController(
            GstInvoiceNumberService gstInvoiceNumberService,
            GstInvoiceService gstInvoiceService
    ) {
        this.gstInvoiceNumberService = gstInvoiceNumberService;
        this.gstInvoiceService = gstInvoiceService;
    }


    // =========================================================
    // GENERATE GST INVOICE NUMBER
    // =========================================================

    @PostMapping("/generate-number")
    public String generateInvoiceNumber() {

        return gstInvoiceNumberService
                .generateNextInvoiceNumber();
    }


    // =========================================================
    // CREATE GST INVOICE
    // =========================================================

    @PostMapping
    public ResponseEntity<GstInvoiceDto> createInvoice(
            @RequestBody GstInvoiceDto invoiceDto
    ) {

        GstInvoiceDto savedInvoice =
                gstInvoiceService.createInvoice(
                        invoiceDto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedInvoice);
    }


    // =========================================================
    // GET GST INVOICE BY INVOICE NUMBER
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<GstInvoiceDto> getInvoiceByNumber(
            @RequestParam String invoiceNumber
    ) {

        GstInvoiceDto invoice =
                gstInvoiceService.getInvoiceByNumber(
                        invoiceNumber
                );

        return ResponseEntity.ok(invoice);
    }


    // =========================================================
    // GET ALL GST INVOICES
    // =========================================================

    @GetMapping
    public ResponseEntity<List<GstInvoiceDto>> getAllInvoices() {

        List<GstInvoiceDto> invoices =
                gstInvoiceService.getAllInvoices();

        return ResponseEntity.ok(invoices);
    }


    // =========================================================
// DELETE GST INVOICE
// =========================================================

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteInvoice(
            @RequestParam String invoiceNumber
    ) {

        gstInvoiceService.deleteInvoice(
                invoiceNumber
        );

        return ResponseEntity.ok(
                "GST Invoice Deleted Successfully"
        );
    }
}