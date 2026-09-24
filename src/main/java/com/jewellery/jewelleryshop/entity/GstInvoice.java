package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "gst_invoices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_gst_invoice_number",
                        columnNames = "invoice_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // INVOICE IDENTIFICATION
    // =========================================================

    @Column(
            name = "invoice_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String invoiceNumber;

    @Column(
            name = "financial_year",
            nullable = false,
            length = 10
    )
    private String financialYear;

    @Column(
            name = "invoice_date_time",
            nullable = false
    )
    private LocalDateTime invoiceDateTime;


    // =========================================================
    // PLACE OF SUPPLY
    // =========================================================

    @Column(
            name = "place_of_supply",
            length = 100
    )
    private String placeOfSupply;

    @Column(
            name = "place_of_supply_state_code",
            length = 10
    )
    private String placeOfSupplyStateCode;


    // =========================================================
    // SELLER GST DETAILS
    // =========================================================

    @Column(
            name = "seller_gstin",
            length = 15
    )
    private String sellerGstin;


    // =========================================================
    // CUSTOMER GST DETAILS
    // =========================================================

    @Column(
            name = "customer_name",
            length = 150
    )
    private String customerName;

    @Column(
            name = "customer_mobile",
            length = 20
    )
    private String customerMobile;

    @Column(
            name = "customer_address",
            length = 500
    )
    private String customerAddress;

    @Column(
            name = "customer_gstin",
            length = 15
    )
    private String customerGstin;


    // =========================================================
    // TAX SUMMARY
    // =========================================================

    @Column(
            name = "taxable_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal taxableAmount = BigDecimal.ZERO;

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

    @Column(
            name = "total_gst_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal totalGstAmount = BigDecimal.ZERO;


    // =========================================================
    // TOTALS
    // =========================================================

    @Column(
            name = "discount_amount",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(
            name = "round_off",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal roundOff = BigDecimal.ZERO;

    @Column(
            name = "grand_total",
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;


    // =========================================================
    // GST SETTINGS
    // =========================================================

    @Column(
            name = "reverse_charge"
    )
    @Builder.Default
    private Boolean reverseCharge = false;


    @OneToMany(
            mappedBy = "gstInvoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<GstInvoiceItem> items;
}