package com.jewellery.jewelleryshop.dto;

import com.jewellery.jewelleryshop.entity.PaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private String paymentNumber;
    private String billNumber;
    private BigDecimal amount;
    private PaymentMode paymentMode;
   // private String transactionId;
    private LocalDateTime paymentDate;

}
