/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class CompensationDetails {
    private int compId;
    private int employeeId;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    
    // ENHANCED: Add composition with allowance objects
    private RiceAllowance riceAllowanceObj;
    private PhoneAllowance phoneAllowanceObj;
    private ClothingAllowance clothingAllowanceObj;

    public CompensationDetails(int employeeId) {
        this.employeeId = employeeId;
        // Initialize allowance objects (COMPOSITION)
        this.riceAllowanceObj = new RiceAllowance(employeeId);
        this.phoneAllowanceObj = new PhoneAllowance(employeeId, 800.0);
        this.clothingAllowanceObj = new ClothingAllowance(employeeId);
        
        // Calculate amounts
        updateAllowances();
    }
    
    private void updateAllowances() {
        this.riceSubsidy = riceAllowanceObj.getCalculatedAmount();
        this.phoneAllowance = phoneAllowanceObj.getCalculatedAmount();
        this.clothingAllowance = clothingAllowanceObj.getCalculatedAmount();
    }
    
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }
    
    // Existing getters and setters...
    public int getCompId() { return compId; }
    public void setCompId(int compId) { this.compId = compId; }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    
    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { 
        this.riceSubsidy = riceSubsidy;
        if (riceAllowanceObj != null) {
            riceAllowanceObj.setAmount(riceSubsidy);
        }
    }
    
    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { 
        this.phoneAllowance = phoneAllowance;
        if (phoneAllowanceObj != null) {
            phoneAllowanceObj.setAmount(phoneAllowance);
        }
    }
    
    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { 
        this.clothingAllowance = clothingAllowance;
        if (clothingAllowanceObj != null) {
            clothingAllowanceObj.setAmount(clothingAllowance);
        }
    }
}
