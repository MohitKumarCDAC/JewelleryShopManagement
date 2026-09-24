package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gst_invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // INVOICE RELATION
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "gst_invoice_id",
            nullable = false
    )
    private GstInvoice gstInvoice;


    // =========================================================
    // ITEM DETAILS
    // =========================================================

    @Column(
            name = "item_code",
            length = 50
    )
    private String itemCode;

    @Column(
            name = "item_name",
            nullable = false,
            length = 150
    )
    private String itemName;

    @Column(
            name = "hsn_code",
            length = 20
    )
    private String hsnCode;

    @Column(
            name = "metal_type",
            length = 20
    )
    private String metalType;

    @Column(
            name = "purity",
            length = 20
    )
    private String purity;


    // =========================================================
    // QUANTITY / WEIGHT
    // =========================================================

    @Column(
            name = "quantity",
            precision = 19,
            scale = 3
    )
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(
            name = "weight",
            precision = 19,
            scale = 3
    )
    @Builder.Default
    private BigDecimal weight = BigDecimal.ZERO;


    // =========================================================
    // RATE / METAL AMOUNT
    // =========================================================

    @Column(
            name = "metal_rate",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal metalRate = BigDecimal.ZERO;

    @Column(
            name = "metal_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal metalAmount = BigDecimal.ZERO;


    // =========================================================
    // MAKING CHARGE
    // =========================================================

    @Column(
            name = "making_charge_type",
            length = 20
    )
    private String makingChargeType;

    @Column(
            name = "making_charge_value",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal makingChargeValue = BigDecimal.ZERO;

    @Column(
            name = "making_charge_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal makingChargeAmount = BigDecimal.ZERO;


    // =========================================================
    // DISCOUNT
    // =========================================================

    @Column(
            name = "discount_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;


    // =========================================================
    // TAXABLE AMOUNT
    // =========================================================

    @Column(
            name = "taxable_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal taxableAmount = BigDecimal.ZERO;


    // =========================================================
    // GST
    // =========================================================

    @Column(
            name = "gst_percent",
            precision = 5,
            scale = 2
    )
    @Builder.Default
    private BigDecimal gstPercent = BigDecimal.ZERO;

    @Column(
            name = "cgst_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(
            name = "sgst_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(
            name = "igst_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;


    // =========================================================
    // ITEM TOTAL
    // =========================================================

    @Column(
            name = "total_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}