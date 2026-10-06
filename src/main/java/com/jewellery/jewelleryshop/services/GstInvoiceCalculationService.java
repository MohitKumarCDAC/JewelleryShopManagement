package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.GstInvoiceDto;
import com.jewellery.jewelleryshop.dto.GstInvoiceItemDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Responsible for all GST invoice calculations.
 *
 * <p>
 * The service intentionally keeps calculation logic separate from
 * invoice persistence logic. This makes the calculation rules easier
 * to maintain and prevents the controller/service layer from becoming
 * tightly coupled with financial calculations.
 * </p>
 *
 * <p>
 * Making charge supports three independent modes:
 * <ul>
 *     <li>PERCENT   - percentage of metal amount</li>
 *     <li>PER_GRAM  - fixed amount multiplied by weight</li>
 *     <li>RUPEES    - fixed amount for the item</li>
 * </ul>
 * </p>
 */
@Service
public class GstInvoiceCalculationService {

    private static final int MONEY_SCALE = 2;

    private static final BigDecimal ONE_HUNDRED =
            BigDecimal.valueOf(100);

    /**
     * Calculates all financial values for a single GST invoice item.
     *
     * @param item invoice item received from frontend
     * @param intraState true when CGST + SGST applies,
     *                   false when IGST applies
     * @return calculated invoice item
     */
    public GstInvoiceItemDto calculateItem(
            GstInvoiceItemDto item,
            boolean intraState
    ) {

        validateItem(item);

        BigDecimal quantity =
                valueOrZero(item.getQuantity());

        BigDecimal weight =
                valueOrZero(item.getWeight());

        BigDecimal metalRate =
                valueOrZero(item.getMetalRate());

        BigDecimal makingChargeValue =
                valueOrZero(item.getMakingChargeValue());

        BigDecimal discountAmount =
                valueOrZero(item.getDiscountAmount());

        BigDecimal gstPercent =
                valueOrZero(item.getGstPercent());


        // =========================================================
        // 1. METAL AMOUNT
        // =========================================================

        BigDecimal metalAmount =
                weight
                        .multiply(quantity)
                        .multiply(metalRate)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );


        // =========================================================
        // 2. MAKING CHARGE
        // =========================================================

        BigDecimal makingChargeAmount =
                calculateMakingCharge(
                        metalAmount,
                        weight,
                        makingChargeValue,
                        item.getMakingChargeType()
                );


        // =========================================================
        // 3. DISCOUNT
        // =========================================================

        if (discountAmount.compareTo(metalAmount.add(
                makingChargeAmount
        )) > 0) {

            throw new IllegalArgumentException(
                    "Discount cannot be greater than item value"
            );
        }


        // =========================================================
        // 4. TAXABLE AMOUNT
        // =========================================================

        BigDecimal taxableAmount =
                metalAmount
                        .add(makingChargeAmount)
                        .subtract(discountAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );


        // =========================================================
        // 5. GST AMOUNT
        // =========================================================

        BigDecimal totalGstAmount =
                taxableAmount
                        .multiply(gstPercent)
                        .divide(
                                ONE_HUNDRED,
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );


        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;


        // =========================================================
        // 6. TAX SPLIT
        // =========================================================

        if (intraState) {

            /*
             * For intra-state supply:
             *
             * CGST = 50% of total GST
             * SGST = 50% of total GST
             */
            cgstAmount =
                    totalGstAmount
                            .divide(
                                    BigDecimal.valueOf(2),
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

            sgstAmount =
                    totalGstAmount
                            .subtract(cgstAmount)
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );

        } else {

            /*
             * For inter-state supply:
             *
             * Entire GST becomes IGST.
             */
            igstAmount = totalGstAmount;
        }


        // =========================================================
        // 7. ITEM TOTAL
        // =========================================================

        BigDecimal totalAmount =
                taxableAmount
                        .add(totalGstAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );


        // =========================================================
        // 8. UPDATE CALCULATED VALUES
        // =========================================================

        item.setQuantity(quantity);
        item.setWeight(weight);
        item.setMetalRate(metalRate);

        item.setMetalAmount(metalAmount);

        item.setMakingChargeValue(
                makingChargeValue
        );

        item.setMakingChargeAmount(
                makingChargeAmount
        );

        item.setDiscountAmount(
                discountAmount
        );

        item.setTaxableAmount(
                taxableAmount
        );

        item.setGstPercent(
                gstPercent
        );

        item.setCgstAmount(
                cgstAmount
        );

        item.setSgstAmount(
                sgstAmount
        );

        item.setIgstAmount(
                igstAmount
        );

        item.setTotalAmount(
                totalAmount
        );


