package com.hms.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;
    private boolean active;

    public User() {}

    public User(int userId, String username, String password, String fullName, String email, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }

    public int getUserId() {return userId;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getFullName() {return fullName;}
    public String getEmail() {return email;}
    public String getRole() {return role;}
    public boolean isActive() {return active;}

    public void setUserId(int userId) {this.userId = userId;}
    public void setUsername(String u) {this.username = u;}
    public void setPassword(String p) {this.password = p;}
    public void setFullName(String f) {this.fullName = f;}
    public void setEmail(String e) {this.email = e;}
    public void setRole(String r) {this.role = r;}
    public void setActive(boolean active) {this.active = active;}

    @Override
    public String toString() {
        return fullName + "(" + role + ")";
    }
}
