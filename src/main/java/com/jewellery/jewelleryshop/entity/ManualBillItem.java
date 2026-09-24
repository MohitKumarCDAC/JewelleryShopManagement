package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "manual_bill_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_manual_item_name_metal",
                        columnNames = {"item_name", "metal_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualBillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "metal_type", nullable = false, length = 20)
    private MetalType metalType;

    @Column(nullable = false)
    private Boolean active = true;
}
