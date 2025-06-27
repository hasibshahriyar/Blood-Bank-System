package com.hasib.bloodbank.server.entity;

public enum BloodGroup {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-");

    private final String displayValue;

    BloodGroup(String displayValue) {
        this.displayValue = displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    // Method to convert from display value back to enum
    public static BloodGroup fromDisplayValue(String displayValue) {
        for (BloodGroup bg : BloodGroup.values()) {
            if (bg.getDisplayValue().equals(displayValue)) {
                return bg;
            }
        }
        throw new IllegalArgumentException("Invalid blood group: " + displayValue);
    }
}
