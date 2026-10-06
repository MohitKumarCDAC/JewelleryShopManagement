package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gst_exchange_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstExchangeItem {

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
    // EXCHANGE ITEM DETAILS
    // =========================================================

    @Column(
            name = "metal_type",
            nullable = false,
            length = 20
    )
    private String metalType;


    // =========================================================
    // WEIGHT
    // =========================================================

    @Column(
            name = "weight",
            nullable = false,
            precision = 19,
            scale = 3
    )
    @Builder.Default
    private BigDecimal weight = BigDecimal.ZERO;


    // =========================================================
    // RATE
    // =========================================================

    @Column(
            name = "rate",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;


    // =========================================================
    // EXCHANGE AMOUNT
    // =========================================================

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
}