        return item;
    }


    /**
     * Calculates all invoice-level totals from calculated items.
     */
    public void calculateInvoiceTotals(
            GstInvoiceDto invoice,
            List<GstInvoiceItemDto> items
    ) {

        BigDecimal taxableAmount = BigDecimal.ZERO;
        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;
        BigDecimal totalGstAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal itemTotal = BigDecimal.ZERO;


        if (items != null) {

            for (GstInvoiceItemDto item : items) {

                taxableAmount =
                        taxableAmount.add(
                                valueOrZero(
                                        item.getTaxableAmount()
                                )
                        );

                cgstAmount =
                        cgstAmount.add(
                                valueOrZero(
                                        item.getCgstAmount()
                                )
                        );

                sgstAmount =
                        sgstAmount.add(
                                valueOrZero(
                                        item.getSgstAmount()
                                )
                        );

                igstAmount =
                        igstAmount.add(
                                valueOrZero(
                                        item.getIgstAmount()
                                )
                        );

                totalGstAmount =
                        totalGstAmount.add(
                                valueOrZero(
                                        item.getCgstAmount()
                                )
                        ).add(
                                valueOrZero(
                                        item.getSgstAmount()
                                )
                        ).add(
                                valueOrZero(
                                        item.getIgstAmount()
                                )
                        );

                discountAmount =
                        discountAmount.add(
                                valueOrZero(
                                        item.getDiscountAmount()
                                )
                        );

                itemTotal =
                        itemTotal.add(
                                valueOrZero(
                                        item.getTotalAmount()
                                )
                        );
            }
        }


        // =========================================================
        // INVOICE LEVEL DISCOUNT
        // =========================================================

        BigDecimal invoiceDiscount =
                valueOrZero(
                        invoice.getDiscountAmount()
                );


        discountAmount =
                discountAmount
                        .add(invoiceDiscount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );


        // =========================================================
        // TOTALS
        // =========================================================

        taxableAmount =
                taxableAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        cgstAmount =
                cgstAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        sgstAmount =
                sgstAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        igstAmount =
                igstAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );

        totalGstAmount =
                totalGstAmount.setScale(
                        MONEY_SCALE,
                        RoundingMode.HALF_UP
                );


        // =========================================================
        // ROUND OFF
        // =========================================================

        BigDecimal roundOff =
                valueOrZero(
                        invoice.getRoundOff()
                );


        BigDecimal amountBeforeRoundOff =
                itemTotal
                        .add(invoiceDiscount.negate());


        BigDecimal grandTotal =
                amountBeforeRoundOff
                        .add(roundOff)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );


        // =========================================================
        // SET INVOICE TOTALS
        // =========================================================

        invoice.setTaxableAmount(
                taxableAmount
        );

        invoice.setCgstAmount(
                cgstAmount
        );

        invoice.setSgstAmount(
                sgstAmount
        );

        invoice.setIgstAmount(
                igstAmount
        );

        invoice.setTotalGstAmount(
                totalGstAmount
        );

        invoice.setDiscountAmount(
                discountAmount
        );

        invoice.setGrandTotal(
                grandTotal
        );
    }


    // =========================================================
    // MAKING CHARGE CALCULATION
    // =========================================================

    private BigDecimal calculateMakingCharge(
            BigDecimal metalAmount,
            BigDecimal weight,
            BigDecimal makingChargeValue,
            String makingChargeType
    ) {

        if (makingChargeType == null ||
                makingChargeType.isBlank()) {

            throw new IllegalArgumentException(
                    "Making charge type is required"
            );
        }


        String normalizedType =
                makingChargeType
                        .trim()
                        .toUpperCase();


        if (makingChargeValue.signum() < 0) {

            throw new IllegalArgumentException(
                    "Making charge value cannot be negative"
            );
        }


        return switch (normalizedType) {

            case "PERCENT" ->

                    metalAmount
                            .multiply(makingChargeValue)
                            .divide(
                                    ONE_HUNDRED,
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );


            case "PER_GRAM" ->

                    weight
                            .multiply(makingChargeValue)
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );


            case "RUPEES" ->

                    makingChargeValue
                            .setScale(
                                    MONEY_SCALE,
                                    RoundingMode.HALF_UP
                            );


            default ->

                    throw new IllegalArgumentException(
                            "Unsupported making charge type: "
                                    + makingChargeType
                    );
        };
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateItem(
            GstInvoiceItemDto item
    ) {

        if (item == null) {

            throw new IllegalArgumentException(
                    "Invoice item cannot be null"
            );
        }


        if (item.getItemName() == null ||
                item.getItemName().isBlank()) {

            throw new IllegalArgumentException(
                    "Item name is required"
            );
        }


        BigDecimal quantity =
                valueOrZero(item.getQuantity());

        BigDecimal weight =
                valueOrZero(item.getWeight());

        BigDecimal metalRate =
                valueOrZero(item.getMetalRate());


        if (quantity.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }


        if (weight.signum() < 0) {

            throw new IllegalArgumentException(
                    "Weight cannot be negative"
            );
        }


        if (metalRate.signum() < 0) {

            throw new IllegalArgumentException(
                    "Metal rate cannot be negative"
            );
        }
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