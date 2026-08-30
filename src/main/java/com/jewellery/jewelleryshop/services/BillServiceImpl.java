
package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.BillDto;
import com.jewellery.jewelleryshop.dto.BillItemDto;
import com.jewellery.jewelleryshop.dto.OutstandingBillDto;
import com.jewellery.jewelleryshop.entity.*;
import com.jewellery.jewelleryshop.repository.BillItemRepository;
import com.jewellery.jewelleryshop.repository.BillRepository;
import com.jewellery.jewelleryshop.repository.CustomerRepositry;
import com.jewellery.jewelleryshop.repository.JewelleryItemRepository;
import com.jewellery.jewelleryshop.repository.PaymentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private CustomerRepositry customerRepositry;

    @Autowired
    private JewelleryItemRepository jewelleryItemRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;


    // ==============================
    // CREATE BILL
    // ==============================

    @Override
    @Transactional
    public BillDto createBill(BillDto billDto) {

        // ==============================
        // CUSTOMER FETCH
        // ==============================

        Customer customer = customerRepositry
                .findByMobileNumber(billDto.getCustomerMobile())
                .orElseThrow(() ->
                        new RuntimeException("Customer Not Found"));


        // ==============================
        // GENERATE BILL NUMBER
        // ==============================

        String billNumber = generateBillNumber();


        // ==============================
        // CREATE BILL
        // ==============================

        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .customer(customer)
                .discount(
                        billDto.getDiscount() == null
                                ? BigDecimal.ZERO
                                : billDto.getDiscount()
                )
                .paidAmount(
                        billDto.getPaidAmount() == null
                                ? BigDecimal.ZERO
                                : billDto.getPaidAmount()
                )
                .paymentMode(billDto.getPaymentMode())
                .status(BillStatus.PARTIAL)
                .build();


        // Save Bill first
        Bill savedBill = billRepository.save(bill);


        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;


        // ==============================
        // ITEMS
        // ==============================

        for (BillItemDto itemDto : billDto.getItems()) {

            boolean manualItem =
                    itemDto.getItemCode() == null
                            || itemDto.getItemCode().trim().isEmpty();


            JewelleryItem jewelleryItem = null;


            // ==============================
            // STOCK BILLING
            // ==============================

            if (!manualItem) {

                jewelleryItem =
                        jewelleryItemRepository
                                .findByItemCode(
                                        itemDto.getItemCode()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Item Not Found: "
                                                        + itemDto.getItemCode()
                                        ));


                // Quantity validation

                if (itemDto.getQuantity() == null
                        || itemDto.getQuantity() <= 0) {

                    throw new RuntimeException(
                            "Quantity must be greater than zero"
                    );
                }


                // Stock check

                if (jewelleryItem.getStockQuantity() == null
                        || jewelleryItem.getStockQuantity()
                        < itemDto.getQuantity()) {

                    throw new RuntimeException(
                            "Insufficient Stock for Item: "
                                    + itemDto.getItemCode()
                    );
                }

            }


            // ==============================
            // MANUAL BILLING
            // ==============================

            else {

                // Manual item name required

                if (itemDto.getItemName() == null
                        || itemDto.getItemName().trim().isEmpty()) {

                    throw new RuntimeException(
                            "Manual item name is required"
                    );
                }


                // Manual weight required

                if (itemDto.getWeight() == null
                        || itemDto.getWeight()
                        .compareTo(BigDecimal.ZERO) <= 0) {

                    throw new RuntimeException(
                            "Manual item weight must be greater than zero"
                    );
                }


                // Manual quantity default = 1

                if (itemDto.getQuantity() == null
                        || itemDto.getQuantity() <= 0) {

                    itemDto.setQuantity(1);
                }

            }


            // ==============================
            // REQUIRED BILLING VALUES
            // ==============================

            if (itemDto.getMetalRate() == null
                    || itemDto.getMetalRate()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "Metal rate must be greater than zero"
                );
            }


            BigDecimal makingPercent =
                    itemDto.getMakingChargePercent() == null
                            ? BigDecimal.ZERO
                            : itemDto.getMakingChargePercent();


            BigDecimal gstPercent =
                    itemDto.getGstPercent() == null
                            ? BigDecimal.ZERO
                            : itemDto.getGstPercent();


            BigDecimal quantity =
                    BigDecimal.valueOf(
                            itemDto.getQuantity()
                    );


            // ==============================
            // WEIGHT
            // ==============================

            BigDecimal weight;


            if (manualItem) {

                // Manual billing ka weight

                weight = itemDto.getWeight();

            } else {

                // Existing JewelleryItem ka weight

                weight = jewelleryItem.getWeight();

            }


            if (weight == null
                    || weight.compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "Weight must be greater than zero"
                );
            }


            // ==============================
            // METAL VALUE
            // ==============================

            BigDecimal metalAmount =
                    weight
                            .multiply(quantity)
                            .multiply(itemDto.getMetalRate());


            // ==============================
            // MAKING CHARGE
            // ==============================

            BigDecimal makingChargeAmount =
                    metalAmount
                            .multiply(makingPercent)
                            .divide(
                                    BigDecimal.valueOf(100)
                            );


            // ==============================
            // TAXABLE AMOUNT
            // ==============================

            BigDecimal taxableAmount =
                    metalAmount
                            .add(makingChargeAmount);


            // ==============================
            // GST
            // ==============================

            BigDecimal gstAmount =
                    taxableAmount
                            .multiply(gstPercent)
                            .divide(
                                    BigDecimal.valueOf(100)
                            );


            // ==============================
            // ITEM TOTAL
            // ==============================

            BigDecimal itemTotal =
                    taxableAmount
                            .add(gstAmount);


            // ==============================
            // BILL TOTAL
            // ==============================

            totalAmount =
                    totalAmount.add(
                            metalAmount
                                    .add(makingChargeAmount)
                    );


            totalGst =
                    totalGst.add(gstAmount);


            // ==============================
            // SAVE BILL ITEM
            // ==============================

            BillItem billItem =
                    BillItem.builder()
                            .bill(savedBill)

                            // Stock item ke case me jewelleryItem
                            // Manual case me NULL
                            .jewelleryItem(jewelleryItem)

                            // Manual item name
                            .itemName(
                                    manualItem
                                            ? itemDto.getItemName()
                                            : jewelleryItem.getItemName()
                            )

                            // Billing time weight
                            .weight(weight)

                            .quantity(
                                    itemDto.getQuantity()
                            )

                            .metalRate(
                                    itemDto.getMetalRate()
                            )

                            .metalAmount(
                                    metalAmount
                            )

                            .makingChargePercent(
                                    makingPercent
                            )

                            .makingChargeAmount(
                                    makingChargeAmount
                            )

                            .gstPercent(
                                    gstPercent
                            )

                            .gstAmount(
                                    gstAmount
                            )

                            .total(
                                    itemTotal
                            )

                            .build();


            billItemRepository.save(billItem);


            // ==============================
            // REDUCE STOCK
            // ONLY STOCK BILLING
            // ==============================

            if (!manualItem) {

                jewelleryItem.setStockQuantity(
                        jewelleryItem.getStockQuantity()
                                - itemDto.getQuantity()
                );


                jewelleryItemRepository.save(
                        jewelleryItem
                );
            }
        }


        // ==============================
        // DISCOUNT
        // ==============================

        BigDecimal discount =
                savedBill.getDiscount() == null
                        ? BigDecimal.ZERO
                        : savedBill.getDiscount();


        // ==============================
        // TOTAL AMOUNT
        // ==============================

        savedBill.setTotalAmount(
                totalAmount
        );


        // ==============================
        // GST
        // ==============================

        savedBill.setGstAmount(
                totalGst
        );


        // ==============================
        // GRAND TOTAL
        // ==============================

        BigDecimal grandTotal =
                totalAmount
                        .add(totalGst)
                        .subtract(discount);


        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {

            grandTotal = BigDecimal.ZERO;
        }


        savedBill.setGrandTotal(
                grandTotal
        );


        // ==============================
        // PAID AMOUNT
        // ==============================

        BigDecimal paidAmount =
                savedBill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : savedBill.getPaidAmount();


        if (paidAmount.compareTo(grandTotal) > 0) {

            throw new RuntimeException(
                    "Paid amount cannot be greater than Grand Total"
            );
        }


        // ==============================
        // DUE
        // ==============================

        BigDecimal dueAmount =
                grandTotal.subtract(
                        paidAmount
                );


        savedBill.setDueAmount(
                dueAmount
        );


        // ==============================
        // STATUS
        // ==============================

        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {

            savedBill.setStatus(
                    BillStatus.PAID
            );

        } else if (
                paidAmount.compareTo(BigDecimal.ZERO) > 0
        ) {

            savedBill.setStatus(
                    BillStatus.PARTIAL
            );

        } else {

            savedBill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        // ==============================
        // SAVE FINAL BILL
        // ==============================

        billRepository.save(
                savedBill
        );


        // ==============================
        // INITIAL PAYMENT HISTORY
        // ==============================

        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {

            PaymentHistory paymentHistory =
                    PaymentHistory.builder()
                            .bill(savedBill)
                            .customer(customer)
                            .amount(paidAmount)
                            .paymentMode(
                                    savedBill.getPaymentMode()
                            )
                            .build();


            paymentHistoryRepository.save(
                    paymentHistory
            );
        }


        // ==============================
        // RETURN BILL
        // ==============================

        return convertToDto(
                savedBill
        );
    }

    // ==============================
    // GET BILL
    // ==============================

    @Override
    public BillDto getBillByBillNumber(
            String billNumber
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));


        return convertToDto(bill);
    }


    // ==============================
    // GET ALL BILLS
    // ==============================

    @Override
    public List<BillDto> getAllBills() {

        return billRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    // ==============================
    // GET CUSTOMER ALL BILLS
    // ==============================

    @Override
    public List<BillDto> getBillsByCustomerMobile(
            String mobileNumber
    ) {

        return billRepository
                .findByCustomer_MobileNumber(mobileNumber)
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    // ==============================
    // PAY DUE
    // ==============================

    @Override
    @Transactional
    public BillDto payDueAmount(
            String billNumber,
            BigDecimal amount
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));


        // ==============================
        // PAYMENT VALIDATION
        // ==============================

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Payment amount must be greater than zero"
            );
        }


        BigDecimal currentDue =
                bill.getDueAmount() == null
                        ? BigDecimal.ZERO
                        : bill.getDueAmount();


        if (amount.compareTo(currentDue) > 0) {

            throw new RuntimeException(
                    "Payment cannot be greater than due amount"
            );
        }


        // ==============================
        // UPDATE PAID AMOUNT
        // ==============================

        BigDecimal currentPaid =
                bill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : bill.getPaidAmount();


        BigDecimal newPaid =
                currentPaid.add(amount);


        bill.setPaidAmount(newPaid);


        // ==============================
        // UPDATE DUE
        // ==============================

        BigDecimal due =
                bill.getGrandTotal()
                        .subtract(newPaid);


        bill.setDueAmount(due);


        // ==============================
        // UPDATE STATUS
        // ==============================

        if (due.compareTo(BigDecimal.ZERO) == 0) {

            bill.setStatus(
                    BillStatus.PAID
            );

        } else {

            bill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        // ==============================
        // SAVE UPDATED BILL
        // ==============================

        Bill savedBill =
                billRepository.save(bill);


        // ==============================
        // SAVE PAYMENT HISTORY
        // ==============================

        PaymentHistory paymentHistory =
                PaymentHistory.builder()
                        .bill(savedBill)
                        .customer(
                                savedBill.getCustomer()
                        )
                        .amount(amount)
                        .paymentMode(
                                savedBill.getPaymentMode()
                        )
                        .build();


        paymentHistoryRepository.save(
                paymentHistory
        );


        return convertToDto(savedBill);
    }


    // ==============================
    // DELETE BILL
    // ==============================

    @Override
    @Transactional
    public void deleteBill(String billNumber) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));


        billRepository.delete(bill);
    }


    // ==============================
    // GENERATE BILL NUMBER
    // ==============================

    private String generateBillNumber() {

        long count =
                billRepository.count() + 1;

        return String.format(
                "BILL%05d",
                count
        );
    }


    // ==============================
    // DTO CONVERTER
    // ==============================

    private BillDto convertToDto(Bill bill) {

        // Bill ke saare items database se fetch karna
        List<BillItem> billItems =
                billItemRepository.findByBill(bill);

        List<BillItemDto> itemDtos =
                new ArrayList<>();


        for (BillItem billItem : billItems) {

            JewelleryItem item =
                    billItem.getJewelleryItem();


            BillItemDto itemDto =
                    BillItemDto.builder()

                            // Stock item ke liye itemCode
                            // Manual item ke liye null
                            .itemCode(
                                    item != null
                                            ? item.getItemCode()
                                            : null
                            )

                            // Manual item ka naam BillItem se
                            // Stock item ka naam JewelleryItem se
                            .itemName(
                                    billItem.getItemName() != null
                                            ? billItem.getItemName()
                                            : item != null
                                            ? item.getItemName()
                                            : null
                            )

                            .quantity(
                                    billItem.getQuantity()
                            )

                            // Bill ke time save hua weight
                            .weight(
                                    billItem.getWeight() != null
                                            ? billItem.getWeight()
                                            : item != null
                                            ? item.getWeight()
                                            : null
                            )

                            .metalRate(
                                    billItem.getMetalRate()
                            )

                            .makingChargePercent(
                                    billItem.getMakingChargePercent()
                            )

                            .gstPercent(
                                    billItem.getGstPercent()
                            )

                            .total(
                                    billItem.getTotal()
                            )

                            .build();


            itemDtos.add(itemDto);
        }

        // ==============================
        // BILL DTO
        // ==============================

        return BillDto.builder()

                .billNumber(
                        bill.getBillNumber()
                )

                .billDate(
                        bill.getBillDate()
                )

                .customerMobile(
                        bill.getCustomer()
                                .getMobileNumber()
                )

                .items(
                        itemDtos
                )

                .discount(
                        bill.getDiscount()
                )

                .paidAmount(
                        bill.getPaidAmount()
                )

                .paymentMode(
                        bill.getPaymentMode()
                )

                .status(
                        bill.getStatus()
                )

                .totalAmount(
                        bill.getTotalAmount()
                )

                .gstAmount(
                        bill.getGstAmount()
                )

                .grandTotal(
                        bill.getGrandTotal()
                )

                .dueAmount(
                        bill.getDueAmount()
                )

                .build();
    }



