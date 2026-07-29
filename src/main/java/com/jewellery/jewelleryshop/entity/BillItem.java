package com.jewellery.jewelleryshop.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="bill_items")
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
    @JoinColumn(name="bill_id")
    private Bill bill;

    @ManyToOne
    @JoinColumn(name="item_id")
    private JewelleryItem jewelleryItem;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal makingCharge;
    private Double gst;
    private BigDecimal total;

}
