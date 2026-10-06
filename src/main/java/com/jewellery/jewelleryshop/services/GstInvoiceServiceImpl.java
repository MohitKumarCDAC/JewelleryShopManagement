package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.GstExchangeItemDto;
import com.jewellery.jewelleryshop.dto.GstInvoiceDto;
import com.jewellery.jewelleryshop.dto.GstInvoiceItemDto;
import com.jewellery.jewelleryshop.entity.GstExchangeItem;
import com.jewellery.jewelleryshop.entity.GstInvoice;
import com.jewellery.jewelleryshop.entity.GstInvoiceItem;
import com.jewellery.jewelleryshop.entity.GstPaymentHistory;
import com.jewellery.jewelleryshop.repository.GstExchangeItemRepository;
import com.jewellery.jewelleryshop.repository.GstInvoiceItemRepository;
import com.jewellery.jewelleryshop.repository.GstInvoiceRepository;
import com.jewellery.jewelleryshop.repository.GstPaymentHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GstInvoiceServiceImpl implements GstInvoiceService {

    private final GstInvoiceRepository gstInvoiceRepository;
    private final GstInvoiceItemRepository gstInvoiceItemRepository;
    private final GstExchangeItemRepository gstExchangeItemRepository;
    private final GstInvoiceNumberService gstInvoiceNumberService;
    private final GstInvoiceCalculationService gstInvoiceCalculationService;
    private final GstExchangeCalculationService gstExchangeCalculationService;
    private final GstPaymentHistoryRepository gstPaymentHistoryRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GstInvoiceServiceImpl(
            GstInvoiceRepository gstInvoiceRepository,
            GstInvoiceItemRepository gstInvoiceItemRepository,
            GstExchangeItemRepository gstExchangeItemRepository,
            GstInvoiceNumberService gstInvoiceNumberService,
            GstInvoiceCalculationService gstInvoiceCalculationService,
            GstExchangeCalculationService gstExchangeCalculationService,
            GstPaymentHistoryRepository gstPaymentHistoryRepository
    ) {

        this.gstInvoiceRepository =
                gstInvoiceRepository;

        this.gstInvoiceItemRepository =
                gstInvoiceItemRepository;

        this.gstExchangeItemRepository =
                gstExchangeItemRepository;

        this.gstInvoiceNumberService =
                gstInvoiceNumberService;

        this.gstInvoiceCalculationService =
                gstInvoiceCalculationService;

        this.gstExchangeCalculationService =
                gstExchangeCalculationService;

        this.gstPaymentHistoryRepository =
                gstPaymentHistoryRepository;
    }


    // =========================================================
    // CREATE GST INVOICE
    // =========================================================

    @Override
    @Transactional
    public GstInvoiceDto createInvoice(
            GstInvoiceDto invoiceDto
    ) {

        if (invoiceDto == null) {

            throw new IllegalArgumentException(
                    "GST Invoice data cannot be null"
            );
        }


        // -----------------------------------------------------
        // GENERATE INVOICE NUMBER
        // -----------------------------------------------------

        String invoiceNumber =
                gstInvoiceNumberService
                        .generateNextInvoiceNumber();


        // -----------------------------------------------------
        // FINANCIAL YEAR
        // -----------------------------------------------------

        String financialYear =
                gstInvoiceNumberService
                        .getCurrentFinancialYear();


        // -----------------------------------------------------
        // DETERMINE TAX TYPE
        // -----------------------------------------------------

        boolean intraState =
                isIntraStateSupply(invoiceDto);


        // =====================================================
        // CALCULATE SALE ITEMS
        // =====================================================

        List<GstInvoiceItemDto> calculatedItems =
                new ArrayList<>();


        if (invoiceDto.getItems() != null) {

            for (
                    GstInvoiceItemDto itemDto
                    : invoiceDto.getItems()
            ) {

                GstInvoiceItemDto calculatedItem =
                        gstInvoiceCalculationService
                                .calculateItem(
                                        itemDto,
                                        intraState
                                );

                calculatedItems.add(
                        calculatedItem
                );
            }
        }


        // =====================================================
        // CALCULATE EXCHANGE ITEMS
        // =====================================================

        List<GstExchangeItemDto> calculatedExchangeItems =
                new ArrayList<>();

        BigDecimal totalExchangeAmount =
                BigDecimal.ZERO;


        if (invoiceDto.getExchangeItems() != null) {

            for (
                    GstExchangeItemDto exchangeDto
                    : invoiceDto.getExchangeItems()
            ) {

                GstExchangeItemDto calculatedExchangeItem =
                        gstExchangeCalculationService
                                .calculateItem(
                                        exchangeDto
                                );

                calculatedExchangeItems.add(
                        calculatedExchangeItem
                );

                totalExchangeAmount =
                        totalExchangeAmount.add(
                                valueOrZero(
                                        calculatedExchangeItem
                                                .getAmount()
                                )
                        );
            }
        }


        totalExchangeAmount =
                totalExchangeAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        invoiceDto.setTotalExchangeAmount(
                totalExchangeAmount
        );


        // =====================================================
        // CALCULATE INVOICE TOTALS
        // =====================================================

        invoiceDto.setDiscountAmount(
                valueOrZero(
                        invoiceDto.getDiscountAmount()
                )
        );

        invoiceDto.setRoundOff(
                valueOrZero(
                        invoiceDto.getRoundOff()
                )
        );


        gstInvoiceCalculationService
                .calculateInvoiceTotals(
                        invoiceDto,
                        calculatedItems
                );


        // =====================================================
        // CALCULATE PAYMENT DETAILS
        // =====================================================

        BigDecimal cashAmount =
                nonNegative(invoiceDto.getCashAmount());

        BigDecimal upiAmount =
                nonNegative(invoiceDto.getUpiAmount());

        BigDecimal cardAmount =
                nonNegative(invoiceDto.getCardAmount());

        BigDecimal otherAmount =
                nonNegative(invoiceDto.getOtherAmount());

        BigDecimal paidAmount =
                cashAmount
                        .add(upiAmount)
                        .add(cardAmount)
                        .add(otherAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal invoiceTotal =
                valueOrZero(invoiceDto.getGrandTotal())
                        .setScale(2, RoundingMode.HALF_UP);

        // Exchange amount reduces the amount actually payable by the customer.
        BigDecimal exchangeAmount =
                nonNegative(totalExchangeAmount);

        BigDecimal payableAmount =
                invoiceTotal
                        .subtract(exchangeAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }

        BigDecimal dueAmount =
                payableAmount
                        .subtract(paidAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        // Do not allow negative due.
        if (dueAmount.compareTo(BigDecimal.ZERO) < 0) {
            dueAmount = BigDecimal.ZERO;
        }

        String paymentStatus;

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = "PAID";
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = "PARTIAL";
        } else {
            paymentStatus = "DUE";
        }

        invoiceDto.setCashAmount(cashAmount);
        invoiceDto.setUpiAmount(upiAmount);
        invoiceDto.setCardAmount(cardAmount);
        invoiceDto.setOtherAmount(otherAmount);
        invoiceDto.setPaidAmount(paidAmount);
        invoiceDto.setDueAmount(dueAmount);
        invoiceDto.setPaymentStatus(paymentStatus);


        // =====================================================
        // CREATE INVOICE ENTITY
        // =====================================================

        GstInvoice invoice =
                GstInvoice.builder()

                        .invoiceNumber(
                                invoiceNumber
                        )

                        .financialYear(
                                financialYear
                        )

                        .invoiceDateTime(
                                invoiceDto.getInvoiceDateTime() != null
                                        ? invoiceDto.getInvoiceDateTime()
                                        : LocalDateTime.now()
                        )

                        .placeOfSupply(
                                invoiceDto.getPlaceOfSupply()
                        )

                        .placeOfSupplyStateCode(
                                invoiceDto
                                        .getPlaceOfSupplyStateCode()
                        )

                        .sellerGstin(
                                invoiceDto.getSellerGstin()
                        )

                        .customerName(
                                invoiceDto.getCustomerName()
                        )

                        .customerMobile(
                                invoiceDto.getCustomerMobile()
                        )

                        .customerAddress(
                                invoiceDto.getCustomerAddress()
                        )

                        .customerGstin(
                                invoiceDto.getCustomerGstin()
                        )

                        .taxableAmount(
                                invoiceDto.getTaxableAmount()
                        )

                        .cgstAmount(
                                invoiceDto.getCgstAmount()
                        )

                        .sgstAmount(
                                invoiceDto.getSgstAmount()
                        )

                        .igstAmount(
                                invoiceDto.getIgstAmount()
                        )

                        .totalGstAmount(
                                invoiceDto.getTotalGstAmount()
                        )

                        .discountAmount(
                                invoiceDto.getDiscountAmount()
                        )

                        .roundOff(
                                invoiceDto.getRoundOff()
                        )

                        .grandTotal(
                                invoiceDto.getGrandTotal()
                        )

                        .reverseCharge(
                                invoiceDto.getReverseCharge()
                        )

                        .totalExchangeAmount(
                                totalExchangeAmount
                        )

                        .cashAmount(
                                cashAmount
                        )

                        .upiAmount(
                                upiAmount
                        )

                        .cardAmount(
                                cardAmount
                        )

                        .otherAmount(
                                otherAmount
                        )

                        .paidAmount(
                                paidAmount
                        )

                        .dueAmount(
                                dueAmount
                        )

                        .paymentStatus(
                                paymentStatus
                        )

                        .build();


        // =====================================================
        // SAVE INVOICE
        // =====================================================

        GstInvoice savedInvoice =
                gstInvoiceRepository.save(
                        invoice
                );

        // Save initial payments into GST payment history.
        saveInitialPaymentHistory(savedInvoice);


        // =====================================================
        // SAVE SALE ITEMS
        // =====================================================

        List<GstInvoiceItem> savedItems =
                new ArrayList<>();


        for (
                GstInvoiceItemDto itemDto
                : calculatedItems
        ) {

            GstInvoiceItem item =
                    GstInvoiceItem.builder()

                            .gstInvoice(
                                    savedInvoice
                            )

                            .itemCode(
                                    itemDto.getItemCode()
                            )

                            .itemName(
                                    itemDto.getItemName()
                            )

                            .hsnCode(
                                    itemDto.getHsnCode()
                            )

                            .metalType(
                                    itemDto.getMetalType()
                            )

                            .purity(
                                    itemDto.getPurity()
                            )

                            .quantity(
                                    itemDto.getQuantity()
                            )

                            .weight(
                                    itemDto.getWeight()
                            )

                            .metalRate(
                                    itemDto.getMetalRate()
                            )

                            .metalAmount(
                                    itemDto.getMetalAmount()
                            )

                            .makingChargeType(
                                    itemDto.getMakingChargeType()
                            )

                            .makingChargeValue(
                                    itemDto.getMakingChargeValue()
                            )

                            .makingChargeAmount(
                                    itemDto.getMakingChargeAmount()
                            )

                            .discountAmount(
                                    itemDto.getDiscountAmount()
                            )

                            .taxableAmount(
                                    itemDto.getTaxableAmount()
                            )

                            .gstPercent(
                                    itemDto.getGstPercent()
                            )

                            .cgstAmount(
                                    itemDto.getCgstAmount()
                            )

                            .sgstAmount(
                                    itemDto.getSgstAmount()
                            )

                            .igstAmount(
                                    itemDto.getIgstAmount()
                            )

                            .totalAmount(
                                    itemDto.getTotalAmount()
                            )

                            .build();


            savedItems.add(
                    gstInvoiceItemRepository.save(
                            item
                    )
            );
        }


        // =====================================================
        // SAVE EXCHANGE ITEMS
        // =====================================================

        List<GstExchangeItem> savedExchangeItems =
                new ArrayList<>();


        for (
                GstExchangeItemDto exchangeDto
                : calculatedExchangeItems
        ) {

            GstExchangeItem exchangeItem =
                    GstExchangeItem.builder()

                            .gstInvoice(
                                    savedInvoice
                            )

                            .metalType(
                                    exchangeDto.getMetalType()
                            )

                            .weight(
                                    exchangeDto.getWeight()
                            )

                            .rate(
                                    exchangeDto.getRate()
                            )

                            .amount(
                                    exchangeDto.getAmount()
                            )

                            .build();


            savedExchangeItems.add(
                    gstExchangeItemRepository.save(
                            exchangeItem
                    )
            );
        }


        // =====================================================
        // RETURN SAVED INVOICE
        // =====================================================

        return convertToDto(
                savedInvoice,
                savedItems,
                savedExchangeItems
        );
    }


    // =========================================================
    // UPDATE GST INVOICE
    // =========================================================

    @Override
    @Transactional
    public GstInvoiceDto updateInvoice(
            String invoiceNumber,
            GstInvoiceDto invoiceDto
    ) {

        if (invoiceNumber == null ||
                invoiceNumber.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Invoice number is required"
            );
        }

        if (invoiceDto == null) {
            throw new IllegalArgumentException(
                    "GST Invoice data cannot be null"
            );
        }

        GstInvoice invoice =
                gstInvoiceRepository
                        .findByInvoiceNumber(
                                invoiceNumber.trim()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "GST Invoice Not Found: "
                                                + invoiceNumber
                                )
                        );

        boolean intraState =
                isIntraStateSupply(invoiceDto);

        // -----------------------------------------------------
        // RECALCULATE SALE ITEMS
        // -----------------------------------------------------

        List<GstInvoiceItemDto> calculatedItems =
                new ArrayList<>();

        if (invoiceDto.getItems() != null) {

            for (
                    GstInvoiceItemDto itemDto
                    : invoiceDto.getItems()
            ) {

                GstInvoiceItemDto calculatedItem =
                        gstInvoiceCalculationService
                                .calculateItem(
                                        itemDto,
                                        intraState
                                );

                calculatedItems.add(calculatedItem);
            }
        }

        // -----------------------------------------------------
        // RECALCULATE EXCHANGE ITEMS
        // -----------------------------------------------------

        List<GstExchangeItemDto> calculatedExchangeItems =
                new ArrayList<>();

        BigDecimal totalExchangeAmount =
                BigDecimal.ZERO;

        if (invoiceDto.getExchangeItems() != null) {

            for (
                    GstExchangeItemDto exchangeDto
                    : invoiceDto.getExchangeItems()
            ) {

                GstExchangeItemDto calculatedExchangeItem =
                        gstExchangeCalculationService
                                .calculateItem(exchangeDto);

                calculatedExchangeItems.add(
                        calculatedExchangeItem
                );

                totalExchangeAmount =
                        totalExchangeAmount.add(
                                valueOrZero(
                                        calculatedExchangeItem.getAmount()
                                )
                        );
            }
        }

        totalExchangeAmount =
                totalExchangeAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        invoiceDto.setTotalExchangeAmount(
                totalExchangeAmount
        );

        // -----------------------------------------------------
        // RECALCULATE INVOICE TOTALS
        // -----------------------------------------------------

        invoiceDto.setDiscountAmount(
                valueOrZero(invoiceDto.getDiscountAmount())
        );

        invoiceDto.setRoundOff(
                valueOrZero(invoiceDto.getRoundOff())
        );

        gstInvoiceCalculationService
                .calculateInvoiceTotals(
                        invoiceDto,
                        calculatedItems
                );

        // -----------------------------------------------------
        // PRESERVE EXISTING PAYMENT
        // -----------------------------------------------------

        // -----------------------------------------------------
        // USE PAYMENT VALUES FROM UPDATE REQUEST
        // -----------------------------------------------------

        BigDecimal cashAmount =
                nonNegative(invoiceDto.getCashAmount());

        BigDecimal upiAmount =
                nonNegative(invoiceDto.getUpiAmount());

        BigDecimal cardAmount =
                nonNegative(invoiceDto.getCardAmount());

        BigDecimal otherAmount =
                nonNegative(invoiceDto.getOtherAmount());

        BigDecimal paidAmount =
                cashAmount
                        .add(upiAmount)
                        .add(cardAmount)
                        .add(otherAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal invoiceTotal =
                valueOrZero(invoiceDto.getGrandTotal())
                        .setScale(2, RoundingMode.HALF_UP);

        // Exchange amount is already calculated from the exchange items.
        BigDecimal exchangeAmount =
                nonNegative(totalExchangeAmount);

        BigDecimal payableAmount =
                invoiceTotal
                        .subtract(exchangeAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }

        if (paidAmount.compareTo(payableAmount) > 0) {

            throw new IllegalArgumentException(
                    "Invoice cannot be updated because paid amount "
                            + paidAmount
                            + " is greater than payable amount after exchange "
                            + payableAmount
            );
        }

        BigDecimal dueAmount =
                payableAmount
                        .subtract(paidAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        String paymentStatus;

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = "PAID";
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = "PARTIAL";
        } else {
            paymentStatus = "DUE";
        }

        // -----------------------------------------------------
        // UPDATE MAIN INVOICE
        // -----------------------------------------------------

        if (invoiceDto.getInvoiceDateTime() != null) {
            invoice.setInvoiceDateTime(invoiceDto.getInvoiceDateTime());
        }

        invoice.setPlaceOfSupply(
                invoiceDto.getPlaceOfSupply()
        );

        invoice.setPlaceOfSupplyStateCode(
                invoiceDto.getPlaceOfSupplyStateCode()
        );

        invoice.setSellerGstin(
                invoiceDto.getSellerGstin()
        );

        invoice.setCustomerName(
                invoiceDto.getCustomerName()
        );

        invoice.setCustomerMobile(
                invoiceDto.getCustomerMobile()
        );

        invoice.setCustomerAddress(
                invoiceDto.getCustomerAddress()
        );

        invoice.setCustomerGstin(
                invoiceDto.getCustomerGstin()
        );

        invoice.setTaxableAmount(
                invoiceDto.getTaxableAmount()
        );

        invoice.setCgstAmount(
                invoiceDto.getCgstAmount()
        );

        invoice.setSgstAmount(
                invoiceDto.getSgstAmount()
        );

        invoice.setIgstAmount(
                invoiceDto.getIgstAmount()
        );

        invoice.setTotalGstAmount(
                invoiceDto.getTotalGstAmount()
        );

        invoice.setDiscountAmount(
                invoiceDto.getDiscountAmount()
        );

        invoice.setRoundOff(
                invoiceDto.getRoundOff()
        );

        invoice.setGrandTotal(
                invoiceDto.getGrandTotal()
        );

        invoice.setReverseCharge(
                invoiceDto.getReverseCharge()
        );

        invoice.setTotalExchangeAmount(
                totalExchangeAmount
        );

        invoice.setCashAmount(cashAmount);
        invoice.setUpiAmount(upiAmount);
        invoice.setCardAmount(cardAmount);
        invoice.setOtherAmount(otherAmount);
        invoice.setPaidAmount(paidAmount);
        invoice.setDueAmount(dueAmount);
        invoice.setPaymentStatus(paymentStatus);

        GstInvoice savedInvoice =
                gstInvoiceRepository.save(invoice);

        // -----------------------------------------------------
        // REPLACE OLD SALE ITEMS
        // -----------------------------------------------------

        List<GstInvoiceItem> oldItems =
                gstInvoiceItemRepository
                        .findByGstInvoiceId(
                                savedInvoice.getId()
                        );

        if (!oldItems.isEmpty()) {
            gstInvoiceItemRepository.deleteAll(oldItems);
        }

        List<GstInvoiceItem> savedItems =
                new ArrayList<>();

        for (
                GstInvoiceItemDto itemDto
                : calculatedItems
        ) {

            GstInvoiceItem item =
                    GstInvoiceItem.builder()
                            .gstInvoice(savedInvoice)
                            .itemCode(itemDto.getItemCode())
                            .itemName(itemDto.getItemName())
                            .hsnCode(itemDto.getHsnCode())
                            .metalType(itemDto.getMetalType())
                            .purity(itemDto.getPurity())
                            .quantity(itemDto.getQuantity())
                            .weight(itemDto.getWeight())
                            .metalRate(itemDto.getMetalRate())
                            .metalAmount(itemDto.getMetalAmount())
                            .makingChargeType(itemDto.getMakingChargeType())
                            .makingChargeValue(itemDto.getMakingChargeValue())
                            .makingChargeAmount(itemDto.getMakingChargeAmount())
                            .discountAmount(itemDto.getDiscountAmount())
                            .taxableAmount(itemDto.getTaxableAmount())
                            .gstPercent(itemDto.getGstPercent())
                            .cgstAmount(itemDto.getCgstAmount())
                            .sgstAmount(itemDto.getSgstAmount())
                            .igstAmount(itemDto.getIgstAmount())
                            .totalAmount(itemDto.getTotalAmount())
                            .build();

            savedItems.add(
                    gstInvoiceItemRepository.save(item)
            );
        }

        // -----------------------------------------------------
        // REPLACE OLD EXCHANGE ITEMS
        // -----------------------------------------------------

        List<GstExchangeItem> oldExchangeItems =
                gstExchangeItemRepository
                        .findByGstInvoiceId(
                                savedInvoice.getId()
                        );

        if (!oldExchangeItems.isEmpty()) {
            gstExchangeItemRepository.deleteAll(
                    oldExchangeItems
            );
        }

        List<GstExchangeItem> savedExchangeItems =
                new ArrayList<>();

        for (
                GstExchangeItemDto exchangeDto
                : calculatedExchangeItems
        ) {

            GstExchangeItem exchangeItem =
                    GstExchangeItem.builder()
                            .gstInvoice(savedInvoice)
                            .metalType(exchangeDto.getMetalType())
                            .weight(exchangeDto.getWeight())
                            .rate(exchangeDto.getRate())
                            .amount(exchangeDto.getAmount())
                            .build();

            savedExchangeItems.add(
                    gstExchangeItemRepository.save(
                            exchangeItem
                    )
            );
        }

        return convertToDto(
                savedInvoice,
                savedItems,
                savedExchangeItems
        );
    }


    // =========================================================
    // DETERMINE INTRA / INTER STATE
    // =========================================================

    private boolean isIntraStateSupply(
            GstInvoiceDto invoiceDto
    ) {

        String sellerStateCode =
                extractStateCodeFromGstin(
                        invoiceDto.getSellerGstin()
                );

        String placeOfSupplyCode =
                invoiceDto
                        .getPlaceOfSupplyStateCode();


        if (
                sellerStateCode == null ||
                        sellerStateCode.isBlank() ||
                        placeOfSupplyCode == null ||
                        placeOfSupplyCode.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Seller GSTIN and Place of Supply State Code are required"
            );
        }


        return sellerStateCode.equals(
                placeOfSupplyCode
        );
    }


    // =========================================================
    // EXTRACT STATE CODE FROM GSTIN
    // =========================================================

    private String extractStateCodeFromGstin(
            String gstin
    ) {

        if (
                gstin == null ||
                        gstin.length() < 2
        ) {

            return null;
        }


        return gstin.substring(
                0,
                2
        );
    }


    // =========================================================
    // GET INVOICE BY NUMBER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public GstInvoiceDto getInvoiceByNumber(
            String invoiceNumber
    ) {

        GstInvoice invoice =
                gstInvoiceRepository
                        .findByInvoiceNumber(
                                invoiceNumber
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "GST Invoice Not Found: "
                                                + invoiceNumber
                                )
                        );


        List<GstInvoiceItem> items =
                gstInvoiceItemRepository
                        .findByGstInvoiceId(
                                invoice.getId()
                        );


        List<GstExchangeItem> exchangeItems =
                gstExchangeItemRepository
                        .findByGstInvoiceId(
                                invoice.getId()
                        );


        return convertToDto(
                invoice,
                items,
                exchangeItems
        );
    }


    // =========================================================
    // GET ALL INVOICES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<GstInvoiceDto> getAllInvoices() {

        List<GstInvoice> invoices =
                gstInvoiceRepository
                        .findAllByOrderByInvoiceDateTimeDesc();


        return invoices.stream()
                .map(invoice -> {

                    List<GstInvoiceItem> items =
                            gstInvoiceItemRepository
                                    .findByGstInvoiceId(
                                            invoice.getId()
                                    );


                    List<GstExchangeItem> exchangeItems =
                            gstExchangeItemRepository
                                    .findByGstInvoiceId(
                                            invoice.getId()
                                    );


                    return convertToDto(
                            invoice,
                            items,
                            exchangeItems
                    );

                })
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET INVOICES BY CUSTOMER MOBILE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<GstInvoiceDto> getInvoicesByCustomerMobile(
            String customerMobile
    ) {

        if (customerMobile == null ||
                customerMobile.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Customer mobile number is required"
            );
        }

        String mobile = customerMobile.trim();

        List<GstInvoice> invoices =
                gstInvoiceRepository
                        .findByCustomerMobileOrderByInvoiceDateTimeDesc(
                                mobile
                        );

        return invoices.stream()
                .map(invoice -> {

                    List<GstInvoiceItem> items =
                            gstInvoiceItemRepository
                                    .findByGstInvoiceId(
                                            invoice.getId()
                                    );

                    List<GstExchangeItem> exchangeItems =
                            gstExchangeItemRepository
                                    .findByGstInvoiceId(
                                            invoice.getId()
                                    );

                    return convertToDto(
                            invoice,
                            items,
                            exchangeItems
                    );

                })
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET OUTSTANDING GST INVOICES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<GstInvoiceDto> getOutstandingInvoices() {

        List<GstInvoice> invoices =
                gstInvoiceRepository
                        .findByDueAmountGreaterThanOrderByInvoiceDateTimeDesc(
                                BigDecimal.ZERO
                        );

        return invoices.stream()
                .map(invoice -> {

                    List<GstInvoiceItem> items =
                            gstInvoiceItemRepository
                                    .findByGstInvoiceId(
                                            invoice.getId()
                                    );

                    List<GstExchangeItem> exchangeItems =
                            gstExchangeItemRepository
                                    .findByGstInvoiceId(
                                            invoice.getId()
                                    );

                    return convertToDto(
                            invoice,
                            items,
                            exchangeItems
                    );

                })
                .collect(Collectors.toList());
    }


    // =========================================================
    // PAY GST INVOICE DUE
    // =========================================================

    @Override
    @Transactional
    public GstInvoiceDto payDue(
            String invoiceNumber,
            BigDecimal amount,
            String paymentMode
    ) {

        if (invoiceNumber == null ||
                invoiceNumber.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Invoice number is required"
            );
        }

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        GstInvoice invoice =
                gstInvoiceRepository
                        .findByInvoiceNumber(
                                invoiceNumber.trim()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "GST Invoice Not Found: "
                                                + invoiceNumber
                                )
                        );

        BigDecimal currentDue =
                nonNegative(invoice.getDueAmount());

        if (currentDue.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "This GST invoice has no outstanding amount"
            );
        }

        BigDecimal paymentAmount =
                amount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        if (paymentAmount.compareTo(currentDue) > 0) {

            throw new IllegalArgumentException(
                    "Payment amount cannot be greater than due amount. "
                            + "Current due: "
                            + currentDue
            );
        }

        String mode =
                paymentMode == null
                        ? "OTHER"
                        : paymentMode.trim().toUpperCase();

        if (!mode.equals("CASH") &&
                !mode.equals("UPI") &&
                !mode.equals("CARD") &&
                !mode.equals("OTHER")) {

            throw new IllegalArgumentException(
                    "Invalid payment mode. Use CASH, UPI, CARD or OTHER"
            );
        }

        BigDecimal cashAmount =
                nonNegative(invoice.getCashAmount());

        BigDecimal upiAmount =
                nonNegative(invoice.getUpiAmount());

        BigDecimal cardAmount =
                nonNegative(invoice.getCardAmount());

        BigDecimal otherAmount =
                nonNegative(invoice.getOtherAmount());

        if (mode.equals("CASH")) {
            cashAmount = cashAmount.add(paymentAmount);
        } else if (mode.equals("UPI")) {
            upiAmount = upiAmount.add(paymentAmount);
        } else if (mode.equals("CARD")) {
            cardAmount = cardAmount.add(paymentAmount);
        } else {
            otherAmount = otherAmount.add(paymentAmount);
        }

        BigDecimal paidAmount =
                cashAmount
                        .add(upiAmount)
                        .add(cardAmount)
                        .add(otherAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal exchangeAmount =
                nonNegative(invoice.getTotalExchangeAmount());

        BigDecimal payableAmount =
                nonNegative(invoice.getGrandTotal())
                        .subtract(exchangeAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }

        BigDecimal dueAmount =
                payableAmount
                        .subtract(paidAmount)
                        .setScale(2, RoundingMode.HALF_UP);

        if (dueAmount.compareTo(BigDecimal.ZERO) < 0) {
            dueAmount = BigDecimal.ZERO;
        }

        String paymentStatus;

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = "PAID";
        } else {
            paymentStatus = "PARTIAL";
        }

        invoice.setCashAmount(cashAmount);
        invoice.setUpiAmount(upiAmount);
        invoice.setCardAmount(cardAmount);
        invoice.setOtherAmount(otherAmount);
        invoice.setPaidAmount(paidAmount);
        invoice.setDueAmount(dueAmount);
        invoice.setPaymentStatus(paymentStatus);

        GstInvoice savedInvoice =
                gstInvoiceRepository.save(invoice);

        // Save this Due payment as a separate GST payment history record.
        saveGstPaymentHistory(
                savedInvoice,
                paymentAmount,
                mode
        );

        List<GstInvoiceItem> items =
                gstInvoiceItemRepository
                        .findByGstInvoiceId(
                                savedInvoice.getId()
                        );

        List<GstExchangeItem> exchangeItems =
                gstExchangeItemRepository
                        .findByGstInvoiceId(
                                savedInvoice.getId()
                        );

        return convertToDto(
                savedInvoice,
                items,
                exchangeItems
        );
    }


    // =========================================================
    // GST PAYMENT HISTORY HELPERS
    // =========================================================

    private void saveInitialPaymentHistory(
            GstInvoice invoice
    ) {

        BigDecimal cashAmount =
                nonNegative(invoice.getCashAmount());

        BigDecimal upiAmount =
                nonNegative(invoice.getUpiAmount());

        BigDecimal cardAmount =
                nonNegative(invoice.getCardAmount());

        BigDecimal otherAmount =
                nonNegative(invoice.getOtherAmount());

        if (cashAmount.compareTo(BigDecimal.ZERO) > 0) {
            saveGstPaymentHistory(invoice, cashAmount, "CASH");
        }

        if (upiAmount.compareTo(BigDecimal.ZERO) > 0) {
            saveGstPaymentHistory(invoice, upiAmount, "UPI");
        }

        if (cardAmount.compareTo(BigDecimal.ZERO) > 0) {
            saveGstPaymentHistory(invoice, cardAmount, "CARD");
        }

        if (otherAmount.compareTo(BigDecimal.ZERO) > 0) {
            saveGstPaymentHistory(invoice, otherAmount, "OTHER");
        }
    }


    private void saveGstPaymentHistory(
            GstInvoice invoice,
            BigDecimal amount,
            String paymentMode
    ) {

        GstPaymentHistory history =
                GstPaymentHistory.builder()
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .customerName(invoice.getCustomerName())
                        .customerMobile(invoice.getCustomerMobile())
                        .amount(amount)
                        .paymentMode(paymentMode)
                        .build();

        gstPaymentHistoryRepository.save(history);
    }


    // =========================================================
    // DELETE INVOICE
    // =========================================================

    @Override
    @Transactional
    public void deleteInvoice(
            String invoiceNumber
    ) {

        GstInvoice invoice =
                gstInvoiceRepository
                        .findByInvoiceNumber(
                                invoiceNumber
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "GST Invoice Not Found: "
                                                + invoiceNumber
                                )
                        );


        gstInvoiceRepository.delete(
                invoice
        );
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private GstInvoiceDto convertToDto(
            GstInvoice invoice,
            List<GstInvoiceItem> items,
            List<GstExchangeItem> exchangeItems
    ) {

        List<GstInvoiceItemDto> itemDtos =
                items.stream()
                        .map(this::convertItemToDto)
                        .collect(Collectors.toList());


        List<GstExchangeItemDto> exchangeItemDtos =
                exchangeItems.stream()
                        .map(this::convertExchangeItemToDto)
                        .collect(Collectors.toList());


        return GstInvoiceDto.builder()

                .id(
                        invoice.getId()
                )

                .invoiceNumber(
                        invoice.getInvoiceNumber()
                )

                .financialYear(
                        invoice.getFinancialYear()
                )

                .invoiceDateTime(
                        invoice.getInvoiceDateTime()
                )

                .placeOfSupply(
                        invoice.getPlaceOfSupply()
                )

                .placeOfSupplyStateCode(
                        invoice.getPlaceOfSupplyStateCode()
                )

                .sellerGstin(
                        invoice.getSellerGstin()
                )

                .customerName(
                        invoice.getCustomerName()
                )

                .customerMobile(
                        invoice.getCustomerMobile()
                )

                .customerAddress(
                        invoice.getCustomerAddress()
                )

                .customerGstin(
                        invoice.getCustomerGstin()
                )

                .items(
                        itemDtos
                )

                .exchangeItems(
                        exchangeItemDtos
                )

                .totalExchangeAmount(
                        invoice.getTotalExchangeAmount()
                )

                .cashAmount(
                        invoice.getCashAmount()
                )

                .upiAmount(
                        invoice.getUpiAmount()
                )

                .cardAmount(
                        invoice.getCardAmount()
                )

                .otherAmount(
                        invoice.getOtherAmount()
                )

                .paidAmount(
                        invoice.getPaidAmount()
                )

                .dueAmount(
                        invoice.getDueAmount()
                )

                .paymentStatus(
                        invoice.getPaymentStatus()
                )

                .taxableAmount(
                        invoice.getTaxableAmount()
                )

                .cgstAmount(
                        invoice.getCgstAmount()
                )

                .sgstAmount(
                        invoice.getSgstAmount()
                )

                .igstAmount(
                        invoice.getIgstAmount()
                )

                .totalGstAmount(
                        invoice.getTotalGstAmount()
                )

                .discountAmount(
                        invoice.getDiscountAmount()
                )

                .roundOff(
                        invoice.getRoundOff()
                )

                .grandTotal(
                        invoice.getGrandTotal()
                )

                .reverseCharge(
                        invoice.getReverseCharge()
                )

                .build();
    }


    // =========================================================
    // EXCHANGE ENTITY → DTO
    // =========================================================

    private GstExchangeItemDto convertExchangeItemToDto(
            GstExchangeItem item
    ) {

        return GstExchangeItemDto.builder()

                .id(
                        item.getId()
                )

                .gstInvoiceId(
                        item.getGstInvoice() != null
                                ? item.getGstInvoice().getId()
                                : null
                )

                .metalType(
                        item.getMetalType()
                )

                .weight(
                        item.getWeight()
                )

                .rate(
                        item.getRate()
                )

                .amount(
                        item.getAmount()
                )

                .build();
    }


    // =========================================================
    // ITEM ENTITY → DTO
    // =========================================================

    private GstInvoiceItemDto convertItemToDto(
            GstInvoiceItem item
    ) {

        return GstInvoiceItemDto.builder()

                .id(
                        item.getId()
                )

                .itemCode(
                        item.getItemCode()
                )

                .itemName(
                        item.getItemName()
                )

                .hsnCode(
                        item.getHsnCode()
                )

                .metalType(
                        item.getMetalType()
                )

                .purity(
                        item.getPurity()
                )

                .quantity(
                        item.getQuantity()
                )

                .weight(
                        item.getWeight()
                )

                .metalRate(
                        item.getMetalRate()
                )

                .metalAmount(
                        item.getMetalAmount()
                )

                .makingChargeType(
                        item.getMakingChargeType()
                )

                .makingChargeValue(
                        item.getMakingChargeValue()
                )

                .makingChargeAmount(
                        item.getMakingChargeAmount()
                )

                .discountAmount(
                        item.getDiscountAmount()
                )

                .taxableAmount(
                        item.getTaxableAmount()
                )

                .gstPercent(
                        item.getGstPercent()
                )

                .cgstAmount(
                        item.getCgstAmount()
                )

                .sgstAmount(
                        item.getSgstAmount()
                )

                .igstAmount(
                        item.getIgstAmount()
                )

                .totalAmount(
                        item.getTotalAmount()
                )

                .build();
    }


    // =========================================================
    // NON NEGATIVE DECIMAL
    // =========================================================

    private BigDecimal nonNegative(
            BigDecimal value
    ) {
        if (value == null ||
                value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }


    // =========================================================
    // NULL SAFE DECIMAL
    // =========================================================

    private BigDecimal valueOrZero(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}