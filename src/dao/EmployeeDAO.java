package dao;

import util.DBConnection;
import model.Employee;
import model.EmployeeFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {
    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM v_employee_details ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee e = mapViewResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching all employees", ex);
            throw new RuntimeException("Failed to fetch employees", ex);
        }

        return employees;
    }

    public Employee getEmployeeById(int employeeId) {
        String query = "SELECT * FROM v_employee_details WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapViewResultSetToEmployee(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch employee", ex);
        }

        return null;
    }

    /**
     * Get employee with position details using the view
     */
    public Employee getEmployeeWithPositionDetails(int employeeId) {
        return getEmployeeById(employeeId); // Now uses the view by default
    }

    /**
     * Enhanced insertEmployee method using stored procedure
     */
    public boolean insertEmployee(Employee e) {
        if (e == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        // Validate required fields
        if (e.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (e.getFirstName() == null || e.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (e.getLastName() == null || e.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        // Use stored procedure for safe insertion
        String sql = "CALL sp_add_new_employee(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getEmployeeId());
            stmt.setString(2, e.getLastName() != null ? e.getLastName().trim() : null);
            stmt.setString(3, e.getFirstName() != null ? e.getFirstName().trim() : null);
            stmt.setDate(4, e.getBirthday() != null ? java.sql.Date.valueOf(e.getBirthday()) : null);
            stmt.setString(5, e.getAddress() != null ? e.getAddress().trim() : null);
            stmt.setString(6, e.getPhoneNumber() != null ? e.getPhoneNumber().trim() : null);
            stmt.setString(7, e.getSssNumber() != null ? e.getSssNumber().trim() : null);
            stmt.setString(8, e.getPhilhealthNumber() != null ? e.getPhilhealthNumber().trim() : null);
            stmt.setString(9, e.getTinNumber() != null ? e.getTinNumber().trim() : null);
            stmt.setString(10, e.getPagibigNumber() != null ? e.getPagibigNumber().trim() : null);
            stmt.setString(11, e.getStatus() != null ? e.getStatus().trim() : "Regular");
            stmt.setInt(12, e.getPositionId() > 0 ? e.getPositionId() : 1); // Default position
            stmt.setObject(13, getSupervisorId(e.getImmediateSupervisor()), java.sql.Types.INTEGER);
            stmt.setString(14, "password1234"); // Default password

            stmt.executeUpdate();
            LOGGER.info("Successfully inserted employee: " + e.getEmployeeId() + " - " + e.getFullName());
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting employee: " + e.getEmployeeId(), ex);
            throw new RuntimeException("Failed to insert employee: " + ex.getMessage(), ex);
        }
    }

    public boolean updateEmployee(Employee e) {
        if (e == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (e.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "UPDATE employees SET last_name=?, first_name=?, birthday=?, address=?, " +
                "phone_number=?, sss_number=?, philhealth_number=?, tin_number=?, " +
                "pagibig_number=?, status=?, position_id=?, supervisor_id=? WHERE employee_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getLastName() != null ? e.getLastName().trim() : null);
            stmt.setString(2, e.getFirstName() != null ? e.getFirstName().trim() : null);
            stmt.setDate(3, e.getBirthday() != null ? java.sql.Date.valueOf(e.getBirthday()) : null);
            stmt.setString(4, e.getAddress() != null ? e.getAddress().trim() : null);
            stmt.setString(5, e.getPhoneNumber() != null ? e.getPhoneNumber().trim() : null);
            stmt.setString(6, e.getSssNumber() != null ? e.getSssNumber().trim() : null);
            stmt.setString(7, e.getPhilhealthNumber() != null ? e.getPhilhealthNumber().trim() : null);
            stmt.setString(8, e.getTinNumber() != null ? e.getTinNumber().trim() : null);
            stmt.setString(9, e.getPagibigNumber() != null ? e.getPagibigNumber().trim() : null);
            stmt.setString(10, e.getStatus() != null ? e.getStatus().trim() : "Regular");
            stmt.setInt(11, e.getPositionId() > 0 ? e.getPositionId() : 1);
            stmt.setObject(12, getSupervisorId(e.getImmediateSupervisor()), java.sql.Types.INTEGER);
            stmt.setInt(13, e.getEmployeeId());

            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully updated employee: " + e.getEmployeeId() + " - " + e.getFullName());
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + e.getEmployeeId() + " for update");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating employee with ID: " + e.getEmployeeId(), ex);
            throw new RuntimeException("Failed to update employee: " + ex.getMessage(), ex);
        }
    }

    public boolean deleteEmployee(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully deleted employee with ID: " + employeeId);
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + employeeId + " for deletion");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to delete employee: " + ex.getMessage(), ex);
        }
    }

    public List<Employee> getEmployeesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM v_employee_details WHERE status = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapViewResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by status: " + status, ex);
            throw new RuntimeException("Failed to fetch employees by status", ex);
        }

        return employees;
    }

    public List<Employee> searchEmployees(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEmployees();
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM v_employee_details WHERE " +
                "employee_id LIKE ? OR " +
                "CONCAT(first_name, ' ', last_name) LIKE ? OR " +
                "position_title LIKE ? OR " +
                "status LIKE ? " +
                "ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm.trim() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapViewResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error searching employees with term: " + searchTerm, ex);
            throw new RuntimeException("Failed to search employees", ex);
        }

        return employees;
    }

    /**
     * Map view result set to Employee with position details using Factory pattern
     */
    private Employee mapViewResultSetToEmployee(ResultSet rs) throws SQLException {
        String status = rs.getString("status");
        int employeeId = rs.getInt("employee_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String position = rs.getString("position_title");
        double basicSalary = rs.getDouble("basic_salary");

        // Use Factory pattern to create appropriate employee type (POLYMORPHISM)
        Employee e;
        if ("Probationary".equalsIgnoreCase(status)) {
            e = new ProbationaryEmployee(employeeId, firstName, lastName, position, basicSalary);
        } else {
            e = new RegularEmployee(employeeId, firstName, lastName, position, basicSalary);
        }

        // Set additional properties
        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            e.setBirthday(birthday.toLocalDate());
        }

        e.setAddress(rs.getString("address"));
        e.setPhoneNumber(rs.getString("phone_number"));
        e.setSssNumber(rs.getString("sss_number"));
        e.setPhilhealthNumber(rs.getString("philhealth_number"));
        e.setTinNumber(rs.getString("tin_number"));
        e.setPagibigNumber(rs.getString("pagibig_number"));
        e.setImmediateSupervisor(rs.getString("supervisor_name"));
        e.setPositionId(rs.getInt("position_id"));

        // Set allowances from position
        e.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        e.setPhoneAllowance(rs.getDouble("phone_allowance"));
        e.setClothingAllowance(rs.getDouble("clothing_allowance"));
        e.setGrossSemiMonthlyRate(rs.getDouble("gross_semi_monthly_rate"));
        e.setHourlyRate(rs.getDouble("hourly_rate"));

        return e;
    }

    private Integer getSupervisorId(String supervisorName) {
        if (supervisorName == null || supervisorName.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT employee_id FROM employees WHERE CONCAT(last_name, ', ', first_name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, supervisorName.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("employee_id");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting supervisor ID for: " + supervisorName, ex);
        }

        return null;
    }

    /**
     * Utility method to check if an employee exists
     */
    public boolean employeeExists(int employeeId) {
        if (employeeId <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking if employee exists: " + employeeId, ex);
            return false;
        }
    }

    /**
     * Get employee count by status
     */
    public int getEmployeeCountByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String query = "SELECT COUNT(*) FROM employees WHERE status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error getting employee count by status: " + status, ex);
            throw new RuntimeException("Failed to get employee count", ex);
        }

        return 0;
    }
}