package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private JewelleryItem jewelleryItem;

    // Manual item name
    private String itemName;


    // Item weight
    private BigDecimal weight;

    private Integer quantity;

    // Rate at the time of billing
    private BigDecimal metalRate;

    // Calculated gold/metal value
    private BigDecimal metalAmount;

    // Making charge percentage
    private BigDecimal makingChargePercent;

    // Making charge amount
    private BigDecimal makingChargeAmount;

    // GST percentage
    private BigDecimal gstPercent;

    // GST amount
    private BigDecimal gstAmount;

    // Final item total
    private BigDecimal total;
}