/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class AttendanceDeduction extends Deduction {
    private int lateDays;
    private int absentDays;
    private double dailyRate;
    
    public AttendanceDeduction(int employeeId, int lateDays, int absentDays, double dailyRate) {
        super(employeeId, "Attendance", 0);
        this.lateDays = lateDays;
        this.absentDays = absentDays;
        this.dailyRate = dailyRate;
    }
    
    @Override
    public void calculateDeduction() {
        amount = (lateDays * dailyRate * 0.1) + (absentDays * dailyRate);
        description = String.format("Late: %d days, Absent: %d days", lateDays, absentDays);
    }
    
    public boolean isRecurring() {
        return false; // Attendance deductions are period-specific
    }
}
