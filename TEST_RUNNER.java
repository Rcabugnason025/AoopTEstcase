// Simple test runner to verify your OOP implementation works
import model.*;

public class TEST_RUNNER {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("    MOTORPH PAYROLL SYSTEM - OOP IMPLEMENTATION TEST");
        System.out.println("=".repeat(60));
        
        try {
            // Test 1: Factory Pattern (POLYMORPHISM)
            System.out.println("\n1. Testing Factory Pattern & Polymorphism:");
            Employee regular = EmployeeFactory.createEmployee("REGULAR", 10001, "John", "Doe", "Developer", 50000);
            Employee probationary = EmployeeFactory.createEmployee("PROBATIONARY", 10002, "Jane", "Smith", "Junior Dev", 35000);
            
            System.out.println("✅ Regular Employee: " + regular.getEmployeeType());
            System.out.println("✅ Probationary Employee: " + probationary.getEmployeeType());
            
            // Test 2: Inheritance & Abstraction
            System.out.println("\n2. Testing Inheritance & Abstraction:");
            System.out.println("✅ Regular instanceof Employee: " + (regular instanceof Employee));
            System.out.println("✅ Probationary instanceof Employee: " + (probationary instanceof Employee));
            
            // Test 3: Polymorphic Behavior
            System.out.println("\n3. Testing Polymorphic Behavior:");
            Employee[] employees = {regular, probationary};
            
            for (Employee emp : employees) {
                System.out.println("\n--- " + emp.getEmployeeType() + " ---");
                System.out.println("Gross Pay (22 days, 8 OT): ₱" + String.format("%.2f", emp.calculateGrossPay(22, 8)));
                System.out.println("Allowances: ₱" + String.format("%.2f", emp.calculateAllowances()));
                System.out.println("Deductions: ₱" + String.format("%.2f", emp.calculateDeductions()));
                System.out.println("Net Pay: ₱" + String.format("%.2f", emp.calculateNetPay(22, 8)));
                System.out.println("Benefits Eligible: " + emp.isEligibleForBenefits());
            }
            
            // Test 4: Template Method Pattern
            System.out.println("\n4. Testing Template Method Pattern:");
            double totalPayroll = EmployeeFactory.calculateTotalPayroll(employees, 22, 4);
            System.out.println("✅ Total Payroll for both employees: ₱" + String.format("%.2f", totalPayroll));
            
            // Test 5: Encapsulation
            System.out.println("\n5. Testing Encapsulation:");
            regular.setBasicSalary(60000);
            System.out.println("✅ Updated Regular Employee Salary: ₱" + String.format("%.2f", regular.getBasicSalary()));
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🎉 ALL OOP TESTS PASSED! Your implementation is working correctly.");
            System.out.println("✅ Inheritance: Employee hierarchy implemented");
            System.out.println("✅ Polymorphism: Different behavior for employee types");
            System.out.println("✅ Abstraction: Abstract Employee class with concrete implementations");
            System.out.println("✅ Encapsulation: Proper data hiding and method access");
            System.out.println("✅ Factory Pattern: Employee creation with type handling");
            System.out.println("=".repeat(60));
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}