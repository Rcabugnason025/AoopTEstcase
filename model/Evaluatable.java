/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public interface Evaluatable {
    double getPerformanceRating();
    void setPerformanceRating(double rating);
    boolean isEligibleForPromotion();
    String getPerformanceCategory();
}
