package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bills")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String billNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime billDate;

    private BigDecimal totalAmount;

    private BigDecimal discount;

    private BigDecimal gstAmount;

    private BigDecimal grandTotal;

    private BigDecimal paidAmount;

    private BigDecimal dueAmount;

    @Enumerated(EnumType.STRING)
    private BillStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @OneToMany(mappedBy = "bill",cascade = CascadeType.ALL)
    private List<BillItem> items;

    @PrePersist
    public void prePersist() {
        this.billDate = LocalDateTime.now();
    }
}