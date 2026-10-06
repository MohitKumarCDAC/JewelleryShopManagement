package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.GstExchangeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GstExchangeItemRepository
        extends JpaRepository<GstExchangeItem, Long> {

    List<GstExchangeItem> findByGstInvoiceId(Long gstInvoiceId);

    void deleteByGstInvoiceId(Long gstInvoiceId);
}