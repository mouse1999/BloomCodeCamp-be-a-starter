package com.hcc.dtos.request.userdto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignUpRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20)
    private String userName;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 15)
    private String password;
    @NotBlank(message = "Role is required")
    private String role;


    public SignUpRequest() {}


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
