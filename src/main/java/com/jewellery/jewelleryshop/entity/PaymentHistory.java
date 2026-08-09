package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kis bill ka payment hai
    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // Kis customer ne payment kiya
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Kitna payment kiya
    @Column(nullable = false)
    private BigDecimal amount;

    // Payment kab hua
    @Column(nullable = false)
    private LocalDateTime paymentDate;

    // Payment kis mode se hua
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @PrePersist
    public void prePersist() {
        this.paymentDate = LocalDateTime.now();
    }
}