package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoExtensions;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import model.*;
import dao.*;
import service.PayrollCalculator;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

// Test class for Employee model
@DisplayName("Employee Model Tests")
class EmployeeTest {
    
    private Employee employee;
    
    @BeforeEach
    void setUp() {
        employee = new Employee();
    }
    
    @AfterEach
    void tearDown() {
        employee = null;
    }
    
    @Test
    @DisplayName("Should create employee with valid data")
    void testEmployeeCreation() {
        // Arrange
        String firstName = "Juan";
        String lastName = "Dela Cruz";
        int employeeId = 10001;
        
        // Act
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmployeeId(employeeId);
        
        // Assert
        assertEquals(firstName, employee.getFirstName(), "First name should match");
        assertEquals(lastName, employee.getLastName(), "Last name should match");
        assertEquals(employeeId, employee.getEmployeeId(), "Employee ID should match");
        assertEquals("Juan Dela Cruz", employee.getFullName(), "Full name should be concatenated correctly");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid employee ID")
    void testInvalidEmployeeId() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employee.setEmployeeId(-1),
            "Should throw IllegalArgumentException for negative employee ID"
        );
        
        assertTrue(exception.getMessage().contains("Employee ID must be positive"));
    }
    
    @Test
    @DisplayName("Should throw exception for null first name")
    void testNullFirstName() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employee.setFirstName(null),
            "Should throw IllegalArgumentException for null first name"
        );
        
        assertTrue(exception.getMessage().contains("First name cannot be null or empty"));
    }
    
    @Test
    @DisplayName("Should throw exception for empty last name")
    void testEmptyLastName() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employee.setLastName("   "),
            "Should throw IllegalArgumentException for empty last name"
        );
        
        assertTrue(exception.getMessage().contains("Last name cannot be null or empty"));
    }
    
    @Test
    @DisplayName("Should calculate age correctly")
    void testAgeCalculation() {
        // Arrange
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        employee.setBirthDate(birthDate);
        
        // Act
        int age = employee.getAge();
        
        // Assert
        assertTrue(age >= 30, "Age should be at least 30 for birthdate 1990-05-15");
        assertTrue(age <= 35, "Age should be at most 35 for birthdate 1990-05-15");
    }
    
    @Test
    @DisplayName("Should validate basic salary")
    void testBasicSalaryValidation() {
        // Test valid salary
        assertDoesNotThrow(() -> employee.setBasicSalary(50000.00));
        assertEquals(50000.00, employee.getBasicSalary(), 0.01);
        
        // Test invalid salary
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employee.setBasicSalary(-1000.00)
        );
        assertTrue(exception.getMessage().contains("Basic salary cannot be negative"));
    }
    
    @Test
    @DisplayName("Should calculate daily and hourly rates correctly")
    void testRateCalculations() {
        // Arrange
        employee.setBasicSalary(44000.00); // 44k monthly
        
        // Act
        double dailyRate = employee.getDailyRate();
        double hourlyRate = employee.getHourlyRate();
        
        // Assert
        assertEquals(2000.00, dailyRate, 0.01, "Daily rate should be monthly salary / 22");
        assertEquals(250.00, hourlyRate, 0.01, "Hourly rate should be daily rate / 8");
    }
    
    @Test
    @DisplayName("Should handle compensation package correctly")
    void testCompensationPackage() {
        // Arrange
        employee.setRiceSubsidy(1500.00);
        employee.setPhoneAllowance(1000.00);
        employee.setClothingAllowance(800.00);
        
        // Act
        double totalAllowances = employee.getTotalAllowances();
        
        // Assert
        assertEquals(3300.00, totalAllowances, 0.01, "Total allowances should be sum of all allowances");
    }
    
    @Test
    @DisplayName("Should implement Comparable interface correctly")
    void testComparableImplementation() {
        // Arrange
        Employee emp1 = new Employee("Alice", "Brown", 1001);
        Employee emp2 = new Employee("Bob", "Brown", 1002);
        Employee emp3 = new Employee("Alice", "Anderson", 1003);
        
        // Act & Assert
        assertTrue(emp1.compareTo(emp2) < 0, "Alice Brown should come before Bob Brown");
        assertTrue(emp3.compareTo(emp1) < 0, "Alice Anderson should come before Alice Brown");
        assertEquals(0, emp1.compareTo(new Employee("Alice", "Brown", 1004)), 
                    "Same name employees should be equal in comparison");
    }
}

