package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Employee extends Person implements Payable, Comparable<Employee> {
    private int employeeId;
    private String position;
    private String status;
    private String immediateSupervisor;
    private double basicSalary;
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;
    
    // Composition - Employee has compensation details
    private CompensationPackage compensationPackage;
    
    // Employee status enum
    public enum EmploymentStatus {
        REGULAR("Regular"),
        PROBATIONARY("Probationary"),
        CONTRACTUAL("Contractual"),
        PART_TIME("Part-time");
        
        private final String displayName;
        
        EmploymentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        @Override
        public String toString() { return displayName; }
    }
    
    // Constructors
    public Employee() {
        super();
        this.compensationPackage = new CompensationPackage();
    }
    
    public Employee(String firstName, String lastName, int employeeId) {
        super(firstName, lastName);
        this.employeeId = employeeId;
        this.compensationPackage = new CompensationPackage();
    }
    
    // Implementing abstract methods from Person
    @Override
    public String getDisplayName() {
        return String.format("%s (ID: %d)", getFullName(), employeeId);
    }
    
    @Override
    public String getRole() {
        return position != null ? position : "Employee";
    }
    
    @Override
    public boolean isActive() {
        return !"TERMINATED".equalsIgnoreCase(status);
    }
    
    // Implementing Payable interface
    @Override
    public double calculateBasePay() {
        return basicSalary;
    }
    
    @Override
    public double calculateAllowances() {
        return compensationPackage.getTotalAllowances();
    }
    
    @Override
    public double calculateGrossPay() {
        return calculateBasePay() + calculateAllowances();
    }
    
    // Implementing Comparable for natural ordering
    @Override
    public int compareTo(Employee other) {
        int lastNameCompare = this.lastName.compareToIgnoreCase(other.lastName);
        if (lastNameCompare != 0) return lastNameCompare;
        return this.firstName.compareToIgnoreCase(other.firstName);
    }
    
    // Business methods
    public boolean isRegularEmployee() {
        return "Regular".equalsIgnoreCase(status);
    }
    
    public boolean canReceiveBonus() {
        return isRegularEmployee() && basicSalary > 0;
    }
    
    public double getDailyRate() {
        return basicSalary / 22.0; // 22 working days per month
    }
    
    public double getHourlyRate() {
        return getDailyRate() / 8.0; // 8 hours per day
    }
    
    // Getters and setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { 
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId; 
    }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) { 
        this.immediateSupervisor = immediateSupervisor; 
    }
    
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { 
        if (basicSalary < 0) {
            throw new IllegalArgumentException("Basic salary cannot be negative");
        }
        this.basicSalary = basicSalary; 
    }
    
    public CompensationPackage getCompensationPackage() { return compensationPackage; }
    public void setCompensationPackage(CompensationPackage compensationPackage) { 
        this.compensationPackage = compensationPackage; 
    }
    
    // Government ID getters/setters
    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    
    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }
    
    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    
    public String getPagibigNumber() { return pagibigNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }
    
    // Backward compatibility methods
    public double getRiceSubsidy() { return compensationPackage.getRiceSubsidy(); }
    public void setRiceSubsidy(double riceSubsidy) { compensationPackage.setRiceSubsidy(riceSubsidy); }
    
    public double getPhoneAllowance() { return compensationPackage.getPhoneAllowance(); }
    public void setPhoneAllowance(double phoneAllowance) { compensationPackage.setPhoneAllowance(phoneAllowance); }
    
    public double getClothingAllowance() { return compensationPackage.getClothingAllowance(); }
    public void setClothingAllowance(double clothingAllowance) { compensationPackage.setClothingAllowance(clothingAllowance); }
    
    public double getTotalAllowances() { return compensationPackage.getTotalAllowances(); }
    
    // Additional getters for backward compatibility
    public LocalDate getBirthday() { return getBirthDate(); }
    public void setBirthday(LocalDate birthday) { setBirthDate(birthday); }
    
    public double getGrossSemiMonthlyRate() { return basicSalary / 2; }
    public void setGrossSemiMonthlyRate(double rate) { /* Calculated field */ }
    
    public double getHourlyRate2() { return getHourlyRate(); }
    public void setHourlyRate(double rate) { /* Calculated field */ }
}

// Payable interface for polymorphism
interface Payable {
    double calculateBasePay();
    double calculateAllowances();
    double calculateGrossPay();
}