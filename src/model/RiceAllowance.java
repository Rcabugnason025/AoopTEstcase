package model;

/**
 * Concrete Rice Allowance implementation demonstrating INHERITANCE
 */
public class RiceAllowance extends Allowance {
    private static final double STANDARD_RICE_ALLOWANCE = 1500.0;
    
    public RiceAllowance(int employeeId) {
        super(employeeId, "Rice Subsidy", STANDARD_RICE_ALLOWANCE, false);
    }
    
    @Override
    public void calculateAllowance() {
        // Rice allowance is typically fixed
        setAmount(STANDARD_RICE_ALLOWANCE);
    }
    
    @Override
    public boolean isEligible(Object employee) {
        if (employee instanceof Employee) {
            Employee emp = (Employee) employee;
            return emp.isRegularEmployee() || emp.getStatus() == Employee.EmploymentStatus.PROBATIONARY;
        }
        return false;
    }
}
