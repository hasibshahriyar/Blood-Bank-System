package com.hasib.bloodbank.server.entity;

public enum BloodGroup {
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-");

    private final String displayValue;

    BloodGroup(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    // Method to convert from display value (e.g., "O+" to O_POSITIVE)
    public static BloodGroup fromDisplayValue(String displayValue) {
        for (BloodGroup bloodGroup : BloodGroup.values()) {
            if (bloodGroup.displayValue.equals(displayValue)) {
                return bloodGroup;
            }
        }
        throw new IllegalArgumentException("Invalid blood group: " + displayValue);
    }

    // Method to get compatible donor blood groups
    public BloodGroup[] getCompatibleDonors() {
        switch (this) {
            case O_POSITIVE:
                return new BloodGroup[]{O_POSITIVE, O_NEGATIVE};
            case O_NEGATIVE:
                return new BloodGroup[]{O_NEGATIVE};
            case A_POSITIVE:
                return new BloodGroup[]{A_POSITIVE, A_NEGATIVE, O_POSITIVE, O_NEGATIVE};
            case A_NEGATIVE:
                return new BloodGroup[]{A_NEGATIVE, O_NEGATIVE};
            case B_POSITIVE:
                return new BloodGroup[]{B_POSITIVE, B_NEGATIVE, O_POSITIVE, O_NEGATIVE};
            case B_NEGATIVE:
                return new BloodGroup[]{B_NEGATIVE, O_NEGATIVE};
            case AB_POSITIVE:
                return BloodGroup.values(); // Universal recipient
            case AB_NEGATIVE:
                return new BloodGroup[]{AB_NEGATIVE, A_NEGATIVE, B_NEGATIVE, O_NEGATIVE};
            default:
                return new BloodGroup[]{};
        }
    }

    // Method to get compatible recipient blood groups
    public BloodGroup[] getCompatibleRecipients() {
        switch (this) {
            case O_NEGATIVE:
                return BloodGroup.values(); // Universal donor
            case O_POSITIVE:
                return new BloodGroup[]{O_POSITIVE, A_POSITIVE, B_POSITIVE, AB_POSITIVE};
            case A_NEGATIVE:
                return new BloodGroup[]{A_NEGATIVE, A_POSITIVE, AB_NEGATIVE, AB_POSITIVE};
            case A_POSITIVE:
                return new BloodGroup[]{A_POSITIVE, AB_POSITIVE};
            case B_NEGATIVE:
                return new BloodGroup[]{B_NEGATIVE, B_POSITIVE, AB_NEGATIVE, AB_POSITIVE};
            case B_POSITIVE:
                return new BloodGroup[]{B_POSITIVE, AB_POSITIVE};
            case AB_NEGATIVE:
                return new BloodGroup[]{AB_NEGATIVE, AB_POSITIVE};
            case AB_POSITIVE:
                return new BloodGroup[]{AB_POSITIVE};
            default:
                return new BloodGroup[]{};
        }
    }
}
