/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Manager extends Employee {
    private double managementAllowance;
    private int teamSize;
    
    public Manager() {
        super();
    }
    
    public Manager(String firstName, String lastName, int employeeId, double managementAllowance) {
        super(firstName, lastName, employeeId);
        this.managementAllowance = managementAllowance;
    }
    
    @Override
    public String getRole() {
        return "Manager";
    }
    
    @Override
    public double calculateAllowances() {
        return super.calculateAllowances() + managementAllowance;
    }
    
    @Override
    public boolean canReceiveBonus() {
        return super.canReceiveBonus() && teamSize > 0;
    }
    
    // Manager-specific methods
    public double getManagementAllowance() { return managementAllowance; }
    public void setManagementAllowance(double managementAllowance) { 
        this.managementAllowance = managementAllowance; 
    }
    
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
}

