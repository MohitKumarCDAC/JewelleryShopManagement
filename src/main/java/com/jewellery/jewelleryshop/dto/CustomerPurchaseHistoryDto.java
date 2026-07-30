package com.jewellery.jewelleryshop.dto;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPurchaseHistoryDto {

    private String customerName;
    private String mobileNumber;
    private Long totalBills;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalDueAmount;
    private List<String> billNumbers;
}
