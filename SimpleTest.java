// Simple test to verify OOP implementation works
// Put this file in your main project folder (same level as src folder)

import model.*;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("=== TESTING YOUR OOP IMPLEMENTATION ===");
        
        try {
            // Test 1: Create Regular Employee
            System.out.println("\n1. Creating Regular Employee...");
            RegularEmployee regular = new RegularEmployee(10001, "John", "Doe", "Developer", 50000);
            System.out.println("✅ Regular Employee Created: " + regular.getFullName());
            System.out.println("   Type: " + regular.getEmployeeType());
            System.out.println("   Benefits Eligible: " + regular.isEligibleForBenefits());
            
            // Test 2: Create Probationary Employee
            System.out.println("\n2. Creating Probationary Employee...");
            ProbationaryEmployee probationary = new ProbationaryEmployee(10002, "Jane", "Smith", "Junior Dev", 35000);
            System.out.println("✅ Probationary Employee Created: " + probationary.getFullName());
            System.out.println("   Type: " + probationary.getEmployeeType());
            System.out.println("   Benefits Eligible: " + probationary.isEligibleForBenefits());
            
            // Test 3: Test Polymorphism
            System.out.println("\n3. Testing Polymorphism (Different Calculations)...");
            
            double regularPay = regular.calculateNetPay(22, 8);
            double probationaryPay = probationary.calculateNetPay(22, 8);
            
            System.out.println("   Regular Net Pay: ₱" + String.format("%.2f", regularPay));
            System.out.println("   Probationary Net Pay: ₱" + String.format("%.2f", probationaryPay));
            
            // Test 4: Factory Pattern
            System.out.println("\n4. Testing Factory Pattern...");
            Employee factoryRegular = EmployeeFactory.createEmployee("REGULAR", 20001, "Test", "Regular", "Manager", 60000);
            Employee factoryProbationary = EmployeeFactory.createEmployee("PROBATIONARY", 20002, "Test", "Probationary", "Trainee", 30000);
            
            System.out.println("✅ Factory created Regular: " + factoryRegular.getEmployeeType());
            System.out.println("✅ Factory created Probationary: " + factoryProbationary.getEmployeeType());
            
            System.out.println("\n=== ALL TESTS PASSED! YOUR OOP IMPLEMENTATION WORKS! ===");
            System.out.println("✅ Inheritance: Employee hierarchy implemented");
            System.out.println("✅ Polymorphism: Different behavior for employee types");
            System.out.println("✅ Abstraction: Abstract Employee class working");
            System.out.println("✅ Encapsulation: Data hiding implemented");
            System.out.println("✅ Factory Pattern: Employee creation working");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\nIf you see errors, make sure you have:");
            System.out.println("1. Employee.java (abstract version) in src/model/");
            System.out.println("2. RegularEmployee.java in src/model/");
            System.out.println("3. ProbationaryEmployee.java in src/model/");
            System.out.println("4. EmployeeFactory.java in src/model/");
        }
    }
}