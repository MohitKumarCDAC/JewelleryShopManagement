package com.jewellery.jewelleryshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "gst_invoice_sequences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_gst_invoice_financial_year",
                        columnNames = "financial_year"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstInvoiceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "financial_year", nullable = false, unique = true, length = 10)
    private String financialYear;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}