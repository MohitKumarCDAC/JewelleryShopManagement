package com.jewellery.jewelleryshop.repository;
import com.jewellery.jewelleryshop.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CustomerRepositry extends JpaRepository<Customer,Long> {
    Optional<Customer>findByMobileNumber(String mobileNumber);


}
