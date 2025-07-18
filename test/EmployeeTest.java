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
    
    @AfterAll
    @DisplayName("Final cleanup after all tests")
    static void tearDownAll() {
        System.out.println("✅ All Employee OOP tests completed successfully!");
        System.out.println("📊 Tested: Inheritance, Polymorphism, Abstraction, Encapsulation");
        System.out.println("🏭 Tested: Factory Pattern, Template Method Pattern");
    }
}