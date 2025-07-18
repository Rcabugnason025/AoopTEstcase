package model;

// Factory pattern for creating employees (DESIGN PATTERN)
public class EmployeeFactory {
    
    /**
     * Creates an employee based on the type specified
     * Demonstrates POLYMORPHISM - returns different concrete types as Employee
     */
    public static Employee createEmployee(String employeeType, int employeeId, 
                                        String firstName, String lastName, String position, 
                                        double basicSalary) {
        if (employeeType == null) {
            throw new IllegalArgumentException("Employee type cannot be null");
        }
        
        switch (employeeType.toUpperCase()) {
            case "REGULAR":
                return new RegularEmployee(employeeId, firstName, lastName, position, basicSalary);
            case "PROBATIONARY":
                return new ProbationaryEmployee(employeeId, firstName, lastName, position, basicSalary);
            default:
                throw new IllegalArgumentException("Unknown employee type: " + employeeType);
        }
    }
    
    /**
     * Creates an employee based on status from database
     */
    public static Employee createEmployeeFromStatus(String status, int employeeId, 
                                                   String firstName, String lastName, 
                                                   String position, double basicSalary) {
        if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(employeeId, firstName, lastName, position, basicSalary);
        } else {
            return new RegularEmployee(employeeId, firstName, lastName, position, basicSalary);
        }
    }
    
    /**
     * Polymorphic method demonstrating runtime behavior
     */
    public static void printEmployeeDetails(Employee employee) {
        System.out.println("Employee Type: " + employee.getEmployeeType());
        System.out.println("Benefits Eligible: " + employee.isEligibleForBenefits());
        System.out.println("Net Pay (22 days, 0 OT): " + employee.calculateNetPay(22, 0));
    }
    
    /**
     * Demonstrates polymorphism with different employee types
     */
    public static double calculateTotalPayroll(Employee[] employees, int daysWorked, double overtimeHours) {
        double totalPayroll = 0;
        for (Employee emp : employees) {
            totalPayroll += emp.calculateNetPay(daysWorked, overtimeHours);
        }
        return totalPayroll;
    }
}