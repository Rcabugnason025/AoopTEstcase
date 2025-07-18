package model;

import java.time.LocalDate;

// Another concrete class showing POLYMORPHISM
public class ProbationaryEmployee extends Employee {
    private static final double OVERTIME_RATE = 1.15; // Reduced overtime rate
    private static final int STANDARD_WORKING_DAYS = 22;
    private LocalDate probationEndDate;
    
    // Default constructor
    public ProbationaryEmployee() {
        super();
    }
    
    // Constructor with parameters
    public ProbationaryEmployee(int employeeId, String firstName, String lastName, String position, double basicSalary) {
        super(employeeId, firstName, lastName, position, basicSalary);
        this.status = "Probationary";
        // Reduced allowances for probationary employees
        this.riceSubsidy = 1000.0;
        this.phoneAllowance = 1000.0;
        this.clothingAllowance = 500.0;
        // Set probation end date (6 months from now)
        this.probationEndDate = LocalDate.now().plusMonths(6);
    }
    
    @Override
    public double calculateGrossPay(int daysWorked, double overtimeHours) {
        double dailyRate = basicSalary / STANDARD_WORKING_DAYS;
        double regularPay = dailyRate * daysWorked;
        // Probationary employees get reduced overtime rate
        double overtimePay = (dailyRate / 8) * overtimeHours * OVERTIME_RATE;
        return regularPay + overtimePay;
    }
    
    @Override
    public double calculateDeductions() {
        // Probationary employees have reduced deductions
        double sss = calculateSSS();
        double philHealth = calculatePhilHealth();
        double pagIbig = calculatePagIBIG();
        // Reduced withholding tax for probationary employees
        double withholdingTax = basicSalary > 30000 ? calculateWithholdingTax() * 0.8 : 0;
        return sss + philHealth + pagIbig + withholdingTax;
    }
    
    @Override
    public double calculateAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }
    
    @Override
    public boolean isEligibleForBenefits() {
        return false; // Limited benefits for probationary employees
    }
    
    @Override
    public String getEmployeeType() {
        return "Probationary Employee";
    }
    
    // Specific methods for probationary employees
    public boolean isProbationPeriodOver() {
        return LocalDate.now().isAfter(probationEndDate);
    }
    
    public long getDaysUntilRegularization() {
        if (isProbationPeriodOver()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), probationEndDate);
    }
    
    // Private helper methods for calculations
    private double calculateSSS() {
        if (basicSalary <= 3250) return 135.0;
        if (basicSalary <= 25000) return basicSalary * 0.045;
        return 1125.0;
    }
    
    private double calculatePhilHealth() {
        return Math.min(basicSalary * 0.025, 1800.0);
    }
    
    private double calculatePagIBIG() {
        if (basicSalary <= 1500) return basicSalary * 0.01;
        return Math.min(basicSalary * 0.02, 100.0);
    }
    
    private double calculateWithholdingTax() {
        double annualSalary = basicSalary * 12;
        if (annualSalary <= 250000) return 0;
        if (annualSalary <= 400000) return (annualSalary - 250000) * 0.15 / 12;
        return (22500 + (annualSalary - 400000) * 0.20) / 12;
    }
    
    // Getters and setters
    public LocalDate getProbationEndDate() { return probationEndDate; }
    public void setProbationEndDate(LocalDate probationEndDate) { 
        this.probationEndDate = probationEndDate; 
        touch();
    }
}