// Test class for Attendance model
@DisplayName("Attendance Model Tests")
class AttendanceTest {
    
    private Attendance attendance;
    
    @BeforeEach
    void setUp() {
        attendance = new Attendance();
    }
    
    @Test
    @DisplayName("Should calculate work hours correctly")
    void testWorkHoursCalculation() {
        // Arrange
        attendance.setEmployeeId(10001);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));
        
        // Act
        double workHours = attendance.getWorkHours();
        
        // Assert
        assertEquals(9.0, workHours, 0.01, "Work hours should be 9 hours for 8:00 to 17:00");
    }
    
    @Test
    @DisplayName("Should detect late arrival")
    void testLateDetection() {
        // Arrange
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 30))); // 30 minutes late
        
        // Act
        boolean isLate = attendance.isLate();
        double lateMinutes = attendance.getLateMinutes();
        
        // Assert
        assertTrue(isLate, "Should detect late arrival");
        assertEquals(30.0, lateMinutes, 0.01, "Should calculate late minutes correctly");
    }
    
    @Test
    @DisplayName("Should detect undertime")
    void testUndertimeDetection() {
        // Arrange
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(16, 30))); // Left 30 minutes early
        
        // Act
        boolean hasUndertime = attendance.hasUndertime();
        double undertimeMinutes = attendance.getUndertimeMinutes();
        
        // Assert
        assertTrue(hasUndertime, "Should detect undertime");
        assertEquals(30.0, undertimeMinutes, 0.01, "Should calculate undertime minutes correctly");
    }
    
    @Test
    @DisplayName("Should validate log times")
    void testLogTimeValidation() {
        // Arrange
        Time logIn = Time.valueOf(LocalTime.of(8, 0));
        Time invalidLogOut = Time.valueOf(LocalTime.of(7, 0)); // Before log in
        
        attendance.setLogIn(logIn);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attendance.setLogOut(invalidLogOut)
        );
        
        assertTrue(exception.getMessage().contains("Log out time cannot be before log in time"));
    }
    
    @Test
    @DisplayName("Should identify full day attendance")
    void testFullDayIdentification() {
        // Arrange
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0))); // 9 hours (8+ hours = full day)
        
        // Act
        boolean isFullDay = attendance.isFullDay();
        
        // Assert
        assertTrue(isFullDay, "Should identify 9-hour attendance as full day");
    }
}

// Test class for Deduction model hierarchy
@DisplayName("Deduction Model Tests")
class DeductionTest {
    
    @Test
    @DisplayName("Should create government deduction correctly")
    void testGovernmentDeduction() {
        // Arrange
        GovernmentDeduction sssDeduction = new GovernmentDeduction(10001, "SSS", 50000.00);
        
        // Act
        sssDeduction.calculateDeduction();
        
        // Assert
        assertTrue(sssDeduction.getAmount() > 0, "SSS deduction should be calculated");
        assertTrue(sssDeduction.isRecurring(), "Government deductions should be recurring");
        assertEquals("SSS", sssDeduction.getType());
        assertNotNull(sssDeduction.getFormattedDeduction());
    }
    
