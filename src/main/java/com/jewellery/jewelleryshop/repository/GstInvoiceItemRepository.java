package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.GstInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GstInvoiceItemRepository
        extends JpaRepository<GstInvoiceItem, Long> {

    List<GstInvoiceItem> findByGstInvoiceId(Long gstInvoiceId);

    void deleteByGstInvoiceId(Long gstInvoiceId);
}