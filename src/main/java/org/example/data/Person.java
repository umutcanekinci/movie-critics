package org.example.data;

public class Person {
    private int personId;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String nationality;
    
    public Person(int personId, String firstName, String lastName, String dateOfBirth, String nationality) {
        this.personId    = personId;
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
    }

    public int    getPersonId()    { return personId; }
    public String getFirstName()   { return firstName; }
    public String getLastName()    { return lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getNationality() { return nationality; }
}
