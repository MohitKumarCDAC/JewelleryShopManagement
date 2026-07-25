package com.jewellery.jewelleryshop.dto;


import com.jewellery.jewelleryshop.entity.Category;
import com.jewellery.jewelleryshop.entity.Purity;
import lombok.*;

import java.math.BigDecimal;

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
    private Double grossWeight;
    private Double netWeight;
    private BigDecimal makingCharge;
    private Double gst;
    private BigDecimal price;
    private  Integer stockQuantity;

}
