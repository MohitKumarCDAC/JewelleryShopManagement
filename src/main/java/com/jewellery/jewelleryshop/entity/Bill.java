package com.jewellery.jewelleryshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "gold_exchange_weight", precision = 19, scale = 3)
    @Builder.Default
    private BigDecimal goldExchangeWeight = BigDecimal.ZERO;

    @Column(name = "gold_exchange_rate", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal goldExchangeRate = BigDecimal.ZERO;

    @Column(name = "gold_exchange_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal goldExchangeAmount = BigDecimal.ZERO;

    @Column(name = "silver_exchange_weight", precision = 19, scale = 3)
    @Builder.Default
    private BigDecimal silverExchangeWeight = BigDecimal.ZERO;

    @Column(name = "silver_exchange_rate", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal silverExchangeRate = BigDecimal.ZERO;

    @Column(name = "silver_exchange_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal silverExchangeAmount = BigDecimal.ZERO;

    @Column(name = "total_exchange_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalExchangeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private BillStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @OneToMany(mappedBy = "bill",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<BillItem> items;

    @PrePersist
    public void prePersist() {
        this.billDate = LocalDateTime.now();
    }
}