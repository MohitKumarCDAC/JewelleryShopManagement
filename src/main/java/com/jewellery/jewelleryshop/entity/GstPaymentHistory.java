package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gst_payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GST Invoice Number
    @Column(name = "invoice_number", nullable = false, length = 30)
    private String invoiceNumber;

    // Customer Name
    @Column(name = "customer_name", length = 150)
    private String customerName;

    // Customer Mobile
    @Column(name = "customer_mobile", length = 20)
    private String customerMobile;

    // Payment Amount
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Payment Date
    @Column(nullable = false)
    private LocalDateTime paymentDate;

    // CASH / UPI / CARD / OTHER
    @Column(name = "payment_mode", nullable = false, length = 20)
    private String paymentMode;

    @PrePersist
    public void prePersist() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }
}