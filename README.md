# MotorPH Payroll System

## 🚀 Advanced Object-Oriented Programming Project
**Course:** MO-IT113 - Advanced Object-Oriented Programming  
**Institution:** Mapúa Malayan Digital College  
**Academic Year:** 2024-2025

## 👥 Development Team - Group 6 (Section A2101)
- **Baguio, Gilliane Rose**
- **Celocia, Jannine Claire**  
- **Cabugnason, Rick**
- **Decastillo, Pamela Loraine**
- **Manalaysay, Rejoice**

## 📋 Project Overview

The MotorPH Payroll System is a comprehensive Java-based desktop application designed to manage employee payroll, attendance, and HR operations for MotorPH company. The system demonstrates advanced Object-Oriented Programming principles including proper inheritance, polymorphism, abstraction, and encapsulation.

### 🎯 Key Features

#### Object-Oriented Programming Implementation
- **Inheritance**: Employee hierarchy with RegularEmployee and ProbationaryEmployee classes
- **Polymorphism**: Different behavior for different employee types in payroll calculations
- **Abstraction**: Abstract Employee base class with concrete implementations
- **Encapsulation**: Proper data hiding and controlled access to class members
- **Design Patterns**: Factory pattern for employee creation, Template method pattern for payroll calculations

#### Database Design (3NF Compliant)
- **Normalized Database**: Properly structured database following Third Normal Form (3NF)
- **Views**: `v_employee_details` for simplified data access
- **Stored Procedures**: `sp_add_new_employee` and `sp_generate_payslip_data` for business logic encapsulation
- **Referential Integrity**: Proper foreign key relationships and constraints

#### Employee Management
- **Complete Employee Records**: Personal information, employment status, position details
- **Role-Based Access Control**: Separate dashboards for employees and HR personnel
- **Employee Search & Filtering**: Advanced search capabilities with multiple criteria
- **Password Management**: Secure credential handling with change password functionality

#### Payroll Processing
- **Polymorphic Payroll Calculation**: Different calculation methods for Regular vs Probationary employees
- **Automated Payroll Calculation**: Comprehensive salary computation including:
  - Basic salary calculation based on days worked
  - Overtime pay with different rates for employee types
  - Government contributions (SSS, PhilHealth, Pag-IBIG)
  - Tax calculations using TRAIN law brackets
  - Allowances (Rice subsidy, Phone allowance, Clothing allowance)
  - Time-based deductions (Late, Undertime, Unpaid leave)
- **JasperReports Integration**: Professional PDF payslip generation using MotorPH template
- **Print & Export**: Print payslips or save for records

#### Attendance Tracking
- **Digital Time Logging**: Log in/log out time tracking
- **Attendance Analytics**: Work hours calculation, late/undertime detection
- **Attendance Reports**: Comprehensive attendance summaries and statistics
- **Bulk Attendance Management**: HR can manage attendance for all employees

#### Leave Management
- **Leave Request System**: Employee self-service leave application
- **Approval Workflow**: HR approval/rejection with status tracking
- **Leave Types**: Annual, Sick, Emergency, Maternity, Paternity leave
- **Overlap Detection**: Prevents conflicting leave requests

#### Reporting & Analytics
- **JasperReports PDF Generation**: Professional reports using MotorPH template
- **Monthly Payroll Reports**: Comprehensive payroll summaries
- **Export Options**: PDF, CSV and HTML export formats
- **Detailed Attendance Reports**: Attendance summary of all employees
- **Government Contributions Reports**: SSS, PhilHealth, and Pag-IBIG reports

#### Unit Testing
- **JUnit 5 Integration**: Proper unit testing framework implementation
- **Comprehensive Test Coverage**: Tests for OOP principles, business logic, and edge cases
- **Assertion-Based Testing**: Proper use of JUnit assertions and test annotations
- **Mocking Support**: Mockito integration for isolated unit testing

## 🛠️ Installation & Setup

### Prerequisites
- **Java Development Kit (JDK) 11 or higher**
- **MySQL Server 8.0+**
- **MySQL Workbench** (recommended)
- **JasperReports Library** (included in dependencies)
- **JUnit 5** (for running tests)

### Database Setup

1. **Download the SQL Setup Script**
   ```
   File: aoopdatabase_payroll.sql
   Location: src/util/aoopdatabase_payroll.sql
   ```

2. **Configure MySQL Connection**
   - **Host**: localhost
   - **Port**: 3306
   - **Username**: root
   - **Password**: admin
   - **Database**: aoopdatabase_payroll

3. **Execute Setup Script**
   - Open MySQL Workbench
   - Connect to your MySQL server
   - Open and execute `aoopdatabase_payroll.sql`
   - Verify successful creation of database, tables, views, and stored procedures

4. **Verify Installation**
   ```sql
   USE aoopdatabase_payroll;
   SELECT COUNT(*) FROM employees; -- Should return 34
   SELECT COUNT(*) FROM credentials; -- Should return 34
   SELECT COUNT(*) FROM attendance; -- Should return sample data
   SELECT COUNT(*) FROM positions; -- Should return 22 positions
   
   -- Test the view
   SELECT * FROM v_employee_details LIMIT 5;
   
   -- Test stored procedure
   CALL sp_generate_payslip_data(10001, '2024-06-01', '2024-06-30');
   ```

### Application Setup

1. **Clone/Download Project**
   ```bash
   git clone <repository-url>
   cd motorph-payroll-system
   ```

2. **Add Dependencies**
   - Ensure `mysql-connector-java.jar` is in classpath
   - Add JasperReports JAR files for PDF generation
   - JUnit 5 dependencies for testing

3. **Configure Database Connection**
   - Update `src/util/DBConnection.java` if needed
   - Default configuration should work with the provided setup

