package com.hasib.bloodbank.server.entity;

public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String displayValue;

    Gender(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    // Method to convert from display value (e.g., "Male" to MALE)
    public static Gender fromDisplayValue(String displayValue) {
        for (Gender gender : Gender.values()) {
            if (gender.displayValue.equalsIgnoreCase(displayValue)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + displayValue);
    }
}