// ==============================
// GET OUTSTANDING BILLS
// ==============================

    @Override
    @Transactional(readOnly = true)
    public List<OutstandingBillDto> getOutstandingBills() {

        List<Bill> outstandingBills =
                billRepository.findByDueAmountGreaterThan(BigDecimal.ZERO);

        List<OutstandingBillDto> result = new ArrayList<>();

        for (Bill bill : outstandingBills) {

            Customer customer = bill.getCustomer();

            List<BillItem> billItems =
                    billItemRepository.findByBill(bill);

            List<BillItemDto> itemDtos = new ArrayList<>();

            for (BillItem billItem : billItems) {

                JewelleryItem item =
                        billItem.getJewelleryItem();

                BillItemDto itemDto =
                        BillItemDto.builder()

                                .itemCode(
                                        item != null
                                                ? item.getItemCode()
                                                : null
                                )

                                .itemName(
                                        billItem.getItemName() != null
                                                ? billItem.getItemName()
                                                : item != null
                                                ? item.getItemName()
                                                : null
                                )

                                .quantity(
                                        billItem.getQuantity()
                                )

                                .weight(
                                        billItem.getWeight()
                                )

                                .metalRate(
                                        billItem.getMetalRate()
                                )

                                .makingChargePercent(
                                        billItem.getMakingChargePercent()
                                )

                                .gstPercent(
                                        billItem.getGstPercent()
                                )

                                .total(
                                        billItem.getTotal()
                                )

                                .build();

                itemDtos.add(itemDto);
            }

            OutstandingBillDto dto =
                    OutstandingBillDto.builder()

                            .billNumber(
                                    bill.getBillNumber()
                            )

                            .billDate(
                                    bill.getBillDate()
                            )

                            .customerName(
                                    customer != null
                                            ? customer.getCustomerName()
                                            : null
                            )

                            .customerMobile(
                                    customer != null
                                            ? customer.getMobileNumber()
                                            : null
                            )

                            .customerPlace(
                                    customer != null
                                            ? customer.getPlace()
                                            : null
                            )

                            .items(
                                    itemDtos
                            )

                            .grandTotal(
                                    bill.getGrandTotal()
                            )

                            .paidAmount(
                                    bill.getPaidAmount()
                            )

                            .dueAmount(
                                    bill.getDueAmount()
                            )

                            .build();

            result.add(dto);
        }

        return result;
    }



}

