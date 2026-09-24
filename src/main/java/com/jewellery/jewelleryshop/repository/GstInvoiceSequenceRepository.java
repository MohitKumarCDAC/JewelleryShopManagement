package com.jewellery.jewelleryshop.repository;

import com.jewellery.jewelleryshop.entity.GstInvoiceSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GstInvoiceSequenceRepository
        extends JpaRepository<GstInvoiceSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM GstInvoiceSequence s
            WHERE s.financialYear = :financialYear
            """)
    Optional<GstInvoiceSequence> findByFinancialYearForUpdate(
            @Param("financialYear") String financialYear
    );
}