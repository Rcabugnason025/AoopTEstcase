package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import dao.PayrollDAO;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import model.Payroll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced PayrollCalculator using polymorphism and stored procedures
 */
public class PayrollCalculator {
    private static final Logger LOGGER = Logger.getLogger(PayrollCalculator.class.getName());

    // Constants for payroll calculations
    private static final int STANDARD_WORKING_DAYS_PER_MONTH = 22;
    private static final int STANDARD_WORKING_HOURS_PER_DAY = 8;
    private static final LocalTime STANDARD_LOGIN_TIME = LocalTime.of(8, 0);
    private static final LocalTime LATE_THRESHOLD_TIME = LocalTime.of(8, 15);
    private static final LocalTime STANDARD_LOGOUT_TIME = LocalTime.of(17, 0);

    // DAO instances
    private final EmployeeDAO employeeDAO;
    private final AttendanceDAO attendanceDAO;
    private final LeaveRequestDAO leaveDAO;
    private final PayrollDAO payrollDAO;

    public PayrollCalculator() {
        this.employeeDAO = new EmployeeDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.leaveDAO = new LeaveRequestDAO();
        this.payrollDAO = new PayrollDAO();
    }

    /**
     * Calculate payroll using polymorphism and stored procedures
     */
    public Payroll calculatePayroll(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {

        try {
            validateInputs(employeeId, periodStart, periodEnd);

            // Get employee using polymorphism
            Employee employee = employeeDAO.getEmployeeWithPositionDetails(employeeId);
            if (employee == null) {
                throw new PayrollCalculationException("Employee not found with ID: " + employeeId);
            }

            // Try to use stored procedure first
            try {
                Map<String, Object> storedProcResult = payrollDAO.generatePayslipData(employeeId, periodStart, periodEnd);
                if (!storedProcResult.isEmpty()) {
                    return createPayrollFromStoredProcedure(storedProcResult, employee, periodStart, periodEnd);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Stored procedure failed, falling back to manual calculation", e);
            }

            // Fallback to manual calculation using polymorphism
            return calculatePayrollManually(employee, periodStart, periodEnd);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to calculate payroll for employee " + employeeId, e);
            throw new PayrollCalculationException("Failed to calculate payroll: " + e.getMessage(), e);
        }
    }

    /**
     * Create payroll from stored procedure result
     */
    private Payroll createPayrollFromStoredProcedure(Map<String, Object> data, Employee employee, 
                                                    LocalDate periodStart, LocalDate periodEnd) {
        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employee.getEmployeeId());
        payroll.setPeriodStart(java.sql.Date.valueOf(periodStart));
        payroll.setPeriodEnd(java.sql.Date.valueOf(periodEnd));
        
        // Get data from stored procedure
        payroll.setMonthlyRate(((Number) data.get("basic_salary")).doubleValue());
        payroll.setGrossPay(((Number) data.get("gross_income")).doubleValue());
        payroll.setSss(((Number) data.get("sss_contribution")).doubleValue());
        payroll.setPhilhealth(((Number) data.get("philhealth_contribution")).doubleValue());
        payroll.setPagibig(((Number) data.get("pagibig_contribution")).doubleValue());
        payroll.setTax(((Number) data.get("withholding_tax")).doubleValue());
        payroll.setTotalDeductions(((Number) data.get("total_deductions")).doubleValue());
        payroll.setNetPay(((Number) data.get("net_income")).doubleValue());

        // Calculate attendance-based data
        calculateAttendanceData(payroll, employee, periodStart, periodEnd);

        return payroll;
    }

    /**
     * Manual calculation using polymorphism
     */
    private Payroll calculatePayrollManually(Employee employee, LocalDate periodStart, LocalDate periodEnd) {
        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employee.getEmployeeId());
        payroll.setPeriodStart(java.sql.Date.valueOf(periodStart));
        payroll.setPeriodEnd(java.sql.Date.valueOf(periodEnd));
        payroll.setMonthlyRate(employee.getBasicSalary());

        // Calculate attendance-based data
        calculateAttendanceData(payroll, employee, periodStart, periodEnd);

        // Use polymorphism for calculations
        double grossPay = employee.calculateGrossPay(payroll.getDaysWorked(), payroll.getOvertimeHours());
        double deductions = employee.calculateDeductions();
        double allowances = employee.calculateAllowances();

        payroll.setGrossPay(grossPay);
        payroll.setTotalDeductions(deductions);

        // Set allowances
        payroll.setRiceSubsidy(employee.getRiceSubsidy());
        payroll.setPhoneAllowance(employee.getPhoneAllowance());
        payroll.setClothingAllowance(employee.getClothingAllowance());

        // Calculate time-based deductions
        calculateTimeBasedDeductions(payroll, employee, periodStart, periodEnd);

        // Calculate final net pay
        payroll.setNetPay(grossPay + allowances - payroll.getTotalDeductions());

        return payroll;
    }

