
// Abstract base class for all people in the system
package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public abstract class Person {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected LocalDate birthDate;
    protected String address;
    protected String phoneNumber;
    
    // Abstract methods that must be implemented by subclasses
    public abstract String getDisplayName();
    public abstract String getRole();
    public abstract boolean isActive();
    
    // Constructor
    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Default constructor
    public Person() {}
    
    // Common behavior for all people
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    // Template method pattern
    public final String getFormattedInfo() {
        return String.format("%s: %s (%s)", getRole(), getDisplayName(), 
                           isActive() ? "Active" : "Inactive");
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName.trim(); 
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.lastName = lastName.trim(); 
    }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { 
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        this.birthDate = birthDate; 
    }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return id == person.id && Objects.equals(firstName, person.firstName) 
               && Objects.equals(lastName, person.lastName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }
    
    @Override
    public String toString() {
        return getFormattedInfo();
    }
}
