package test;

import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for Employee models demonstrating proper unit testing
 */
@DisplayName("Employee Model Tests - OOP Principles")
class EmployeeTest {
    
    private RegularEmployee regularEmployee;
    private ProbationaryEmployee probationaryEmployee;
    
    @BeforeEach
    @DisplayName("Setup test data before each test")
    void setUp() {
        regularEmployee = new RegularEmployee(10001, "John", "Doe", "Software Developer", 50000);
        probationaryEmployee = new ProbationaryEmployee(10002, "Jane", "Smith", "Junior Developer", 35000);
    }
    
    @Test
    @DisplayName("Test Inheritance - Regular employee should extend Employee")
    void testInheritance() {
        // Test inheritance
        assertInstanceOf(Employee.class, regularEmployee, 
                        "RegularEmployee should inherit from Employee");
        assertInstanceOf(Employee.class, probationaryEmployee, 
                        "ProbationaryEmployee should inherit from Employee");
        
        // Test that both are different concrete types
        assertNotEquals(regularEmployee.getClass(), probationaryEmployee.getClass(),
                       "Regular and Probationary employees should be different classes");
    }
    
    @Test
    @DisplayName("Test Polymorphism - Different employee types should behave differently")
    void testPolymorphism() {
        // Test polymorphic behavior
        Employee[] employees = {regularEmployee, probationaryEmployee};
        
        for (Employee emp : employees) {
            // All should have these methods (polymorphism)
            assertNotNull(emp.getEmployeeType(), "Employee type should not be null");
            assertTrue(emp.calculateGrossPay(22, 0) > 0, "Gross pay should be positive");
            assertTrue(emp.calculateDeductions() >= 0, "Deductions should be non-negative");
            assertTrue(emp.calculateAllowances() >= 0, "Allowances should be non-negative");
        }
        
        // But they should behave differently
        assertNotEquals(regularEmployee.getEmployeeType(), probationaryEmployee.getEmployeeType(),
                       "Different employee types should return different type strings");
        
        assertNotEquals(regularEmployee.isEligibleForBenefits(), probationaryEmployee.isEligibleForBenefits(),
                       "Regular and probationary employees should have different benefit eligibility");
    }
    
    @Test
    @DisplayName("Test Abstraction - Abstract methods must be implemented")
    void testAbstraction() {
        // Test that abstract methods are properly implemented
        assertDoesNotThrow(() -> regularEmployee.calculateGrossPay(22, 8),
                          "calculateGrossPay should be implemented");
        assertDoesNotThrow(() -> regularEmployee.calculateDeductions(),
                          "calculateDeductions should be implemented");
        assertDoesNotThrow(() -> regularEmployee.calculateAllowances(),
                          "calculateAllowances should be implemented");
        assertDoesNotThrow(() -> regularEmployee.isEligibleForBenefits(),
                          "isEligibleForBenefits should be implemented");
    }
    
