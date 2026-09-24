package com.jewellery.jewelleryshop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstInvoiceDto {

    // =========================================================
    // INVOICE IDENTIFICATION
    // =========================================================

    private Long id;

    private String invoiceNumber;

    private String financialYear;

    private LocalDateTime invoiceDateTime;


    // =========================================================
    // PLACE OF SUPPLY
    // =========================================================

    private String placeOfSupply;

    private String placeOfSupplyStateCode;


    // =========================================================
    // SELLER GST DETAILS
    // =========================================================

    private String sellerGstin;


    // =========================================================
    // CUSTOMER DETAILS
    // =========================================================

    private String customerName;

    private String customerMobile;

    private String customerAddress;

    private String customerGstin;


    // =========================================================
    // ITEMS
    // =========================================================

    private List<GstInvoiceItemDto> items;


    // =========================================================
    // TAX SUMMARY
    // =========================================================

    private BigDecimal taxableAmount;

    private BigDecimal cgstAmount;

    private BigDecimal sgstAmount;

    private BigDecimal igstAmount;

    private BigDecimal totalGstAmount;


    // =========================================================
    // TOTALS
    // =========================================================

    private BigDecimal discountAmount;

    private BigDecimal roundOff;

    private BigDecimal grandTotal;


    // =========================================================
    // GST SETTINGS
    // =========================================================

    private Boolean reverseCharge;
}