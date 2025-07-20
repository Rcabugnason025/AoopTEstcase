package model;

public interface DeductionCalculator {
    double calculateAmount();
    String getDeductionType();
    boolean isApplicable();
}