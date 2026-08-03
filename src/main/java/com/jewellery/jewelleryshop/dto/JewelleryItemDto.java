package com.jewellery.jewelleryshop.dto;

import com.jewellery.jewelleryshop.entity.Category;
import com.jewellery.jewelleryshop.entity.Purity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JewelleryItemDto {

    private String itemCode;
    private String itemName;
    private Category category;
    private Purity purity;
    private Double weight;
    private Integer stockQuantity;
}