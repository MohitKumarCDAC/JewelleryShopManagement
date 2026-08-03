package com.jewellery.jewelleryshop.dto;

import com.jewellery.jewelleryshop.entity.BillStatus;
import com.jewellery.jewelleryshop.entity.PaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDto {

    private String billNumber;

    private String customerMobile;

    private List<BillItemDto> items;

    private BigDecimal discount;

    private BigDecimal paidAmount;

    private PaymentMode paymentMode;

    private BillStatus status;

    // Calculated values
    private BigDecimal totalAmount;

    private BigDecimal gstAmount;

    private BigDecimal grandTotal;

    private BigDecimal dueAmount;
}