/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class CompensationPackage {
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double transportationAllowance;
    private double mealAllowance;
    
    public CompensationPackage() {}
    
    public CompensationPackage(double riceSubsidy, double phoneAllowance, double clothingAllowance) {
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
    }
    
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance + 
               transportationAllowance + mealAllowance;
    }
    
    public double getTotalTaxableAllowances() {
        // Some allowances may not be taxable
        return phoneAllowance + clothingAllowance;
    }
    
    // Getters and setters
    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { 
        if (riceSubsidy < 0) throw new IllegalArgumentException("Rice subsidy cannot be negative");
        this.riceSubsidy = riceSubsidy; 
    }
    
    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { 
        if (phoneAllowance < 0) throw new IllegalArgumentException("Phone allowance cannot be negative");
        this.phoneAllowance = phoneAllowance; 
    }
    
    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { 
        if (clothingAllowance < 0) throw new IllegalArgumentException("Clothing allowance cannot be negative");
        this.clothingAllowance = clothingAllowance; 
    }
    
    public double getTransportationAllowance() { return transportationAllowance; }
    public void setTransportationAllowance(double transportationAllowance) { 
        this.transportationAllowance = transportationAllowance; 
    }
    
    public double getMealAllowance() { return mealAllowance; }
    public void setMealAllowance(double mealAllowance) { this.mealAllowance = mealAllowance; }
}
