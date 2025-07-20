/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

public abstract class Allowance {
    protected int allowanceId;
    protected int employeeId;
    protected String type;
    protected double amount;
    protected boolean isTaxable;
    protected LocalDate effectiveDate;
    
    // Abstract methods
    public abstract void calculateAllowance();
    public abstract boolean isEligible(Object employee);
    
    // Constructor
    public Allowance() {
        this.effectiveDate = LocalDate.now();
    }
    
    public Allowance(int employeeId, String type, double amount, boolean isTaxable) {
        this();
        setEmployeeId(employeeId);
        setType(type);
        setAmount(amount);
        setTaxable(isTaxable);
    }
    
    // Template method
    public final double getCalculatedAmount() {
        calculateAllowance();
        return amount;
    }
    
    // Getters and setters
    public int getAllowanceId() { return allowanceId; }
    public void setAllowanceId(int allowanceId) { this.allowanceId = allowanceId; }
    
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId;
    }
    
    public String getType() { return type; }
    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        this.type = type.trim();
    }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }
    
    public boolean isTaxable() { return isTaxable; }
    public void setTaxable(boolean taxable) { isTaxable = taxable; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