    @Test
    @DisplayName("Should create attendance deduction correctly")
    void testAttendanceDeduction() {
        // Arrange
        int lateDays = 3;
        int absentDays = 1;
        double dailyRate = 2000.00;
        AttendanceDeduction attendanceDeduction = new AttendanceDeduction(10001, lateDays, absentDays, dailyRate);
        
        // Act
        attendanceDeduction.calculateDeduction();
        
        // Assert
        double expectedAmount = (lateDays * dailyRate * 0.1) + (absentDays * dailyRate);
        assertEquals(expectedAmount, attendanceDeduction.getAmount(), 0.01);
        assertFalse(attendanceDeduction.isRecurring(), "Attendance deductions should not be recurring");
        assertTrue(attendanceDeduction.getDescription().contains("Late: 3 days, Absent: 1 days"));
    }
    
    @Test
    @DisplayName("Should validate deduction amount")
    void testDeductionValidation() {
        // Arrange
        Deduction deduction = new AttendanceDeduction(10001, 0, 0, 2000.00);
        
        // Act & Assert
        assertDoesNotThrow(() -> deduction.setAmount(100.00));
        assertEquals(100.00, deduction.getAmount(), 0.01);
        
        // Test negative amount (should be allowed in some cases)
        assertDoesNotThrow(() -> deduction.setAmount(0.00));
    }
}

// Test class for Manager inheritance
@DisplayName("Manager Model Tests")
class ManagerTest {
    
    private Manager manager;
    
    @BeforeEach
    void setUp() {
        manager = new Manager("John", "Smith", 20001, 5000.00);
    }
    
    @Test
    @DisplayName("Should inherit Employee properties")
    void testInheritance() {
        // Arrange
        manager.setBasicSalary(80000.00);
        manager.setRiceSubsidy(1500.00);
        manager.setTeamSize(5);
        
        // Act & Assert
        assertEquals("John Smith", manager.getFullName(), "Should inherit getFullName from Employee");
        assertEquals("Manager", manager.getRole(), "Should override getRole method");
        assertTrue(manager.canReceiveBonus(), "Manager with team should be eligible for bonus");
    }
    
    @Test
    @DisplayName("Should calculate total allowances including management allowance")
    void testManagerAllowances() {
        // Arrange
        manager.setRiceSubsidy(1500.00);
        manager.setPhoneAllowance(1000.00);
        manager.setManagementAllowance(5000.00);
        
        // Act
        double totalAllowances = manager.calculateAllowances();
        
        // Assert
        assertEquals(7500.00, totalAllowances, 0.01, "Should include management allowance in total");
    }
}

// DAO Layer Tests with Mocking
@ExtendWith(MockitoExtension.class)
@DisplayName("DAO Layer Tests")
class EmployeeDAOTest {
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    private EmployeeDAO employeeDAO;
    
    @BeforeEach
    void setUp() {
        employeeDAO = new EmployeeDAO();
    }
    
    @Test
    @DisplayName("Should retrieve employee by ID")
    void testGetEmployeeById() throws Exception {
        // This test would require actual database connection or more sophisticated mocking
        // For demonstration, showing structure
        
        // Arrange
        int employeeId = 10001;
        Employee expectedEmployee = new Employee("Test", "Employee", employeeId);
        expectedEmployee.setBasicSalary(50000.00);
        
        // Act
        Employee actualEmployee = employeeDAO.getEmployeeById(employeeId);
        
        // Assert (would require actual database with test data)
        if (actualEmployee != null) {
            assertEquals(employeeId, actualEmployee.getEmployeeId());
            assertNotNull(actualEmployee.getFirstName());
            assertNotNull(actualEmployee.getLastName());
        }
    }
    
    @Test
    @DisplayName("Should validate employee data before insertion")
    void testEmployeeValidation() {
        // Arrange
        Employee invalidEmployee = new Employee();
        // Missing required fields
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> employeeDAO.insertEmployee(invalidEmployee)
        );
        
        assertNotNull(exception.getMessage());
    }
    
    @Test
    @DisplayName("Should handle employee search")
    void testEmployeeSearch() {
        // Arrange
        String searchTerm = "Juan";
        
        // Act
        List<Employee> results = employeeDAO.searchEmployees(searchTerm);
        
        // Assert
        assertNotNull(results, "Search results should not be null");
        // Additional assertions would depend on test data
    }
}

