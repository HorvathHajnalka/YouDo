package com.example.youdo;


import androidx.annotation.NonNull;



public class User {
    private int userId;
    private String userName;
    private String email;
    private String password;

    // functions
    public User(@NonNull String userName, @NonNull String email, @NonNull String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    // Setterek
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }
}
