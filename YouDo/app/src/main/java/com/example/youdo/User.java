package com.example.youdo;


public class User {
    private int userId;
    private String userName;
    private String email;
    private String password;
    private String googleId;
    // functions
    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    public int getUserId() {
        return userId;
    }
    public String getGoogleId() { return googleId; }
    public String getUserName() {
        return userName;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public void setGoogleId(String googleId) {this.googleId = googleId; }
}
