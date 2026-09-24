package com.jewellery.jewelleryshop.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstInvoiceItemDto {

    // =========================================================
    // ITEM DETAILS
    // =========================================================

    private Long id;

    private String itemCode;

    private String itemName;

    private String hsnCode;

    private String metalType;

    private String purity;


    // =========================================================
    // QUANTITY / WEIGHT
    // =========================================================

    private BigDecimal quantity;

    private BigDecimal weight;


    // =========================================================
    // RATE / METAL AMOUNT
    // =========================================================

    private BigDecimal metalRate;

    private BigDecimal metalAmount;


    // =========================================================
    // MAKING CHARGE
    // =========================================================

    private String makingChargeType;

    private BigDecimal makingChargeValue;

    private BigDecimal makingChargeAmount;


    // =========================================================
    // DISCOUNT
    // =========================================================

    private BigDecimal discountAmount;


    // =========================================================
    // TAXABLE AMOUNT
    // =========================================================

    private BigDecimal taxableAmount;


    // =========================================================
    // GST
    // =========================================================

    private BigDecimal gstPercent;

    private BigDecimal cgstAmount;

    private BigDecimal sgstAmount;

    private BigDecimal igstAmount;


    // =========================================================
    // ITEM TOTAL
    // =========================================================

    private BigDecimal totalAmount;
}