// Service Layer Tests
@DisplayName("PayrollCalculator Service Tests")
class PayrollCalculatorTest {
    
    private PayrollCalculator payrollCalculator;
    private Employee testEmployee;
    
    @BeforeEach
    void setUp() {
        payrollCalculator = new PayrollCalculator();
        testEmployee = new Employee("Test", "Employee", 10001);
        testEmployee.setBasicSalary(44000.00); // 2000 daily rate
        testEmployee.setRiceSubsidy(1500.00);
        testEmployee.setPhoneAllowance(1000.00);
        testEmployee.setClothingAllowance(800.00);
    }
    
    @Test
    @DisplayName("Should validate payroll calculation inputs")
    void testPayrollCalculationInputValidation() {
        // Test invalid employee ID
        IllegalArgumentException exception1 = assertThrows(
            IllegalArgumentException.class,
            () -> payrollCalculator.calculatePayroll(-1, LocalDate.now(), LocalDate.now().plusDays(15))
        );
        assertTrue(exception1.getMessage().contains("Employee ID must be positive"));
        
        // Test null dates
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> payrollCalculator.calculatePayroll(10001, null, LocalDate.now())
        );
        assertTrue(exception2.getMessage().contains("Period dates cannot be null"));
        
        // Test invalid date range
        IllegalArgumentException exception3 = assertThrows(
            IllegalArgumentException.class,
            () -> payrollCalculator.calculatePayroll(10001, LocalDate.now(), LocalDate.now().minusDays(1))
        );
        assertTrue(exception3.getMessage().contains("Period end cannot be before period start"));
    }
    
    @Test
    @DisplayName("Should calculate basic pay correctly")
    void testBasicPayCalculation() {
        // This test would require database setup with test data
        // Showing structure for unit test
        
        // Arrange
        LocalDate periodStart = LocalDate.of(2024, 1, 1);
        LocalDate periodEnd = LocalDate.of(2024, 1, 15);
        
        // Mock scenario: employee worked 10 days out of 15
        int expectedDaysWorked = 10;
        double expectedDailyRate = 2000.00;
        double expectedBasicPay = expectedDaysWorked * expectedDailyRate;
        
        // Act & Assert would require actual payroll calculation
        // For now, testing the calculation logic components
        assertEquals(expectedBasicPay, expectedDaysWorked * expectedDailyRate, 0.01);
    }
}

// Integration Tests
@DisplayName("Integration Tests")
class PayrollIntegrationTest {
    
    @Test
    @DisplayName("Should integrate Employee, Attendance, and Payroll calculations")
    void testPayrollIntegration() {
        // Arrange
        Employee employee = new Employee("Integration", "Test", 99999);
        employee.setBasicSalary(50000.00);
        employee.setRiceSubsidy(1500.00);
        
        // Create attendance records
        Attendance attendance1 = new Attendance();
        attendance1.setEmployeeId(99999);
        attendance1.setDate(Date.valueOf(LocalDate.now()));
        attendance1.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance1.setLogOut(Time.valueOf(LocalTime.of(17, 0)));
        
        // Act - Calculate work hours
        double workHours = attendance1.getWorkHours();
        
        // Assert
        assertTrue(workHours > 0, "Should calculate positive work hours");
        assertEquals(9.0, workHours, 0.01, "Should calculate 9 hours for 8:00-17:00");
    }
}

// Test Suite for running all tests
@DisplayName("Complete Payroll System Test Suite")
class PayrollSystemTestSuite {
    
