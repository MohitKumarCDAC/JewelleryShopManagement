package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.entity.GstInvoiceSequence;
import com.jewellery.jewelleryshop.repository.GstInvoiceSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GstInvoiceNumberService {

    private final GstInvoiceSequenceRepository repository;

    public GstInvoiceNumberService(
            GstInvoiceSequenceRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Generates and reserves the next GST invoice number.
     *
     * Format:
     * MJ/26-27/0001
     * MJ/26-27/0002
     */
    @Transactional
    public synchronized String generateNextInvoiceNumber() {

        String financialYear = getCurrentFinancialYear();

        GstInvoiceSequence sequence =
                repository.findByFinancialYearForUpdate(financialYear)
                        .orElseGet(() -> {

                            GstInvoiceSequence newSequence =
                                    GstInvoiceSequence.builder()
                                            .financialYear(financialYear)
                                            .lastNumber(0L)
                                            .build();

                            return repository.save(newSequence);
                        });

        long nextNumber = sequence.getLastNumber() + 1;

        sequence.setLastNumber(nextNumber);

        repository.save(sequence);

        return String.format(
                "MJ/%s/%04d",
                financialYear,
                nextNumber
        );
    }

    /**
     * Indian Financial Year
     *
     * April to March
     *
     * Example:
     * 24-09-2026 -> 26-27
     * 10-03-2027 -> 26-27
     * 10-04-2027 -> 27-28
     */
    public String getCurrentFinancialYear() {

        LocalDate today = LocalDate.now();

        int year = today.getYear();

        int startYear;

        if (today.getMonthValue() >= 4) {
            startYear = year;
        } else {
            startYear = year - 1;
        }

        int endYear = startYear + 1;

        return String.format(
                "%02d-%02d",
                startYear % 100,
                endYear % 100
        );
    }
}