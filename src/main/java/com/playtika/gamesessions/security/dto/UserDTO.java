package com.playtika.gamesessions.security.dto;

public class UserDTO {
    private String userName;
    private String email;

    public UserDTO(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

    public UserDTO(){

    }

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
}