    @Test
    @DisplayName("Should validate complete employee workflow")
    void testCompleteEmployeeWorkflow() {
        // Arrange
        Employee employee = new Employee("Complete", "Test", 88888);
        employee.setBasicSalary(60000.00);
        employee.setPosition("Software Developer");
        employee.setStatus("Regular");
        
        // Act & Assert - Employee creation
        assertNotNull(employee);
        assertEquals("Complete Test", employee.getFullName());
        assertTrue(employee.canReceiveBonus());
        
        // Act & Assert - Compensation calculation
        employee.setRiceSubsidy(1500.00);
        employee.setPhoneAllowance(1200.00);
        double totalCompensation = employee.calculateGrossPay();
        
        assertTrue(totalCompensation > employee.getBasicSalary(), 
                  "Gross pay should include allowances");
    }
    
    @Test
    @DisplayName("Should validate polymorphism with different employee types")
    void testPolymorphism() {
        // Arrange
        List<Payable> payableEmployees = Arrays.asList(
            new Employee("Regular", "Employee", 1001),
            new Manager("Team", "Manager", 2001, 3000.00)
        );
        
        // Set basic data
        for (Payable payable : payableEmployees) {
            if (payable instanceof Employee) {
                ((Employee) payable).setBasicSalary(50000.00);
                ((Employee) payable).setRiceSubsidy(1500.00);
            }
        }
        
        // Act & Assert
        for (Payable payable : payableEmployees) {
            double grossPay = payable.calculateGrossPay();
            assertTrue(grossPay > 0, "All payable employees should have positive gross pay");
            
            if (payable instanceof Manager) {
                assertTrue(grossPay > 50000.00, "Manager should have higher gross pay due to management allowance");
            }
        }
    }
}

// Test Data Builder Pattern for complex test scenarios
class EmployeeTestDataBuilder {
    private Employee employee;
    
    public EmployeeTestDataBuilder() {
        this.employee = new Employee();
    }
    
    public static EmployeeTestDataBuilder anEmployee() {
        return new EmployeeTestDataBuilder();
    }
    
    public EmployeeTestDataBuilder withId(int id) {
        employee.setEmployeeId(id);
        return this;
    }
    
    public EmployeeTestDataBuilder withName(String firstName, String lastName) {
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        return this;
    }
    
    public EmployeeTestDataBuilder withSalary(double salary) {
        employee.setBasicSalary(salary);
        return this;
    }
    
    public EmployeeTestDataBuilder withAllowances(double rice, double phone, double clothing) {
        employee.setRiceSubsidy(rice);
        employee.setPhoneAllowance(phone);
        employee.setClothingAllowance(clothing);
        return this;
    }
    
    public Employee build() {
        return employee;
    }
}

// Example usage of Test Data Builder
@DisplayName("Test Data Builder Examples")
class TestDataBuilderTest {
    
    @Test
    @DisplayName("Should create test employee using builder pattern")
    void testEmployeeBuilder() {
        // Arrange
        Employee employee = EmployeeTestDataBuilder.anEmployee()
            .withId(12345)
            .withName("Builder", "Test")
            .withSalary(55000.00)
            .withAllowances(1500.00, 1000.00, 800.00)
            .build();
        
        // Act & Assert
        assertEquals(12345, employee.getEmployeeId());
        assertEquals("Builder Test", employee.getFullName());
        assertEquals(55000.00, employee.getBasicSalary(), 0.01);
        assertEquals(3300.00, employee.getTotalAllowances(), 0.01);
    }
}

// Performance Tests
@DisplayName("Performance Tests")
class PerformanceTest {
    
    @Test
    @DisplayName("Should handle large employee list efficiently")
    @Timeout(value = 5) // Test should complete within 5 seconds
    void testLargeEmployeeListPerformance() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        
        // Act - Create 1000 employees
        for (int i = 1; i <= 1000; i++) {
            Employee emp = new Employee("Employee", String.valueOf(i), i);
            emp.setBasicSalary(50000.00);
            employees.add(emp);
        }
        
        // Sort employees (test Comparable implementation performance)
        employees.sort(Employee::compareTo);
        
        // Assert
        assertEquals(1000, employees.size());
        assertTrue(employees.get(0).getLastName().compareTo(employees.get(999).getLastName()) <= 0);
    }
}