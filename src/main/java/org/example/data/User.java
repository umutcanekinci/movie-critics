package org.example.data;

public class User {
    private int id;
    private String username;
    private String password;
    private int userType;
    private String email;

    public User(int id, String username, String password, int userType, String email) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.email    = email;
    }

    public int    getId()       { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int    getUserType() { return userType; }
    public String getEmail()    { return email; }
}
