package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

// Abstract base class demonstrating ABSTRACTION
public abstract class Employee {
    // Protected fields for inheritance (ENCAPSULATION)
    protected int employeeId;
    protected String firstName;
    protected String lastName;
    protected LocalDate birthday;
    protected String address;
    protected String phoneNumber;
    protected String sssNumber;
    protected String philhealthNumber;
    protected String tinNumber;
    protected String pagibigNumber;
    protected String status;
    protected String position;
    protected String department;
    protected String immediateSupervisor;
    protected double basicSalary;
    protected double riceSubsidy;
    protected double phoneAllowance;
    protected double clothingAllowance;
    protected double grossSemiMonthlyRate;
    protected double hourlyRate;
    protected int positionId;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Default constructor
    public Employee() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with basic information
    public Employee(int employeeId, String firstName, String lastName, String position, double basicSalary) {
        this();
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.basicSalary = basicSalary;
        this.status = "Regular";
    }

    // Abstract methods - MUST be implemented by subclasses (ABSTRACTION)
    public abstract double calculateGrossPay(int daysWorked, double overtimeHours);
    public abstract double calculateDeductions();
    public abstract double calculateAllowances();
    public abstract boolean isEligibleForBenefits();

    // Template method pattern - defines algorithm structure (POLYMORPHISM)
    public final double calculateNetPay(int daysWorked, double overtimeHours) {
        double grossPay = calculateGrossPay(daysWorked, overtimeHours);
        double deductions = calculateDeductions();
        double allowances = calculateAllowances();
        return grossPay + allowances - deductions;
    }

    // Polymorphic method - can be overridden
    public String getEmployeeType() {
        return "General Employee";
    }

    // Business logic methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAge() {
        if (birthday == null) return 0;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public double getDailyRate() {
        return basicSalary / 22.0; // 22 working days per month
    }

    public double getCalculatedHourlyRate() {
        return getDailyRate() / 8.0; // 8 hours per day
    }

    // Attendance utility methods
    public boolean isFullDay() {
        return true; // Default implementation
    }

    public boolean isLate() {
        return false; // Default implementation
    }

    public boolean hasUndertime() {
        return false; // Default implementation
    }

    public double getWorkHours() {
        return 8.0; // Default implementation
    }

    // Getters and setters with proper encapsulation
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { 
        this.employeeId = employeeId; 
        touch();
    }

    public int getId() { return employeeId; }
    public void setId(int id) { 
        this.employeeId = id; 
        touch();
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
        touch();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
        touch();
    }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { 
        this.birthday = birthday; 
        touch();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { 
        this.address = address; 
        touch();
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
        touch();
    }

    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { 
        this.sssNumber = sssNumber; 
        touch();
    }

    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { 
        this.philhealthNumber = philhealthNumber; 
        touch();
    }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { 
        this.tinNumber = tinNumber; 
        touch();
    }

    public String getPagibigNumber() { return pagibigNumber; }
    public void setPagibigNumber(String pagibigNumber) { 
        this.pagibigNumber = pagibigNumber; 
        touch();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        touch();
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { 
        this.position = position; 
        touch();
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { 
        this.department = department; 
        touch();
    }

    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) { 
        this.immediateSupervisor = immediateSupervisor; 
        touch();
    }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { 
        this.basicSalary = basicSalary; 
        touch();
    }

    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { 
        this.riceSubsidy = riceSubsidy; 
        touch();
    }

    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { 
        this.phoneAllowance = phoneAllowance; 
        touch();
    }

    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { 
        this.clothingAllowance = clothingAllowance; 
        touch();
    }

    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) { 
        this.grossSemiMonthlyRate = grossSemiMonthlyRate; 
        touch();
    }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { 
        this.hourlyRate = hourlyRate; 
        touch();
    }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { 
        this.positionId = positionId; 
        touch();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    protected void touch() { 
        this.updatedAt = LocalDateTime.now(); 
    }

    public boolean isValid() {
        return employeeId > 0 && 
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty();
    }

    public String getDisplayName() {
        return getFullName() + " (" + employeeId + ")";
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", name='" + getFullName() + '\'' +
                ", position='" + position + '\'' +
                ", status='" + status + '\'' +
                ", basicSalary=" + basicSalary +
                '}';
    }
}