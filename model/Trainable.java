/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate;
import java.util.List;

public interface Trainable {
    void addTrainingRecord(String training, LocalDate completionDate);
    List<String> getCompletedTrainings();
    boolean hasRequiredTraining(String requiredTraining);
    int getTrainingHours();
}