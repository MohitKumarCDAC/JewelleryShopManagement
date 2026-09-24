package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.GstInvoiceDto;
import com.jewellery.jewelleryshop.dto.GstInvoiceItemDto;
import com.jewellery.jewelleryshop.entity.GstInvoice;
import com.jewellery.jewelleryshop.entity.GstInvoiceItem;
import com.jewellery.jewelleryshop.repository.GstInvoiceItemRepository;
import com.jewellery.jewelleryshop.repository.GstInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GstInvoiceServiceImpl implements GstInvoiceService {

    private final GstInvoiceRepository gstInvoiceRepository;
    private final GstInvoiceItemRepository gstInvoiceItemRepository;
    private final GstInvoiceNumberService gstInvoiceNumberService;

    public GstInvoiceServiceImpl(
            GstInvoiceRepository gstInvoiceRepository,
            GstInvoiceItemRepository gstInvoiceItemRepository,
            GstInvoiceNumberService gstInvoiceNumberService
    ) {
        this.gstInvoiceRepository = gstInvoiceRepository;
        this.gstInvoiceItemRepository = gstInvoiceItemRepository;
        this.gstInvoiceNumberService = gstInvoiceNumberService;
    }


    // =========================================================
    // CREATE GST INVOICE
    // =========================================================

    @Override
    @Transactional
    public GstInvoiceDto createInvoice(GstInvoiceDto invoiceDto) {

        if (invoiceDto == null) {
            throw new RuntimeException("GST Invoice data cannot be null");
        }

        // -----------------------------------------------------
        // GENERATE INVOICE NUMBER
        // -----------------------------------------------------

        String invoiceNumber =
                gstInvoiceNumberService.generateNextInvoiceNumber();


        // -----------------------------------------------------
        // CREATE INVOICE
        // -----------------------------------------------------

        GstInvoice invoice = GstInvoice.builder()
                .invoiceNumber(invoiceNumber)

                .financialYear(
                        invoiceNumber.substring(
                                3,
                                8
                        )
                )

                .invoiceDateTime(
                        LocalDateTime.now()
                )

                .placeOfSupply(
                        invoiceDto.getPlaceOfSupply()
                )

                .placeOfSupplyStateCode(
                        invoiceDto.getPlaceOfSupplyStateCode()
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

                .discountAmount(
                        invoiceDto.getDiscountAmount()
                )

                .roundOff(
                        invoiceDto.getRoundOff()
                )

                .reverseCharge(
                        invoiceDto.getReverseCharge()
                )

                .build();


        // -----------------------------------------------------
        // SAVE INVOICE FIRST
        // -----------------------------------------------------

        GstInvoice savedInvoice =
                gstInvoiceRepository.save(invoice);


        // -----------------------------------------------------
        // SAVE ITEMS
        // -----------------------------------------------------

        List<GstInvoiceItem> savedItems =
                new ArrayList<>();

        if (invoiceDto.getItems() != null) {

            for (GstInvoiceItemDto itemDto :
                    invoiceDto.getItems()) {

                GstInvoiceItem item =
                        GstInvoiceItem.builder()

                                .gstInvoice(savedInvoice)

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
                        gstInvoiceItemRepository.save(item)
                );
            }
        }


        // -----------------------------------------------------
        // SUMMARY VALUES
        // -----------------------------------------------------

        savedInvoice.setTaxableAmount(
                invoiceDto.getTaxableAmount()
        );

        savedInvoice.setCgstAmount(
                invoiceDto.getCgstAmount()
        );

        savedInvoice.setSgstAmount(
                invoiceDto.getSgstAmount()
        );

        savedInvoice.setIgstAmount(
                invoiceDto.getIgstAmount()
        );

        savedInvoice.setTotalGstAmount(
                invoiceDto.getTotalGstAmount()
        );

        savedInvoice.setDiscountAmount(
                invoiceDto.getDiscountAmount()
        );

        savedInvoice.setRoundOff(
                invoiceDto.getRoundOff()
        );

        savedInvoice.setGrandTotal(
                invoiceDto.getGrandTotal()
        );


        // -----------------------------------------------------
        // SAVE FINAL INVOICE
        // -----------------------------------------------------

        savedInvoice =
                gstInvoiceRepository.save(savedInvoice);


        return convertToDto(
                savedInvoice,
                savedItems
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
                        .findByInvoiceNumber(invoiceNumber)
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

        return convertToDto(
                invoice,
                items
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

                    return convertToDto(
                            invoice,
                            items
                    );

                })
                .collect(Collectors.toList());
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
                        .findByInvoiceNumber(invoiceNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "GST Invoice Not Found: "
                                                + invoiceNumber
                                )
                        );

        gstInvoiceRepository.delete(invoice);
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private GstInvoiceDto convertToDto(
            GstInvoice invoice,
            List<GstInvoiceItem> items
    ) {

        List<GstInvoiceItemDto> itemDtos =
                items.stream()
                        .map(this::convertItemToDto)
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
}