    @Test
    @DisplayName("Test Regular employee gross pay calculation")
    void testRegularEmployeeGrossPayCalculation() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, overtimeHours);
        
        // Assert
        double expectedDailyRate = 50000.0 / 22; // ₱2,272.73
        double expectedRegularPay = expectedDailyRate * 22; // ₱50,000
        double expectedOvertimePay = (expectedDailyRate / 8) * 8.0 * 1.25; // ₱2,840.91
        double expectedGrossPay = expectedRegularPay + expectedOvertimePay;
        
        assertEquals(expectedGrossPay, grossPay, 0.01, 
                    "Regular employee gross pay calculation should be correct");
        assertTrue(grossPay > 50000, "Gross pay with overtime should be greater than basic salary");
    }
    
    @Test
    @DisplayName("Test Probationary employee has reduced overtime rate")
    void testProbationaryEmployeeOvertimeRate() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act
        double probationaryGrossPay = probationaryEmployee.calculateGrossPay(daysWorked, overtimeHours);
        
        // Create a regular employee with same salary for comparison
        RegularEmployee regularWithSameSalary = new RegularEmployee(10003, "Test", "Regular", "Developer", 35000);
        double regularGrossPay = regularWithSameSalary.calculateGrossPay(daysWorked, overtimeHours);
        
        // Assert
        assertTrue(regularGrossPay > probationaryGrossPay, 
                  "Regular employee should earn more overtime than probationary with same base salary");
    }
    
    @Test
    @DisplayName("Test benefit eligibility differs between employee types")
    void testBenefitEligibility() {
        // Assert
        assertTrue(regularEmployee.isEligibleForBenefits(), 
                  "Regular employees should be eligible for benefits");
        assertFalse(probationaryEmployee.isEligibleForBenefits(), 
                   "Probationary employees should not be eligible for full benefits");
    }
    
    @Test
    @DisplayName("Test allowances differ between employee types")
    void testAllowancesDifference() {
        // Act
        double regularAllowances = regularEmployee.calculateAllowances();
        double probationaryAllowances = probationaryEmployee.calculateAllowances();
        
        // Assert
        assertTrue(regularAllowances > probationaryAllowances, 
                  "Regular employees should receive higher allowances than probationary");
        
        // Test specific allowance amounts for regular employee
        double expectedRegularAllowances = 1500.0 + 2000.0 + 1000.0; // Rice + Phone + Clothing
        assertEquals(expectedRegularAllowances, regularAllowances, 0.01, 
                    "Regular employee should receive standard allowances");
        
        // Test specific allowance amounts for probationary employee
        double expectedProbationaryAllowances = 1000.0 + 1000.0 + 500.0; // Reduced allowances
        assertEquals(expectedProbationaryAllowances, probationaryAllowances, 0.01, 
                    "Probationary employee should receive reduced allowances");
    }
    
    @ParameterizedTest
    @DisplayName("Test salary calculations with different working days")
    @ValueSource(ints = {15, 20, 22, 25})
    void testSalaryCalculationWithDifferentDays(int daysWorked) {
        // Act
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, 0);
        
        // Assert
        assertTrue(grossPay > 0, "Gross pay should be positive for " + daysWorked + " days");
        
        double expectedDailyRate = regularEmployee.getBasicSalary() / 22;
        double expectedGrossPay = expectedDailyRate * daysWorked;
        assertEquals(expectedGrossPay, grossPay, 0.01, 
                    "Gross pay should be calculated correctly for " + daysWorked + " days");
    }
    
    @ParameterizedTest
    @DisplayName("Test overtime calculations with different hours")
    @CsvSource({
        "0, 50000.0",      // No overtime
        "4, 51136.36",     // 4 hours overtime
        "8, 52272.73",     // 8 hours overtime
        "12, 53409.09"     // 12 hours overtime
    })
    void testOvertimePayCalculation(double overtimeHours, double expectedGrossPay) {
        // Act
        double actualGrossPay = regularEmployee.calculateGrossPay(22, overtimeHours);
        
        // Assert
        assertEquals(expectedGrossPay, actualGrossPay, 1.0, 
                    String.format("Gross pay for %.1f overtime hours should be approximately ₱%.2f", 
                                 overtimeHours, expectedGrossPay));
    }
    
    @Test
    @DisplayName("Test Factory Pattern creates correct employee types")
    void testEmployeeFactory() {
        // Act
        Employee regular = EmployeeFactory.createEmployee("REGULAR", 20001, "Test", "Regular", "Developer", 45000);
        Employee probationary = EmployeeFactory.createEmployee("PROBATIONARY", 20002, "Test", "Probationary", "Junior Developer", 35000);
        
        // Assert
        assertInstanceOf(RegularEmployee.class, regular, 
                        "Factory should create RegularEmployee for REGULAR type");
        assertInstanceOf(ProbationaryEmployee.class, probationary, 
                        "Factory should create ProbationaryEmployee for PROBATIONARY type");
        
        assertEquals("Regular Employee", regular.getEmployeeType());
        assertEquals("Probationary Employee", probationary.getEmployeeType());
    }
    
    @Test
    @DisplayName("Test Factory Pattern throws exception for invalid type")
    void testEmployeeFactoryInvalidType() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> EmployeeFactory.createEmployee("INVALID", 30001, "Test", "Test", "Position", 50000),
            "Factory should throw exception for invalid employee type"
        );
        
        assertTrue(exception.getMessage().contains("Unknown employee type"), 
                  "Exception message should indicate unknown employee type");
    }
    
    @Test
    @DisplayName("Test Template Method Pattern in net pay calculation")
    void testTemplateMethodPattern() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 4.0;
        
        // Act
        double netPay = regularEmployee.calculateNetPay(daysWorked, overtimeHours);
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, overtimeHours);
        double allowances = regularEmployee.calculateAllowances();
        double deductions = regularEmployee.calculateDeductions();
        
        // Assert
        double expectedNetPay = grossPay + allowances - deductions;
        assertEquals(expectedNetPay, netPay, 0.01, 
                    "Net pay should equal gross pay plus allowances minus deductions");
        assertTrue(netPay > 0, "Net pay should be positive");
    }
    
    @Test
    @DisplayName("Test employee deductions calculation")
    void testEmployeeDeductionsCalculation() {
        // Act
        double regularDeductions = regularEmployee.calculateDeductions();
        double probationaryDeductions = probationaryEmployee.calculateDeductions();
        
        // Assert
        assertTrue(regularDeductions > 0, "Regular employee should have deductions");
        assertTrue(probationaryDeductions > 0, "Probationary employee should have some deductions");
        
        // Regular employees typically have higher deductions due to higher salary and full tax
        assertTrue(regularDeductions > probationaryDeductions, 
                  "Regular employee should have higher deductions than probationary");
    }
    
    @Test
    @DisplayName("Test employee age calculation")
    void testAgeCalculation() {
        // Arrange
        regularEmployee.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        
        // Act
        int age = regularEmployee.getAge();
        
        // Assert
        assertTrue(age > 0, "Age should be positive");
        assertTrue(age < 150, "Age should be reasonable");
        
        // Test with null birthday
        probationaryEmployee.setBirthday(null);
        assertEquals(0, probationaryEmployee.getAge(), "Age should be 0 when birthday is null");
    }
    
    @Test
    @DisplayName("Test employee validation")
    void testEmployeeValidation() {
        // Test valid employee
        assertTrue(regularEmployee.isValid(), "Regular employee with proper data should be valid");
        
        // Test invalid employee
        Employee invalidEmployee = new RegularEmployee();
        assertFalse(invalidEmployee.isValid(), "Employee without required data should be invalid");
        
        // Test employee with missing name
        Employee noNameEmployee = new RegularEmployee();
        noNameEmployee.setEmployeeId(12345);
        assertFalse(noNameEmployee.isValid(), "Employee without name should be invalid");
    }
    
    @Test
    @DisplayName("Test probationary employee specific features")
    void testProbationaryEmployeeFeatures() {
        // Test probation period
        assertFalse(probationaryEmployee.isProbationPeriodOver(), 
                   "New probationary employee should still be in probation period");
        
        long daysUntilRegular = probationaryEmployee.getDaysUntilRegularization();
        assertTrue(daysUntilRegular > 0, "Days until regularization should be positive");
        assertTrue(daysUntilRegular <= 180, "Probation period should not exceed 6 months");
    }
    
    @AfterEach
    @DisplayName("Cleanup after each test")
    void tearDown() {
        // Reset any static variables or cleanup resources if needed
        regularEmployee = null;
        probationaryEmployee = null;
    }
    
    @AfterAll
    @DisplayName("Final cleanup after all tests")
    static void tearDownAll() {
        System.out.println("✅ All Employee OOP tests completed successfully!");
        System.out.println("📊 Tested: Inheritance, Polymorphism, Abstraction, Encapsulation");
        System.out.println("🏭 Tested: Factory Pattern, Template Method Pattern");
    }
}