4. **Compile and Run**
   ```bash
   # Using Maven (if pom.xml is configured)
   mvn clean compile exec:java -Dexec.mainClass="ui.MainApplication"
   
   # Or using command line
   javac -cp ".:lib/*" src/**/*.java
   java -cp ".:lib/*:src" ui.MainApplication
   ```

5. **Run Tests**
   ```bash
   # Run all JUnit tests
   java -cp ".:lib/*:src:test" test.TestRunner
   
   # Or using Maven
   mvn test
   ```

## 🔐 Default Login Credentials

The system comes with pre-configured test accounts:

### Employee Accounts
- **Employee IDs**: 10001 to 10034
- **Default Password**: `password1234`

### Sample HR/Management Accounts
- **Employee ID**: 10001 (CEO) - Access to HR Dashboard
- **Employee ID**: 10002 (COO) - Access to HR Dashboard
- **Employee ID**: 10003 (CFO) - Access to HR Dashboard
- **Employee ID**: 10006 (HR Manager) - Access to HR Dashboard
- **Password**: `password1234`

## 🏗️ Architecture & OOP Implementation

### Class Hierarchy (Inheritance)
```
BaseEntity (Abstract)
├── Employee (Abstract)
│   ├── RegularEmployee (Concrete)
│   └── ProbationaryEmployee (Concrete)
├── Attendance (Concrete)
├── LeaveRequest (Concrete)
└── Payroll (Concrete)
```

### Polymorphism Examples
- **Employee Types**: Different payroll calculation methods for Regular vs Probationary employees
- **Factory Pattern**: `EmployeeFactory.createEmployee()` returns different concrete types
- **Template Method**: `Employee.calculateNetPay()` uses different implementations of abstract methods

### Abstraction
- **Abstract Employee Class**: Defines contract for all employee types
- **Abstract Methods**: `calculateGrossPay()`, `calculateDeductions()`, `calculateAllowances()`, `isEligibleForBenefits()`
- **Interface-like Behavior**: Consistent API across different employee implementations

### Encapsulation
- **Private Fields**: Internal data hidden from external access
- **Protected Fields**: Accessible to subclasses only
- **Public Methods**: Controlled access to functionality
- **Validation**: Input validation in setters and business methods

## 📊 Database Schema (3NF)

### Normalized Tables
- **positions**: Eliminates transitive dependencies from employees table
- **employees**: References positions table, uses supervisor_id for hierarchy
- **credentials**: Separate authentication data
- **attendance**: Time tracking with proper constraints
- **leave_requests**: Leave management with status tracking

### Views
- **v_employee_details**: Denormalized view for easy querying

### Stored Procedures
- **sp_add_new_employee**: Safe employee creation with credentials
- **sp_generate_payslip_data**: Centralized payroll calculation logic

## 📄 JasperReports Integration

### MotorPH Template
- **Template File**: `motorph_payslip.jrxml` in resources
- **Company Branding**: MotorPH logo and company information
- **Professional Layout**: Structured payslip format
- **PDF Generation**: High-quality PDF output

### Report Features
- **Employee Information**: Complete employee details
- **Earnings Breakdown**: Detailed salary components
- **Deductions Summary**: Government contributions and taxes
- **Company Header**: MotorPH branding and contact information
- **Professional Footer**: Generated timestamp and validation

## 🧪 Unit Testing

### JUnit 5 Implementation
- **Test Classes**: Comprehensive test coverage for all major components
- **Assertions**: Proper use of JUnit assertions (`assertEquals`, `assertTrue`, etc.)
- **Test Lifecycle**: `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`
- **Parameterized Tests**: Testing with multiple input values
- **Conditional Tests**: Tests that run only when dependencies are available

### Test Coverage
- **OOP Principles**: Tests for inheritance, polymorphism, abstraction
- **Business Logic**: Payroll calculations, tax computations, allowances
- **Edge Cases**: Boundary conditions, error handling, validation
- **Integration**: JasperReports functionality, database operations

### Running Tests
```bash
# Run all tests with detailed output
java -cp ".:lib/*:src:test" test.TestRunner

# Expected output:
# ✅ All tests passed
# 📊 Tested: Inheritance, Polymorphism, Abstraction, Encapsulation
# 🏭 Tested: Factory Pattern, Template Method Pattern
# 💰 Tested: Payroll calculations and business logic
# 📄 Tested: JasperReports integration
```

## 🚀 Future Enhancements

### Planned Features
- **Enhanced Reporting System**: 
  - Employee performance dashboards
  - Custom report builder
- **Web-based Interface**: Browser-accessible application
- **Biometric Integration**: Fingerprint/face recognition for attendance
- **Mobile Application**: Mobile app for employee self-service
- **Advanced Analytics**: Business intelligence dashboards
- **API Integration**: External system integrations
- **Cloud Deployment**: Cloud-based hosting options

## 📈 Mentor Feedback Implementation

### ✅ Completed Improvements
1. **OOP Principles**: Implemented proper inheritance, polymorphism, abstraction, and encapsulation
2. **Database Normalization**: Upgraded to 3NF with views and stored procedures
3. **JasperReports**: Integrated proper PDF generation using MotorPH template
4. **Unit Testing**: Implemented JUnit 5 with proper assertions and test structure
5. **Code Organization**: Improved package structure and separation of concerns

### 🔧 Technical Improvements
- **Factory Pattern**: Employee creation with proper type handling
- **Template Method Pattern**: Consistent payroll calculation algorithm
- **Stored Procedures**: Database-level business logic encapsulation
- **Views**: Simplified data access with proper joins
- **Exception Handling**: Comprehensive error handling and logging

## 📄 License

Academic use only. Developed by Group 6, Mapúa Malayan Digital College.

**© 2025 MotorPH Payroll System - Enhanced with OOP Principles**