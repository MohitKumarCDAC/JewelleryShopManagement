package com.jewellery.jewelleryshop.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, unique = true ,length = 10)
    private String mobileNumber;

    @Column(nullable = false)
    private String place;

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist(){
        this.createdDate=LocalDateTime.now();
    }
}
