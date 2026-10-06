package com.jewellery.jewelleryshop.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstExchangeItemDto {

    private Long id;

    private Long gstInvoiceId;

    private String metalType;

    private BigDecimal weight;

    private BigDecimal rate;

    private BigDecimal amount;
}