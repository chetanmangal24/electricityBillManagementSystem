package com.ebms.service;

import com.ebms.model.Consumer.ConnectionType;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Configurable electricity tariff rates.
 *
 * IMPORTANT: These are placeholder rates for demonstration only.
 * Replace with actual tariff rates from your electricity board.
 *
 * Tariff Formula:
 * - Residential: ₹5/unit (0-100 units), ₹7/unit (101-200), ₹9/unit (>200)
 * - Commercial: ₹8/unit (0-100 units), ₹10/unit (>100)
 * - Industrial: Flat ₹12/unit
 * - Fixed Charge: ₹50 (Residential), ₹100 (Commercial), ₹200 (Industrial)
 */
public class TariffConfig {

    // Residential rates
    private static BigDecimal RES_RATE_0_100 = new BigDecimal("5.00");
    private static BigDecimal RES_RATE_101_200 = new BigDecimal("7.00");
    private static BigDecimal RES_RATE_ABOVE_200 = new BigDecimal("9.00");
    private static BigDecimal RES_FIXED_CHARGE = new BigDecimal("50.00");

    // Commercial rates
    private static BigDecimal COM_RATE_0_100 = new BigDecimal("8.00");
    private static BigDecimal COM_RATE_ABOVE_100 = new BigDecimal("10.00");
    private static BigDecimal COM_FIXED_CHARGE = new BigDecimal("100.00");

    // Industrial rates
    private static BigDecimal IND_RATE_FLAT = new BigDecimal("12.00");
    private static BigDecimal IND_FIXED_CHARGE = new BigDecimal("200.00");

    private TariffConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Calculate bill amount based on connection type and units consumed.
     * @param connectionType Type of connection
     * @param unitsConsumed Units consumed
     * @return Total bill amount including fixed charges
     */
    public static BigDecimal calculateBillAmount(ConnectionType connectionType, BigDecimal unitsConsumed) {
        BigDecimal energyCharge = BigDecimal.ZERO;
        BigDecimal fixedCharge = BigDecimal.ZERO;

        switch (connectionType) {
            case RESIDENTIAL:
                energyCharge = calculateResidentialCharge(unitsConsumed);
                fixedCharge = RES_FIXED_CHARGE;
                break;
            case COMMERCIAL:
                energyCharge = calculateCommercialCharge(unitsConsumed);
                fixedCharge = COM_FIXED_CHARGE;
                break;
            case INDUSTRIAL:
                energyCharge = calculateIndustrialCharge(unitsConsumed);
                fixedCharge = IND_FIXED_CHARGE;
                break;
        }

        return energyCharge.add(fixedCharge).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateResidentialCharge(BigDecimal units) {
        BigDecimal charge = BigDecimal.ZERO;
        BigDecimal remaining = units;

        // First 100 units
        BigDecimal slab1 = remaining.min(new BigDecimal("100"));
        charge = charge.add(slab1.multiply(RES_RATE_0_100));
        remaining = remaining.subtract(slab1);

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            // Next 100 units (101-200)
            BigDecimal slab2 = remaining.min(new BigDecimal("100"));
            charge = charge.add(slab2.multiply(RES_RATE_101_200));
            remaining = remaining.subtract(slab2);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            // Above 200 units
            charge = charge.add(remaining.multiply(RES_RATE_ABOVE_200));
        }

        return charge.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateCommercialCharge(BigDecimal units) {
        BigDecimal charge = BigDecimal.ZERO;

        // First 100 units
        BigDecimal slab1 = units.min(new BigDecimal("100"));
        charge = charge.add(slab1.multiply(COM_RATE_0_100));

        // Above 100 units
        BigDecimal remaining = units.subtract(slab1);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            charge = charge.add(remaining.multiply(COM_RATE_ABOVE_100));
        }

        return charge.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateIndustrialCharge(BigDecimal units) {
        return units.multiply(IND_RATE_FLAT).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get tariff breakdown for display purposes.
     */
    public static String getTariffInfo(ConnectionType connectionType) {
        switch (connectionType) {
            case RESIDENTIAL:
                return String.format(
                    "Residential Tariff:\n" +
                    "0-100 units: ₹%.2f/unit\n" +
                    "101-200 units: ₹%.2f/unit\n" +
                    "Above 200 units: ₹%.2f/unit\n" +
                    "Fixed Charge: ₹%.2f",
                    RES_RATE_0_100, RES_RATE_101_200, RES_RATE_ABOVE_200, RES_FIXED_CHARGE
                );
            case COMMERCIAL:
                return String.format(
                    "Commercial Tariff:\n" +
                    "0-100 units: ₹%.2f/unit\n" +
                    "Above 100 units: ₹%.2f/unit\n" +
                    "Fixed Charge: ₹%.2f",
                    COM_RATE_0_100, COM_RATE_ABOVE_100, COM_FIXED_CHARGE
                );
            case INDUSTRIAL:
                return String.format(
                    "Industrial Tariff:\n" +
                    "Flat Rate: ₹%.2f/unit\n" +
                    "Fixed Charge: ₹%.2f",
                    IND_RATE_FLAT, IND_FIXED_CHARGE
                );
            default:
                return "Unknown connection type";
        }
    }
}
