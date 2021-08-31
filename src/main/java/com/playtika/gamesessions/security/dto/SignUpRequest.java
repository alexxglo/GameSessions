package com.playtika.gamesessions.security.dto;

import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.RoleType;

import java.util.ArrayList;
import java.util.List;

public class SignUpRequest {
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int maxDailyTime;
    private List<Role> roles;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getMaxDailyTime() {
        return maxDailyTime;
    }

    public void setMaxDailyTime(int maxDailyTime) {
        this.maxDailyTime = maxDailyTime;
    }
}