    /**
     * Calculate attendance-based earnings
     */
    private void calculateAttendanceData(Payroll payroll, Employee employee, 
                                        LocalDate periodStart, LocalDate periodEnd) {
        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employee.getEmployeeId(), periodStart, periodEnd);

        int validDays = 0;
        double totalHours = 0.0;
        double overtimeHours = 0.0;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogIn() != null) {
                validDays++;
                double workHours = attendance.getWorkHours();
                totalHours += workHours;
                
                // Calculate overtime (hours over 8 per day)
                if (workHours > 8) {
                    overtimeHours += (workHours - 8);
                }
            }
        }

        payroll.setDaysWorked(validDays);
        payroll.setOvertimeHours(overtimeHours);
        
        // Set daily rate
        double dailyRate = employee.getBasicSalary() / STANDARD_WORKING_DAYS_PER_MONTH;
        payroll.setGrossEarnings(validDays * dailyRate);

        LOGGER.info(String.format("Employee %d worked %d days, %.2f total hours, %.2f overtime hours",
                employee.getEmployeeId(), validDays, totalHours, overtimeHours));
    }

    /**
     * Calculate time-based deductions (late, undertime, unpaid leave)
     */
    private void calculateTimeBasedDeductions(Payroll payroll, Employee employee,
                                              LocalDate periodStart, LocalDate periodEnd) {
        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employee.getEmployeeId(), periodStart, periodEnd);

        double hourlyRate = employee.getBasicSalary() / STANDARD_WORKING_DAYS_PER_MONTH / STANDARD_WORKING_HOURS_PER_DAY;
        
        double lateDeduction = calculateLateDeduction(attendanceList, hourlyRate);
        double undertimeDeduction = calculateUndertimeDeduction(attendanceList, hourlyRate);
        double unpaidLeaveDeduction = calculateUnpaidLeaveDeduction(employee.getEmployeeId(), periodStart, periodEnd, hourlyRate * 8);

        payroll.setLateDeduction(lateDeduction);
        payroll.setUndertimeDeduction(undertimeDeduction);
        payroll.setUnpaidLeaveDeduction(unpaidLeaveDeduction);

        // Update total deductions
        double currentDeductions = payroll.getTotalDeductions();
        payroll.setTotalDeductions(currentDeductions + lateDeduction + undertimeDeduction + unpaidLeaveDeduction);
    }

    /**
     * Calculate late deduction
     */
    private double calculateLateDeduction(List<Attendance> attendanceList, double hourlyRate) {
        double totalLateDeduction = 0.0;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogIn() != null) {
                LocalTime loginTime = attendance.getLogIn().toLocalTime();
                if (loginTime.isAfter(LATE_THRESHOLD_TIME)) {
                    long minutesLate = ChronoUnit.MINUTES.between(STANDARD_LOGIN_TIME, loginTime);
                    double hoursLate = minutesLate / 60.0;
                    totalLateDeduction += hoursLate * hourlyRate;
                }
            }
        }

        return totalLateDeduction;
    }

    /**
     * Calculate undertime deduction
     */
    private double calculateUndertimeDeduction(List<Attendance> attendanceList, double hourlyRate) {
        double totalUndertimeDeduction = 0.0;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogOut() != null) {
                LocalTime logoutTime = attendance.getLogOut().toLocalTime();
                if (logoutTime.isBefore(STANDARD_LOGOUT_TIME)) {
                    long minutesShort = ChronoUnit.MINUTES.between(logoutTime, STANDARD_LOGOUT_TIME);
                    double hoursShort = minutesShort / 60.0;
                    totalUndertimeDeduction += hoursShort * hourlyRate;
                }
            }
        }

        return totalUndertimeDeduction;
    }

    /**
     * Calculate unpaid leave deduction
     */
    private double calculateUnpaidLeaveDeduction(int employeeId, LocalDate periodStart, LocalDate periodEnd, double dailyRate) {
        try {
            List<LeaveRequest> approvedLeaves = leaveDAO.getApprovedLeavesByEmployeeIdAndDateRange(
                    employeeId, periodStart, periodEnd);

            int unpaidLeaveDays = 0;
            for (LeaveRequest leave : approvedLeaves) {
                if ("Unpaid".equalsIgnoreCase(leave.getLeaveType())) {
                    unpaidLeaveDays += leave.getLeaveDays();
                }
            }

            return unpaidLeaveDays * dailyRate;
        } catch (Exception e) {
            LOGGER.warning("Error calculating unpaid leave deduction: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Validation methods
     */
    private void validateInputs(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {
        if (employeeId <= 0) {
            throw new PayrollCalculationException("Invalid employee ID: " + employeeId);
        }
        if (periodStart == null || periodEnd == null) {
            throw new PayrollCalculationException("Period dates cannot be null");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new PayrollCalculationException("Period end cannot be before period start");
        }
    }

    /**
     * Custom exception for payroll calculation errors
     */
    public static class PayrollCalculationException extends Exception {
        public PayrollCalculationException(String message) {
            super(message);
        }

        public PayrollCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}