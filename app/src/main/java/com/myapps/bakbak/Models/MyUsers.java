package com.myapps.bakbak.Models;

public class MyUsers {
    String username,email,password,profile_pic,userId;

    public MyUsers(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public MyUsers(String username, String email, String password, String profile_pic, String userId) {
        this(username,email,password);
        this.profile_pic = profile_pic;
        this.userId = userId;
    }

    public MyUsers(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
