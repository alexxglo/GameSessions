package com.playtika.gamesessions.security.dto;

import com.playtika.gamesessions.security.models.Role;

import java.util.ArrayList;
import java.util.List;

public class PatchUser {
    private String userName = null;
    private String email = null;
    private String password = null;
    private List<Role> roles = new ArrayList<>();

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

}
