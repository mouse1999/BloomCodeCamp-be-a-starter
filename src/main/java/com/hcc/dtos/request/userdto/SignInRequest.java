package com.hcc.dtos.request.userdto;

import jakarta.validation.constraints.NotNull;

public class SignInRequest {

    @NotNull
    private String username;
    @NotNull
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
