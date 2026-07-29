package com.jewellery.jewelleryshop.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentNumber;

    @ManyToOne
    @JoinColumn(name="bill_id")
    private Bill bill;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    //private String transactionId;

    private LocalDateTime paymentDate;

    @PrePersist
    public void prePersist()
    {
        paymentDate=LocalDateTime.now();
    }
}
