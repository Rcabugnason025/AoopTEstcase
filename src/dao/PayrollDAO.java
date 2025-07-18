package dao;

import util.DBConnection;
import model.Payroll;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DAO for payroll operations using stored procedures where applicable
 */
public class PayrollDAO {
    private static final Logger LOGGER = Logger.getLogger(PayrollDAO.class.getName());

    /**
     * Generate payslip data using stored procedure
     */
    public Map<String, Object> generatePayslipData(int employeeId, LocalDate startDate, LocalDate endDate) {
        String sql = "CALL sp_generate_payslip_data(?, ?, ?)";
        Map<String, Object> payslipData = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    payslipData.put("employee_id", rs.getInt("employee_id"));
                    payslipData.put("full_name", rs.getString("full_name"));
                    payslipData.put("position_title", rs.getString("position_title"));
                    payslipData.put("pay_period_start", rs.getDate("pay_period_start"));
                    payslipData.put("pay_period_end", rs.getDate("pay_period_end"));
                    payslipData.put("basic_salary", rs.getDouble("basic_salary"));
                    payslipData.put("gross_income", rs.getDouble("gross_income"));
                    payslipData.put("sss_contribution", rs.getDouble("sss_contribution"));
                    payslipData.put("philhealth_contribution", rs.getDouble("philhealth_contribution"));
                    payslipData.put("pagibig_contribution", rs.getDouble("pagibig_contribution"));
                    payslipData.put("withholding_tax", rs.getDouble("withholding_tax"));
                    payslipData.put("total_deductions", rs.getDouble("total_deductions"));
                    payslipData.put("net_income", rs.getDouble("net_income"));
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error generating payslip data for employee: " + employeeId, ex);
            throw new RuntimeException("Failed to generate payslip data", ex);
        }

        return payslipData;
    }

    /**
     * Insert payroll record
     */
    public int insertPayroll(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll cannot be null");
        }

        String sql = "INSERT INTO payroll (employee_id, period_start, period_end, monthly_rate, " +
                "days_worked, overtime_hours, gross_pay, total_deductions, net_pay, " +
                "rice_subsidy, phone_allowance, clothing_allowance, sss, philhealth, pagibig, tax) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payroll.getEmployeeId());
            stmt.setDate(2, payroll.getPeriodStart());
            stmt.setDate(3, payroll.getPeriodEnd());
            stmt.setDouble(4, payroll.getMonthlyRate());
            stmt.setInt(5, payroll.getDaysWorked());
            stmt.setDouble(6, payroll.getOvertimeHours());
            stmt.setDouble(7, payroll.getGrossPay());
            stmt.setDouble(8, payroll.getTotalDeductions());
            stmt.setDouble(9, payroll.getNetPay());
            stmt.setDouble(10, payroll.getRiceSubsidy());
            stmt.setDouble(11, payroll.getPhoneAllowance());
            stmt.setDouble(12, payroll.getClothingAllowance());
            stmt.setDouble(13, payroll.getSss());
            stmt.setDouble(14, payroll.getPhilhealth());
            stmt.setDouble(15, payroll.getPagibig());
            stmt.setDouble(16, payroll.getTax());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating payroll failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    payroll.setPayrollId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating payroll failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting payroll record", ex);
            throw new RuntimeException("Failed to insert payroll record", ex);
        }
    }

    /**
     * Get payroll records by employee ID
     */
    public List<Payroll> getPayrollByEmployeeId(int employeeId) {
        List<Payroll> payrolls = new ArrayList<>();
        String query = "SELECT * FROM payroll WHERE employee_id = ? ORDER BY period_end DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payroll payroll = mapResultSetToPayroll(rs);
                    payrolls.add(payroll);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving payroll for employee: " + employeeId, ex);
            throw new RuntimeException("Failed to retrieve payroll records", ex);
        }

        return payrolls;
    }

    /**
     * Get payroll by period
     */
    public List<Payroll> getPayrollByPeriod(LocalDate startDate, LocalDate endDate) {
        List<Payroll> payrolls = new ArrayList<>();
        String query = "SELECT * FROM payroll WHERE period_start >= ? AND period_end <= ? ORDER BY employee_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payroll payroll = mapResultSetToPayroll(rs);
                    payrolls.add(payroll);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving payroll for period: " + startDate + " to " + endDate, ex);
            throw new RuntimeException("Failed to retrieve payroll records", ex);
        }

        return payrolls;
    }

    /**
     * Map ResultSet to Payroll object
     */
    private Payroll mapResultSetToPayroll(ResultSet rs) throws SQLException {
        Payroll payroll = new Payroll();
        payroll.setPayrollId(rs.getInt("payroll_id"));
        payroll.setEmployeeId(rs.getInt("employee_id"));
        payroll.setPeriodStart(rs.getDate("period_start"));
        payroll.setPeriodEnd(rs.getDate("period_end"));
        payroll.setMonthlyRate(rs.getDouble("monthly_rate"));
        payroll.setDaysWorked(rs.getInt("days_worked"));
        payroll.setOvertimeHours(rs.getDouble("overtime_hours"));
        payroll.setGrossPay(rs.getDouble("gross_pay"));
        payroll.setTotalDeductions(rs.getDouble("total_deductions"));
        payroll.setNetPay(rs.getDouble("net_pay"));
        payroll.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        payroll.setPhoneAllowance(rs.getDouble("phone_allowance"));
        payroll.setClothingAllowance(rs.getDouble("clothing_allowance"));
        payroll.setSss(rs.getDouble("sss"));
        payroll.setPhilhealth(rs.getDouble("philhealth"));
        payroll.setPagibig(rs.getDouble("pagibig"));
        payroll.setTax(rs.getDouble("tax"));

        return payroll;
    }

    /**
     * Check if payroll table exists, create if not
     */
    private void createPayrollTableIfNotExists() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS payroll (
                payroll_id INT AUTO_INCREMENT PRIMARY KEY,
                employee_id INT NOT NULL,
                period_start DATE NOT NULL,
                period_end DATE NOT NULL,
                monthly_rate DECIMAL(10,2) NOT NULL,
                days_worked INT NOT NULL,
                overtime_hours DECIMAL(5,2) DEFAULT 0,
                gross_pay DECIMAL(10,2) NOT NULL,
                total_deductions DECIMAL(10,2) NOT NULL,
                net_pay DECIMAL(10,2) NOT NULL,
                rice_subsidy DECIMAL(8,2) DEFAULT 0,
                phone_allowance DECIMAL(8,2) DEFAULT 0,
                clothing_allowance DECIMAL(8,2) DEFAULT 0,
                sss DECIMAL(8,2) DEFAULT 0,
                philhealth DECIMAL(8,2) DEFAULT 0,
                pagibig DECIMAL(8,2) DEFAULT 0,
                tax DECIMAL(8,2) DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
                UNIQUE KEY uq_employee_period (employee_id, period_start, period_end)
            )
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {

            stmt.executeUpdate();
            LOGGER.info("Payroll table verified/created successfully");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating payroll table", e);
            throw new RuntimeException("Failed to create payroll table", e);
        }
    }

    // Initialize table on first use
    static {
        try {
            new PayrollDAO().createPayrollTableIfNotExists();
        } catch (Exception e) {
            Logger.getLogger(PayrollDAO.class.getName()).log(Level.WARNING, "Could not initialize payroll table", e);
        }
    }
}