package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepositry extends JpaRepository<Customer, Long> {

    // Existing mobile number search
    Optional<Customer> findByMobileNumber(String mobileNumber);

    // Search customers by name - starts with, case insensitive
    List<Customer> findByCustomerNameStartingWithIgnoreCase(String customerName);
}