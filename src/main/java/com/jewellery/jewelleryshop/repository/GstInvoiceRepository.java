package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.GstInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GstInvoiceRepository extends JpaRepository<GstInvoice, Long> {

    Optional<GstInvoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<GstInvoice> findAllByOrderByInvoiceDateTimeDesc();
}