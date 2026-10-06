package com.jewellery.jewelleryshop.controller;

import com.jewellery.jewelleryshop.dto.GstInvoiceDto;
import com.jewellery.jewelleryshop.services.GstInvoiceNumberService;
import com.jewellery.jewelleryshop.services.GstInvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    // UPDATE GST INVOICE
    // =========================================================

    @PutMapping("/update")
    public ResponseEntity<GstInvoiceDto> updateInvoice(
            @RequestParam String invoiceNumber,
            @RequestBody GstInvoiceDto invoiceDto
    ) {

        GstInvoiceDto updatedInvoice =
                gstInvoiceService.updateInvoice(
                        invoiceNumber,
                        invoiceDto
                );

        return ResponseEntity.ok(updatedInvoice);
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
    // GET GST INVOICES BY CUSTOMER MOBILE
    // =========================================================

    @GetMapping("/customer")
    public ResponseEntity<List<GstInvoiceDto>>
    getInvoicesByCustomerMobile(
            @RequestParam String mobile
    ) {

        List<GstInvoiceDto> invoices =
                gstInvoiceService.getInvoicesByCustomerMobile(
                        mobile
                );

        return ResponseEntity.ok(invoices);
    }

    // =========================================================
    // GET OUTSTANDING GST INVOICES
    // =========================================================

    @GetMapping("/outstanding")
    public ResponseEntity<List<GstInvoiceDto>>
    getOutstandingInvoices() {

        List<GstInvoiceDto> invoices =
                gstInvoiceService.getOutstandingInvoices();

        return ResponseEntity.ok(invoices);
    }

    // =========================================================
    // PAY GST INVOICE DUE
    // =========================================================

    @PutMapping("/pay")
    public ResponseEntity<GstInvoiceDto> payDue(
            @RequestParam String invoiceNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "CASH") String paymentMode
    ) {

        GstInvoiceDto updatedInvoice =
                gstInvoiceService.payDue(
                        invoiceNumber,
                        amount,
                        paymentMode
                );

        return ResponseEntity.ok(updatedInvoice);
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
