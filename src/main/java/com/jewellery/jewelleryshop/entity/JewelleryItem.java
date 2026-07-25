package com.jewellery.jewelleryshop.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="jewellery_items")
@Getter
@Setter@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JewelleryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String itemCode;

    @Column(nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Purity purity;

    private Double grossWeight;

    private Double netWeight;

    private BigDecimal makingCharge;

    private Double gst;

    private BigDecimal price;

    private Integer stockQuantity;

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist(){
        this.createdDate=LocalDateTime.now();
    }


}
