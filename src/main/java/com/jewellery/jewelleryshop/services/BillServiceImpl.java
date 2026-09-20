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
import java.math.RoundingMode;
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


    // =======================================================
    // CREATE BILL
    // =======================================================

    @Override
    @Transactional
    public BillDto createBill(BillDto billDto) {

        // ===================================================
        // CUSTOMER FETCH
        // ===================================================

        Customer customer = customerRepositry
                .findByMobileNumber(billDto.getCustomerMobile())
                .orElseThrow(() ->
                        new RuntimeException("Customer Not Found"));


        // ===================================================
        // GENERATE BILL NUMBER
        // ===================================================

        String billNumber = generateBillNumber();


        // ===================================================
        // EXCHANGE VALUES
        // ===================================================

        BigDecimal goldExchangeWeight =
                billDto.getGoldExchangeWeight() == null
                        ? BigDecimal.ZERO
                        : billDto.getGoldExchangeWeight();

        BigDecimal goldExchangeRate =
                billDto.getGoldExchangeRate() == null
                        ? BigDecimal.ZERO
                        : billDto.getGoldExchangeRate();

        BigDecimal goldExchangeAmount =
                billDto.getGoldExchangeAmount() == null
                        ? BigDecimal.ZERO
                        : billDto.getGoldExchangeAmount();

        BigDecimal silverExchangeWeight =
                billDto.getSilverExchangeWeight() == null
                        ? BigDecimal.ZERO
                        : billDto.getSilverExchangeWeight();

        BigDecimal silverExchangeRate =
                billDto.getSilverExchangeRate() == null
                        ? BigDecimal.ZERO
                        : billDto.getSilverExchangeRate();

        BigDecimal silverExchangeAmount =
                billDto.getSilverExchangeAmount() == null
                        ? BigDecimal.ZERO
                        : billDto.getSilverExchangeAmount();


        // ===================================================
        // EXCHANGE VALIDATION
        // ===================================================

        if (goldExchangeWeight.compareTo(BigDecimal.ZERO) < 0
                || goldExchangeRate.compareTo(BigDecimal.ZERO) < 0
                || goldExchangeAmount.compareTo(BigDecimal.ZERO) < 0
                || silverExchangeWeight.compareTo(BigDecimal.ZERO) < 0
                || silverExchangeRate.compareTo(BigDecimal.ZERO) < 0
                || silverExchangeAmount.compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Exchange weight, rate and amount cannot be negative"
            );
        }


        // ===================================================
        // TOTAL EXCHANGE
        // ===================================================

        BigDecimal totalExchangeAmount =
                goldExchangeAmount.add(silverExchangeAmount);


        // ===================================================
        // CREATE BILL
        // ===================================================

        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .customer(customer)

                .discount(
                        billDto.getDiscount() == null
                                ? BigDecimal.ZERO
                                : billDto.getDiscount()
                )

                .goldExchangeWeight(goldExchangeWeight)
                .goldExchangeRate(goldExchangeRate)
                .goldExchangeAmount(goldExchangeAmount)

                .silverExchangeWeight(silverExchangeWeight)
                .silverExchangeRate(silverExchangeRate)
                .silverExchangeAmount(silverExchangeAmount)

                .totalExchangeAmount(totalExchangeAmount)

                .paidAmount(
                        billDto.getPaidAmount() == null
                                ? BigDecimal.ZERO
                                : billDto.getPaidAmount()
                )

                .paymentMode(billDto.getPaymentMode())

                .status(BillStatus.PARTIAL)

                .build();


        // ===================================================
        // SAVE BILL FIRST
        // ===================================================

        Bill savedBill = billRepository.save(bill);


        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;


        // ===================================================
        // ITEMS
        // ===================================================

        for (BillItemDto itemDto : billDto.getItems()) {

            boolean manualItem =
                    itemDto.getItemCode() == null
                            || itemDto.getItemCode().trim().isEmpty();


            JewelleryItem jewelleryItem = null;


            // =================================================
            // STOCK BILLING
            // =================================================

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


                // Stock validation

                if (jewelleryItem.getStockQuantity() == null
                        || jewelleryItem.getStockQuantity()
                        < itemDto.getQuantity()) {

                    throw new RuntimeException(
                            "Insufficient Stock for Item: "
                                    + itemDto.getItemCode()
                    );
                }
            }


            // =================================================
            // MANUAL BILLING
            // =================================================

            else {

                // Manual item name validation

                if (itemDto.getItemName() == null
                        || itemDto.getItemName().trim().isEmpty()) {

                    throw new RuntimeException(
                            "Manual item name is required"
                    );
                }


                // Manual weight validation

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


            // =================================================
            // METAL RATE VALIDATION
            // =================================================

            if (itemDto.getMetalRate() == null
                    || itemDto.getMetalRate()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "Metal rate must be greater than zero"
                );
            }


            // =================================================
            // GST
            // =================================================

            BigDecimal gstPercent =
                    itemDto.getGstPercent() == null
                            ? BigDecimal.ZERO
                            : itemDto.getGstPercent();


            // =================================================
            // QUANTITY
            // =================================================

            BigDecimal quantity =
                    BigDecimal.valueOf(
                            itemDto.getQuantity()
                    );


            // =================================================
            // WEIGHT
            // =================================================

            BigDecimal weight;

            if (manualItem) {

                // Manual billing weight
                weight = itemDto.getWeight();

            } else {

                // Stock item weight
                weight = jewelleryItem.getWeight();
            }


            if (weight == null
                    || weight.compareTo(BigDecimal.ZERO) <= 0) {

                throw new RuntimeException(
                        "Weight must be greater than zero"
                );
            }


            // =================================================
            // METAL AMOUNT
            // =================================================

            BigDecimal metalAmount =
                    weight
                            .multiply(quantity)
                            .multiply(itemDto.getMetalRate());


            // =================================================
            // MAKING CHARGE
            //
            // Weight < 1 gram
            //     -> Making Charge = RUPEES
            //
            // Weight >= 1 gram
            //     -> Making Charge = PERCENTAGE
            // =================================================

            BigDecimal makingValue =
                    itemDto.getMakingChargeValue() == null
                            ? BigDecimal.ZERO
                            : itemDto.getMakingChargeValue();


            String makingType =
                    itemDto.getMakingChargeType();


            // Safety fallback:
            // Agar frontend se type nahi aayi,
            // weight ke according automatically decide hoga.

            if (makingType == null
                    || makingType.trim().isEmpty()) {

                makingType =
                        weight.compareTo(BigDecimal.ONE) < 0
                                ? "RUPEES"
                                : "PERCENT";
            }


            // Normalize value

            makingType =
                    makingType.trim().toUpperCase();


            BigDecimal makingChargeAmount;


            if ("RUPEES".equals(makingType)) {

                // =========================================
                // BELOW 1 GRAM
                // DIRECT RUPEE AMOUNT
                // =========================================

                makingChargeAmount =
                        makingValue;

            } else if ("PERCENT".equals(makingType)) {

                // =========================================
                // 1 GRAM OR ABOVE
                // PERCENTAGE OF METAL AMOUNT
                // =========================================

                makingChargeAmount =
                        metalAmount
                                .multiply(makingValue)
                                .divide(
                                        BigDecimal.valueOf(100),
                                        2,
                                        RoundingMode.HALF_UP
                                );

            } else {

                throw new RuntimeException(
                        "Invalid making charge type: "
                                + makingType
                );
            }


            // =================================================
            // TAXABLE AMOUNT
            // =================================================

            BigDecimal taxableAmount =
                    metalAmount
                            .add(makingChargeAmount);


            // =================================================
            // GST
            // =================================================

            BigDecimal gstAmount =
                    taxableAmount
                            .multiply(gstPercent)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // =================================================
            // ITEM TOTAL
            // =================================================

            BigDecimal itemTotal =
                    taxableAmount
                            .add(gstAmount);


            // =================================================
            // BILL TOTAL
            // =================================================

            totalAmount =
                    totalAmount.add(
                            metalAmount
                                    .add(makingChargeAmount)
                    );


            totalGst =
                    totalGst.add(gstAmount);


            // =================================================
            // SAVE BILL ITEM
            // =================================================

            BillItem billItem =
                    BillItem.builder()
                            .bill(savedBill)

                            // Stock item -> JewelleryItem
                            // Manual item -> NULL
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

                            // Keep old field for compatibility
                            .makingChargePercent(
                                    "PERCENT".equals(makingType)
                                            ? makingValue
                                            : BigDecimal.ZERO
                            )

                            // New fields
                            .makingChargeType(
                                    makingType
                            )

                            .makingChargeValue(
                                    makingValue
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


            // =================================================
            // REDUCE STOCK
            // ONLY STOCK BILLING
            // =================================================

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


        // ===================================================
        // DISCOUNT
        // ===================================================

        BigDecimal discount =
                savedBill.getDiscount() == null
                        ? BigDecimal.ZERO
                        : savedBill.getDiscount();


        // ===================================================
        // TOTAL AMOUNT
        // ===================================================

        savedBill.setTotalAmount(
                totalAmount
        );


        // ===================================================
        // GST
        // ===================================================

        savedBill.setGstAmount(
                totalGst
        );


        // ===================================================
        // GRAND TOTAL
        // ===================================================

        BigDecimal grandTotal =
                totalAmount
                        .add(totalGst)
                        .subtract(discount)
                        .subtract(totalExchangeAmount);


        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {

            grandTotal = BigDecimal.ZERO;
        }


        savedBill.setGrandTotal(
                grandTotal
        );


        // ===================================================
        // PAID AMOUNT
        // ===================================================

        BigDecimal paidAmount =
                savedBill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : savedBill.getPaidAmount();


        // Nearest rupee round-off

        BigDecimal roundedGrandTotal =
                grandTotal.setScale(
                        0,
                        RoundingMode.HALF_UP
                );


        BigDecimal roundedPaidAmount =
                paidAmount.setScale(
                        0,
                        RoundingMode.HALF_UP
                );


        // ===================================================
        // PAID VALIDATION
        // ===================================================

        if (roundedPaidAmount.compareTo(
                roundedGrandTotal) > 0) {

            throw new RuntimeException(
                    "Paid amount cannot be greater than Grand Total"
            );
        }


        // ===================================================
        // SAVE ROUNDED VALUES
        // ===================================================

        savedBill.setGrandTotal(
                roundedGrandTotal
        );

        savedBill.setPaidAmount(
                roundedPaidAmount
        );


        // ===================================================
        // DUE AMOUNT
        // ===================================================

        BigDecimal dueAmount =
                roundedGrandTotal
                        .subtract(roundedPaidAmount);


        if (dueAmount.compareTo(
                BigDecimal.ZERO) < 0) {

            dueAmount = BigDecimal.ZERO;
        }


        savedBill.setDueAmount(
                dueAmount
        );


        // ===================================================
        // STATUS
        // ===================================================

        if (dueAmount.compareTo(
                BigDecimal.ZERO) == 0) {

            savedBill.setStatus(
                    BillStatus.PAID
            );

        } else if (
                paidAmount.compareTo(
                        BigDecimal.ZERO) > 0
        ) {

            savedBill.setStatus(
                    BillStatus.PARTIAL
            );

        } else {

            savedBill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        // ===================================================
        // SAVE FINAL BILL
        // ===================================================

        billRepository.save(
                savedBill
        );


        // ===================================================
        // INITIAL PAYMENT HISTORY
        // ===================================================

        if (paidAmount.compareTo(
                BigDecimal.ZERO) > 0) {

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


        // ===================================================
        // RETURN BILL
        // ===================================================

        return convertToDto(
                savedBill
        );
    }



    // =======================================================
    // UPDATE BILL
    // =======================================================

    @Override
    @Transactional
    public BillDto updateBill(
            String billNumber,
            BillDto billDto
    ) {

        /*
         * IMPORTANT:
         * Existing bill number and existing Bill ID are preserved.
         *
         * We first restore the stock used by the old bill so that
         * the new bill data can be validated against the correct
         * available stock.
         *
         * createBill() is then reused for the existing, already-tested
         * billing calculation logic. Everything is inside the same
         * transaction, so if anything fails the complete update rolls
         * back.
         */

        Bill existingBill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));

        // ===================================================
        // 1. RESTORE OLD STOCK
        // ===================================================

        List<BillItem> oldBillItems =
                billItemRepository.findByBill(existingBill);

        for (BillItem oldItem : oldBillItems) {

            if (oldItem.getJewelleryItem() != null) {

                Integer oldQuantity = oldItem.getQuantity();

                if (oldQuantity != null && oldQuantity > 0) {

                    JewelleryItem jewelleryItem =
                            oldItem.getJewelleryItem();

                    Integer currentStock =
                            jewelleryItem.getStockQuantity();

                    int restoredStock =
                            (currentStock == null ? 0 : currentStock)
                                    + oldQuantity;

                    jewelleryItem.setStockQuantity(restoredStock);

                    jewelleryItemRepository.save(jewelleryItem);
                }
            }
        }

        // ===================================================
        // 2. CREATE UPDATED BILL TEMPORARILY
        // ===================================================
        //
        // createBill() contains the complete working calculation
        // logic for:
        // - stock validation
        // - weight
        // - metal amount
        // - making charge
        // - GST
        // - discount
        // - exchange
        // - grand total
        // - paid / due
        // - status
        //
        // Therefore we reuse it instead of duplicating that logic.
        //

        BillDto updatedDto = createBill(billDto);

        Bill temporaryBill =
                billRepository
                        .findByBillNumber(updatedDto.getBillNumber())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Temporary updated bill could not be found"
                                ));

        // ===================================================
        // 3. DELETE OLD BILL ITEMS
        // ===================================================

        if (!oldBillItems.isEmpty()) {
            billItemRepository.deleteAll(oldBillItems);
            billItemRepository.flush();
        }

        // ===================================================
        // 4. DELETE OLD PAYMENT HISTORY
        // ===================================================

        List<PaymentHistory> oldPaymentHistories =
                paymentHistoryRepository
                        .findByBill_BillNumberOrderByPaymentDateDesc(
                                billNumber
                        );

        if (!oldPaymentHistories.isEmpty()) {
            paymentHistoryRepository.deleteAll(
                    oldPaymentHistories
            );
            paymentHistoryRepository.flush();
        }

        // ===================================================
        // 5. COPY UPDATED BILL DATA TO ORIGINAL BILL
        // ===================================================
        //
        // Existing ID and BILL NUMBER are intentionally preserved.
        //

        existingBill.setCustomer(
                temporaryBill.getCustomer()
        );

        existingBill.setTotalAmount(
                temporaryBill.getTotalAmount()
        );

        existingBill.setDiscount(
                temporaryBill.getDiscount()
        );

        existingBill.setGstAmount(
                temporaryBill.getGstAmount()
        );

        existingBill.setGrandTotal(
                temporaryBill.getGrandTotal()
        );

        existingBill.setPaidAmount(
                temporaryBill.getPaidAmount()
        );

        existingBill.setDueAmount(
                temporaryBill.getDueAmount()
        );

        existingBill.setGoldExchangeWeight(
                temporaryBill.getGoldExchangeWeight()
        );

        existingBill.setGoldExchangeRate(
                temporaryBill.getGoldExchangeRate()
        );

        existingBill.setGoldExchangeAmount(
                temporaryBill.getGoldExchangeAmount()
        );

        existingBill.setSilverExchangeWeight(
                temporaryBill.getSilverExchangeWeight()
        );

        existingBill.setSilverExchangeRate(
                temporaryBill.getSilverExchangeRate()
        );

        existingBill.setSilverExchangeAmount(
                temporaryBill.getSilverExchangeAmount()
        );

        existingBill.setTotalExchangeAmount(
                temporaryBill.getTotalExchangeAmount()
        );

        existingBill.setStatus(
                temporaryBill.getStatus()
        );

        existingBill.setPaymentMode(
                temporaryBill.getPaymentMode()
        );

        // Keep the original bill date.
        // Bill number also remains unchanged.

        Bill savedExistingBill =
                billRepository.save(existingBill);

        // ===================================================
        // 6. MOVE NEW BILL ITEMS TO ORIGINAL BILL
        // ===================================================

        List<BillItem> newBillItems =
                billItemRepository.findByBill(temporaryBill);

        for (BillItem newItem : newBillItems) {

            newItem.setBill(savedExistingBill);

            billItemRepository.save(newItem);
        }

        // ===================================================
        // 7. DELETE TEMPORARY PAYMENT HISTORY
        // ===================================================

        List<PaymentHistory> temporaryPaymentHistories =
                paymentHistoryRepository
                        .findByBill_BillNumberOrderByPaymentDateDesc(
                                temporaryBill.getBillNumber()
                        );

        if (!temporaryPaymentHistories.isEmpty()) {
            paymentHistoryRepository.deleteAll(
                    temporaryPaymentHistories
            );
            paymentHistoryRepository.flush();
        }

        // ===================================================
        // 8. CREATE PAYMENT HISTORY FOR UPDATED BILL
        // ===================================================

        BigDecimal updatedPaidAmount =
                savedExistingBill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : savedExistingBill.getPaidAmount();

        if (updatedPaidAmount.compareTo(BigDecimal.ZERO) > 0) {

            PaymentHistory paymentHistory =
                    PaymentHistory.builder()
                            .bill(savedExistingBill)
                            .customer(
                                    savedExistingBill.getCustomer()
                            )
                            .amount(updatedPaidAmount)
                            .paymentMode(
                                    savedExistingBill.getPaymentMode()
                            )
                            .build();

            paymentHistoryRepository.save(paymentHistory);
        }

        // ===================================================
        // 9. REMOVE TEMPORARY BILL
        // ===================================================

        /*
         * New items have already been moved to the original bill.
         * Temporary payment history has already been removed.
         * Therefore the temporary bill can now be deleted safely.
         */

        billRepository.delete(temporaryBill);
        billRepository.flush();

        // ===================================================
        // 10. RETURN UPDATED ORIGINAL BILL
        // ===================================================

        return convertToDto(savedExistingBill);
    }

    // =======================================================
    // GET BILL BY BILL NUMBER
    // =======================================================

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


    // =======================================================
    // GET ALL BILLS
    // =======================================================

    @Override
    public List<BillDto> getAllBills() {

        return billRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    // =======================================================
    // GET CUSTOMER ALL BILLS
    // =======================================================

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


    // =======================================================
    // PAY DUE
    // =======================================================

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


        // ===================================================
        // PAYMENT VALIDATION
        // ===================================================

        if (amount == null
                || amount.compareTo(
                BigDecimal.ZERO) <= 0) {

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


        // ===================================================
        // UPDATE PAID AMOUNT
        // ===================================================

        BigDecimal currentPaid =
                bill.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : bill.getPaidAmount();


        BigDecimal newPaid =
                currentPaid.add(amount);


        bill.setPaidAmount(
                newPaid
        );


        // ===================================================
        // UPDATE DUE
        // ===================================================

        BigDecimal due =
                bill.getGrandTotal()
                        .subtract(newPaid);


        bill.setDueAmount(
                due
        );


        // ===================================================
        // UPDATE STATUS
        // ===================================================

        if (due.compareTo(
                BigDecimal.ZERO) == 0) {

            bill.setStatus(
                    BillStatus.PAID
            );

        } else {

            bill.setStatus(
                    BillStatus.PARTIAL
            );
        }


        // ===================================================
        // SAVE UPDATED BILL
        // ===================================================

        Bill savedBill =
                billRepository.save(bill);


        // ===================================================
        // SAVE PAYMENT HISTORY
        // ===================================================

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


        return convertToDto(
                savedBill
        );
    }


    // =======================================================
    // DELETE BILL
    // =======================================================

    @Override
    @Transactional
    public void deleteBill(
            String billNumber
    ) {

        Bill bill =
                billRepository
                        .findByBillNumber(billNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill Not Found"
                                ));

        // ===================================================
        // 1. RESTORE STOCK
        // ===================================================

        List<BillItem> billItems =
                billItemRepository.findByBill(bill);

        for (BillItem billItem : billItems) {

            if (billItem.getJewelleryItem() != null) {

                Integer quantity = billItem.getQuantity();

                if (quantity != null && quantity > 0) {

                    JewelleryItem jewelleryItem =
                            billItem.getJewelleryItem();

                    Integer currentStock =
                            jewelleryItem.getStockQuantity();

                    int restoredStock =
                            (currentStock == null ? 0 : currentStock)
                                    + quantity;

                    jewelleryItem.setStockQuantity(restoredStock);

                    jewelleryItemRepository.save(jewelleryItem);
                }
            }
        }

        // ===================================================
        // 2. DELETE BILL ITEMS
        // ===================================================

        if (!billItems.isEmpty()) {
            billItemRepository.deleteAll(billItems);
            billItemRepository.flush();
        }

        // ===================================================
        // 3. DELETE PAYMENT HISTORY
        // ===================================================

        List<PaymentHistory> paymentHistories =
                paymentHistoryRepository
                        .findByBill_BillNumberOrderByPaymentDateDesc(
                                billNumber
                        );

        if (!paymentHistories.isEmpty()) {
            paymentHistoryRepository.deleteAll(paymentHistories);
            paymentHistoryRepository.flush();
        }

        // ===================================================
        // 4. DELETE BILL
        // ===================================================

        billRepository.delete(bill);
    }


    // =======================================================
    // GENERATE BILL NUMBER
    // =======================================================

    private synchronized String generateBillNumber() {

        /*
         * IMPORTANT:
         * Do NOT use billRepository.count() + 1 here.
         *
         * Example:
         * BILL00001
         * BILL00002  <- deleted
         * BILL00003
         *
         * count() = 2, so count() + 1 would generate BILL00003
         * again and PostgreSQL would throw a duplicate-key error.
         *
         * Instead, find the highest existing BILL number and generate
         * the next number. Deleted bill numbers are NOT reused.
         *
         * synchronized also prevents two bill-creation requests from
         * generating the same number inside this running application.
         */

        long maxBillNumber = 0;

        List<Bill> allBills = billRepository.findAll();

        for (Bill existingBill : allBills) {

            String existingNumber =
                    existingBill.getBillNumber();

            if (existingNumber == null) {
                continue;
            }

            existingNumber =
                    existingNumber.trim().toUpperCase();

            if (!existingNumber.startsWith("BILL")) {
                continue;
            }

            String numberPart =
                    existingNumber.substring(4);

            try {

                long number =
                        Long.parseLong(numberPart);

                if (number > maxBillNumber) {
                    maxBillNumber = number;
                }

            } catch (NumberFormatException ignored) {
                /*
                 * Ignore old/invalid bill numbers such as
                 * BILLTEST or other non-numeric values.
                 */
            }
        }

        long nextBillNumber =
                maxBillNumber + 1;

        return String.format(
                "BILL%05d",
                nextBillNumber
        );
    }


    // =======================================================
    // DTO CONVERTER
    // =======================================================

    private BillDto convertToDto(
            Bill bill
    ) {

        // Bill ke saare items database se fetch
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

                            // Item name
                            .itemName(
                                    billItem.getItemName() != null
                                            ? billItem.getItemName()
                                            : item != null
                                            ? item.getItemName()
                                            : null
                            )

                            // Quantity
                            .quantity(
                                    billItem.getQuantity()
                            )

                            // Billing time weight
                            .weight(
                                    billItem.getWeight() != null
                                            ? billItem.getWeight()
                                            : item != null
                                            ? item.getWeight()
                                            : null
                            )

                            // Metal rate
                            .metalRate(
                                    billItem.getMetalRate()
                            )

                            // Old field
                            .makingChargePercent(
                                    billItem.getMakingChargePercent()
                            )

                            // New making fields
                            .makingChargeType(
                                    billItem.getMakingChargeType()
                            )

                            .makingChargeValue(
                                    billItem.getMakingChargeValue()
                            )

                            // GST
                            .gstPercent(
                                    billItem.getGstPercent()
                            )

                            // Total
                            .total(
                                    billItem.getTotal()
                            )

                            .build();


            itemDtos.add(itemDto);
        }


        // ===================================================
        // BILL DTO
        // ===================================================

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

                .goldExchangeWeight(
                        bill.getGoldExchangeWeight() == null
                                ? BigDecimal.ZERO
                                : bill.getGoldExchangeWeight()
                )

                .goldExchangeRate(
                        bill.getGoldExchangeRate() == null
                                ? BigDecimal.ZERO
                                : bill.getGoldExchangeRate()
                )

                .silverExchangeWeight(
                        bill.getSilverExchangeWeight() == null
                                ? BigDecimal.ZERO
                                : bill.getSilverExchangeWeight()
                )

                .silverExchangeRate(
                        bill.getSilverExchangeRate() == null
                                ? BigDecimal.ZERO
                                : bill.getSilverExchangeRate()
                )

                .goldExchangeAmount(
                        bill.getGoldExchangeAmount() == null
                                ? BigDecimal.ZERO
                                : bill.getGoldExchangeAmount()
                )

                .silverExchangeAmount(
                        bill.getSilverExchangeAmount() == null
                                ? BigDecimal.ZERO
                                : bill.getSilverExchangeAmount()
                )

                .totalExchangeAmount(
                        bill.getTotalExchangeAmount() == null
                                ? BigDecimal.ZERO
                                : bill.getTotalExchangeAmount()
                )

                .build();
    }


    // =======================================================
    // GET OUTSTANDING BILLS
    // =======================================================

    @Override
    @Transactional(readOnly = true)
    public List<OutstandingBillDto> getOutstandingBills() {

        List<Bill> outstandingBills =
                billRepository.findByDueAmountGreaterThan(
                        BigDecimal.ZERO
                );


        List<OutstandingBillDto> result =
                new ArrayList<>();


        for (Bill bill : outstandingBills) {

            Customer customer =
                    bill.getCustomer();


            List<BillItem> billItems =
                    billItemRepository.findByBill(bill);


            List<BillItemDto> itemDtos =
                    new ArrayList<>();


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

                                .makingChargeType(
                                        billItem.getMakingChargeType()
                                )

                                .makingChargeValue(
                                        billItem.getMakingChargeValue()
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