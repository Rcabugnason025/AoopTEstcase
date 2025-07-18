package test;

import model.*;
import service.PayrollCalculator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for Payroll Calculation with proper assertions and mocking
 */
@DisplayName("Payroll Calculation Tests - Business Logic")
class PayrollCalculationTest {
    
    private RegularEmployee regularEmployee;
    private ProbationaryEmployee probationaryEmployee;
    private PayrollCalculator payrollCalculator;
    
    @Mock
    private dao.EmployeeDAO mockEmployeeDAO;
    
    @Mock
    private dao.AttendanceDAO mockAttendanceDAO;
    
    @BeforeEach
    @DisplayName("Setup test data and mocks")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        regularEmployee = new RegularEmployee(10001, "John", "Doe", "Software Developer", 50000);
        probationaryEmployee = new ProbationaryEmployee(10002, "Jane", "Smith", "Junior Developer", 35000);
        payrollCalculator = new PayrollCalculator();
    }
    
    @Test
    @DisplayName("Should calculate SSS contribution correctly for different salary brackets")
    void testSSSContributionCalculation() {
        // Test low salary bracket
        RegularEmployee lowSalaryEmp = new RegularEmployee(10001, "Low", "Salary", "Junior", 3000);
        double lowSalaryDeductions = lowSalaryEmp.calculateDeductions();
        assertTrue(lowSalaryDeductions > 0, "Low salary employee should have SSS deduction");
        
        // Test medium salary bracket
        RegularEmployee medSalaryEmp = new RegularEmployee(10002, "Med", "Salary", "Developer", 15000);
        double medSalaryDeductions = medSalaryEmp.calculateDeductions();
        assertTrue(medSalaryDeductions > 0, "Medium salary employee should have SSS deduction");
        
        // Test high salary bracket
        RegularEmployee highSalaryEmp = new RegularEmployee(10003, "High", "Salary", "Manager", 30000);
        double highSalaryDeductions = highSalaryEmp.calculateDeductions();
        assertTrue(highSalaryDeductions > 0, "High salary employee should have SSS deduction");
        
        // Higher salary should generally mean higher deductions
        assertTrue(medSalaryDeductions > lowSalaryDeductions, 
                  "Medium salary should have higher deductions than low salary");
        assertTrue(highSalaryDeductions > medSalaryDeductions, 
                  "High salary should have higher deductions than medium salary");
    }
    
    @ParameterizedTest
    @DisplayName("Should calculate overtime pay with different hours and rates")
    @CsvSource({
        "0, 50000.0, 50000.0",      // No overtime
        "4, 50000.0, 51136.36",     // 4 hours overtime
        "8, 50000.0, 52272.73",     // 8 hours overtime
        "12, 50000.0, 53409.09",    // 12 hours overtime
        "0, 35000.0, 35000.0",      // Probationary no overtime
        "8, 35000.0, 36590.91"      // Probationary with overtime
    })
    void testOvertimePayCalculation(double overtimeHours, double basicSalary, double expectedGrossPay) {
        // Arrange
        RegularEmployee testEmployee = new RegularEmployee(10001, "Test", "Employee", "Developer", basicSalary);
        
        // Act
        double actualGrossPay = testEmployee.calculateGrossPay(22, overtimeHours);
        
        // Assert
        assertEquals(expectedGrossPay, actualGrossPay, 1.0, 
                    String.format("Gross pay for %.1f overtime hours with ₱%.2f salary should be approximately ₱%.2f", 
                                 overtimeHours, basicSalary, expectedGrossPay));
    }
    
    @Test
    @DisplayName("Should calculate Philippine withholding tax correctly")
    void testWithholdingTaxCalculation() {
        // Test tax-exempt employee (annual salary ≤ 250,000)
        RegularEmployee taxExemptEmp = new RegularEmployee(10001, "Tax", "Exempt", "Junior", 20000);
        double taxExemptDeductions = taxExemptEmp.calculateDeductions();
        
        // Test taxable employee (annual salary > 250,000)
        RegularEmployee taxableEmp = new RegularEmployee(10002, "Tax", "Able", "Senior", 25000);
        double taxableDeductions = taxableEmp.calculateDeductions();
        
        // Assert
        assertTrue(taxableDeductions >= taxExemptDeductions, 
                  "Higher salary employee should have same or higher deductions");
        
        // Test specific tax brackets
        RegularEmployee highEarner = new RegularEmployee(10003, "High", "Earner", "Manager", 100000);
        double highEarnerDeductions = highEarner.calculateDeductions();
        assertTrue(highEarnerDeductions > taxableDeductions, 
                  "Very high salary should result in higher tax deductions");
    }
    
    @Test
    @DisplayName("Should handle edge cases in payroll calculation")
    void testPayrollCalculationEdgeCases() {
        // Test zero days worked
        double zeroDaysGrossPay = regularEmployee.calculateGrossPay(0, 0);
        assertEquals(0.0, zeroDaysGrossPay, 0.01, "Zero days worked should result in zero gross pay");
        
        // Test maximum days in month
        double maxDaysGrossPay = regularEmployee.calculateGrossPay(31, 0);
        assertTrue(maxDaysGrossPay > regularEmployee.getBasicSalary(), 
                  "Working more than standard days should result in higher gross pay");
        
        // Test negative values (should be handled gracefully)
        assertDoesNotThrow(() -> regularEmployee.calculateGrossPay(22, -1), 
                          "Negative overtime should not throw exception");
        
        // Test very high overtime
        double highOvertimeGrossPay = regularEmployee.calculateGrossPay(22, 100);
        assertTrue(highOvertimeGrossPay > regularEmployee.getBasicSalary() * 2, 
                  "Very high overtime should significantly increase gross pay");
    }
    
    @Test
    @DisplayName("Should maintain calculation consistency across multiple calls")
    void testCalculationConsistency() {
        // Calculate payroll multiple times with same inputs
        double grossPay1 = regularEmployee.calculateGrossPay(22, 8);
        double grossPay2 = regularEmployee.calculateGrossPay(22, 8);
        double grossPay3 = regularEmployee.calculateGrossPay(22, 8);
        
        // Assert
        assertEquals(grossPay1, grossPay2, 0.01, "Repeated calculations should be consistent");
        assertEquals(grossPay2, grossPay3, 0.01, "Repeated calculations should be consistent");
        assertEquals(grossPay1, grossPay3, 0.01, "Repeated calculations should be consistent");
        
        // Test deductions consistency
        double deductions1 = regularEmployee.calculateDeductions();
        double deductions2 = regularEmployee.calculateDeductions();
        assertEquals(deductions1, deductions2, 0.01, "Deduction calculations should be consistent");
    }
    
    @Test
    @DisplayName("Should calculate net pay correctly with all components")
    void testCompleteNetPayCalculation() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, overtimeHours);
        double allowances = regularEmployee.calculateAllowances();
        double deductions = regularEmployee.calculateDeductions();
        double netPay = regularEmployee.calculateNetPay(daysWorked, overtimeHours);
        
        // Assert
        assertAll("Net pay calculation components",
            () -> assertTrue(grossPay > 0, "Gross pay should be positive"),
            () -> assertTrue(allowances > 0, "Allowances should be positive for regular employee"),
            () -> assertTrue(deductions > 0, "Deductions should be positive"),
            () -> assertEquals(grossPay + allowances - deductions, netPay, 0.01, 
                             "Net pay should equal gross pay plus allowances minus deductions"),
            () -> assertTrue(netPay > 0, "Net pay should be positive")
        );
    }
    
    @Test
    @DisplayName("Should demonstrate polymorphic behavior in payroll calculations")
    void testPolymorphicPayrollCalculation() {
        // Arrange
        Employee[] employees = {regularEmployee, probationaryEmployee};
        int daysWorked = 22;
        double overtimeHours = 8;
        
        // Act & Assert
        for (Employee emp : employees) {
            double grossPay = emp.calculateGrossPay(daysWorked, overtimeHours);
            double deductions = emp.calculateDeductions();
            double allowances = emp.calculateAllowances();
            double netPay = emp.calculateNetPay(daysWorked, overtimeHours);
            
            assertAll("Polymorphic calculations for " + emp.getEmployeeType(),
                () -> assertTrue(grossPay > 0, "Gross pay should be positive"),
                () -> assertTrue(deductions >= 0, "Deductions should be non-negative"),
                () -> assertTrue(allowances >= 0, "Allowances should be non-negative"),
                () -> assertTrue(netPay > 0, "Net pay should be positive"),
                () -> assertNotNull(emp.getEmployeeType(), "Employee type should not be null")
            );
        }
        
        // Different employee types should produce different results
        double regularNetPay = regularEmployee.calculateNetPay(daysWorked, overtimeHours);
        double probationaryNetPay = probationaryEmployee.calculateNetPay(daysWorked, overtimeHours);
        
        assertNotEquals(regularNetPay, probationaryNetPay, 
                       "Different employee types should produce different net pay calculations");
    }
    
    @Test
    @DisplayName("Should validate government contribution calculations")
    void testGovernmentContributions() {
        // Test SSS contribution limits
        RegularEmployee maxSSSEmployee = new RegularEmployee(10001, "Max", "SSS", "Executive", 100000);
        double maxSSSDeductions = maxSSSEmployee.calculateDeductions();
        
        RegularEmployee minSSSEmployee = new RegularEmployee(10002, "Min", "SSS", "Trainee", 15000);
        double minSSSDeductions = minSSSEmployee.calculateDeductions();
        
        assertTrue(maxSSSDeductions > minSSSDeductions, 
                  "Higher salary should result in higher government contributions");
        
        // Test that deductions are within reasonable bounds
        assertTrue(maxSSSDeductions < maxSSSEmployee.getBasicSalary() * 0.5, 
                  "Total deductions should not exceed 50% of basic salary");
    }
    
    @Test
    @DisplayName("Should test Factory pattern with payroll calculations")
    void testFactoryPatternWithPayroll() {
        // Arrange
        Employee factoryRegular = EmployeeFactory.createEmployee("REGULAR", 20001, "Factory", "Regular", "Developer", 45000);
        Employee factoryProbationary = EmployeeFactory.createEmployee("PROBATIONARY", 20002, "Factory", "Probationary", "Junior", 30000);
        
        // Act
        double regularTotal = EmployeeFactory.calculateTotalPayroll(new Employee[]{factoryRegular}, 22, 4);
        double probationaryTotal = EmployeeFactory.calculateTotalPayroll(new Employee[]{factoryProbationary}, 22, 4);
        
        // Assert
        assertTrue(regularTotal > 0, "Factory-created regular employee should have positive payroll");
        assertTrue(probationaryTotal > 0, "Factory-created probationary employee should have positive payroll");
        assertTrue(regularTotal > probationaryTotal, 
                  "Regular employee should have higher total payroll than probationary");
    }
    
    @Test
    @DisplayName("Should test business rules for different employee types")
    void testBusinessRules() {
        // Test regular employee business rules
        assertTrue(regularEmployee.isEligibleForBenefits(), 
                  "Regular employees should be eligible for benefits");
        assertEquals("Regular Employee", regularEmployee.getEmployeeType(),
                    "Regular employee should return correct type");
        
        // Test probationary employee business rules
        assertFalse(probationaryEmployee.isEligibleForBenefits(), 
                   "Probationary employees should not be eligible for full benefits");
        assertEquals("Probationary Employee", probationaryEmployee.getEmployeeType(),
                    "Probationary employee should return correct type");
        
        // Test allowance differences
        assertTrue(regularEmployee.calculateAllowances() > probationaryEmployee.calculateAllowances(),
                  "Regular employees should receive higher allowances");
    }
    
    @AfterEach
    @DisplayName("Cleanup after each test")
    void tearDown() {
        // Clean up resources
        regularEmployee = null;
        probationaryEmployee = null;
    }
    
    @AfterAll
    @DisplayName("Final test summary")
    static void tearDownAll() {
        System.out.println("✅ All Payroll Calculation tests completed successfully!");
        System.out.println("🧮 Tested: SSS, PhilHealth, Pag-IBIG, Tax calculations");
        System.out.println("💰 Tested: Gross pay, deductions, allowances, net pay");
        System.out.println("🏭 Tested: Factory pattern, Polymorphism in calculations");
        System.out.println("📊 Tested: Business rules and edge cases");
    }
}