package com.jewellery.jewelleryshop.services;

import com.jewellery.jewelleryshop.dto.GstExchangeItemDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class GstExchangeCalculationService {

    private static final int AMOUNT_SCALE = 2;
    private static final int WEIGHT_SCALE = 3;

    // =========================================================
    // CALCULATE EXCHANGE ITEM
    // =========================================================

    public GstExchangeItemDto calculateItem(
            GstExchangeItemDto itemDto
    ) {

        if (itemDto == null) {
            throw new IllegalArgumentException(
                    "Exchange item data cannot be null"
            );
        }

        // -----------------------------------------------------
        // METAL TYPE VALIDATION
        // -----------------------------------------------------

        String metalType = itemDto.getMetalType();

        if (metalType == null || metalType.isBlank()) {
            throw new IllegalArgumentException(
                    "Exchange metal type is required"
            );
        }

        metalType = metalType.trim().toUpperCase();

        if (!metalType.equals("GOLD")
                && !metalType.equals("SILVER")
                && !metalType.equals("OTHER")) {

            throw new IllegalArgumentException(
                    "Invalid exchange metal type: " + metalType
            );
        }

        itemDto.setMetalType(metalType);


        // -----------------------------------------------------
        // WEIGHT
        // -----------------------------------------------------

        BigDecimal weight = valueOrZero(
                itemDto.getWeight()
        );

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Exchange weight must be greater than zero"
            );
        }

        weight = weight.setScale(
                WEIGHT_SCALE,
                RoundingMode.HALF_UP
        );


        // -----------------------------------------------------
        // RATE
        // -----------------------------------------------------

        BigDecimal rate = valueOrZero(
                itemDto.getRate()
        );

        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Exchange rate cannot be negative"
            );
        }

        rate = rate.setScale(
                AMOUNT_SCALE,
                RoundingMode.HALF_UP
        );


        // -----------------------------------------------------
        // EXCHANGE AMOUNT
        // -----------------------------------------------------

        BigDecimal amount = weight
                .multiply(rate)
                .setScale(
                        AMOUNT_SCALE,
                        RoundingMode.HALF_UP
                );


        // -----------------------------------------------------
        // SET CALCULATED VALUES
        // -----------------------------------------------------

        itemDto.setWeight(weight);
        itemDto.setRate(rate);
        itemDto.setAmount(amount);

        return itemDto;
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