package com.jewellery.jewelleryshop.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemDto {

    private String itemCode;
    private Integer quantity;
}
