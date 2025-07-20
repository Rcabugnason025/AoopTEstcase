/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class GovernmentDeduction extends Deduction {
    private String governmentType; // SSS, PhilHealth, Pag-IBIG, Tax
    private double baseSalary;
    
    public GovernmentDeduction(int employeeId, String governmentType, double baseSalary) {
        super(employeeId, governmentType, 0);
        this.governmentType = governmentType;
        this.baseSalary = baseSalary;
    }
    
    @Override
    public void calculateDeduction() {
        switch (governmentType.toUpperCase()) {
            case "SSS":
                amount = calculateSSS(baseSalary);
                break;
            case "PHILHEALTH":
                amount = calculatePhilHealth(baseSalary);
                break;
            case "PAGIBIG":
                amount = calculatePagIbig(baseSalary);
                break;
            case "TAX":
                amount = calculateTax(baseSalary);
                break;
            default:
                amount = 0;
        }
    }
    
    public boolean isRecurring() {
        return true; // Government deductions are always recurring
    }
    
    private double calculateSSS(double salary) {
        // SSS calculation logic
        if (salary <= 4000) return 180.00;
        if (salary <= 4750) return 202.50;
        // ... more brackets
        return Math.min(salary * 0.045, 1125.00);
    }
    
    private double calculatePhilHealth(double salary) {
        return Math.min(Math.max(salary * 0.025, 500.00), 5000.00);
    }
    
    private double calculatePagIbig(double salary) {
        return Math.min(salary * 0.02, 200.00);
    }
    
    private double calculateTax(double salary) {
        double annual = salary * 12;
        if (annual <= 250000) return 0;
        if (annual <= 400000) return (annual - 250000) * 0.15 / 12;
        // ... more tax brackets
        return 0;
    }
}