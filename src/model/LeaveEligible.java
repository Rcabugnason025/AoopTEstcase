/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public interface LeaveEligible {
    boolean isEligibleForLeave(String leaveType);
    int getMaxLeavesDays(String leaveType);
    double getLeaveAllowance();
    boolean requiresApproval(String leaveType);
}