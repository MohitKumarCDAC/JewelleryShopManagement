package com.jewellery.jewelleryshop.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutstandingBillDto {
    private String billNumber;
    private LocalDateTime billDate;
    private String customerName;
    private String customerMobile;
    private String customerPlace;
    private List<BillItemDto> items;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;




}
